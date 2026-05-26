package com.sps.compra.service;

import com.sps.compra.dto.*;
import com.sps.compra.entity.Compra;
import com.sps.compra.entity.PlanCompra;
import com.sps.compra.entity.PlanSalud;
import com.sps.compra.entity.ServicioMedico;
import com.sps.compra.enums.EstadoCompra;
import com.sps.compra.enums.EstadoSNS;
import com.sps.compra.integrador.IntegradorSAM;
import com.sps.compra.integrador.IntegradorSHC;
import com.sps.compra.proxy.ProxyEmail;
import com.sps.compra.proxy.ProxySNS;
import com.sps.compra.proxy.ProxySaludPay;
import com.sps.compra.repository.RepoCompra;
import com.sps.compra.repository.RepoPlanSalud;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Service
@Transactional
public class ServiceCompra {

    private static final Logger logger = Logger.getLogger(ServiceCompra.class.getName());

    private final RepoCompra repoCompra;
    private final RepoPlanSalud repoPlanSalud;
    private final ProxySNS proxySNS;
    private final ProxySaludPay proxySaludPay;
    private final ProxyEmail proxyEmail;
    private final IntegradorSHC integradorSHC;
    private final IntegradorSAM integradorSAM;

    @Value("${app.sns.max.intentos:10}")
    private int maxIntentosSNS;

    @Value("${app.internal.api.key}")
    private String internalApiKey;

    public ServiceCompra(RepoCompra repoCompra,
                         RepoPlanSalud repoPlanSalud,
                         ProxySNS proxySNS,
                         ProxySaludPay proxySaludPay,
                         ProxyEmail proxyEmail,
                         IntegradorSHC integradorSHC,
                         IntegradorSAM integradorSAM) {
        this.repoCompra = repoCompra;
        this.repoPlanSalud = repoPlanSalud;
        this.proxySNS = proxySNS;
        this.proxySaludPay = proxySaludPay;
        this.proxyEmail = proxyEmail;
        this.integradorSHC = integradorSHC;
        this.integradorSAM = integradorSAM;
    }

    public List<PlanSaludDTO> listarPlanesActivos() {
        return repoPlanSalud.findByActivoTrue().stream()
                .map(this::toPlanSaludDTO)
                .collect(Collectors.toList());
    }

    public PlanSaludDTO obtenerPlan(String codigoPlan) {
        PlanSalud plan = repoPlanSalud.findById(codigoPlan)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado: " + codigoPlan));
        return toPlanSaludDTO(plan);
    }

    public CompraResumenDTO crearCompra(CompraRequestDTO request,
                                        String cedulaCliente,
                                        String nombreCliente,
                                        String correoCliente) {
        List<String> codigosPlanes = request.getCodigosPlanes();
        if (codigosPlanes == null || codigosPlanes.isEmpty()) {
            throw new RuntimeException("Debe seleccionar al menos un plan");
        }

        List<PlanSalud> planesSeleccionados = new ArrayList<>();
        for (String codigoPlan : codigosPlanes) {
            PlanSalud plan = repoPlanSalud.findById(codigoPlan)
                    .orElseThrow(() -> new RuntimeException("Plan no encontrado: " + codigoPlan));
            if (!Boolean.TRUE.equals(plan.getActivo())) {
                throw new RuntimeException("El plan no esta activo: " + codigoPlan);
            }
            planesSeleccionados.add(plan);
        }

        BigDecimal precioTotal = planesSeleccionados.stream()
                .map(PlanSalud::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Compra compra = new Compra();
        compra.setCedulaCliente(cedulaCliente);
        compra.setNombreCliente(nombreCliente);
        compra.setCorreoCliente(correoCliente);
        compra.setPrecioTotal(precioTotal);
        compra.setEstado(EstadoCompra.PENDIENTE_VALIDACION_SNS);

        List<PlanCompra> planesCompra = new ArrayList<>();
        for (PlanSalud plan : planesSeleccionados) {
            PlanCompra planCompra = new PlanCompra();
            planCompra.setCompra(compra);
            planCompra.setCodigoPlan(plan.getCodigoPlan());
            planCompra.setNombrePlan(plan.getNombrePlan());
            planCompra.setPrecio(plan.getPrecio());
            planCompra.setEstadoSNS(EstadoSNS.PENDIENTE);
            planCompra.setIntentosSNS(0);
            planesCompra.add(planCompra);
        }

        compra.setPlanes(planesCompra);
        Compra saved = repoCompra.save(compra);

        logger.info("ServiceCompra: compra creada con numero " + saved.getNumeroCompra());
        return new CompraResumenDTO(saved.getNumeroCompra(), saved.getEstado().name());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void procesarValidacionesSNSPendientes() {
        List<EstadoCompra> estadosBuscados = List.of(
                EstadoCompra.PENDIENTE_VALIDACION_SNS,
                EstadoCompra.EN_PROCESO_SNS
        );

        List<Compra> comprasPendientes = repoCompra.findByEstadoIn(estadosBuscados);
        logger.info("ServiceCompra: procesando " + comprasPendientes.size() + " compras pendientes de validacion SNS");

        for (Compra compra : comprasPendientes) {
            try {
                procesarValidacionCompra(compra);
            } catch (Exception e) {
                logger.severe("ServiceCompra: error procesando compra " + compra.getNumeroCompra() + ": " + e.getMessage());
            }
        }
    }

    private void procesarValidacionCompra(Compra compra) {
        boolean hayPendientes = false;
        boolean hayRechazados = false;
        boolean todosAprobados = true;

        for (PlanCompra planCompra : compra.getPlanes()) {
            EstadoSNS estadoActual = planCompra.getEstadoSNS();

            if (estadoActual == EstadoSNS.APROBADO) {
                continue;
            }
            if (estadoActual == EstadoSNS.RECHAZADO) {
                hayRechazados = true;
                todosAprobados = false;
                continue;
            }

            todosAprobados = false;

            if (planCompra.getIntentosSNS() >= maxIntentosSNS) {
                planCompra.setEstadoSNS(EstadoSNS.RECHAZADO);
                hayRechazados = true;
                continue;
            }

            if (planCompra.getIdValidacionSNS() == null) {
                Long idValidacion = proxySNS.iniciarValidacion(planCompra.getCodigoPlan());
                if (idValidacion != null) {
                    planCompra.setIdValidacionSNS(idValidacion);
                    planCompra.setEstadoSNS(EstadoSNS.EN_PROCESO);
                }
            } else {
                EstadoSNS nuevoEstado = proxySNS.consultarEstado(planCompra.getIdValidacionSNS());
                planCompra.setEstadoSNS(nuevoEstado);
                if (nuevoEstado == EstadoSNS.APROBADO) {
                    // will be caught in next iteration check
                } else if (nuevoEstado == EstadoSNS.RECHAZADO) {
                    hayRechazados = true;
                } else {
                    hayPendientes = true;
                }
            }

            planCompra.setIntentosSNS(planCompra.getIntentosSNS() + 1);
            planCompra.setUltimoIntento(LocalDateTime.now());
        }

        // Re-evaluate after update
        todosAprobados = compra.getPlanes().stream()
                .allMatch(p -> p.getEstadoSNS() == EstadoSNS.APROBADO);
        hayRechazados = compra.getPlanes().stream()
                .anyMatch(p -> p.getEstadoSNS() == EstadoSNS.RECHAZADO);
        hayPendientes = compra.getPlanes().stream()
                .anyMatch(p -> p.getEstadoSNS() == EstadoSNS.PENDIENTE
                        || p.getEstadoSNS() == EstadoSNS.EN_PROCESO);

        repoCompra.save(compra);

        if (todosAprobados) {
            notificarAprobacion(compra);
        } else if (hayRechazados && !hayPendientes) {
            compra.setEstado(EstadoCompra.RECHAZADO_SNS);
            repoCompra.save(compra);
            logger.info("ServiceCompra: compra " + compra.getNumeroCompra() + " rechazada por SNS");
        } else {
            compra.setEstado(EstadoCompra.EN_PROCESO_SNS);
            repoCompra.save(compra);
        }
    }

    private void notificarAprobacion(Compra compra) {
        compra.setEstado(EstadoCompra.APROBADO_SNS);
        repoCompra.save(compra);

        proxyEmail.enviarCorreoAprobacion(
                compra.getCorreoCliente(),
                compra.getNombreCliente(),
                compra.getNumeroCompra(),
                compra.getPrecioTotal()
        );

        proxySaludPay.registrarCompraPendiente(
                compra.getCedulaCliente(),
                compra.getNumeroCompra(),
                compra.getPrecioTotal()
        );

        compra.setEstado(EstadoCompra.PENDIENTE_PAGO);
        repoCompra.save(compra);
        logger.info("ServiceCompra: compra " + compra.getNumeroCompra() + " aprobada y en espera de pago");
    }

    public CompraDTO procesarPago(Long numeroCompra, PagoNotificacionDTO pago, String internalKey) {
        if (!this.internalApiKey.equals(internalKey)) {
            throw new SecurityException("Clave interna invalida");
        }

        Compra compra = repoCompra.findById(numeroCompra)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada: " + numeroCompra));

        if (compra.getEstado() != EstadoCompra.PENDIENTE_PAGO) {
            throw new RuntimeException("La compra no esta en estado PENDIENTE_PAGO. Estado actual: " + compra.getEstado());
        }

        if (pago.getValorPagado().compareTo(compra.getPrecioTotal()) < 0) {
            throw new RuntimeException("El valor pagado es insuficiente. Total: "
                    + compra.getPrecioTotal() + ", Pagado: " + pago.getValorPagado());
        }

        compra.setEstado(EstadoCompra.PAGADO);
        repoCompra.save(compra);

        proxyEmail.enviarCorreoConfirmacion(
                compra.getCorreoCliente(),
                compra.getNombreCliente(),
                compra.getNumeroCompra(),
                compra.getPrecioTotal()
        );

        compra.setEstado(EstadoCompra.COMPLETADO);
        repoCompra.save(compra);

        integradorSHC.publicar(compra);
        integradorSAM.publicar(compra);

        logger.info("ServiceCompra: compra " + compra.getNumeroCompra() + " completada exitosamente");
        return toCompraDTO(compra);
    }

    public CompraDTO obtenerCompra(Long numeroCompra) {
        Compra compra = repoCompra.findById(numeroCompra)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada: " + numeroCompra));
        return toCompraDTO(compra);
    }

    public List<CompraDTO> obtenerComprasPorCliente(String cedulaCliente) {
        return repoCompra.findByCedulaCliente(cedulaCliente).stream()
                .map(this::toCompraDTO)
                .collect(Collectors.toList());
    }

    public CompraDTO toCompraDTO(Compra compra) {
        CompraDTO dto = new CompraDTO();
        dto.setNumeroCompra(compra.getNumeroCompra());
        dto.setCedulaCliente(compra.getCedulaCliente());
        dto.setNombreCliente(compra.getNombreCliente());
        dto.setPrecioTotal(compra.getPrecioTotal());
        dto.setEstado(compra.getEstado().name());
        if (compra.getFechaCreacion() != null) {
            dto.setFechaCreacion(compra.getFechaCreacion().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (compra.getPlanes() != null) {
            dto.setPlanes(compra.getPlanes().stream()
                    .map(this::toPlanCompraDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public PlanCompraDTO toPlanCompraDTO(PlanCompra planCompra) {
        PlanCompraDTO dto = new PlanCompraDTO();
        dto.setId(planCompra.getId());
        dto.setCodigoPlan(planCompra.getCodigoPlan());
        dto.setNombrePlan(planCompra.getNombrePlan());
        dto.setPrecio(planCompra.getPrecio());
        dto.setEstadoSNS(planCompra.getEstadoSNS().name());
        return dto;
    }

    public PlanSaludDTO toPlanSaludDTO(PlanSalud plan) {
        PlanSaludDTO dto = new PlanSaludDTO();
        dto.setCodigoPlan(plan.getCodigoPlan());
        dto.setNombrePlan(plan.getNombrePlan());
        dto.setDescripcion(plan.getDescripcion());
        dto.setPrecio(plan.getPrecio());
        dto.setActivo(plan.getActivo());
        if (plan.getServiciosMedicos() != null) {
            dto.setServiciosMedicos(plan.getServiciosMedicos().stream()
                    .map(this::toServicioMedicoDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private ServicioMedicoDTO toServicioMedicoDTO(ServicioMedico servicio) {
        ServicioMedicoDTO dto = new ServicioMedicoDTO();
        dto.setCodigoServicio(servicio.getCodigoServicio());
        dto.setNombre(servicio.getNombre());
        dto.setDescripcion(servicio.getDescripcion());
        dto.setTipo(servicio.getTipo().name());
        dto.setPrecio(servicio.getPrecio());
        return dto;
    }
}

package com.sps.shc.service;

import com.sps.shc.dto.*;
import com.sps.shc.entity.HistoriaClinica;
import com.sps.shc.entity.PlanSHC;
import com.sps.shc.entity.ServicioSHC;
import com.sps.shc.repository.RepoHistoriaClinica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServiceSHC {

    private static final Logger log = LoggerFactory.getLogger(ServiceSHC.class);

    @Autowired
    private RepoHistoriaClinica repoHistoriaClinica;

    @Transactional(readOnly = true)
    public boolean yaExisteHistoria(Long numeroCompra) {
        return repoHistoriaClinica.existsByNumeroCompraOrigen(numeroCompra);
    }

    public void crearHistoriaClinica(MensajeCompraDTO mensaje) {
        if (mensaje.getNumeroCompra() == null) {
            throw new IllegalArgumentException("numeroCompra es obligatorio");
        }

        HistoriaClinica historia = new HistoriaClinica();
        historia.setCedulaCliente(mensaje.getCedulaCliente());
        historia.setNombreCliente(mensaje.getNombreCliente());
        historia.setCorreoCliente(mensaje.getCorreoCliente());
        historia.setNumeroCompraOrigen(mensaje.getNumeroCompra());

        List<PlanSHC> planes = new ArrayList<>();
        if (mensaje.getPlanes() != null) {
            for (PlanMensajeDTO planDto : mensaje.getPlanes()) {
                PlanSHC plan = new PlanSHC();
                plan.setCodigoPlan(planDto.getCodigoPlan());
                plan.setNombrePlan(planDto.getNombrePlan());
                plan.setHistoriaClinica(historia);

                List<ServicioSHC> servicios = new ArrayList<>();
                if (planDto.getServiciosMedicos() != null) {
                    for (ServicioMensajeDTO svcDto : planDto.getServiciosMedicos()) {
                        ServicioSHC svc = new ServicioSHC();
                        svc.setCodigoServicio(svcDto.getCodigoServicio());
                        svc.setNombre(svcDto.getNombre());
                        svc.setTipo(svcDto.getTipo());
                        svc.setPlanSHC(plan);
                        servicios.add(svc);
                    }
                }
                plan.setServicios(servicios);
                planes.add(plan);
            }
        }
        historia.setPlanes(planes);
        repoHistoriaClinica.save(historia);
        log.info("Historia clinica creada para compra {} (cedula {})", mensaje.getNumeroCompra(), mensaje.getCedulaCliente());
    }

    @Transactional(readOnly = true)
    public List<HistoriaClinicaDTO> listarPorCedula(String cedula) {
        return repoHistoriaClinica.findByCedulaCliente(cedula)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HistoriaClinicaDTO obtenerPorNumeroCompra(Long numeroCompra) {
        return repoHistoriaClinica.findByNumeroCompraOrigen(numeroCompra)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Historia no encontrada"));
    }

    private HistoriaClinicaDTO toDTO(HistoriaClinica h) {
        HistoriaClinicaDTO dto = new HistoriaClinicaDTO();
        dto.setId(h.getId());
        dto.setCedulaCliente(h.getCedulaCliente());
        dto.setNombreCliente(h.getNombreCliente());
        dto.setCorreoCliente(h.getCorreoCliente());
        dto.setNumeroCompraOrigen(h.getNumeroCompraOrigen());
        dto.setFechaRegistro(h.getFechaRegistro() != null ? h.getFechaRegistro().toString() : null);
        dto.setPlanes(h.getPlanes().stream().map(this::toPlanDTO).collect(Collectors.toList()));
        return dto;
    }

    private PlanSHCDTO toPlanDTO(PlanSHC p) {
        PlanSHCDTO dto = new PlanSHCDTO();
        dto.setId(p.getId());
        dto.setCodigoPlan(p.getCodigoPlan());
        dto.setNombrePlan(p.getNombrePlan());
        dto.setServicios(p.getServicios().stream().map(this::toServicioDTO).collect(Collectors.toList()));
        return dto;
    }

    private ServicioSHCDTO toServicioDTO(ServicioSHC s) {
        ServicioSHCDTO dto = new ServicioSHCDTO();
        dto.setId(s.getId());
        dto.setCodigoServicio(s.getCodigoServicio());
        dto.setNombre(s.getNombre());
        dto.setTipo(s.getTipo());
        return dto;
    }
}

package com.sps.compra.integrador;

import com.sps.compra.dto.MensajeSAMDTO;
import com.sps.compra.dto.MensajeSAMDTO.ServicioSAMDTO;
import com.sps.compra.entity.Compra;
import com.sps.compra.entity.PlanCompra;
import com.sps.compra.entity.PlanSalud;
import com.sps.compra.entity.ServicioMedico;
import com.sps.compra.repository.RepoPlanSalud;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Component
public class IntegradorSAM {

    private static final Logger logger = Logger.getLogger(IntegradorSAM.class.getName());

    private final RabbitTemplate rabbitTemplate;
    private final RepoPlanSalud repoPlanSalud;

    public IntegradorSAM(RabbitTemplate rabbitTemplate, RepoPlanSalud repoPlanSalud) {
        this.rabbitTemplate = rabbitTemplate;
        this.repoPlanSalud = repoPlanSalud;
    }

    public void publicar(Compra compra) {
        try {
            MensajeSAMDTO mensaje = new MensajeSAMDTO();
            mensaje.setNumeroCompra(compra.getNumeroCompra());
            mensaje.setCedulaCliente(compra.getCedulaCliente());
            mensaje.setNombreCliente(compra.getNombreCliente());
            mensaje.setFechaCompra(compra.getFechaCreacion()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            List<ServicioSAMDTO> todosLosServicios = new ArrayList<>();
            for (PlanCompra planCompra : compra.getPlanes()) {
                Optional<PlanSalud> planSaludOpt = repoPlanSalud.findById(planCompra.getCodigoPlan());
                if (planSaludOpt.isPresent()) {
                    PlanSalud planSalud = planSaludOpt.get();
                    for (ServicioMedico servicio : planSalud.getServiciosMedicos()) {
                        ServicioSAMDTO servicioSAM = new ServicioSAMDTO(
                                servicio.getCodigoServicio(),
                                servicio.getNombre(),
                                servicio.getTipo().name(),
                                planCompra.getCodigoPlan()
                        );
                        todosLosServicios.add(servicioSAM);
                    }
                }
            }

            mensaje.setServiciosMedicos(todosLosServicios);

            rabbitTemplate.convertAndSend("sam.exchange", "sam.compra.nueva", mensaje);
            logger.info("IntegradorSAM: mensaje publicado para compra " + compra.getNumeroCompra());
        } catch (Exception e) {
            logger.severe("IntegradorSAM.publicar error para compra " + compra.getNumeroCompra() + ": " + e.getMessage());
        }
    }
}

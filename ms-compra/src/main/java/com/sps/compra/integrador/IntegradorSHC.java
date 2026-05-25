package com.sps.compra.integrador;

import com.sps.compra.dto.MensajeSHCDTO;
import com.sps.compra.dto.PlanMensajeDTO;
import com.sps.compra.dto.ServicioMensajeDTO;
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
public class IntegradorSHC {

    private static final Logger logger = Logger.getLogger(IntegradorSHC.class.getName());

    private final RabbitTemplate rabbitTemplate;
    private final RepoPlanSalud repoPlanSalud;

    public IntegradorSHC(RabbitTemplate rabbitTemplate, RepoPlanSalud repoPlanSalud) {
        this.rabbitTemplate = rabbitTemplate;
        this.repoPlanSalud = repoPlanSalud;
    }

    public void publicar(Compra compra) {
        try {
            MensajeSHCDTO mensaje = new MensajeSHCDTO();
            mensaje.setNumeroCompra(compra.getNumeroCompra());
            mensaje.setCedulaCliente(compra.getCedulaCliente());
            mensaje.setNombreCliente(compra.getNombreCliente());
            mensaje.setCorreoCliente(compra.getCorreoCliente());
            mensaje.setFechaCompra(compra.getFechaCreacion()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            List<PlanMensajeDTO> planesMsg = new ArrayList<>();
            for (PlanCompra planCompra : compra.getPlanes()) {
                Optional<PlanSalud> planSaludOpt = repoPlanSalud.findById(planCompra.getCodigoPlan());

                List<ServicioMensajeDTO> serviciosMsg = new ArrayList<>();
                if (planSaludOpt.isPresent()) {
                    PlanSalud planSalud = planSaludOpt.get();
                    for (ServicioMedico servicio : planSalud.getServiciosMedicos()) {
                        ServicioMensajeDTO servicioMsg = new ServicioMensajeDTO(
                                servicio.getCodigoServicio(),
                                servicio.getNombre(),
                                servicio.getTipo().name()
                        );
                        serviciosMsg.add(servicioMsg);
                    }
                }

                PlanMensajeDTO planMsg = new PlanMensajeDTO(
                        planCompra.getCodigoPlan(),
                        planCompra.getNombrePlan(),
                        serviciosMsg
                );
                planesMsg.add(planMsg);
            }

            mensaje.setPlanes(planesMsg);

            rabbitTemplate.convertAndSend("shc.exchange", "shc.compra.nueva", mensaje);
            logger.info("IntegradorSHC: mensaje publicado para compra " + compra.getNumeroCompra());
        } catch (Exception e) {
            logger.severe("IntegradorSHC.publicar error para compra " + compra.getNumeroCompra() + ": " + e.getMessage());
        }
    }
}

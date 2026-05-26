package com.sps.sam.listener;

import com.sps.sam.dto.MensajeCompraDTO;
import com.sps.sam.service.ServiceSAM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListenerSAM {

    private static final Logger log = LoggerFactory.getLogger(ListenerSAM.class);

    @Autowired
    private ServiceSAM serviceSAM;

    @RabbitListener(queues = "sam.compras.queue")
    public void recibirCompra(MensajeCompraDTO mensaje) {
        log.info("Mensaje recibido en sam.compras.queue para compra {}", mensaje.getNumeroCompra());
        try {
            if (serviceSAM.yaExisteAgenda(mensaje.getNumeroCompra())) {
                log.info("Agenda ya existe para compra {} - ignorando (idempotencia)", mensaje.getNumeroCompra());
                return;
            }
            serviceSAM.crearAgendaMedica(mensaje);
        } catch (Exception e) {
            log.error("Error procesando mensaje de compra {}: {}", mensaje.getNumeroCompra(), e.getMessage(), e);
            throw e;
        }
    }
}

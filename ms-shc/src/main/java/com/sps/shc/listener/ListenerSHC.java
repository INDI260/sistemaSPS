package com.sps.shc.listener;

import com.sps.shc.dto.MensajeCompraDTO;
import com.sps.shc.service.ServiceSHC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListenerSHC {

    private static final Logger log = LoggerFactory.getLogger(ListenerSHC.class);

    @Autowired
    private ServiceSHC serviceSHC;

    @RabbitListener(queues = "shc.compras.queue")
    public void recibirCompra(MensajeCompraDTO mensaje) {
        log.info("Mensaje recibido en shc.compras.queue para compra {}", mensaje.getNumeroCompra());
        try {
            if (serviceSHC.yaExisteHistoria(mensaje.getNumeroCompra())) {
                log.info("Historia ya existe para compra {} - ignorando (idempotencia)", mensaje.getNumeroCompra());
                return;
            }
            serviceSHC.crearHistoriaClinica(mensaje);
        } catch (Exception e) {
            log.error("Error procesando mensaje de compra {}: {}", mensaje.getNumeroCompra(), e.getMessage(), e);
            throw e;
        }
    }
}

package com.sps.compra.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class ProxySaludPay {

    private static final Logger logger = Logger.getLogger(ProxySaludPay.class.getName());

    private final RestTemplate restTemplate;

    @Value("${app.saludpay.url}")
    private String saludPayUrl;

    @Value("${app.internal.api.key}")
    private String internalApiKey;

    public ProxySaludPay(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void registrarCompraPendiente(String cedulaCliente, Long numeroCompra, BigDecimal valor) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Key", internalApiKey);
            headers.set("Content-Type", "application/json");

            Map<String, Object> body = new HashMap<>();
            body.put("cedulaCliente", cedulaCliente);
            body.put("numeroCompra", numeroCompra);
            body.put("valorPendiente", valor);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(
                    saludPayUrl + "/ws/saludpay/compras-pendientes",
                    entity,
                    Map.class
            );

            logger.info("ProxySaludPay: compra pendiente registrada para compra " + numeroCompra);
        } catch (Exception e) {
            logger.severe("ProxySaludPay.registrarCompraPendiente error para compra " + numeroCompra + ": " + e.getMessage());
        }
    }
}

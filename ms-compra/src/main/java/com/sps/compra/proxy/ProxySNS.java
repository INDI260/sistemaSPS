package com.sps.compra.proxy;

import com.sps.compra.enums.EstadoSNS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class ProxySNS {

    private static final Logger logger = Logger.getLogger(ProxySNS.class.getName());

    private final RestTemplate restTemplate;

    @Value("${app.sns.url}")
    private String snsUrl;

    public ProxySNS(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Long iniciarValidacion(String codigoPlan) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("codigoPlan", codigoPlan);
            body.put("codigoEmpresaAseguradora", "EMP-SPS-001");

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    snsUrl + "/ws/sns/validar",
                    body,
                    Map.class
            );

            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Object data = responseBody.get("data");
                if (data instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    Object idValidacion = dataMap.get("idValidacion");
                    if (idValidacion instanceof Number) {
                        return ((Number) idValidacion).longValue();
                    }
                }
            }
            logger.warning("ProxySNS.iniciarValidacion: respuesta inesperada para plan " + codigoPlan);
            return null;
        } catch (Exception e) {
            logger.severe("ProxySNS.iniciarValidacion error para plan " + codigoPlan + ": " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public EstadoSNS consultarEstado(Long idValidacion) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    snsUrl + "/ws/sns/estado/" + idValidacion,
                    Map.class
            );

            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Object data = responseBody.get("data");
                if (data instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    Object estadoValidacion = dataMap.get("estadoValidacion");
                    if (estadoValidacion != null) {
                        try {
                            return EstadoSNS.valueOf(estadoValidacion.toString());
                        } catch (IllegalArgumentException ex) {
                            logger.warning("ProxySNS.consultarEstado: estado desconocido: " + estadoValidacion);
                        }
                    }
                }
            }
            return EstadoSNS.EN_PROCESO;
        } catch (Exception e) {
            logger.severe("ProxySNS.consultarEstado error para id " + idValidacion + ": " + e.getMessage());
            return EstadoSNS.EN_PROCESO;
        }
    }
}

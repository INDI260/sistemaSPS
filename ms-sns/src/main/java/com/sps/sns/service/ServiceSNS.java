package com.sps.sns.service;

import com.sps.sns.dto.ValidacionRequestDTO;
import com.sps.sns.dto.ValidacionResponseDTO;
import com.sps.sns.entity.ValidacionSNS;
import com.sps.sns.enums.EstadoSNS;
import com.sps.sns.repository.RepoSNS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

@Service
@Transactional
public class ServiceSNS {

    private static final Logger logger = Logger.getLogger(ServiceSNS.class.getName());

    private final RepoSNS repoSNS;

    @Value("${app.sns.tiempo.procesamiento.ms:15000}")
    private long tiempoProcesamiento;

    private final Timer timerSNS = new Timer("SNSProcessingTimer", true);

    public ServiceSNS(RepoSNS repoSNS) {
        this.repoSNS = repoSNS;
    }

    public ValidacionResponseDTO iniciarValidacion(ValidacionRequestDTO req) {
        ValidacionSNS validacion = new ValidacionSNS();
        validacion.setCodigoPlan(req.getCodigoPlan());
        validacion.setCodigoEmpresaAseguradora(req.getCodigoEmpresaAseguradora());
        validacion.setEstadoValidacion(EstadoSNS.EN_PROCESO);
        validacion.setFechaSolicitud(LocalDateTime.now());

        ValidacionSNS saved = repoSNS.save(validacion);
        Long id = saved.getId();

        timerSNS.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    EstadoSNS resultado = Math.random() > 0.3 ? EstadoSNS.APROBADO : EstadoSNS.RECHAZADO;
                    repoSNS.findById(id).ifPresent(v -> {
                        v.setEstadoValidacion(resultado);
                        v.setFechaRespuesta(LocalDateTime.now());
                        repoSNS.save(v);
                        logger.info("Validacion SNS " + id + " procesada con resultado: " + resultado);
                    });
                } catch (Exception e) {
                    logger.severe("Error procesando validacion SNS " + id + ": " + e.getMessage());
                }
            }
        }, tiempoProcesamiento);

        return new ValidacionResponseDTO(id, EstadoSNS.EN_PROCESO);
    }

    @Transactional(readOnly = true)
    public ValidacionResponseDTO consultarEstado(Long id) {
        ValidacionSNS validacion = repoSNS.findById(id)
                .orElseThrow(() -> new RuntimeException("Validacion SNS no encontrada con id: " + id));
        return new ValidacionResponseDTO(validacion.getId(), validacion.getEstadoValidacion());
    }
}

package com.sps.compra.timer;

import com.sps.compra.service.ServiceCompra;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.logging.Logger;

@Component
public class TimerConfig {

    private static final Logger logger = Logger.getLogger(TimerConfig.class.getName());

    @Autowired
    private ServiceCompra serviceCompra;

    @Value("${app.timer.intervalo.ms:30000}")
    private long intervaloMs;

    @PostConstruct
    public void iniciarTimer() {
        Timer timer = new Timer("ValidacionSNSTimer", true);
        ValidacionSNSTimerTask task = new ValidacionSNSTimerTask(serviceCompra);
        timer.schedule(task, 10000L, intervaloMs);
        logger.info("TimerConfig: ValidacionSNSTimer iniciado con intervalo " + intervaloMs + "ms");
    }
}

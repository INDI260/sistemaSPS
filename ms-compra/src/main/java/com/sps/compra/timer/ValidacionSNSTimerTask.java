package com.sps.compra.timer;

import com.sps.compra.service.ServiceCompra;

import java.util.TimerTask;
import java.util.logging.Logger;

public class ValidacionSNSTimerTask extends TimerTask {

    private static final Logger logger = Logger.getLogger(ValidacionSNSTimerTask.class.getName());

    private final ServiceCompra serviceCompra;

    public ValidacionSNSTimerTask(ServiceCompra serviceCompra) {
        this.serviceCompra = serviceCompra;
    }

    @Override
    public void run() {
        try {
            serviceCompra.procesarValidacionesSNSPendientes();
        } catch (Exception e) {
            logger.severe("ValidacionSNSTimerTask.run error: " + e.getMessage());
        }
    }
}

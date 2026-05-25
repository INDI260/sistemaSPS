package com.sps.compra.dto;

import java.util.List;

public class PlanMensajeDTO {

    private String codigoPlan;
    private String nombrePlan;
    private List<ServicioMensajeDTO> serviciosMedicos;

    public PlanMensajeDTO() {}

    public PlanMensajeDTO(String codigoPlan, String nombrePlan, List<ServicioMensajeDTO> serviciosMedicos) {
        this.codigoPlan = codigoPlan;
        this.nombrePlan = nombrePlan;
        this.serviciosMedicos = serviciosMedicos;
    }

    public String getCodigoPlan() {
        return codigoPlan;
    }

    public void setCodigoPlan(String codigoPlan) {
        this.codigoPlan = codigoPlan;
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(String nombrePlan) {
        this.nombrePlan = nombrePlan;
    }

    public List<ServicioMensajeDTO> getServiciosMedicos() {
        return serviciosMedicos;
    }

    public void setServiciosMedicos(List<ServicioMensajeDTO> serviciosMedicos) {
        this.serviciosMedicos = serviciosMedicos;
    }
}

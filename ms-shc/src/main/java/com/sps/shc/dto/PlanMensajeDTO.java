package com.sps.shc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanMensajeDTO {

    private String codigoPlan;
    private String nombrePlan;
    private List<ServicioMensajeDTO> serviciosMedicos;

    public String getCodigoPlan() { return codigoPlan; }
    public void setCodigoPlan(String codigoPlan) { this.codigoPlan = codigoPlan; }

    public String getNombrePlan() { return nombrePlan; }
    public void setNombrePlan(String nombrePlan) { this.nombrePlan = nombrePlan; }

    public List<ServicioMensajeDTO> getServiciosMedicos() { return serviciosMedicos; }
    public void setServiciosMedicos(List<ServicioMensajeDTO> serviciosMedicos) { this.serviciosMedicos = serviciosMedicos; }
}

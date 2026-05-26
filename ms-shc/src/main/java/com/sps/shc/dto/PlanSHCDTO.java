package com.sps.shc.dto;

import java.util.List;

public class PlanSHCDTO {

    private Long id;
    private String codigoPlan;
    private String nombrePlan;
    private List<ServicioSHCDTO> servicios;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoPlan() { return codigoPlan; }
    public void setCodigoPlan(String codigoPlan) { this.codigoPlan = codigoPlan; }

    public String getNombrePlan() { return nombrePlan; }
    public void setNombrePlan(String nombrePlan) { this.nombrePlan = nombrePlan; }

    public List<ServicioSHCDTO> getServicios() { return servicios; }
    public void setServicios(List<ServicioSHCDTO> servicios) { this.servicios = servicios; }
}

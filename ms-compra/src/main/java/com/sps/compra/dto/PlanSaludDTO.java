package com.sps.compra.dto;

import java.math.BigDecimal;
import java.util.List;

public class PlanSaludDTO {

    private String codigoPlan;
    private String nombrePlan;
    private String descripcion;
    private BigDecimal precio;
    private Boolean activo;
    private List<ServicioMedicoDTO> serviciosMedicos;

    public PlanSaludDTO() {}

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public List<ServicioMedicoDTO> getServiciosMedicos() {
        return serviciosMedicos;
    }

    public void setServiciosMedicos(List<ServicioMedicoDTO> serviciosMedicos) {
        this.serviciosMedicos = serviciosMedicos;
    }
}

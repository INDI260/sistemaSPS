package com.sps.compra.dto;

import java.math.BigDecimal;

public class PlanCompraDTO {

    private Long id;
    private String codigoPlan;
    private String nombrePlan;
    private BigDecimal precio;
    private String estadoSNS;

    public PlanCompraDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getEstadoSNS() {
        return estadoSNS;
    }

    public void setEstadoSNS(String estadoSNS) {
        this.estadoSNS = estadoSNS;
    }
}

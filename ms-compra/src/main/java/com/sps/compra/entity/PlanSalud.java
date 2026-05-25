package com.sps.compra.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "plan_salud")
public class PlanSalud {

    @Id
    @Column(name = "codigo_plan")
    private String codigoPlan;

    @Column(name = "nombre_plan", nullable = false)
    private String nombrePlan;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 15, scale = 2)
    private BigDecimal precio;

    @Column(name = "activo")
    private Boolean activo = true;

    @OneToMany(mappedBy = "planSalud", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ServicioMedico> serviciosMedicos;

    public PlanSalud() {}

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

    public List<ServicioMedico> getServiciosMedicos() {
        return serviciosMedicos;
    }

    public void setServiciosMedicos(List<ServicioMedico> serviciosMedicos) {
        this.serviciosMedicos = serviciosMedicos;
    }
}

package com.sps.compra.entity;

import com.sps.compra.enums.TipoServicioMedico;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "servicio_medico")
public class ServicioMedico {

    @Id
    @Column(name = "codigo_servicio")
    private String codigoServicio;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoServicioMedico tipo;

    @Column(name = "precio", nullable = false, precision = 15, scale = 2)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_plan", nullable = false)
    private PlanSalud planSalud;

    public ServicioMedico() {}

    public String getCodigoServicio() {
        return codigoServicio;
    }

    public void setCodigoServicio(String codigoServicio) {
        this.codigoServicio = codigoServicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoServicioMedico getTipo() {
        return tipo;
    }

    public void setTipo(TipoServicioMedico tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public PlanSalud getPlanSalud() {
        return planSalud;
    }

    public void setPlanSalud(PlanSalud planSalud) {
        this.planSalud = planSalud;
    }
}

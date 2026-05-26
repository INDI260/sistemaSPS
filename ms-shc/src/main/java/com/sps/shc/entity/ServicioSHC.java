package com.sps.shc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "servicio_shc")
public class ServicioSHC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_shc", nullable = false)
    @JsonIgnore
    private PlanSHC planSHC;

    @Column(name = "codigo_servicio", nullable = false, length = 20)
    private String codigoServicio;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PlanSHC getPlanSHC() { return planSHC; }
    public void setPlanSHC(PlanSHC planSHC) { this.planSHC = planSHC; }

    public String getCodigoServicio() { return codigoServicio; }
    public void setCodigoServicio(String codigoServicio) { this.codigoServicio = codigoServicio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}

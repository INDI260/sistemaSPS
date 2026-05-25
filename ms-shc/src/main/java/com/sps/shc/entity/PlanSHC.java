package com.sps.shc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plan_shc")
public class PlanSHC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_historia_clinica", nullable = false)
    @JsonIgnore
    private HistoriaClinica historiaClinica;

    @Column(name = "codigo_plan", nullable = false, length = 20)
    private String codigoPlan;

    @Column(name = "nombre_plan", nullable = false, length = 100)
    private String nombrePlan;

    @OneToMany(mappedBy = "planSHC", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ServicioSHC> servicios = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public HistoriaClinica getHistoriaClinica() { return historiaClinica; }
    public void setHistoriaClinica(HistoriaClinica historiaClinica) { this.historiaClinica = historiaClinica; }

    public String getCodigoPlan() { return codigoPlan; }
    public void setCodigoPlan(String codigoPlan) { this.codigoPlan = codigoPlan; }

    public String getNombrePlan() { return nombrePlan; }
    public void setNombrePlan(String nombrePlan) { this.nombrePlan = nombrePlan; }

    public List<ServicioSHC> getServicios() { return servicios; }
    public void setServicios(List<ServicioSHC> servicios) { this.servicios = servicios; }
}

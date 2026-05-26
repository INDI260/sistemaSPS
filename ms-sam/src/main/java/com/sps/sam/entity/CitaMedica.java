package com.sps.sam.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sps.sam.enums.EstadoCita;
import jakarta.persistence.*;

@Entity
@Table(name = "cita_medica")
public class CitaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agenda_medica", nullable = false)
    @JsonIgnore
    private AgendaMedica agendaMedica;

    @Column(name = "codigo_servicio", nullable = false, length = 20)
    private String codigoServicio;

    @Column(name = "nombre_servicio", nullable = false, length = 100)
    private String nombreServicio;

    @Column(name = "tipo_servicio", nullable = false, length = 30)
    private String tipoServicio;

    @Column(name = "codigo_plan", nullable = false, length = 20)
    private String codigoPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCita estado = EstadoCita.PENDIENTE;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AgendaMedica getAgendaMedica() { return agendaMedica; }
    public void setAgendaMedica(AgendaMedica agendaMedica) { this.agendaMedica = agendaMedica; }

    public String getCodigoServicio() { return codigoServicio; }
    public void setCodigoServicio(String codigoServicio) { this.codigoServicio = codigoServicio; }

    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }

    public String getCodigoPlan() { return codigoPlan; }
    public void setCodigoPlan(String codigoPlan) { this.codigoPlan = codigoPlan; }

    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }
}

package com.sps.sns.entity;

import com.sps.sns.enums.EstadoSNS;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "validacion_sns")
public class ValidacionSNS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_plan", nullable = false)
    private String codigoPlan;

    @Column(name = "codigo_empresa_aseguradora", nullable = false)
    private String codigoEmpresaAseguradora;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", nullable = false)
    private EstadoSNS estadoValidacion;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    public ValidacionSNS() {}

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

    public String getCodigoEmpresaAseguradora() {
        return codigoEmpresaAseguradora;
    }

    public void setCodigoEmpresaAseguradora(String codigoEmpresaAseguradora) {
        this.codigoEmpresaAseguradora = codigoEmpresaAseguradora;
    }

    public EstadoSNS getEstadoValidacion() {
        return estadoValidacion;
    }

    public void setEstadoValidacion(EstadoSNS estadoValidacion) {
        this.estadoValidacion = estadoValidacion;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }
}

package com.sps.sns.dto;

import com.sps.sns.enums.EstadoSNS;

public class ValidacionResponseDTO {

    private Long idValidacion;
    private String estadoValidacion;

    public ValidacionResponseDTO() {}

    public ValidacionResponseDTO(Long idValidacion, EstadoSNS estadoValidacion) {
        this.idValidacion = idValidacion;
        this.estadoValidacion = estadoValidacion != null ? estadoValidacion.name() : null;
    }

    public Long getIdValidacion() {
        return idValidacion;
    }

    public void setIdValidacion(Long idValidacion) {
        this.idValidacion = idValidacion;
    }

    public String getEstadoValidacion() {
        return estadoValidacion;
    }

    public void setEstadoValidacion(String estadoValidacion) {
        this.estadoValidacion = estadoValidacion;
    }
}

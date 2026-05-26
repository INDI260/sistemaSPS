package com.sps.sam.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServicioMensajeDTO {

    private String codigoServicio;
    private String nombreServicio;
    private String tipoServicio;
    private String codigoPlan;

    public String getCodigoServicio() { return codigoServicio; }
    public void setCodigoServicio(String codigoServicio) { this.codigoServicio = codigoServicio; }

    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }

    public String getCodigoPlan() { return codigoPlan; }
    public void setCodigoPlan(String codigoPlan) { this.codigoPlan = codigoPlan; }
}

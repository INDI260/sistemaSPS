package com.sps.sam.dto;

public class CitaMedicaDTO {

    private Long id;
    private String codigoServicio;
    private String nombreServicio;
    private String tipoServicio;
    private String codigoPlan;
    private String estado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoServicio() { return codigoServicio; }
    public void setCodigoServicio(String codigoServicio) { this.codigoServicio = codigoServicio; }

    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }

    public String getCodigoPlan() { return codigoPlan; }
    public void setCodigoPlan(String codigoPlan) { this.codigoPlan = codigoPlan; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}

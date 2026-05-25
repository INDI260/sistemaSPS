package com.sps.shc.dto;

public class ServicioSHCDTO {

    private Long id;
    private String codigoServicio;
    private String nombre;
    private String tipo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoServicio() { return codigoServicio; }
    public void setCodigoServicio(String codigoServicio) { this.codigoServicio = codigoServicio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}

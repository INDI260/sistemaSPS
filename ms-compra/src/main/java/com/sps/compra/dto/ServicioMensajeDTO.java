package com.sps.compra.dto;

public class ServicioMensajeDTO {

    private String codigoServicio;
    private String nombre;
    private String tipo;

    public ServicioMensajeDTO() {}

    public ServicioMensajeDTO(String codigoServicio, String nombre, String tipo) {
        this.codigoServicio = codigoServicio;
        this.nombre = nombre;
        this.tipo = tipo;
    }

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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}

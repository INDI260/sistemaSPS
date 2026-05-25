package com.sps.sam.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MensajeCompraDTO {

    private Long numeroCompra;
    private String cedulaCliente;
    private String nombreCliente;
    private List<ServicioMensajeDTO> serviciosMedicos;
    private String fechaCompra;

    public Long getNumeroCompra() { return numeroCompra; }
    public void setNumeroCompra(Long numeroCompra) { this.numeroCompra = numeroCompra; }

    public String getCedulaCliente() { return cedulaCliente; }
    public void setCedulaCliente(String cedulaCliente) { this.cedulaCliente = cedulaCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public List<ServicioMensajeDTO> getServiciosMedicos() { return serviciosMedicos; }
    public void setServiciosMedicos(List<ServicioMensajeDTO> serviciosMedicos) { this.serviciosMedicos = serviciosMedicos; }

    public String getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(String fechaCompra) { this.fechaCompra = fechaCompra; }
}

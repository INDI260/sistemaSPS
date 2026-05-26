package com.sps.shc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MensajeCompraDTO {

    private Long numeroCompra;
    private String cedulaCliente;
    private String nombreCliente;
    private String correoCliente;
    private List<PlanMensajeDTO> planes;
    private String fechaCompra;

    public Long getNumeroCompra() { return numeroCompra; }
    public void setNumeroCompra(Long numeroCompra) { this.numeroCompra = numeroCompra; }

    public String getCedulaCliente() { return cedulaCliente; }
    public void setCedulaCliente(String cedulaCliente) { this.cedulaCliente = cedulaCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getCorreoCliente() { return correoCliente; }
    public void setCorreoCliente(String correoCliente) { this.correoCliente = correoCliente; }

    public List<PlanMensajeDTO> getPlanes() { return planes; }
    public void setPlanes(List<PlanMensajeDTO> planes) { this.planes = planes; }

    public String getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(String fechaCompra) { this.fechaCompra = fechaCompra; }
}

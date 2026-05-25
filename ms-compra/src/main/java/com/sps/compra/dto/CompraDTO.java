package com.sps.compra.dto;

import java.math.BigDecimal;
import java.util.List;

public class CompraDTO {

    private Long numeroCompra;
    private String cedulaCliente;
    private String nombreCliente;
    private BigDecimal precioTotal;
    private String estado;
    private String fechaCreacion;
    private List<PlanCompraDTO> planes;

    public CompraDTO() {}

    public Long getNumeroCompra() {
        return numeroCompra;
    }

    public void setNumeroCompra(Long numeroCompra) {
        this.numeroCompra = numeroCompra;
    }

    public String getCedulaCliente() {
        return cedulaCliente;
    }

    public void setCedulaCliente(String cedulaCliente) {
        this.cedulaCliente = cedulaCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public BigDecimal getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(BigDecimal precioTotal) {
        this.precioTotal = precioTotal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<PlanCompraDTO> getPlanes() {
        return planes;
    }

    public void setPlanes(List<PlanCompraDTO> planes) {
        this.planes = planes;
    }
}

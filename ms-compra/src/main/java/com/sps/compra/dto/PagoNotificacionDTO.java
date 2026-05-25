package com.sps.compra.dto;

import java.math.BigDecimal;

public class PagoNotificacionDTO {

    private String cedulaCliente;
    private Long numeroCompra;
    private BigDecimal valorPagado;

    public PagoNotificacionDTO() {}

    public String getCedulaCliente() {
        return cedulaCliente;
    }

    public void setCedulaCliente(String cedulaCliente) {
        this.cedulaCliente = cedulaCliente;
    }

    public Long getNumeroCompra() {
        return numeroCompra;
    }

    public void setNumeroCompra(Long numeroCompra) {
        this.numeroCompra = numeroCompra;
    }

    public BigDecimal getValorPagado() {
        return valorPagado;
    }

    public void setValorPagado(BigDecimal valorPagado) {
        this.valorPagado = valorPagado;
    }
}

package com.sps.compra.dto;

public class CompraResumenDTO {

    private Long numeroCompra;
    private String estado;

    public CompraResumenDTO() {}

    public CompraResumenDTO(Long numeroCompra, String estado) {
        this.numeroCompra = numeroCompra;
        this.estado = estado;
    }

    public Long getNumeroCompra() {
        return numeroCompra;
    }

    public void setNumeroCompra(Long numeroCompra) {
        this.numeroCompra = numeroCompra;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

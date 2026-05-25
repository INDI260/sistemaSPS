package com.sps.compra.dto;

import java.util.List;

public class CompraRequestDTO {

    private List<String> codigosPlanes;

    public CompraRequestDTO() {}

    public CompraRequestDTO(List<String> codigosPlanes) {
        this.codigosPlanes = codigosPlanes;
    }

    public List<String> getCodigosPlanes() {
        return codigosPlanes;
    }

    public void setCodigosPlanes(List<String> codigosPlanes) {
        this.codigosPlanes = codigosPlanes;
    }
}

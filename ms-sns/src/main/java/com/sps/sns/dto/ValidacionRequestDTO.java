package com.sps.sns.dto;

public class ValidacionRequestDTO {

    private String codigoPlan;
    private String codigoEmpresaAseguradora;

    public ValidacionRequestDTO() {}

    public ValidacionRequestDTO(String codigoPlan, String codigoEmpresaAseguradora) {
        this.codigoPlan = codigoPlan;
        this.codigoEmpresaAseguradora = codigoEmpresaAseguradora;
    }

    public String getCodigoPlan() {
        return codigoPlan;
    }

    public void setCodigoPlan(String codigoPlan) {
        this.codigoPlan = codigoPlan;
    }

    public String getCodigoEmpresaAseguradora() {
        return codigoEmpresaAseguradora;
    }

    public void setCodigoEmpresaAseguradora(String codigoEmpresaAseguradora) {
        this.codigoEmpresaAseguradora = codigoEmpresaAseguradora;
    }
}

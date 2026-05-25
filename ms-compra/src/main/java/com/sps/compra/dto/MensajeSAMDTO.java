package com.sps.compra.dto;

import java.util.List;

public class MensajeSAMDTO {

    private Long numeroCompra;
    private String cedulaCliente;
    private String nombreCliente;
    private List<ServicioSAMDTO> serviciosMedicos;
    private String fechaCompra;

    public MensajeSAMDTO() {}

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

    public List<ServicioSAMDTO> getServiciosMedicos() {
        return serviciosMedicos;
    }

    public void setServiciosMedicos(List<ServicioSAMDTO> serviciosMedicos) {
        this.serviciosMedicos = serviciosMedicos;
    }

    public String getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(String fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public static class ServicioSAMDTO {

        private String codigoServicio;
        private String nombreServicio;
        private String tipoServicio;
        private String codigoPlan;

        public ServicioSAMDTO() {}

        public ServicioSAMDTO(String codigoServicio, String nombreServicio, String tipoServicio, String codigoPlan) {
            this.codigoServicio = codigoServicio;
            this.nombreServicio = nombreServicio;
            this.tipoServicio = tipoServicio;
            this.codigoPlan = codigoPlan;
        }

        public String getCodigoServicio() {
            return codigoServicio;
        }

        public void setCodigoServicio(String codigoServicio) {
            this.codigoServicio = codigoServicio;
        }

        public String getNombreServicio() {
            return nombreServicio;
        }

        public void setNombreServicio(String nombreServicio) {
            this.nombreServicio = nombreServicio;
        }

        public String getTipoServicio() {
            return tipoServicio;
        }

        public void setTipoServicio(String tipoServicio) {
            this.tipoServicio = tipoServicio;
        }

        public String getCodigoPlan() {
            return codigoPlan;
        }

        public void setCodigoPlan(String codigoPlan) {
            this.codigoPlan = codigoPlan;
        }
    }
}

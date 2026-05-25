package com.sps.shc.dto;

import java.util.List;

public class HistoriaClinicaDTO {

    private Long id;
    private String cedulaCliente;
    private String nombreCliente;
    private String correoCliente;
    private Long numeroCompraOrigen;
    private String fechaRegistro;
    private List<PlanSHCDTO> planes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCedulaCliente() { return cedulaCliente; }
    public void setCedulaCliente(String cedulaCliente) { this.cedulaCliente = cedulaCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getCorreoCliente() { return correoCliente; }
    public void setCorreoCliente(String correoCliente) { this.correoCliente = correoCliente; }

    public Long getNumeroCompraOrigen() { return numeroCompraOrigen; }
    public void setNumeroCompraOrigen(Long numeroCompraOrigen) { this.numeroCompraOrigen = numeroCompraOrigen; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public List<PlanSHCDTO> getPlanes() { return planes; }
    public void setPlanes(List<PlanSHCDTO> planes) { this.planes = planes; }
}

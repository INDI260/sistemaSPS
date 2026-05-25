package com.sps.sam.dto;

import java.util.List;

public class AgendaMedicaDTO {

    private Long id;
    private String cedulaCliente;
    private String nombreCliente;
    private Long numeroCompraOrigen;
    private String fechaRegistro;
    private List<CitaMedicaDTO> citas;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCedulaCliente() { return cedulaCliente; }
    public void setCedulaCliente(String cedulaCliente) { this.cedulaCliente = cedulaCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public Long getNumeroCompraOrigen() { return numeroCompraOrigen; }
    public void setNumeroCompraOrigen(Long numeroCompraOrigen) { this.numeroCompraOrigen = numeroCompraOrigen; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public List<CitaMedicaDTO> getCitas() { return citas; }
    public void setCitas(List<CitaMedicaDTO> citas) { this.citas = citas; }
}

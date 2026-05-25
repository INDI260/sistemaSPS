package com.sps.shc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "historia_clinica")
public class HistoriaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cedula_cliente", nullable = false, length = 20)
    private String cedulaCliente;

    @Column(name = "nombre_cliente", nullable = false, length = 200)
    private String nombreCliente;

    @Column(name = "correo_cliente", nullable = false, length = 150)
    private String correoCliente;

    @Column(name = "numero_compra_origen", nullable = false, unique = true)
    private Long numeroCompraOrigen;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "historiaClinica", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonIgnore
    private List<PlanSHC> planes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
    }

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

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public List<PlanSHC> getPlanes() { return planes; }
    public void setPlanes(List<PlanSHC> planes) { this.planes = planes; }
}

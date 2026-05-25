package com.sps.compra.entity;

import com.sps.compra.enums.EstadoSNS;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "plan_compra")
public class PlanCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "numero_compra", nullable = false)
    private Compra compra;

    @Column(name = "codigo_plan", nullable = false)
    private String codigoPlan;

    @Column(name = "nombre_plan", nullable = false)
    private String nombrePlan;

    @Column(name = "precio", nullable = false, precision = 15, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_sns", nullable = false)
    private EstadoSNS estadoSNS = EstadoSNS.PENDIENTE;

    @Column(name = "intentos_sns")
    private int intentosSNS = 0;

    @Column(name = "id_validacion_sns")
    private Long idValidacionSNS;

    @Column(name = "ultimo_intento")
    private LocalDateTime ultimoIntento;

    public PlanCompra() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public String getCodigoPlan() {
        return codigoPlan;
    }

    public void setCodigoPlan(String codigoPlan) {
        this.codigoPlan = codigoPlan;
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(String nombrePlan) {
        this.nombrePlan = nombrePlan;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public EstadoSNS getEstadoSNS() {
        return estadoSNS;
    }

    public void setEstadoSNS(EstadoSNS estadoSNS) {
        this.estadoSNS = estadoSNS;
    }

    public int getIntentosSNS() {
        return intentosSNS;
    }

    public void setIntentosSNS(int intentosSNS) {
        this.intentosSNS = intentosSNS;
    }

    public Long getIdValidacionSNS() {
        return idValidacionSNS;
    }

    public void setIdValidacionSNS(Long idValidacionSNS) {
        this.idValidacionSNS = idValidacionSNS;
    }

    public LocalDateTime getUltimoIntento() {
        return ultimoIntento;
    }

    public void setUltimoIntento(LocalDateTime ultimoIntento) {
        this.ultimoIntento = ultimoIntento;
    }
}

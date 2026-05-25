package com.sps.compra.repository;

import com.sps.compra.entity.Compra;
import com.sps.compra.enums.EstadoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoCompra extends JpaRepository<Compra, Long> {

    List<Compra> findByEstadoIn(List<EstadoCompra> estados);

    List<Compra> findByCedulaCliente(String cedulaCliente);
}

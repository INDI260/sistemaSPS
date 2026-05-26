package com.sps.shc.repository;

import com.sps.shc.entity.HistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoHistoriaClinica extends JpaRepository<HistoriaClinica, Long> {
    List<HistoriaClinica> findByCedulaCliente(String cedula);
    Optional<HistoriaClinica> findByNumeroCompraOrigen(Long numeroCompra);
    boolean existsByNumeroCompraOrigen(Long numeroCompra);
}

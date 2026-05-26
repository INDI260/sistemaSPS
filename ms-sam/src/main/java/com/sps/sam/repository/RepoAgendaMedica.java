package com.sps.sam.repository;

import com.sps.sam.entity.AgendaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoAgendaMedica extends JpaRepository<AgendaMedica, Long> {
    List<AgendaMedica> findByCedulaCliente(String cedula);
    Optional<AgendaMedica> findByNumeroCompraOrigen(Long numeroCompra);
    boolean existsByNumeroCompraOrigen(Long numeroCompra);
}

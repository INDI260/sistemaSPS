package com.sps.compra.repository;

import com.sps.compra.entity.ServicioMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoServicioMedico extends JpaRepository<ServicioMedico, String> {
}

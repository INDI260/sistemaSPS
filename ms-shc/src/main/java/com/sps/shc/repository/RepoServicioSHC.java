package com.sps.shc.repository;

import com.sps.shc.entity.ServicioSHC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoServicioSHC extends JpaRepository<ServicioSHC, Long> {
}

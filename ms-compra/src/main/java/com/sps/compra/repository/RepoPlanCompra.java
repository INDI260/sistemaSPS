package com.sps.compra.repository;

import com.sps.compra.entity.PlanCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoPlanCompra extends JpaRepository<PlanCompra, Long> {
}

package com.sps.compra.repository;

import com.sps.compra.entity.PlanSalud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoPlanSalud extends JpaRepository<PlanSalud, String> {

    List<PlanSalud> findByActivoTrue();
}

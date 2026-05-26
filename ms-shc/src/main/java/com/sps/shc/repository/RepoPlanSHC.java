package com.sps.shc.repository;

import com.sps.shc.entity.PlanSHC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoPlanSHC extends JpaRepository<PlanSHC, Long> {
}

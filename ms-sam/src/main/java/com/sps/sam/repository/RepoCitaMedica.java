package com.sps.sam.repository;

import com.sps.sam.entity.CitaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoCitaMedica extends JpaRepository<CitaMedica, Long> {
}

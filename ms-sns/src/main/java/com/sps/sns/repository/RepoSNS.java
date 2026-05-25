package com.sps.sns.repository;

import com.sps.sns.entity.ValidacionSNS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoSNS extends JpaRepository<ValidacionSNS, Long> {
}

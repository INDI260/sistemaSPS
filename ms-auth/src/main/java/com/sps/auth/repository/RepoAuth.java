package com.sps.auth.repository;

import com.sps.auth.entity.UsuarioSPS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepoAuth extends JpaRepository<UsuarioSPS, String> {
    Optional<UsuarioSPS> findByCedula(String cedula);
    Optional<UsuarioSPS> findByCorreo(String correo);
    boolean existsByCedula(String cedula);
}

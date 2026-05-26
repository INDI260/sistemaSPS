package com.sps.auth.service;

import com.sps.auth.dto.AuthDTOs.*;
import com.sps.auth.entity.RolUsuario;
import com.sps.auth.entity.UsuarioSPS;
import com.sps.auth.repository.RepoAuth;
import com.sps.auth.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ServiceAuth {

    @Autowired private RepoAuth repoAuth;
    @Autowired private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public LoginResponseDTO login(String cedula, String contrasena) {
        UsuarioSPS usuario = repoAuth.findByCedula(cedula)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!encoder.matches(contrasena, usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        if (!usuario.getActivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inactivo");
        }

        String token = jwtUtil.generarToken(usuario);
        return new LoginResponseDTO(
            token,
            usuario.getCedula(),
            usuario.getNombre() + " " + usuario.getApellido(),
            usuario.getRol().name()
        );
    }

    public UsuarioDTO registrar(RegistroRequestDTO dto) {
        if (repoAuth.existsByCedula(dto.cedula())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cédula ya está registrada");
        }

        UsuarioSPS usuario = new UsuarioSPS();
        usuario.setCedula(dto.cedula());
        usuario.setNombre(dto.nombre());
        usuario.setApellido(dto.apellido());
        usuario.setCorreo(dto.correo());
        usuario.setContrasena(encoder.encode(dto.contrasena()));
        usuario.setRol(RolUsuario.CLIENTE);

        repoAuth.save(usuario);
        return toDTO(usuario);
    }

    public UsuarioDTO validarToken(String token) {
        Claims claims = jwtUtil.validarToken(token);
        String nombreCompleto = claims.get("nombre", String.class);
        return new UsuarioDTO(
            claims.get("cedula", String.class),
            nombreCompleto,
            "",
            claims.get("correo", String.class),
            claims.get("rol", String.class)
        );
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorCedula(String cedula) {
        UsuarioSPS usuario = repoAuth.findByCedula(cedula)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return toDTO(usuario);
    }

    private UsuarioDTO toDTO(UsuarioSPS u) {
        return new UsuarioDTO(u.getCedula(), u.getNombre(), u.getApellido(), u.getCorreo(), u.getRol().name());
    }
}

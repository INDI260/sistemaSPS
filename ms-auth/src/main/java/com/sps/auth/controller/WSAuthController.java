package com.sps.auth.controller;

import com.sps.auth.dto.ApiResponse;
import com.sps.auth.dto.AuthDTOs.*;
import com.sps.auth.service.ServiceAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ws/auth")
public class WSAuthController {

    @Autowired private ServiceAuth serviceAuth;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody LoginRequestDTO req) {
        LoginResponseDTO data = serviceAuth.login(req.cedula(), req.contrasena());
        return ResponseEntity.ok(ApiResponse.ok(data, "Login exitoso"));
    }

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<UsuarioDTO>> registro(@RequestBody RegistroRequestDTO req) {
        UsuarioDTO data = serviceAuth.registrar(req);
        return ResponseEntity.status(201).body(ApiResponse.ok(data, "Usuario registrado"));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<UsuarioDTO>> validate(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        UsuarioDTO data = serviceAuth.validarToken(token);
        return ResponseEntity.ok(ApiResponse.ok(data, "Token válido"));
    }

    @GetMapping("/usuarios/{cedula}")
    public ResponseEntity<ApiResponse<UsuarioDTO>> obtenerUsuario(@PathVariable String cedula) {
        UsuarioDTO data = serviceAuth.obtenerPorCedula(cedula);
        return ResponseEntity.ok(ApiResponse.ok(data, "Usuario encontrado"));
    }
}

package com.sps.auth.dto;

public class AuthDTOs {

    public record LoginRequestDTO(String cedula, String contrasena) {}

    public record LoginResponseDTO(String token, String cedula, String nombre, String rol) {}

    public record RegistroRequestDTO(
            String cedula,
            String nombre,
            String apellido,
            String correo,
            String contrasena) {}

    public record UsuarioDTO(
            String cedula,
            String nombre,
            String apellido,
            String correo,
            String rol) {}
}

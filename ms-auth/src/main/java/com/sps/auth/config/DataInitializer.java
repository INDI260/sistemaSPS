package com.sps.auth.config;

import com.sps.auth.entity.RolUsuario;
import com.sps.auth.entity.UsuarioSPS;
import com.sps.auth.repository.RepoAuth;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final RepoAuth repoAuth;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public DataInitializer(RepoAuth repoAuth) {
        this.repoAuth = repoAuth;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!repoAuth.existsByCedula("1000000001")) {
            UsuarioSPS usuario = new UsuarioSPS();
            usuario.setCedula("1000000001");
            usuario.setNombre("Carlos");
            usuario.setApellido("Pérez");
            usuario.setCorreo("carlos@test.com");
            usuario.setContrasena(encoder.encode("Password123!"));
            usuario.setRol(RolUsuario.CLIENTE);
            usuario.setActivo(true);
            repoAuth.save(usuario);
        }
    }
}

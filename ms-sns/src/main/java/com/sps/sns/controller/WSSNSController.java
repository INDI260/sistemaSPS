package com.sps.sns.controller;

import com.sps.sns.dto.ApiResponse;
import com.sps.sns.dto.ValidacionRequestDTO;
import com.sps.sns.dto.ValidacionResponseDTO;
import com.sps.sns.service.ServiceSNS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ws/sns")
public class WSSNSController {

    private final ServiceSNS serviceSNS;

    public WSSNSController(ServiceSNS serviceSNS) {
        this.serviceSNS = serviceSNS;
    }

    @PostMapping("/validar")
    public ResponseEntity<ApiResponse<ValidacionResponseDTO>> iniciarValidacion(
            @RequestBody ValidacionRequestDTO request) {
        try {
            ValidacionResponseDTO response = serviceSNS.iniciarValidacion(request);
            return ResponseEntity.ok(ApiResponse.ok(response, "Validacion iniciada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al iniciar la validacion: " + e.getMessage()));
        }
    }

    @GetMapping("/estado/{id}")
    public ResponseEntity<ApiResponse<ValidacionResponseDTO>> consultarEstado(@PathVariable Long id) {
        try {
            ValidacionResponseDTO response = serviceSNS.consultarEstado(id);
            return ResponseEntity.ok(ApiResponse.ok(response, "Estado consultado exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Error interno al consultar estado: " + e.getMessage()));
        }
    }
}

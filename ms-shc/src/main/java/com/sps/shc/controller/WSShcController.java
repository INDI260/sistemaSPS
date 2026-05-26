package com.sps.shc.controller;

import com.sps.shc.dto.HistoriaClinicaDTO;
import com.sps.shc.service.ServiceSHC;
import com.sps.shc.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ws/shc")
public class WSShcController {

    @Autowired
    private ServiceSHC serviceSHC;

    @GetMapping("/historias/{cedula}")
    public ResponseEntity<ApiResponse<List<HistoriaClinicaDTO>>> listarHistorias(@PathVariable String cedula) {
        List<HistoriaClinicaDTO> historias = serviceSHC.listarPorCedula(cedula);
        return ResponseEntity.ok(ApiResponse.ok(historias, "Historias clinicas encontradas"));
    }

    @GetMapping("/historias/compra/{numeroCompra}")
    public ResponseEntity<ApiResponse<HistoriaClinicaDTO>> obtenerPorCompra(@PathVariable Long numeroCompra) {
        HistoriaClinicaDTO historia = serviceSHC.obtenerPorNumeroCompra(numeroCompra);
        return ResponseEntity.ok(ApiResponse.ok(historia, "Historia encontrada"));
    }
}

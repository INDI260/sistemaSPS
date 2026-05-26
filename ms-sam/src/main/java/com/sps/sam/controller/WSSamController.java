package com.sps.sam.controller;

import com.sps.sam.dto.AgendaMedicaDTO;
import com.sps.sam.service.ServiceSAM;
import com.sps.sam.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ws/sam")
public class WSSamController {

    @Autowired
    private ServiceSAM serviceSAM;

    @GetMapping("/agenda/{cedula}")
    public ResponseEntity<ApiResponse<List<AgendaMedicaDTO>>> listarAgenda(@PathVariable String cedula) {
        List<AgendaMedicaDTO> agenda = serviceSAM.listarPorCedula(cedula);
        return ResponseEntity.ok(ApiResponse.ok(agenda, "Agenda medica encontrada"));
    }

    @GetMapping("/agenda/compra/{numeroCompra}")
    public ResponseEntity<ApiResponse<AgendaMedicaDTO>> obtenerPorCompra(@PathVariable Long numeroCompra) {
        AgendaMedicaDTO agenda = serviceSAM.obtenerPorNumeroCompra(numeroCompra);
        return ResponseEntity.ok(ApiResponse.ok(agenda, "Agenda encontrada"));
    }
}

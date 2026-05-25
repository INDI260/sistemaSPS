package com.sps.sam.service;

import com.sps.sam.dto.*;
import com.sps.sam.entity.AgendaMedica;
import com.sps.sam.entity.CitaMedica;
import com.sps.sam.enums.EstadoCita;
import com.sps.sam.repository.RepoAgendaMedica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServiceSAM {

    private static final Logger log = LoggerFactory.getLogger(ServiceSAM.class);

    @Autowired
    private RepoAgendaMedica repoAgendaMedica;

    @Transactional(readOnly = true)
    public boolean yaExisteAgenda(Long numeroCompra) {
        return repoAgendaMedica.existsByNumeroCompraOrigen(numeroCompra);
    }

    public void crearAgendaMedica(MensajeCompraDTO mensaje) {
        if (mensaje.getNumeroCompra() == null) {
            throw new IllegalArgumentException("numeroCompra es obligatorio");
        }

        AgendaMedica agenda = new AgendaMedica();
        agenda.setCedulaCliente(mensaje.getCedulaCliente());
        agenda.setNombreCliente(mensaje.getNombreCliente());
        agenda.setNumeroCompraOrigen(mensaje.getNumeroCompra());

        List<CitaMedica> citas = new ArrayList<>();
        if (mensaje.getServiciosMedicos() != null) {
            for (ServicioMensajeDTO svcDto : mensaje.getServiciosMedicos()) {
                CitaMedica cita = new CitaMedica();
                cita.setCodigoServicio(svcDto.getCodigoServicio());
                cita.setNombreServicio(svcDto.getNombreServicio());
                cita.setTipoServicio(svcDto.getTipoServicio());
                cita.setCodigoPlan(svcDto.getCodigoPlan());
                cita.setEstado(EstadoCita.PENDIENTE);
                cita.setAgendaMedica(agenda);
                citas.add(cita);
            }
        }
        agenda.setCitas(citas);
        repoAgendaMedica.save(agenda);
        log.info("Agenda medica creada para compra {} (cedula {}) con {} citas",
                mensaje.getNumeroCompra(), mensaje.getCedulaCliente(), citas.size());
    }

    @Transactional(readOnly = true)
    public List<AgendaMedicaDTO> listarPorCedula(String cedula) {
        return repoAgendaMedica.findByCedulaCliente(cedula)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AgendaMedicaDTO obtenerPorNumeroCompra(Long numeroCompra) {
        return repoAgendaMedica.findByNumeroCompraOrigen(numeroCompra)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agenda no encontrada"));
    }

    private AgendaMedicaDTO toDTO(AgendaMedica a) {
        AgendaMedicaDTO dto = new AgendaMedicaDTO();
        dto.setId(a.getId());
        dto.setCedulaCliente(a.getCedulaCliente());
        dto.setNombreCliente(a.getNombreCliente());
        dto.setNumeroCompraOrigen(a.getNumeroCompraOrigen());
        dto.setFechaRegistro(a.getFechaRegistro() != null ? a.getFechaRegistro().toString() : null);
        dto.setCitas(a.getCitas().stream().map(this::toCitaDTO).collect(Collectors.toList()));
        return dto;
    }

    private CitaMedicaDTO toCitaDTO(CitaMedica c) {
        CitaMedicaDTO dto = new CitaMedicaDTO();
        dto.setId(c.getId());
        dto.setCodigoServicio(c.getCodigoServicio());
        dto.setNombreServicio(c.getNombreServicio());
        dto.setTipoServicio(c.getTipoServicio());
        dto.setCodigoPlan(c.getCodigoPlan());
        dto.setEstado(c.getEstado() != null ? c.getEstado().name() : null);
        return dto;
    }
}

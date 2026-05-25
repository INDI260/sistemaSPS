package com.sps.compra.controller;

import com.sps.compra.dto.*;
import com.sps.compra.service.ServiceCompra;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/ws/compra")
public class WSCompraController {

    private static final Logger logger = Logger.getLogger(WSCompraController.class.getName());

    private final ServiceCompra serviceCompra;

    public WSCompraController(ServiceCompra serviceCompra) {
        this.serviceCompra = serviceCompra;
    }

    @GetMapping("/planes")
    public ResponseEntity<ApiResponse<List<PlanSaludDTO>>> listarPlanes() {
        try {
            List<PlanSaludDTO> planes = serviceCompra.listarPlanesActivos();
            return ResponseEntity.ok(ApiResponse.ok(planes, "Planes obtenidos exitosamente"));
        } catch (Exception e) {
            logger.severe("WSCompraController.listarPlanes error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener los planes: " + e.getMessage()));
        }
    }

    @GetMapping("/planes/{codigo}")
    public ResponseEntity<ApiResponse<PlanSaludDTO>> obtenerPlan(@PathVariable String codigo) {
        try {
            PlanSaludDTO plan = serviceCompra.obtenerPlan(codigo);
            return ResponseEntity.ok(ApiResponse.ok(plan, "Plan obtenido exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.severe("WSCompraController.obtenerPlan error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener el plan: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompraResumenDTO>> crearCompra(
            @RequestBody CompraRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            String cedulaCliente = (String) httpRequest.getAttribute("cedula_cliente");
            String nombreCliente = (String) httpRequest.getAttribute("nombre_cliente");
            String correoCliente = (String) httpRequest.getAttribute("correo_cliente");

            if (cedulaCliente == null || cedulaCliente.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("No se pudo obtener la informacion del cliente del token JWT"));
            }

            CompraResumenDTO resumen = serviceCompra.crearCompra(request, cedulaCliente, nombreCliente, correoCliente);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(ApiResponse.ok(resumen, "Compra creada exitosamente, en proceso de validacion SNS"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.severe("WSCompraController.crearCompra error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al crear la compra: " + e.getMessage()));
        }
    }

    @GetMapping("/{numero}")
    public ResponseEntity<ApiResponse<CompraDTO>> obtenerCompra(@PathVariable Long numero) {
        try {
            CompraDTO compra = serviceCompra.obtenerCompra(numero);
            return ResponseEntity.ok(ApiResponse.ok(compra, "Compra obtenida exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.severe("WSCompraController.obtenerCompra error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener la compra: " + e.getMessage()));
        }
    }

    @GetMapping("/cliente/{cedula}")
    public ResponseEntity<ApiResponse<List<CompraDTO>>> obtenerComprasPorCliente(@PathVariable String cedula) {
        try {
            List<CompraDTO> compras = serviceCompra.obtenerComprasPorCliente(cedula);
            return ResponseEntity.ok(ApiResponse.ok(compras, "Compras del cliente obtenidas exitosamente"));
        } catch (Exception e) {
            logger.severe("WSCompraController.obtenerComprasPorCliente error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener las compras del cliente: " + e.getMessage()));
        }
    }

    @PostMapping("/{numero}/pago")
    public ResponseEntity<ApiResponse<CompraDTO>> procesarPago(
            @PathVariable Long numero,
            @RequestBody PagoNotificacionDTO pago,
            @RequestHeader(value = "X-Internal-Key", required = false) String internalKey) {
        try {
            CompraDTO compra = serviceCompra.procesarPago(numero, pago, internalKey);
            return ResponseEntity.ok(ApiResponse.ok(compra, "Pago procesado y compra completada exitosamente"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.severe("WSCompraController.procesarPago error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al procesar el pago: " + e.getMessage()));
        }
    }
}

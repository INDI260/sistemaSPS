import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpResponse } from '@angular/common/http';
import { Observable, of, delay } from 'rxjs';

const MOCK_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDAwMDAwMDAxIiwiY2VkdWxhIjoiMTAwMDAwMDAwMSIsIm5vbWJyZSI6IkNhcmxvcyBQw6lyZXoiLCJyb2wiOiJDTElFTlRFIn0.mock';

const PLANES = [
  {
    codigoPlan: 'PLAN-001',
    nombrePlan: 'Plan Básico',
    descripcion: 'Cobertura esencial con consulta general y exámenes básicos de laboratorio.',
    precio: 150000,
    serviciosMedicos: [
      { codigoServicio: 'SERV-001', nombre: 'Consulta General', tipo: 'CONSULTA', precio: 50000 },
      { codigoServicio: 'SERV-002', nombre: 'Examen de Sangre', tipo: 'EXAMEN', precio: 100000 }
    ]
  },
  {
    codigoPlan: 'PLAN-002',
    nombrePlan: 'Plan Plus',
    descripcion: 'Cobertura ampliada con hospitalización y consulta con especialista.',
    precio: 350000,
    serviciosMedicos: [
      { codigoServicio: 'SERV-003', nombre: 'Hospitalización', tipo: 'HOSPITALIZACION', precio: 200000 },
      { codigoServicio: 'SERV-004', nombre: 'Consulta Especialista', tipo: 'CONSULTA', precio: 150000 }
    ]
  },
  {
    codigoPlan: 'PLAN-003',
    nombrePlan: 'Plan Premium',
    descripcion: 'Cobertura completa incluyendo cirugía ambulatoria y medicina preventiva.',
    precio: 600000,
    serviciosMedicos: [
      { codigoServicio: 'SERV-005', nombre: 'Cirugía Ambulatoria', tipo: 'EXAMEN', precio: 400000 },
      { codigoServicio: 'SERV-006', nombre: 'Consulta Preventiva', tipo: 'CONSULTA', precio: 200000 }
    ]
  }
];

const MOCK_COMPRAS = [
  {
    numeroCompra: 1001,
    cedulaCliente: '1000000001',
    nombreCliente: 'Carlos Pérez',
    precioTotal: 500000,
    estado: 'COMPLETADO',
    fechaCreacion: new Date().toISOString(),
    planes: [
      { codigoPlan: 'PLAN-001', nombrePlan: 'Plan Básico', precio: 150000, estadoSns: 'APROBADO' },
      { codigoPlan: 'PLAN-002', nombrePlan: 'Plan Plus', precio: 350000, estadoSns: 'APROBADO' }
    ]
  },
  {
    numeroCompra: 1002,
    cedulaCliente: '1000000001',
    nombreCliente: 'Carlos Pérez',
    precioTotal: 600000,
    estado: 'PENDIENTE_PAGO',
    fechaCreacion: new Date(Date.now() - 86400000).toISOString(),
    planes: [
      { codigoPlan: 'PLAN-003', nombrePlan: 'Plan Premium', precio: 600000, estadoSns: 'APROBADO' }
    ]
  },
  {
    numeroCompra: 1003,
    cedulaCliente: '1000000001',
    nombreCliente: 'Carlos Pérez',
    precioTotal: 150000,
    estado: 'PENDIENTE_VALIDACION_SNS',
    fechaCreacion: new Date(Date.now() - 3600000).toISOString(),
    planes: [
      { codigoPlan: 'PLAN-001', nombrePlan: 'Plan Básico', precio: 150000, estadoSns: 'PENDIENTE' }
    ]
  }
];

@Injectable()
export class MockInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const { method, url } = req;

    if (url.endsWith('/api/auth/login') && method === 'POST') {
      return of(new HttpResponse({
        status: 200,
        body: {
          status: 'OK',
          data: { token: MOCK_TOKEN, cedula: '1000000001', nombre: 'Carlos Pérez', rol: 'CLIENTE' },
          message: 'Inicio de sesión exitoso',
          timestamp: new Date().toISOString()
        }
      })).pipe(delay(500));
    }

    if (url.endsWith('/api/compra/planes') && method === 'GET') {
      return of(new HttpResponse({
        status: 200,
        body: {
          status: 'OK',
          data: PLANES,
          message: 'Planes obtenidos exitosamente',
          timestamp: new Date().toISOString()
        }
      })).pipe(delay(400));
    }

    if (url.includes('/api/compra/cliente/') && method === 'GET') {
      return of(new HttpResponse({
        status: 200,
        body: {
          status: 'OK',
          data: MOCK_COMPRAS,
          message: 'Compras obtenidas exitosamente',
          timestamp: new Date().toISOString()
        }
      })).pipe(delay(400));
    }

    if (url.endsWith('/api/compra') && method === 'POST') {
      const body: any = req.body;
      return of(new HttpResponse({
        status: 202,
        body: {
          status: 'OK',
          data: { numeroCompra: 1004, estado: 'PENDIENTE_VALIDACION_SNS' },
          message: 'Solicitud recibida. Recibirá un correo cuando sea posible continuar la compra.',
          timestamp: new Date().toISOString()
        }
      })).pipe(delay(600));
    }

    return next.handle(req);
  }
}

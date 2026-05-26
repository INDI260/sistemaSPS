export interface Compra {
  numeroCompra: number;
  cedulaCliente: string;
  nombreCliente: string;
  precioTotal: number;
  estado: string;
  fechaCreacion: string;
  planes: PlanCompra[];
}

export interface PlanCompra {
  codigoPlan: string;
  nombrePlan: string;
  precio: number;
  estadoSns: string;
}

export interface CompraRequest {
  cedulaCliente: string;
  codigosPlanes: string[];
}

export interface CompraResumen {
  numeroCompra: number;
  estado: string;
}

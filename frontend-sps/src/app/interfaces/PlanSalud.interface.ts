import { ServicioMedico } from './ServicioMedico.interface';

export interface PlanSalud {
  codigoPlan: string;
  nombrePlan: string;
  descripcion: string;
  precio: number;
  serviciosMedicos: ServicioMedico[];
}

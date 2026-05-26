export interface ServicioMedico {
  codigoServicio: string;
  nombre: string;
  tipo: 'CONSULTA' | 'EXAMEN' | 'HOSPITALIZACION';
  precio: number;
}

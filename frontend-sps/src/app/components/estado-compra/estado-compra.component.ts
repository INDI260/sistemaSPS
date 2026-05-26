import { Component, OnInit } from '@angular/core';
import { CompraService } from '../../services/compra.service';
import { AuthService } from '../../services/auth.service';
import { Compra } from '../../interfaces/Compra.interface';

@Component({
  selector: 'app-estado-compra',
  templateUrl: './estado-compra.component.html',
  styleUrls: ['./estado-compra.component.css']
})
export class EstadoCompraComponent implements OnInit {
  compras: Compra[] = [];
  loading: boolean = true;
  error: string = '';

  constructor(
    private compraService: CompraService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.cargarCompras();
  }

  cargarCompras(): void {
    const usuario = this.authService.getUsuarioActual();
    if (!usuario) return;

    this.loading = true;
    this.error = '';

    this.compraService.listarMisCompras(usuario.cedula).subscribe({
      next: (res) => {
        this.compras = res.data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error al cargar las compras.';
        this.loading = false;
      }
    });
  }

  getEstadoClass(estado: string): string {
    const map: Record<string, string> = {
      'PENDIENTE_VALIDACION_SNS': 'estado-pendiente-sns',
      'EN_PROCESO_SNS': 'estado-proceso-sns',
      'APROBADO_SNS': 'estado-aprobado-sns',
      'RECHAZADO_SNS': 'estado-rechazado-sns',
      'PENDIENTE_PAGO': 'estado-pendiente-pago',
      'PAGADO': 'estado-pagado',
      'COMPLETADO': 'estado-completado',
      'CANCELADO': 'estado-cancelado'
    };
    return map[estado] || '';
  }
}

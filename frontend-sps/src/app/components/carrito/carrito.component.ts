import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PlanService } from '../../services/plan.service';
import { CompraService } from '../../services/compra.service';
import { AuthService } from '../../services/auth.service';
import { PlanSalud } from '../../interfaces/PlanSalud.interface';

@Component({
  selector: 'app-carrito',
  templateUrl: './carrito.component.html',
  styleUrls: ['./carrito.component.css']
})
export class CarritoComponent implements OnInit {
  planes: PlanSalud[] = [];
  precioTotal: number = 0;
  loading: boolean = false;
  errorMessage: string = '';

  constructor(
    private planService: PlanService,
    private compraService: CompraService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.planService.obtenerCarrito().subscribe(planes => {
      this.planes = planes;
      this.precioTotal = planes.reduce((sum, p) => sum + p.precio, 0);
    });
  }

  eliminar(codigoPlan: string): void {
    this.planService.removerDelCarrito(codigoPlan);
  }

  confirmarCompra(): void {
    const usuario = this.authService.getUsuarioActual();
    if (!usuario) return;

    this.loading = true;
    this.errorMessage = '';

    this.compraService.crearCompra({
      cedulaCliente: usuario.cedula,
      codigosPlanes: this.planes.map(p => p.codigoPlan)
    }).subscribe({
      next: (res) => {
        this.planService.limpiarCarrito();
        this.router.navigate(['/confirmacion'], {
          queryParams: { numeroCompra: res.data.numeroCompra }
        });
      },
      error: () => {
        this.errorMessage = 'Error al procesar la compra. Intente nuevamente.';
        this.loading = false;
      }
    });
  }

  seguirComprando(): void {
    this.router.navigate(['/catalogo']);
  }
}

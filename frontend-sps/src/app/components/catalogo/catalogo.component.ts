import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PlanService } from '../../services/plan.service';
import { PlanSalud } from '../../interfaces/PlanSalud.interface';

@Component({
  selector: 'app-catalogo',
  templateUrl: './catalogo.component.html',
  styleUrls: ['./catalogo.component.css']
})
export class CatalogoComponent implements OnInit {
  planes: PlanSalud[] = [];
  loading: boolean = true;
  error: string = '';
  cantidadCarrito: number = 0;

  constructor(
    private planService: PlanService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.planService.listarPlanes().subscribe({
      next: (res) => {
        this.planes = res.data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error al cargar los planes.';
        this.loading = false;
      }
    });

    this.planService.obtenerCarrito().subscribe(planes => {
      this.cantidadCarrito = planes.length;
    });
  }

  agregarAlCarrito(plan: PlanSalud): void {
    this.planService.agregarAlCarrito(plan);
  }

  irAlCarrito(): void {
    this.router.navigate(['/carrito']);
  }
}

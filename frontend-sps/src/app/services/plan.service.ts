import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../interfaces/ApiResponse.interface';
import { PlanSalud } from '../interfaces/PlanSalud.interface';

@Injectable({ providedIn: 'root' })
export class PlanService {
  private carritoSubject = new BehaviorSubject<PlanSalud[]>([]);
  carrito$ = this.carritoSubject.asObservable();

  constructor(private http: HttpClient) {}

  listarPlanes(): Observable<ApiResponse<PlanSalud[]>> {
    return this.http.get<ApiResponse<PlanSalud[]>>(`${environment.apiBaseUrl}/compra/planes`);
  }

  obtenerPlan(codigo: string): Observable<ApiResponse<PlanSalud>> {
    return this.http.get<ApiResponse<PlanSalud>>(`${environment.apiBaseUrl}/compra/planes/${codigo}`);
  }

  agregarAlCarrito(plan: PlanSalud): void {
    const actual = this.carritoSubject.value;
    if (!actual.find(p => p.codigoPlan === plan.codigoPlan)) {
      this.carritoSubject.next([...actual, plan]);
    }
  }

  removerDelCarrito(codigoPlan: string): void {
    const actual = this.carritoSubject.value.filter(p => p.codigoPlan !== codigoPlan);
    this.carritoSubject.next(actual);
  }

  obtenerCarrito(): Observable<PlanSalud[]> {
    return this.carrito$;
  }

  limpiarCarrito(): void {
    this.carritoSubject.next([]);
  }

  obtenerCantidadCarrito(): Observable<number> {
    return new Observable<number>(observer => {
      this.carrito$.subscribe(planes => observer.next(planes.length));
    });
  }
}

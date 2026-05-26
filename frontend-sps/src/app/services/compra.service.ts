import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../interfaces/ApiResponse.interface';
import { Compra, CompraRequest, CompraResumen } from '../interfaces/Compra.interface';

@Injectable({ providedIn: 'root' })
export class CompraService {
  constructor(private http: HttpClient) {}

  crearCompra(request: CompraRequest): Observable<ApiResponse<CompraResumen>> {
    return this.http.post<ApiResponse<CompraResumen>>(`${environment.apiBaseUrl}/compra`, request);
  }

  obtenerCompra(numero: number): Observable<ApiResponse<Compra>> {
    return this.http.get<ApiResponse<Compra>>(`${environment.apiBaseUrl}/compra/${numero}`);
  }

  listarMisCompras(cedula: string): Observable<ApiResponse<Compra[]>> {
    return this.http.get<ApiResponse<Compra[]>>(`${environment.apiBaseUrl}/compra/cliente/${cedula}`);
  }
}

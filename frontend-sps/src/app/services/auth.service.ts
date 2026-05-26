import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../interfaces/ApiResponse.interface';
import { LoginResponse, Usuario } from '../interfaces/Usuario.interface';
import { StorageService } from '../core/services/storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(
    private http: HttpClient,
    private storageService: StorageService,
    private router: Router
  ) {}

  login(cedula: string, contrasena: string): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(
      `${environment.apiBaseUrl}/auth/login`,
      { cedula, contrasena }
    );
  }

  logout(): void {
    this.storageService.clear();
    this.router.navigate(['/login']);
  }

  getUsuarioActual(): Usuario | null {
    const data = this.storageService.getUsuario();
    if (!data) return null;
    return JSON.parse(data) as Usuario;
  }

  getToken(): string | null {
    return this.storageService.getToken();
  }

  isLoggedIn(): boolean {
    return this.storageService.getToken() !== null;
  }
}

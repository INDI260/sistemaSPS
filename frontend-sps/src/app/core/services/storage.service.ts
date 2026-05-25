import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class StorageService {
  private readonly TOKEN_KEY = 'sps_token';
  private readonly USUARIO_KEY = 'sps_usuario';

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  removeToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  getUsuario(): string | null {
    return localStorage.getItem(this.USUARIO_KEY);
  }

  setUsuario(usuario: string): void {
    localStorage.setItem(this.USUARIO_KEY, usuario);
  }

  removeUsuario(): void {
    localStorage.removeItem(this.USUARIO_KEY);
  }

  clear(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USUARIO_KEY);
  }
}

export interface Usuario {
  cedula: string;
  nombre: string;
  correo: string;
  rol: string;
}

export interface LoginResponse {
  token: string;
  cedula: string;
  nombre: string;
  rol: string;
}

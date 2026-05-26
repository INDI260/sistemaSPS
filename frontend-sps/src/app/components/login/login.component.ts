import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { StorageService } from '../../core/services/storage.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage: string = '';
  loading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private storageService: StorageService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      cedula: ['', Validators.required],
      contrasena: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.loading = true;
    this.errorMessage = '';

    const { cedula, contrasena } = this.loginForm.value;

    this.authService.login(cedula, contrasena).subscribe({
      next: (res) => {
        this.storageService.setToken(res.data.token);
        this.storageService.setUsuario(JSON.stringify({
          cedula: res.data.cedula,
          nombre: res.data.nombre,
          rol: res.data.rol
        }));
        this.router.navigate(['/catalogo']);
      },
      error: () => {
        this.errorMessage = 'Cédula o contraseña incorrectos.';
        this.loading = false;
      }
    });
  }
}

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './components/login/login.component';
import { CatalogoComponent } from './components/catalogo/catalogo.component';
import { CarritoComponent } from './components/carrito/carrito.component';
import { ConfirmacionComponent } from './components/confirmacion/confirmacion.component';
import { EstadoCompraComponent } from './components/estado-compra/estado-compra.component';
import { AuthGuard } from './core/guards/auth.guard';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'catalogo', component: CatalogoComponent, canActivate: [AuthGuard] },
  { path: 'carrito', component: CarritoComponent, canActivate: [AuthGuard] },
  { path: 'confirmacion', component: ConfirmacionComponent, canActivate: [AuthGuard] },
  { path: 'mis-compras', component: EstadoCompraComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

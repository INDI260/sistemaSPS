# Módulo 1 — Frontend SPS (Angular) + NGINX

**Artefactos a producir:** `FrontendSPASPS.zip` · `nginx.conf`  
**Carpetas del repositorio:** `frontend-sps/` · `nginx/`  
**Responsable de:** Todo lo que ve el cliente del sistema SPS en su navegador, más el servidor web que enruta el tráfico entrante.

---

## 1. Responsabilidades

| Componente        | Qué hace                                                       |
|-------------------|----------------------------------------------------------------|
| PantallaSPASPS    | Vistas Angular: login, búsqueda de planes, carrito, estado compra |
| CESPASPS          | Lógica de presentación Angular (componentes, guards, interceptores) |
| ProxySPASPS       | Servicios Angular que llaman a los backends vía HTTP           |
| NGINX             | Sirve Angular estático, hace proxy a todos los backends, balancea ms-compra |

## 2. Fuera del Alcance de Este Módulo

- **No** implementar lógica de negocio en ningún componente Angular.
- **No** conectarse directamente a ninguna base de datos.
- **No** implementar la interfaz de SaludPay (eso lo hace el Módulo 3).
- **No** implementar ningún backend Spring Boot o .NET.
- **No** enviar correos ni mensajes AMQP.
- Toda validación de negocio va al backend; Angular solo valida formato de formularios.

---

## 3. Estructura de Carpetas a Crear

```
frontend-sps/
├── package.json
├── angular.json
├── tsconfig.json
├── src/
│   ├── index.html
│   ├── main.ts
│   ├── styles.css
│   └── app/
│       ├── app.module.ts
│       ├── app-routing.module.ts
│       ├── app.component.ts/html/css
│       │
│       ├── core/
│       │   ├── guards/
│       │   │   └── auth.guard.ts
│       │   ├── interceptors/
│       │   │   └── jwt.interceptor.ts
│       │   └── services/
│       │       └── storage.service.ts
│       │
│       ├── interfaces/
│       │   ├── Usuario.interface.ts
│       │   ├── PlanSalud.interface.ts
│       │   ├── ServicioMedico.interface.ts
│       │   ├── Compra.interface.ts
│       │   └── ApiResponse.interface.ts
│       │
│       ├── services/
│       │   ├── auth.service.ts
│       │   ├── plan.service.ts
│       │   └── compra.service.ts
│       │
│       └── components/
│           ├── login/
│           │   ├── login.component.ts
│           │   ├── login.component.html
│           │   └── login.component.css
│           ├── catalogo/
│           │   ├── catalogo.component.ts
│           │   ├── catalogo.component.html
│           │   └── catalogo.component.css
│           ├── carrito/
│           │   ├── carrito.component.ts
│           │   ├── carrito.component.html
│           │   └── carrito.component.css
│           ├── confirmacion/
│           │   ├── confirmacion.component.ts
│           │   ├── confirmacion.component.html
│           │   └── confirmacion.component.css
│           └── estado-compra/
│               ├── estado-compra.component.ts
│               ├── estado-compra.component.html
│               └── estado-compra.component.css

nginx/
├── nginx.conf
└── ssl/
    ├── cert.pem      (certificado autofirmado para dev)
    └── key.pem
```

---

## 4. Rutas Angular

| Ruta Angular         | Componente          | Guard     | Descripción                       |
|----------------------|---------------------|-----------|-----------------------------------|
| `/login`             | LoginComponent      | No        | Autenticación del cliente         |
| `/catalogo`          | CatalogoComponent   | AuthGuard | Búsqueda y listado de planes      |
| `/carrito`           | CarritoComponent    | AuthGuard | Carrito y envío de compra         |
| `/confirmacion`      | ConfirmacionComponent | AuthGuard | Mensaje de aceptación (202)     |
| `/mis-compras`       | EstadoCompraComponent | AuthGuard | Historial de compras del cliente |
| `**`                 | Redirect a `/login` | No        | Ruta por defecto                  |

---

## 5. Servicios Angular y Endpoints que Consumen

### auth.service.ts
```
POST  /api/auth/login    → { cedula, contrasena }
                         ← { token, cedula, nombre, rol }
```
- Guarda el JWT en `localStorage` con key `sps_token`
- Guarda los datos del usuario en `localStorage` con key `sps_usuario`

### plan.service.ts
```
GET  /api/compra/planes           → Lista de PlanSaludDTO[]
GET  /api/compra/planes/{codigo}  → PlanSaludDTO
```

### compra.service.ts
```
POST /api/compra                      → { cedulaCliente, codigosPlanes[] }
                                      ← { numeroCompra, estado }
GET  /api/compra/{numero}             → CompraDTO con estado actual
GET  /api/compra/cliente/{cedula}     → CompraDTO[]
```

---

## 6. Interfaces TypeScript

```typescript
// ApiResponse.interface.ts
export interface ApiResponse<T> {
  status: 'OK' | 'ERROR';
  data: T;
  message: string;
  timestamp: string;
}

// PlanSalud.interface.ts
export interface PlanSalud {
  codigoPlan: string;
  nombrePlan: string;
  descripcion: string;
  precio: number;
  serviciosMedicos: ServicioMedico[];
}

// ServicioMedico.interface.ts
export interface ServicioMedico {
  codigoServicio: string;
  nombre: string;
  tipo: 'CONSULTA' | 'EXAMEN' | 'HOSPITALIZACION';
  precio: number;
}

// Compra.interface.ts
export interface Compra {
  numeroCompra: number;
  cedulaCliente: string;
  nombreCliente: string;
  precioTotal: number;
  estado: string;
  fechaCreacion: string;
  planes: PlanCompra[];
}

export interface PlanCompra {
  codigoPlan: string;
  nombrePlan: string;
  precio: number;
  estadoSns: string;
}

// Usuario.interface.ts
export interface Usuario {
  cedula: string;
  nombre: string;
  correo: string;
  rol: string;
}
```

---

## 7. JWT Interceptor

El interceptor `jwt.interceptor.ts` debe:
1. Leer el token de `localStorage` con key `sps_token`
2. Si existe, añadir el header `Authorization: Bearer <token>` a toda petición HTTP
3. Si la respuesta es 401, limpiar localStorage y redirigir a `/login`

---

## 8. Auth Guard

`auth.guard.ts` debe:
1. Leer el token de localStorage
2. Si no existe → redirigir a `/login`, devolver false
3. Si existe → devolver true (la validación real la hace el backend)

---

## 9. Flujo de Pantallas

```
[login] 
  → usuario ingresa cedula + contraseña
  → llama POST /api/auth/login
  → guarda JWT y datos de usuario
  → navega a /catalogo

[catalogo]
  → carga GET /api/compra/planes
  → muestra tarjetas de planes con sus servicios y precio
  → botón "Agregar al carrito" añade plan a estado local (array en servicio)
  → botón "Ver Carrito" navega a /carrito

[carrito]
  → muestra planes seleccionados y precio total (suma calculada en frontend)
  → botón "Confirmar Compra" llama POST /api/compra
  → en éxito (202) navega a /confirmacion con el número de compra

[confirmacion]
  → muestra mensaje: "Su solicitud fue recibida. Recibirá un correo electrónico cuando 
    sea posible continuar con la compra."
  → muestra número de compra
  → enlace a /mis-compras

[mis-compras]
  → llama GET /api/compra/cliente/{cedula}
  → muestra tabla con número, fecha, estado, valor
  → botón "Actualizar" recarga la lista
```

---

## 10. Configuración NGINX

El archivo `nginx/nginx.conf` debe configurar:

### Upstream para balanceo de ms-compra:
```nginx
upstream compra_backend {
    server localhost:8082;
    # Agregar más instancias cuando se escale:
    # server localhost:8086;
}
```

### Server block principal (HTTPS):
```nginx
server {
    listen 443 ssl;
    server_name localhost;

    ssl_certificate     /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;

    # Sirve la SPA Angular
    location / {
        root /var/www/frontend-sps/dist/frontend-sps/browser;
        try_files $uri $uri/ /index.html;
    }

    # Proxy a ms-auth
    location /api/auth/ {
        proxy_pass http://localhost:8081/ws/auth/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Proxy balanceado a ms-compra
    location /api/compra/ {
        proxy_pass http://compra_backend/ws/compra/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Proxy a FrontendSaludPay (.NET — construido por Módulo 3)
    location /saludpay/ {
        proxy_pass http://localhost:5001/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# Redirect HTTP → HTTPS
server {
    listen 80;
    return 301 https://$host$request_uri;
}
```

---

## 11. Variables de Entorno Angular (environment)

```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiBaseUrl: '/api'
};

// src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiBaseUrl: '/api'
};
```

Todas las llamadas HTTP usan `environment.apiBaseUrl` como prefijo.

---

## 12. Dependencias npm Requeridas

```json
{
  "dependencies": {
    "@angular/animations": "^17.0.0",
    "@angular/common": "^17.0.0",
    "@angular/compiler": "^17.0.0",
    "@angular/core": "^17.0.0",
    "@angular/forms": "^17.0.0",
    "@angular/platform-browser": "^17.0.0",
    "@angular/platform-browser-dynamic": "^17.0.0",
    "@angular/router": "^17.0.0",
    "rxjs": "~7.8.0",
    "tslib": "^2.3.0",
    "zone.js": "~0.14.2"
  }
}
```

No se requieren librerías adicionales de UI; usar CSS puro o el mínimo necesario.

---

## 13. Integración con Otros Módulos

| Módulo proveedor | Qué provee                               | Punto de contacto    |
|------------------|------------------------------------------|----------------------|
| Módulo 2         | API de planes y compras                  | puerto 8082          |
| Módulo 3         | API de autenticación                     | puerto 8081          |
| Módulo 3         | Interfaz web de SaludPay (ya construida) | puerto 5001          |

Este módulo **no llama** directamente a SNS, SHC ni SAM.

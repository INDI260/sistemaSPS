# PROMPT MAESTRO — Módulo 1: Frontend SPS (Angular) + NGINX

---

Eres un agente de codificación experto en Angular 17 y configuración de NGINX. Tu misión es implementar completamente el **Módulo 1** del Sistema de Compra de Planes de Salud en Línea (SPS): la aplicación SPA Angular que usan los clientes para buscar planes, armar su carrito y enviar solicitudes de compra, más la configuración NGINX que sirve todo el sistema como proxy inverso con balanceo de carga.

Lee el archivo `docs/CONVENCIONES_ENTIDADES.md` antes de comenzar. Respeta estrictamente todas las convenciones de nomenclatura, puertos, interfaces y contratos API definidos allí.

---

## Contexto del Sistema

SPS es un sistema donde los clientes compran planes de salud en línea. Un plan agrupa servicios médicos (consultas, exámenes, hospitalización). El cliente selecciona planes, los agrega a un carrito y envía la solicitud. El backend procesa la compra de forma asíncrona y notifica al cliente por correo cuando puede continuar.

El sistema tiene cuatro subsistemas backend construidos por otros equipos:
- **ms-auth** (puerto 8081): autenticación JWT
- **ms-compra** (puerto 8082): gestión de planes y compras
- **ms-saludpay-frontend** (puerto 5001): interfaz web de SaludPay (.NET, ya construida por Módulo 3)
- **ms-saludpay** (puerto 5000): backend de pagos (.NET)

Tu módulo **no implementa ningún backend**. Solo consume las APIs ya definidas.

---

## Tu Responsabilidad

Debes crear dos carpetas en la raíz del repositorio:

### 1. `frontend-sps/` — Aplicación Angular 17

Una SPA (Single Page Application) con los siguientes componentes:

**Tecnología:** Angular 17, TypeScript, CSS puro (sin librerías UI externas)  
**Puerto de desarrollo:** 4200

#### Componentes a implementar:

**`login.component`**
- Formulario con dos campos: Cédula (texto) y Contraseña (password)
- Al enviar: llama `POST /api/auth/login` con `{ cedula, contrasena }`
- Si respuesta OK: guarda `token` en `localStorage['sps_token']` y el objeto usuario en `localStorage['sps_usuario']`, navega a `/catalogo`
- Si error: muestra mensaje de error debajo del formulario
- La ruta `/login` no requiere guard

**`catalogo.component`**
- Al iniciar: llama `GET /api/compra/planes` para cargar la lista de planes
- Muestra cada plan en una tarjeta con: nombre, descripción, precio total, lista de servicios (nombre + tipo + precio)
- Botón "Agregar al carrito" por cada plan
- Indicador del número de planes en el carrito (badge)
- Botón "Ir al carrito" que navega a `/carrito`
- Requiere AuthGuard

**`carrito.component`**
- Muestra los planes seleccionados con nombre y precio
- Muestra el precio total calculado (suma de precios de planes)
- Botón "Eliminar" por cada plan
- Botón "Confirmar Compra" que llama `POST /api/compra` con `{ cedulaCliente: <cedula del JWT>, codigosPlanes: [<codigos>] }`
- En respuesta 202: navega a `/confirmacion` pasando el `numeroCompra` como query param
- Requiere AuthGuard

**`confirmacion.component`**
- Muestra el mensaje: *"Su solicitud de compra N° [numeroCompra] fue recibida exitosamente. Recibirá un correo electrónico cuando sea posible continuar con la compra."*
- Enlace a `/mis-compras`
- Requiere AuthGuard

**`estado-compra.component`**
- Al iniciar: llama `GET /api/compra/cliente/{cedula}` donde cedula viene de `localStorage['sps_usuario']`
- Muestra tabla con columnas: N° Compra, Fecha, Planes, Precio Total, Estado
- El estado se muestra con colores: PENDIENTE_VALIDACION_SNS (gris), EN_PROCESO_SNS (amarillo), APROBADO_SNS (azul), PENDIENTE_PAGO (naranja), PAGADO (verde claro), COMPLETADO (verde), RECHAZADO_SNS (rojo), CANCELADO (rojo oscuro)
- Botón "Actualizar" que recarga la lista
- Requiere AuthGuard

#### Servicios Angular:

**`auth.service.ts`**
```typescript
login(cedula: string, contrasena: string): Observable<ApiResponse<LoginResponse>>
logout(): void  // limpia localStorage y navega a /login
getUsuarioActual(): Usuario | null
getToken(): string | null
isLoggedIn(): boolean
```

**`plan.service.ts`**
```typescript
listarPlanes(): Observable<ApiResponse<PlanSalud[]>>
obtenerPlan(codigo: string): Observable<ApiResponse<PlanSalud>>
// Estado del carrito (en memoria, BehaviorSubject):
agregarAlCarrito(plan: PlanSalud): void
removerDelCarrito(codigoPlan: string): void
obtenerCarrito(): Observable<PlanSalud[]>
limpiarCarrito(): void
```

**`compra.service.ts`**
```typescript
crearCompra(request: CompraRequest): Observable<ApiResponse<CompraResumen>>
obtenerCompra(numero: number): Observable<ApiResponse<Compra>>
listarMisCompras(cedula: string): Observable<ApiResponse<Compra[]>>
```

#### Interfaces TypeScript (en `src/app/interfaces/`):

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

export interface CompraRequest {
  cedulaCliente: string;
  codigosPlanes: string[];
}

export interface CompraResumen {
  numeroCompra: number;
  estado: string;
}

// Usuario.interface.ts
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
```

#### Guards e Interceptores:

**`auth.guard.ts`**: Si no hay token en localStorage → redirige a `/login` y devuelve false. Si hay token → devuelve true.

**`jwt.interceptor.ts`**: Lee token de `localStorage['sps_token']`. Si existe, añade header `Authorization: Bearer <token>` a toda petición. Si respuesta es 401 → llama `auth.service.logout()`.

#### Rutas (`app-routing.module.ts`):
```typescript
{ path: 'login', component: LoginComponent },
{ path: 'catalogo', component: CatalogoComponent, canActivate: [AuthGuard] },
{ path: 'carrito', component: CarritoComponent, canActivate: [AuthGuard] },
{ path: 'confirmacion', component: ConfirmacionComponent, canActivate: [AuthGuard] },
{ path: 'mis-compras', component: EstadoCompraComponent, canActivate: [AuthGuard] },
{ path: '', redirectTo: '/login', pathMatch: 'full' },
{ path: '**', redirectTo: '/login' }
```

#### Environments:
```typescript
// src/environments/environment.ts
export const environment = { production: false, apiBaseUrl: '/api' };
// src/environments/environment.prod.ts
export const environment = { production: true, apiBaseUrl: '/api' };
```

Todos los servicios usan `environment.apiBaseUrl` como prefijo de URL.

---

### 2. `nginx/` — Configuración NGINX

**Archivo principal:** `nginx/nginx.conf`

El archivo de configuración debe:

1. Definir un `upstream` para balancear el tráfico hacia ms-compra:
```nginx
upstream compra_backend {
    server 127.0.0.1:8082;
    # Descomentar para segunda instancia (escalabilidad):
    # server 127.0.0.1:8086;
}
```

2. Redirigir HTTP a HTTPS:
```nginx
server {
    listen 80;
    server_name _;
    return 301 https://$host$request_uri;
}
```

3. Servidor HTTPS principal:
```nginx
server {
    listen 443 ssl;
    server_name localhost;
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;

    # SPA Angular
    location / {
        root /var/www/frontend-sps;
        try_files $uri $uri/ /index.html;
        index index.html;
    }

    # API de autenticación → ms-auth
    location /api/auth/ {
        proxy_pass http://127.0.0.1:8081/ws/auth/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # API de compra → ms-compra (load balanced)
    location /api/compra/ {
        proxy_pass http://compra_backend/ws/compra/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Frontend SaludPay (.NET Razor Pages — Módulo 3)
    location /saludpay/ {
        proxy_pass http://127.0.0.1:5001/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

4. Incluir también `nginx/ssl/` con un script de generación de certificado autofirmado para desarrollo:
```
nginx/
├── nginx.conf
├── ssl/
│   └── generar-cert.sh     # openssl req -x509 -nodes -days 365 -newkey rsa:2048 ...
└── README.md               # instrucciones de despliegue
```

---

## Instrucciones de Implementación (en este orden)

1. **Inicializar proyecto Angular:**
   ```
   cd frontend-sps
   ng new frontend-sps --routing --style=css --standalone=false
   ```

2. **Crear las interfaces** en `src/app/interfaces/`

3. **Crear los servicios** en `src/app/services/` (auth, plan, compra)

4. **Crear los guards e interceptores** en `src/app/core/`

5. **Registrar el interceptor** en `app.module.ts` como `HTTP_INTERCEPTORS`

6. **Crear los componentes** en `src/app/components/`

7. **Configurar las rutas** en `app-routing.module.ts`

8. **Configurar environments**

9. **Crear `nginx/nginx.conf`** con el contenido especificado

10. **Crear el script de certificado** `nginx/ssl/generar-cert.sh`

---

## Restricciones Críticas

- **Prohibido** poner lógica de negocio en los componentes Angular. Los componentes solo formatean y muestran datos; los servicios hacen las llamadas HTTP.
- **Prohibido** acceder directamente a los puertos de los backends (8081, 8082). Todas las llamadas van a `/api/auth/` y `/api/compra/` que NGINX redirige.
- **No usar** librerías de UI como Angular Material, Bootstrap, PrimeNG u otras. Solo CSS puro.
- **No implementar** el módulo de SaludPay (eso lo hace el Módulo 3).
- El `jwt.interceptor` **no debe** decodificar ni validar el JWT en el cliente; solo lo adjunta al header.

---

## Verificación de Éxito

El módulo está completo cuando:
- `ng serve` levanta la app en localhost:4200 sin errores de compilación
- La pantalla de login carga correctamente
- El guard redirige a `/login` si no hay token
- El interceptor añade el header Authorization en todas las peticiones
- `nginx -t -c nginx/nginx.conf` no reporta errores de sintaxis
- Todos los archivos siguen la convención de nombres definida en `docs/CONVENCIONES_ENTIDADES.md`

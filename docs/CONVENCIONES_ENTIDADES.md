# Convenciones de Entidades y Estándares del Proyecto SPS

> **Lectura obligatoria para todos los módulos antes de escribir cualquier línea de código.**  
> Este archivo es la fuente de verdad para nombres, contratos y formatos compartidos.

---

## 1. Estructura de Directorios del Proyecto

```
sistemaSPS/
├── docs/                          ← documentación y prompts
├── frontend-sps/                  ← Angular SPA (Módulo 1)
├── nginx/                         ← configuración NGINX (Módulo 1)
├── ms-auth/                       ← Spring Boot WAR (Módulo 3)
├── ms-compra/                     ← Spring Boot WAR (Módulo 2)
├── ms-sns/                        ← Spring Boot WAR mock SNS (Módulo 2)
├── ms-saludpay/                   ← ASP.NET Core Web API (Módulo 3)
├── ms-saludpay-frontend/          ← ASP.NET Core Razor Pages (Módulo 3)
├── ms-shc/                        ← Spring Boot WAR (Módulo 4)
└── ms-sam/                        ← Spring Boot WAR (Módulo 4)
```

---

## 2. Registro de Puertos

| Servicio                   | Puerto | Protocolo | Módulo |
|----------------------------|--------|-----------|--------|
| NGINX                      | 443    | HTTPS     | 1      |
| NGINX (redirect)           | 80     | HTTP      | 1      |
| frontend-sps (dev)         | 4200   | HTTP      | 1      |
| ms-auth                    | 8081   | HTTP      | 3      |
| ms-compra                  | 8082   | HTTP      | 2      |
| ms-sns                     | 8083   | HTTP      | 2      |
| ms-saludpay (API backend)  | 5000   | HTTP      | 3      |
| ms-saludpay-frontend       | 5001   | HTTP      | 3      |
| ms-shc                     | 8084   | HTTP      | 4      |
| ms-sam                     | 8085   | HTTP      | 4      |
| RabbitMQ AMQP              | 5672   | AMQP      | 4      |
| RabbitMQ Management UI     | 15672  | HTTP      | 4      |

---

## 3. Stack Tecnológico por Módulo

| Módulo | Componente             | Tecnología              | Artefacto          |
|--------|------------------------|-------------------------|--------------------|
| 1      | SPA de compra SPS      | Angular 17 (TypeScript) | FrontendSPASPS.zip |
| 1      | Servidor web/proxy     | NGINX 1.25              | nginx.conf         |
| 2      | Servicio de compra     | Spring Boot 3 / WAR     | MSCompraSPS.war    |
| 2      | Mock SNS               | Spring Boot 3 / WAR     | SNS.war            |
| 3      | Servicio de auth       | Spring Boot 3 / WAR     | MSAuth.war         |
| 3      | SaludPay backend API   | ASP.NET Core 8 Web API  | MSSaludPay.dll     |
| 3      | SaludPay frontend      | ASP.NET Core 8 Razor    | FrontendSaludPay.dll |
| 4      | Historias Clínicas     | Spring Boot 3 / WAR     | MSSHC.war          |
| 4      | Agenda Médica          | Spring Boot 3 / WAR     | MSSAM.war          |
| 4      | Broker de mensajes     | RabbitMQ 3.x            | MOMSHS / MOMSAM    |

### Versiones Java / .NET:
- Java: 21 LTS
- Spring Boot: 3.2.x
- Maven: 3.9.x
- .NET: 8.0 LTS
- Node.js: 20 LTS
- Angular CLI: 17.x

---

## 4. Convenciones de Nomenclatura

### 4.1 Java (Spring Boot)

| Elemento        | Patrón                          | Ejemplo                        |
|-----------------|---------------------------------|--------------------------------|
| Package base    | `com.sps.{modulo}`              | `com.sps.compra`               |
| Controlador     | `WS{Nombre}Controller`          | `WSCompraController`           |
| Servicio        | `Service{Nombre}`               | `ServiceCompra`                |
| Repositorio     | `Repo{Nombre}`                  | `RepoCompra`                   |
| Entidad JPA     | Nombre de la entidad            | `Compra`, `PlanSalud`          |
| DTO             | `{Nombre}DTO`                   | `CompraDTO`, `PlanSaludDTO`    |
| Proxy HTTP      | `Proxy{Sistema}`                | `ProxySNS`, `ProxySaludPay`    |
| Timer class     | `{Nombre}TimerTask`             | `ValidacionSNSTimerTask`       |
| Listener AMQP   | `Listener{Nombre}`              | `ListenerSHC`, `ListenerSAM`   |
| Integrador AMQP | `Integrador{Sistema}`           | `IntegradorSHC`, `IntegradorSAM` |
| Mapping URL WS  | `/ws/{recurso}`                 | `/ws/compra`, `/ws/auth`       |

### 4.2 .NET (ASP.NET Core)

| Elemento        | Patrón                          | Ejemplo                         |
|-----------------|---------------------------------|---------------------------------|
| Namespace base  | `SPS.{Modulo}`                  | `SPS.SaludPay`, `SPS.Auth`      |
| Controlador     | `WS{Nombre}Controller`          | `WSSaludPayController`          |
| Servicio        | `I{Nombre}Service` + impl       | `ISaludPayService`              |
| Repositorio     | `I{Nombre}Repository` + impl    | `IPagoSPSRepository`            |
| Entidad EF Core | Nombre de la entidad            | `PagoSPS`, `UsuarioSaludPay`    |
| DTO             | `{Nombre}Dto`                   | `PagoSPSDto`                    |
| Mapping URL     | `/ws/{recurso}`                 | `/ws/saludpay`                  |
| DbContext       | `{Modulo}DbContext`             | `SaludPayDbContext`             |

### 4.3 Angular (TypeScript)

| Elemento        | Patrón                          | Ejemplo                         |
|-----------------|---------------------------------|---------------------------------|
| Componente      | `{nombre}.component.ts`         | `carrito.component.ts`          |
| Servicio        | `{nombre}.service.ts`           | `compra.service.ts`             |
| Interfaz DTO    | `{Nombre}.interface.ts`         | `Compra.interface.ts`           |
| Guard           | `{nombre}.guard.ts`             | `auth.guard.ts`                 |
| Interceptor     | `{nombre}.interceptor.ts`       | `jwt.interceptor.ts`            |
| Módulo ruta     | `app-routing.module.ts`         | —                               |

---

## 5. Formato de Respuesta REST Estándar

**Todos los endpoints REST de todos los módulos devuelven este wrapper:**

```json
{
  "status": "OK",
  "data": { },
  "message": "Descripción legible",
  "timestamp": "2026-05-25T10:30:00"
}
```

- `status`: `"OK"` | `"ERROR"`
- `data`: objeto o array con el payload; `null` en caso de error
- `message`: string legible por humanos
- `timestamp`: ISO-8601 UTC

**HTTP Status codes:**
- 200 OK → operación exitosa
- 201 Created → recurso creado
- 202 Accepted → solicitud aceptada para procesamiento asíncrono
- 400 Bad Request → validación fallida
- 401 Unauthorized → sin token o token inválido
- 403 Forbidden → sin permisos
- 404 Not Found → recurso no encontrado
- 500 Internal Server Error → error inesperado

---

## 6. Especificación JWT

- **Algoritmo:** HS256  
- **Header HTTP:** `Authorization: Bearer <token>`  
- **Variable de entorno del secret:** `JWT_SECRET` (igual en todos los módulos Spring Boot)  
- **Expiración:** 7200 segundos (2 horas)  
- **Claims del payload:**

```json
{
  "sub": "1234567890",
  "cedula": "1234567890",
  "nombre": "Juan Pérez",
  "correo": "juan@email.com",
  "rol": "CLIENTE",
  "iat": 1716631800,
  "exp": 1716638999
}
```

- **Validación en Spring Boot:** usar `io.jsonwebtoken:jjwt-api` (JJWT 0.12.x)  
- **Validación en .NET:** usar `Microsoft.AspNetCore.Authentication.JwtBearer`  
- **Ruta de login excluida de validación JWT:** `POST /ws/auth/login`, `POST /ws/auth/registro`  
- **Ruta de SaludPay exenta de JWT SPS** (tiene su propio login separado)

---

## 7. Seguridad de Contraseñas

- **Algoritmo:** BCrypt con strength 12
- **Java:** `org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder`
- **.NET:** `BCrypt.Net-Next` NuGet package

---

## 8. Enumeraciones Compartidas

Estas enumeraciones deben tener exactamente estos valores en todos los módulos que las usen:

### EstadoCompra
```
PENDIENTE_VALIDACION_SNS
EN_PROCESO_SNS
APROBADO_SNS
RECHAZADO_SNS
PENDIENTE_PAGO
PAGADO
COMPLETADO
CANCELADO
```

### EstadoSNS (por plan dentro de una compra)
```
PENDIENTE
EN_PROCESO
APROBADO
RECHAZADO
```

### TipoServicioMedico
```
CONSULTA
EXAMEN
HOSPITALIZACION
```

### RolUsuario
```
CLIENTE
ADMIN
```

### EstadoPago (SaludPay)
```
PENDIENTE
PAGADO
```

### EstadoCita (SAM)
```
PENDIENTE
CONFIRMADA
CANCELADA
```

---

## 9. Entidades por Módulo

### 9.1 Módulo 3 — `db_auth` (ms-auth)

**Tabla: `usuario`**
```
cedula            VARCHAR(20)   PK
nombre            VARCHAR(100)  NOT NULL
apellido          VARCHAR(100)  NOT NULL
correo            VARCHAR(150)  UNIQUE NOT NULL
contrasena        VARCHAR(255)  NOT NULL  (BCrypt)
rol               VARCHAR(20)   NOT NULL  DEFAULT 'CLIENTE'
activo            BOOLEAN       NOT NULL  DEFAULT true
fecha_creacion    DATETIME      NOT NULL
```

### 9.2 Módulo 2 — `db_compra` (ms-compra)

**Tabla: `plan_salud`**
```
codigo_plan       VARCHAR(20)   PK
nombre_plan       VARCHAR(100)  NOT NULL
descripcion       TEXT
precio            DECIMAL(12,2) NOT NULL
activo            BOOLEAN       NOT NULL  DEFAULT true
```

**Tabla: `servicio_medico`**
```
codigo_servicio   VARCHAR(20)   PK
nombre            VARCHAR(100)  NOT NULL
descripcion       TEXT
tipo              VARCHAR(30)   NOT NULL  (TipoServicioMedico)
precio            DECIMAL(12,2) NOT NULL
codigo_plan       VARCHAR(20)   FK → plan_salud
```

**Tabla: `compra`**
```
numero_compra            BIGINT        PK AUTO_INCREMENT
cedula_cliente           VARCHAR(20)   NOT NULL
nombre_cliente           VARCHAR(200)  NOT NULL
correo_cliente           VARCHAR(150)  NOT NULL
precio_total             DECIMAL(12,2) NOT NULL
estado                   VARCHAR(40)   NOT NULL  (EstadoCompra)
fecha_creacion           DATETIME      NOT NULL
fecha_ultima_actualizacion DATETIME    NOT NULL
```

**Tabla: `plan_compra`**
```
id                BIGINT        PK AUTO_INCREMENT
numero_compra     BIGINT        FK → compra NOT NULL
codigo_plan       VARCHAR(20)   NOT NULL
nombre_plan       VARCHAR(100)  NOT NULL
precio            DECIMAL(12,2) NOT NULL
estado_sns        VARCHAR(20)   NOT NULL  DEFAULT 'PENDIENTE'  (EstadoSNS)
intentos_sns      INT           NOT NULL  DEFAULT 0
ultimo_intento    DATETIME
```

### 9.3 Módulo 2 — `db_sns` (ms-sns)

**Tabla: `validacion_sns`**
```
id                          BIGINT        PK AUTO_INCREMENT
codigo_plan                 VARCHAR(20)   NOT NULL
codigo_empresa_aseguradora  VARCHAR(20)   NOT NULL
estado_validacion           VARCHAR(20)   NOT NULL  (EstadoSNS)
fecha_solicitud             DATETIME      NOT NULL
fecha_respuesta             DATETIME
```

### 9.4 Módulo 3 — `db_salud_pay` (ms-saludpay)

**Tabla: `usuario_saludpay`**
```
cedula            VARCHAR(20)   PK
nombre            VARCHAR(200)  NOT NULL
contrasena        VARCHAR(255)  NOT NULL  (BCrypt)
correo            VARCHAR(150)  NOT NULL
activo            BOOLEAN       NOT NULL  DEFAULT true
```

**Tabla: `pago_sps`**
```
id                BIGINT        PK AUTO_INCREMENT
cedula_cliente    VARCHAR(20)   NOT NULL
numero_compra     BIGINT        UNIQUE NOT NULL
valor_pendiente   DECIMAL(12,2) NOT NULL
valor_pagado      DECIMAL(12,2)
estado            VARCHAR(20)   NOT NULL  DEFAULT 'PENDIENTE'  (EstadoPago)
fecha_creacion    DATETIME      NOT NULL
fecha_pago        DATETIME
```

### 9.5 Módulo 4 — `db_shc` (ms-shc)

**Tabla: `historia_clinica`**
```
id                     BIGINT        PK AUTO_INCREMENT
cedula_cliente         VARCHAR(20)   NOT NULL
nombre_cliente         VARCHAR(200)  NOT NULL
correo_cliente         VARCHAR(150)  NOT NULL
numero_compra_origen   BIGINT        UNIQUE NOT NULL
fecha_registro         DATETIME      NOT NULL
```

**Tabla: `plan_shc`**
```
id                     BIGINT        PK AUTO_INCREMENT
id_historia_clinica    BIGINT        FK → historia_clinica NOT NULL
codigo_plan            VARCHAR(20)   NOT NULL
nombre_plan            VARCHAR(100)  NOT NULL
```

**Tabla: `servicio_shc`**
```
id                BIGINT        PK AUTO_INCREMENT
id_plan_shc       BIGINT        FK → plan_shc NOT NULL
codigo_servicio   VARCHAR(20)   NOT NULL
nombre            VARCHAR(100)  NOT NULL
tipo              VARCHAR(30)   NOT NULL
```

### 9.6 Módulo 4 — `db_sam` (ms-sam)

**Tabla: `agenda_medica`**
```
id                     BIGINT        PK AUTO_INCREMENT
cedula_cliente         VARCHAR(20)   NOT NULL
nombre_cliente         VARCHAR(200)  NOT NULL
numero_compra_origen   BIGINT        UNIQUE NOT NULL
fecha_registro         DATETIME      NOT NULL
```

**Tabla: `cita_medica`**
```
id                  BIGINT        PK AUTO_INCREMENT
id_agenda_medica    BIGINT        FK → agenda_medica NOT NULL
codigo_servicio     VARCHAR(20)   NOT NULL
nombre_servicio     VARCHAR(100)  NOT NULL
tipo_servicio       VARCHAR(30)   NOT NULL
codigo_plan         VARCHAR(20)   NOT NULL
estado              VARCHAR(20)   NOT NULL  DEFAULT 'PENDIENTE'  (EstadoCita)
```

---

## 10. Catálogo de Endpoints API

### ms-auth (puerto 8081)

| Método | Ruta                  | Auth JWT | Descripción                    |
|--------|-----------------------|----------|--------------------------------|
| POST   | /ws/auth/login        | No       | Login, devuelve JWT            |
| POST   | /ws/auth/registro     | No       | Registra nuevo usuario         |
| GET    | /ws/auth/validate     | Sí       | Valida token, devuelve claims  |
| GET    | /ws/auth/usuarios/{cedula} | Sí  | Obtiene datos del usuario      |

**POST /ws/auth/login — Request:**
```json
{ "cedula": "1234567890", "contrasena": "miPassword" }
```
**POST /ws/auth/login — Response data:**
```json
{ "token": "<jwt>", "cedula": "1234567890", "nombre": "Juan Pérez", "rol": "CLIENTE" }
```

---

### ms-compra (puerto 8082)

| Método | Ruta                         | Auth JWT | Descripción                              |
|--------|------------------------------|----------|------------------------------------------|
| GET    | /ws/compra/planes            | Sí       | Lista todos los planes activos           |
| GET    | /ws/compra/planes/{codigo}   | Sí       | Obtiene plan por código                  |
| POST   | /ws/compra                   | Sí       | Crea nueva compra (responde 202)         |
| GET    | /ws/compra/{numero}          | Sí       | Consulta estado de compra                |
| GET    | /ws/compra/cliente/{cedula}  | Sí       | Lista compras del cliente                |
| POST   | /ws/compra/{numero}/pago     | No*      | Notificación de pago desde SaludPay      |

> *El endpoint `/pago` no lleva JWT de cliente; lleva una API Key interna `X-Internal-Key` definida en variable de entorno `INTERNAL_API_KEY` (misma en ms-compra y ms-saludpay).

**POST /ws/compra — Request:**
```json
{
  "cedulaCliente": "1234567890",
  "codigosPlanes": ["PLAN-001", "PLAN-002"]
}
```
**POST /ws/compra — Response (202):**
```json
{
  "status": "OK",
  "data": { "numeroCompra": 1001, "estado": "PENDIENTE_VALIDACION_SNS" },
  "message": "Solicitud recibida. Recibirá un correo cuando sea posible continuar la compra.",
  "timestamp": "2026-05-25T10:30:00"
}
```

**POST /ws/compra/{numero}/pago — Request:**
```json
{
  "cedulaCliente": "1234567890",
  "numeroCompra": 1001,
  "valorPagado": 250000.00
}
```

---

### ms-sns (puerto 8083)

| Método | Ruta              | Auth JWT | Descripción                              |
|--------|-------------------|----------|------------------------------------------|
| POST   | /ws/sns/validar   | No       | Valida un plan (mock SNS externo)        |
| GET    | /ws/sns/estado/{id} | No     | Consulta estado de validación por ID     |

**POST /ws/sns/validar — Request:**
```json
{ "codigoPlan": "PLAN-001", "codigoEmpresaAseguradora": "EMP-SPS-001" }
```
**POST /ws/sns/validar — Response:**
```json
{
  "status": "OK",
  "data": { "idValidacion": 5, "estadoValidacion": "EN_PROCESO" },
  "message": "Validación iniciada",
  "timestamp": "..."
}
```

---

### ms-saludpay API backend (puerto 5000)

| Método | Ruta                          | Auth             | Descripción                                |
|--------|-------------------------------|------------------|--------------------------------------------|
| POST   | /ws/saludpay/compras-pendientes | X-Internal-Key | SPS envía compras pendientes de pago       |
| GET    | /ws/saludpay/compras/{cedula} | SaludPay JWT     | Frontend obtiene compras del cliente       |
| POST   | /ws/saludpay/pagar            | SaludPay JWT     | Cliente paga, luego llama callback a SPS   |
| POST   | /ws/saludpay/auth/login       | No               | Login de usuario SaludPay (cedula/pass)    |

**POST /ws/saludpay/compras-pendientes — Request:**
```json
{
  "cedulaCliente": "1234567890",
  "numeroCompra": 1001,
  "valorPendiente": 250000.00
}
```

**POST /ws/saludpay/pagar — Request:**
```json
{ "numeroCompra": 1001, "valorPagado": 250000.00 }
```

---

### ms-shc (puerto 8084)

| Método | Ruta                              | Auth JWT | Descripción                      |
|--------|-----------------------------------|----------|----------------------------------|
| GET    | /ws/shc/historias/{cedula}        | Sí       | Lista historias del cliente      |
| GET    | /ws/shc/historias/compra/{numero} | Sí       | Historia por número de compra    |

---

### ms-sam (puerto 8085)

| Método | Ruta                            | Auth JWT | Descripción                      |
|--------|---------------------------------|----------|----------------------------------|
| GET    | /ws/sam/agenda/{cedula}         | Sí       | Lista agenda del cliente         |
| GET    | /ws/sam/agenda/compra/{numero}  | Sí       | Agenda por número de compra      |

---

## 11. Mensajería AMQP (RabbitMQ)

### SHCQueueServer — Compras a SHC

- **Exchange:** `shc.exchange` (type: direct, durable: true)
- **Queue:** `shc.compras.queue` (durable: true)
- **Routing key:** `shc.compra.nueva`
- **Publicador:** `IntegradorSHC` en ms-compra
- **Consumidor:** `ListenerSHC` en ms-shc

**Formato del mensaje JSON:**
```json
{
  "numeroCompra": 1001,
  "cedulaCliente": "1234567890",
  "nombreCliente": "Juan Pérez",
  "correoCliente": "juan@email.com",
  "planes": [
    {
      "codigoPlan": "PLAN-001",
      "nombrePlan": "Plan Básico",
      "serviciosMedicos": [
        {
          "codigoServicio": "SERV-001",
          "nombre": "Consulta General",
          "tipo": "CONSULTA"
        }
      ]
    }
  ],
  "fechaCompra": "2026-05-25T10:30:00"
}
```

### SAMQueueServer — Compras a SAM

- **Exchange:** `sam.exchange` (type: direct, durable: true)
- **Queue:** `sam.compras.queue` (durable: true)
- **Routing key:** `sam.compra.nueva`
- **Publicador:** `IntegradorSAM` en ms-compra
- **Consumidor:** `ListenerSAM` en ms-sam

**Formato del mensaje JSON:**
```json
{
  "numeroCompra": 1001,
  "cedulaCliente": "1234567890",
  "nombreCliente": "Juan Pérez",
  "serviciosMedicos": [
    {
      "codigoServicio": "SERV-001",
      "nombreServicio": "Consulta General",
      "tipoServicio": "CONSULTA",
      "codigoPlan": "PLAN-001"
    }
  ],
  "fechaCompra": "2026-05-25T10:30:00"
}
```

---

## 12. Enrutamiento NGINX

| Ruta entrante          | Destino backend                      | Notas                               |
|------------------------|--------------------------------------|-------------------------------------|
| `/`                    | Archivos estáticos Angular (SPS)     | try_files con fallback index.html   |
| `/api/auth/`           | http://localhost:8081/ws/auth/       | Proxy pass                          |
| `/api/compra/`         | upstream compra_backend (8082)       | Load balanced (múltiples instancias)|
| `/saludpay/`           | http://localhost:5001/               | Proxy pass a FrontendSaludPay       |

---

## 13. Variables de Entorno por Módulo

### Todas las Spring Boot apps comparten:
```
JWT_SECRET=<cadena_aleatoria_min_256bits>
INTERNAL_API_KEY=<cadena_aleatoria_para_comunicacion_interna>
```

### ms-compra:
```
DB_COMPRA_URL=jdbc:mysql://localhost:3306/db_compra
DB_COMPRA_USER=sps_compra
DB_COMPRA_PASS=<password>
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=sps_user
RABBITMQ_PASS=<password>
MAIL_HOST=<smtp_host>
MAIL_PORT=587
MAIL_USER=<correo>
MAIL_PASS=<password>
SNS_URL=http://localhost:8083
SALUDPAY_URL=http://localhost:5000
COMPRA_BASE_URL=http://localhost:8082
TIMER_INTERVALO_MS=30000
MAX_INTENTOS_SNS=10
```

### ms-auth:
```
DB_AUTH_URL=jdbc:mysql://localhost:3306/db_auth
DB_AUTH_USER=sps_auth
DB_AUTH_PASS=<password>
```

### ms-sns:
```
DB_SNS_URL=jdbc:mysql://localhost:3306/db_sns
DB_SNS_USER=sps_sns
DB_SNS_PASS=<password>
SNS_TIEMPO_PROCESAMIENTO_MS=15000
```

### ms-saludpay (.NET):
```
ConnectionStrings__SaludPayDb=Server=localhost;Database=db_salud_pay;...
Jwt__Secret=<mismo_JWT_SECRET>
SPS_COMPRA_URL=http://localhost:8082
INTERNAL_API_KEY=<misma_clave_interna>
```

### ms-shc y ms-sam:
```
DB_SHC_URL=jdbc:mysql://localhost:3306/db_shc   (o db_sam)
DB_SHC_USER=sps_shc
DB_SHC_PASS=<password>
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=sps_user
RABBITMQ_PASS=<password>
```

---

## 14. Código de Empresa Aseguradora

En todas las llamadas a SNS se usa el código fijo: `EMP-SPS-001`

---

## 15. Datos de Prueba Iniciales (Data Seeding)

Al arrancar cada módulo, si la base de datos está vacía, insertar:

**ms-compra — Planes de Salud:**
```
PLAN-001 | Plan Básico        | $150,000
  SERV-001 | Consulta General  | CONSULTA       | $50,000
  SERV-002 | Examen de Sangre  | EXAMEN         | $100,000

PLAN-002 | Plan Plus          | $350,000
  SERV-003 | Hospitalización   | HOSPITALIZACION| $200,000
  SERV-004 | Consulta Especialista | CONSULTA    | $150,000

PLAN-003 | Plan Premium       | $600,000
  SERV-005 | Cirugía Ambulatoria | EXAMEN       | $400,000
  SERV-006 | Consulta Preventiva | CONSULTA     | $200,000
```

**ms-auth — Usuario de prueba:**
```
cedula: 1000000001 | nombre: Carlos | apellido: Pérez
correo: carlos@test.com | contrasena: Password123! | rol: CLIENTE
```

**ms-saludpay — Usuario de prueba:**
```
cedula: 1000000001 | nombre: Carlos Pérez
contrasena: Password123! | correo: carlos@test.com
```

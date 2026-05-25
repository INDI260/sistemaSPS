# Módulo 3 — Auth SPS + SaludPay (.NET)

**Artefactos a producir:** `MSAuth.war` · `MSSaludPay.dll` · `FrontendSaludPay.dll`  
**Carpetas del repositorio:** `ms-auth/` · `ms-saludpay/` · `ms-saludpay-frontend/`  
**Este módulo centraliza la autenticación del sistema y el subsistema de pagos transaccional en .NET.**

---

## 1. Responsabilidades

| Componente           | Qué hace                                                          |
|----------------------|-------------------------------------------------------------------|
| WSAuth               | REST: login, registro, validación de JWT                          |
| ServiceAuth          | Generación/validación de JWT, BCrypt, gestión de usuarios         |
| RepoAuth             | JPA repository para entidad UsuarioSPS en db_auth                 |
| WSSaludPayController | REST API del backend de SaludPay (recibe compras, procesa pagos)  |
| ServiceSaludPay      | Lógica de pagos: registrar pendientes, procesar pago, callback    |
| RepoSaludPay         | EF Core repositories para PagoSPS y UsuarioSaludPay              |
| PantallaSaludPay     | Razor Pages: login SaludPay, listado de compras pendientes, pago  |
| CESaludPay           | Lógica de presentación Razor (no lógica de negocio)               |
| ProxySaludPay (FE)   | Cliente HTTP de las Razor Pages hacia el backend SaludPay API     |

## 2. Fuera del Alcance

- **No** implementar la SPA Angular (eso es Módulo 1).
- **No** implementar la lógica de compra ni validación SNS (eso es Módulo 2).
- **No** implementar listeners AMQP ni lógica de SHC/SAM (eso es Módulo 4).
- **No** gestionar planes de salud ni servicios médicos.
- El backend SaludPay **no** persiste datos de planes; solo maneja registros de pago.
- La contraseña de UsuarioSaludPay es independiente de la contraseña de UsuarioSPS.

---

## 3. Estructura de Carpetas

```
ms-auth/
├── pom.xml
└── src/main/
    ├── java/com/sps/auth/
    │   ├── MSAuthApplication.java
    │   ├── config/
    │   │   └── WebSecurityConfig.java
    │   ├── controller/
    │   │   └── WSAuthController.java
    │   ├── service/
    │   │   └── ServiceAuth.java
    │   ├── repository/
    │   │   └── RepoAuth.java
    │   ├── entity/
    │   │   └── UsuarioSPS.java
    │   ├── dto/
    │   │   ├── LoginRequestDTO.java
    │   │   ├── LoginResponseDTO.java
    │   │   ├── RegistroRequestDTO.java
    │   │   └── UsuarioDTO.java
    │   ├── enums/
    │   │   └── RolUsuario.java
    │   └── util/
    │       └── JwtUtil.java
    └── resources/
        └── application.properties

ms-saludpay/
├── MSSaludPay.csproj
└── src/
    ├── Program.cs
    ├── appsettings.json
    ├── Controllers/
    │   └── WSSaludPayController.cs
    ├── Services/
    │   ├── ISaludPayService.cs
    │   └── SaludPayService.cs
    ├── Repositories/
    │   ├── IPagoSPSRepository.cs
    │   ├── PagoSPSRepository.cs
    │   ├── IUsuarioSaludPayRepository.cs
    │   └── UsuarioSaludPayRepository.cs
    ├── Data/
    │   └── SaludPayDbContext.cs
    ├── Models/
    │   ├── PagoSPS.cs
    │   └── UsuarioSaludPay.cs
    ├── Dtos/
    │   ├── CompraPendienteDto.cs
    │   ├── PagoRequestDto.cs
    │   ├── LoginSaludPayDto.cs
    │   └── PagoSPSDto.cs
    └── Enums/
        └── EstadoPago.cs

ms-saludpay-frontend/
├── FrontendSaludPay.csproj
└── src/
    ├── Program.cs
    ├── appsettings.json
    ├── Pages/
    │   ├── Index.cshtml / Index.cshtml.cs      ← redirect a Login
    │   ├── Login.cshtml / Login.cshtml.cs       ← login cedula/contraseña
    │   ├── MisCompras.cshtml / MisCompras.cshtml.cs  ← lista compras pendientes
    │   └── Pagar.cshtml / Pagar.cshtml.cs       ← confirmar pago
    └── Services/
        └── SaludPayApiClient.cs                 ← cliente HTTP hacia ms-saludpay (5000)
```

---

## 4. Módulo Auth — Flujos

### Login
```
POST /ws/auth/login
  → Body: { cedula, contrasena }
  → ServiceAuth.login(cedula, contrasena)
      → RepoAuth.findByCedula(cedula) → NotFoundException si no existe
      → BCrypt.checkpw(contrasena, usuario.contrasena) → 401 si falla
      → JwtUtil.generarToken(usuario)
  ← 200 { token, cedula, nombre, rol }
```

### Registro
```
POST /ws/auth/registro
  → Body: { cedula, nombre, apellido, correo, contrasena }
  → ServiceAuth.registrar(dto)
      → Verifica que cédula no exista → 400 si ya existe
      → BCrypt.hash(contrasena)
      → Persiste UsuarioSPS
  ← 201 { cedula, nombre, rol }
```

### Validar Token
```
GET /ws/auth/validate
  → Header: Authorization: Bearer <token>
  → JwtUtil.validarToken(token) → claims
  ← 200 { cedula, nombre, correo, rol }
  ← 401 si token inválido o expirado
```

---

## 5. Módulo Auth — Implementación JwtUtil

```java
public class JwtUtil {
    private final String secret; // cargado de env JWT_SECRET

    public String generarToken(UsuarioSPS usuario) {
        return Jwts.builder()
            .subject(usuario.getCedula())
            .claim("cedula", usuario.getCedula())
            .claim("nombre", usuario.getNombre() + " " + usuario.getApellido())
            .claim("correo", usuario.getCorreo())
            .claim("rol", usuario.getRol().name())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 7200_000L))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
            .compact();
    }

    public Claims validarToken(String token) {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
```

---

## 6. ms-auth — application.properties

```properties
server.port=8081
spring.application.name=ms-auth
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=${DB_AUTH_URL}
spring.datasource.username=${DB_AUTH_USER}
spring.datasource.password=${DB_AUTH_PASS}
app.jwt.secret=${JWT_SECRET}
```

---

## 7. ms-auth — Dependencias Maven

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

---

## 8. SaludPay Backend — Flujo de Negocio (.NET)

```
PASO A: ms-compra registra compra pendiente de pago
────────────────────────────────────────────────────
POST /ws/saludpay/compras-pendientes  (header X-Internal-Key)
  Body: { cedulaCliente, numeroCompra, valorPendiente }
  → SaludPayService.registrarCompraPendiente(dto)
      → Verifica X-Internal-Key
      → Crea PagoSPS con estado PENDIENTE
      → Persiste en db_salud_pay
  ← 201

PASO B: Login del cliente en SaludPay (usado por el frontend Razor)
────────────────────────────────────────────────────────────────────
POST /ws/saludpay/auth/login
  Body: { cedula, contrasena }
  → SaludPayService.loginSaludPay(dto)
      → Busca UsuarioSaludPay por cedula
      → BCrypt.Verify(contrasena, hash)
  ← 200 { tokenSaludPay, cedula, nombre }  (JWT propio de SaludPay)

PASO C: Cliente ve sus compras pendientes (usado por el frontend Razor)
────────────────────────────────────────────────────────────────────────
GET /ws/saludpay/compras/{cedula}  (header Authorization: Bearer <tokenSaludPay>)
  → SaludPayService.obtenerCompraPendiente(cedula)
  ← [ { id, numeroCompra, valorPendiente, estado, fechaCreacion } ]

PASO D: Cliente confirma el pago (usado por el frontend Razor)
──────────────────────────────────────────────────────────────
POST /ws/saludpay/pagar  (header Authorization: Bearer <tokenSaludPay>)
  Body: { numeroCompra, valorPagado }
  → SaludPayService.procesarPago(dto)
      → Carga PagoSPS por numeroCompra
      → Verifica valorPagado >= valorPendiente → error si no
      → Actualiza PagoSPS: estado = PAGADO, valorPagado, fechaPago = now
      → Llama callback HTTP a ms-compra:
          POST http://ms-compra:8082/ws/compra/{numeroCompra}/pago
          Header: X-Internal-Key: <INTERNAL_API_KEY>
          Body: { cedulaCliente, numeroCompra, valorPagado }
  ← 200 { mensaje: "Pago procesado exitosamente" }
```

---

## 9. SaludPay JWT Propio

SaludPay usa su **propio** JWT separado del JWT de SPS:
- Secret: variable de entorno `SALUDPAY_JWT_SECRET`
- Claims: `{ sub: cedula, nombre, exp }`
- Expiry: 3600 segundos (1 hora)
- Este token solo es válido para los endpoints `/ws/saludpay/*` del backend SaludPay

---

## 10. ms-saludpay — appsettings.json

```json
{
  "Kestrel": { "Endpoints": { "Http": { "Url": "http://localhost:5000" } } },
  "ConnectionStrings": {
    "SaludPayDb": "Server=localhost;Database=db_salud_pay;User=sps_saludpay;Password=..."
  },
  "Jwt": {
    "Secret": "<SALUDPAY_JWT_SECRET>",
    "ExpirySeconds": 3600
  },
  "SPS": {
    "CompraCallbackUrl": "http://localhost:8082/ws/compra",
    "InternalApiKey": "<INTERNAL_API_KEY>"
  }
}
```

---

## 11. FrontendSaludPay — Razor Pages

El proyecto `ms-saludpay-frontend` corre en **puerto 5001** y usa Razor Pages (no API controllers). NGINX lo proxea bajo la ruta `/saludpay/`.

| Página                  | Ruta Razor       | Descripción                                    |
|-------------------------|------------------|------------------------------------------------|
| Login                   | `/Login`         | Formulario cedula + contraseña                 |
| Mis Compras Pendientes  | `/MisCompras`    | Lista de compras con estado PENDIENTE          |
| Pagar                   | `/Pagar`         | Confirmación de pago con monto a pagar         |

El `SaludPayApiClient.cs` es el único lugar que hace llamadas HTTP al backend (`ms-saludpay` en puerto 5000). **No** hace lógica de negocio; solo formatea datos para la vista y delega todo al backend.

### ms-saludpay-frontend — appsettings.json
```json
{
  "Kestrel": { "Endpoints": { "Http": { "Url": "http://localhost:5001" } } },
  "SaludPayApi": {
    "BaseUrl": "http://localhost:5000"
  }
}
```

---

## 12. Dependencias .NET (ambos proyectos .NET)

```xml
<!-- ms-saludpay/MSSaludPay.csproj -->
<PackageReference Include="Microsoft.EntityFrameworkCore" Version="8.0.*" />
<PackageReference Include="Microsoft.EntityFrameworkCore.SqlServer" Version="8.0.*" />
<!-- O para MySQL: -->
<PackageReference Include="Pomelo.EntityFrameworkCore.MySql" Version="8.0.*" />
<PackageReference Include="Microsoft.AspNetCore.Authentication.JwtBearer" Version="8.0.*" />
<PackageReference Include="BCrypt.Net-Next" Version="4.0.*" />

<!-- ms-saludpay-frontend/FrontendSaludPay.csproj -->
<PackageReference Include="Microsoft.AspNetCore.Authentication.Cookies" Version="8.0.*" />
```

---

## 13. Separación de Máquinas (Restricción del Profesor)

La restricción indica que lógica de negocio y lógica de presentación deben estar en máquinas separadas:

| Máquina                | Corre                        | Puerto |
|------------------------|------------------------------|--------|
| ProxyServerSPS         | NGINX + FrontendSaludPay.dll | 443, 5001 |
| AppServerSaludPay      | MSSaludPay.dll (Kestrel API) | 5000   |
| AuthServerSPS          | MSAuth.war (Tomcat)          | 8081   |

Los archivos de configuración de despliegue (no del código) deben reflejar estas IPs.

---

## 14. Integración con Otros Módulos

| Módulo origen  | Llama a este módulo         | Qué necesita                     |
|----------------|-----------------------------|----------------------------------|
| Módulo 1       | ms-auth (puerto 8081)       | Endpoint login para proxear      |
| Módulo 2       | ms-saludpay (puerto 5000)   | `INTERNAL_API_KEY` compartida    |
| Módulo 2       | ms-auth JWT secret          | Mismo `JWT_SECRET` para validar  |

Este módulo **llama** a:
- ms-compra (puerto 8082) → callback de pago con `X-Internal-Key`

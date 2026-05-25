# PROMPT MAESTRO — Módulo 3: Auth SPS + SaludPay (.NET)

---

Eres un agente de codificación experto en Spring Boot 3 y ASP.NET Core 8. Tu misión es implementar completamente el **Módulo 3** del Sistema SPS: el microservicio de autenticación (`ms-auth`) en Java/Spring Boot, y el subsistema de pagos SaludPay en .NET 8 dividido en dos proyectos: un backend API (`ms-saludpay`) y un frontend Razor Pages (`ms-saludpay-frontend`).

Lee el archivo `docs/CONVENCIONES_ENTIDADES.md` antes de comenzar. Respeta estrictamente todas las convenciones de nomenclatura, puertos, contratos y formatos definidos allí.

---

## Contexto del Sistema

SPS es un sistema de compra de planes de salud. Cuando un cliente compra planes:
1. Se autentica en el sistema SPS → **ms-auth** genera un JWT
2. El backend de compras (Módulo 2) procesa la compra y, al aprobarse, notifica a **ms-saludpay** (backend)
3. El cliente accede a **ms-saludpay-frontend** (interfaz web), se identifica con su cédula y contraseña, ve sus compras pendientes y confirma el pago
4. **ms-saludpay** procesa el pago y hace un callback HTTP a ms-compra para confirmar

**Restricción del profesor:** La lógica de presentación y la lógica de negocio de SaludPay deben estar en **máquinas separadas**:
- `ms-saludpay-frontend` (presentación) → corre en la misma máquina que NGINX, puerto 5001
- `ms-saludpay` (backend/negocio) → corre en máquina separada, puerto 5000

---

## Tu Responsabilidad: TRES proyectos

### Proyecto 1: `ms-auth/` — Spring Boot WAR, Puerto 8081

**Paquete base:** `com.sps.auth`  
**Artefacto:** `MSAuth.war`

#### Entidad `UsuarioSPS.java`

```java
@Entity
@Table(name = "usuario")
public class UsuarioSPS {
    @Id
    @Column(name = "cedula", length = 20)
    private String cedula;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;  // BCrypt hash

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private RolUsuario rol = RolUsuario.CLIENTE;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() { this.fechaCreacion = LocalDateTime.now(); }
    // getters y setters
}
```

#### Enum `RolUsuario.java`

```java
public enum RolUsuario { CLIENTE, ADMIN }
```

#### `RepoAuth.java` (interface)

```java
@Repository
public interface RepoAuth extends JpaRepository<UsuarioSPS, String> {
    Optional<UsuarioSPS> findByCedula(String cedula);
    Optional<UsuarioSPS> findByCorreo(String correo);
    boolean existsByCedula(String cedula);
}
```

#### `JwtUtil.java`

```java
@Component
public class JwtUtil {
    @Value("${app.jwt.secret}")
    private String secret;

    private static final long EXPIRY_MS = 7200_000L; // 2 horas

    public String generarToken(UsuarioSPS usuario) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Jwts.builder()
            .subject(usuario.getCedula())
            .claim("cedula", usuario.getCedula())
            .claim("nombre", usuario.getNombre() + " " + usuario.getApellido())
            .claim("correo", usuario.getCorreo())
            .claim("rol", usuario.getRol().name())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRY_MS))
            .signWith(Keys.hmacShaKeyFor(keyBytes))
            .compact();
    }

    public Claims validarToken(String token) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(keyBytes))
            .build()
            .parseSignedClaims(token)
            .getPayload();
        // Lanza JwtException si el token es inválido o expirado
    }

    public boolean esValido(String token) {
        try { validarToken(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }
}
```

#### `ServiceAuth.java`

```java
@Service
@Transactional
public class ServiceAuth {
    @Autowired private RepoAuth repoAuth;
    @Autowired private JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public LoginResponseDTO login(String cedula, String contrasena) {
        UsuarioSPS usuario = repoAuth.findByCedula(cedula)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
        if (!encoder.matches(contrasena, usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        if (!usuario.getActivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inactivo");
        }
        String token = jwtUtil.generarToken(usuario);
        return new LoginResponseDTO(token, usuario.getCedula(), usuario.getNombre() + " " + usuario.getApellido(), usuario.getRol().name());
    }

    public UsuarioDTO registrar(RegistroRequestDTO dto) {
        if (repoAuth.existsByCedula(dto.getCedula())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cédula ya está registrada");
        }
        UsuarioSPS usuario = new UsuarioSPS();
        usuario.setCedula(dto.getCedula());
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setCorreo(dto.getCorreo());
        usuario.setContrasena(encoder.encode(dto.getContrasena()));
        usuario.setRol(RolUsuario.CLIENTE);
        repoAuth.save(usuario);
        return new UsuarioDTO(usuario.getCedula(), usuario.getNombre(), usuario.getApellido(), usuario.getCorreo(), usuario.getRol().name());
    }

    public UsuarioDTO validarToken(String token) {
        Claims claims = jwtUtil.validarToken(token);
        // Mapear claims a UsuarioDTO
        return new UsuarioDTO(
            claims.get("cedula", String.class),
            claims.get("nombre", String.class),
            "",
            claims.get("correo", String.class),
            claims.get("rol", String.class)
        );
    }
}
```

#### `WSAuthController.java`

```java
@RestController
@RequestMapping("/ws/auth")
public class WSAuthController {
    @Autowired private ServiceAuth serviceAuth;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody LoginRequestDTO req) {
        LoginResponseDTO data = serviceAuth.login(req.getCedula(), req.getContrasena());
        return ResponseEntity.ok(ApiResponse.ok(data, "Login exitoso"));
    }

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<UsuarioDTO>> registro(@RequestBody RegistroRequestDTO req) {
        UsuarioDTO data = serviceAuth.registrar(req);
        return ResponseEntity.status(201).body(ApiResponse.created(data, "Usuario registrado"));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<UsuarioDTO>> validate(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        UsuarioDTO data = serviceAuth.validarToken(token);
        return ResponseEntity.ok(ApiResponse.ok(data, "Token válido"));
    }

    @GetMapping("/usuarios/{cedula}")
    public ResponseEntity<ApiResponse<UsuarioDTO>> obtenerUsuario(@PathVariable String cedula) {
        // Cargar usuario por cedula y retornar DTO (sin contraseña)
    }
}
```

#### Clase utilitaria `ApiResponse<T>` (también útil en otros módulos Java)

```java
public class ApiResponse<T> {
    private String status;
    private T data;
    private String message;
    private String timestamp;

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>("OK", data, message, LocalDateTime.now().toString());
    }
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", null, message, LocalDateTime.now().toString());
    }
    // constructor all-args, getters, setters
}
```

#### `WebSecurityConfig.java` (ms-auth)

En ms-auth, Spring Security debe estar configurado para permitir acceso sin autenticación a todos los endpoints `/ws/auth/**` (no requiere JWT propio; ms-auth ES el emisor de JWT).

```java
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a.anyRequest().permitAll());
        return http.build();
    }
}
```

#### `application.properties` (ms-auth)

```properties
server.port=8081
spring.application.name=ms-auth
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=${DB_AUTH_URL:jdbc:mysql://localhost:3306/db_auth}
spring.datasource.username=${DB_AUTH_USER:root}
spring.datasource.password=${DB_AUTH_PASS:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
app.jwt.secret=${JWT_SECRET:supersecretkey256bitsminimum1234567890}
```

#### DTOs de ms-auth

```java
// LoginRequestDTO: { cedula, contrasena }
// LoginResponseDTO: { token, cedula, nombre, rol }
// RegistroRequestDTO: { cedula, nombre, apellido, correo, contrasena }
// UsuarioDTO: { cedula, nombre, apellido, correo, rol }
```

#### Data seed ms-auth

Crear `data.sql` con el usuario de prueba de `docs/CONVENCIONES_ENTIDADES.md` sección 15 (contraseña en BCrypt).

#### Dependencias Maven ms-auth

```xml
spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security,
jjwt-api (0.12.3), jjwt-impl (0.12.3, runtime), jjwt-jackson (0.12.3, runtime),
mysql-connector-j (runtime)
```

Empaquetar como WAR.

---

### Proyecto 2: `ms-saludpay/` — ASP.NET Core 8 Web API, Puerto 5000

**Namespace base:** `SPS.SaludPay`  
**Artefacto:** `MSSaludPay.dll`

#### Modelos EF Core (en `Models/`)

```csharp
// PagoSPS.cs
[Table("pago_sps")]
public class PagoSPS {
    [Key] [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required] [MaxLength(20)]
    public string CedulaCliente { get; set; } = string.Empty;

    [Required]
    public long NumeroCompra { get; set; }

    [Required] [Column(TypeName = "decimal(12,2)")]
    public decimal ValorPendiente { get; set; }

    [Column(TypeName = "decimal(12,2)")]
    public decimal? ValorPagado { get; set; }

    [Required] [MaxLength(20)]
    public string Estado { get; set; } = "PENDIENTE";  // EstadoPago

    [Required]
    public DateTime FechaCreacion { get; set; } = DateTime.UtcNow;

    public DateTime? FechaPago { get; set; }
}

// UsuarioSaludPay.cs
[Table("usuario_saludpay")]
public class UsuarioSaludPay {
    [Key] [MaxLength(20)]
    public string Cedula { get; set; } = string.Empty;

    [Required] [MaxLength(200)]
    public string Nombre { get; set; } = string.Empty;

    [Required] [MaxLength(255)]
    public string Contrasena { get; set; } = string.Empty;  // BCrypt

    [Required] [MaxLength(150)]
    public string Correo { get; set; } = string.Empty;

    [Required]
    public bool Activo { get; set; } = true;
}
```

#### `SaludPayDbContext.cs`

```csharp
public class SaludPayDbContext : DbContext {
    public SaludPayDbContext(DbContextOptions<SaludPayDbContext> options) : base(options) { }
    public DbSet<PagoSPS> PagosSPS { get; set; }
    public DbSet<UsuarioSaludPay> UsuariosSaludPay { get; set; }
}
```

#### Interfaces de Repositorio

```csharp
// IPagoSPSRepository.cs
public interface IPagoSPSRepository {
    Task<PagoSPS?> FindByNumeroCompraAsync(long numeroCompra);
    Task<IEnumerable<PagoSPS>> FindByCedulaAndEstadoAsync(string cedula, string estado);
    Task<PagoSPS> SaveAsync(PagoSPS pago);
    Task<PagoSPS> UpdateAsync(PagoSPS pago);
}

// IUsuarioSaludPayRepository.cs
public interface IUsuarioSaludPayRepository {
    Task<UsuarioSaludPay?> FindByCedulaAsync(string cedula);
}
```

#### `ISaludPayService.cs` + `SaludPayService.cs`

```csharp
public interface ISaludPayService {
    Task RegistrarCompraPendienteAsync(CompraPendienteDto dto);
    Task<IEnumerable<PagoSPSDto>> ObtenerCompraPendienteAsync(string cedula);
    Task<string> LoginAsync(LoginSaludPayDto dto);         // retorna JWT SaludPay
    Task PagarAsync(PagoRequestDto dto, string cedula);
}
```

**`SaludPayService.PagarAsync(dto, cedula)`** debe:
1. Cargar `PagoSPS` por `dto.NumeroCompra`
2. Verificar que `dto.ValorPagado >= pago.ValorPendiente`
3. Actualizar: `Estado = "PAGADO"`, `ValorPagado = dto.ValorPagado`, `FechaPago = DateTime.UtcNow`
4. Persistir
5. Hacer HTTP POST a `ms-compra`:
   ```
   POST {SPSCompraCallbackUrl}/ws/compra/{numeroCompra}/pago
   Header: X-Internal-Key: {InternalApiKey}
   Body: { cedulaCliente, numeroCompra, valorPagado }
   ```
   Usar `IHttpClientFactory` para el cliente HTTP.
6. Si el callback falla → loguear el error pero no revertir el pago (el pago ya fue procesado)

#### `WSSaludPayController.cs`

```csharp
[ApiController]
[Route("ws/saludpay")]
public class WSSaludPayController : ControllerBase {

    // POST ws/saludpay/auth/login — login de usuario SaludPay
    [HttpPost("auth/login")]
    public async Task<IActionResult> Login([FromBody] LoginSaludPayDto dto) { ... }

    // POST ws/saludpay/compras-pendientes — recibe compra desde ms-compra (X-Internal-Key)
    [HttpPost("compras-pendientes")]
    public async Task<IActionResult> RecibirCompraPendiente([FromBody] CompraPendienteDto dto) {
        // Validar header X-Internal-Key
        if (!Request.Headers.TryGetValue("X-Internal-Key", out var key) || key != _internalApiKey)
            return Unauthorized();
        ...
    }

    // GET ws/saludpay/compras/{cedula} — lista compras (requiere JWT SaludPay)
    [Authorize]
    [HttpGet("compras/{cedula}")]
    public async Task<IActionResult> ObtenerCompras(string cedula) { ... }

    // POST ws/saludpay/pagar — pagar (requiere JWT SaludPay)
    [Authorize]
    [HttpPost("pagar")]
    public async Task<IActionResult> Pagar([FromBody] PagoRequestDto dto) {
        var cedula = User.FindFirst("cedula")?.Value;
        ...
    }
}
```

#### DTOs .NET

```csharp
// CompraPendienteDto.cs
public record CompraPendienteDto(string CedulaCliente, long NumeroCompra, decimal ValorPendiente);

// PagoRequestDto.cs
public record PagoRequestDto(long NumeroCompra, decimal ValorPagado);

// LoginSaludPayDto.cs
public record LoginSaludPayDto(string Cedula, string Contrasena);

// PagoSPSDto.cs — para respuestas
public record PagoSPSDto(long Id, long NumeroCompra, decimal ValorPendiente, string Estado, DateTime FechaCreacion);
```

#### Respuesta estándar .NET

```csharp
public record ApiResponseDto<T>(string Status, T? Data, string Message, string Timestamp) {
    public static ApiResponseDto<T> Ok(T data, string msg) =>
        new("OK", data, msg, DateTime.UtcNow.ToString("O"));
    public static ApiResponseDto<object> Error(string msg) =>
        new ApiResponseDto<object>("ERROR", null, msg, DateTime.UtcNow.ToString("O"));
}
```

#### JWT SaludPay (.NET)

```csharp
// JwtSaludPayService.cs — genera y valida JWT propio de SaludPay
// Secret: config["Jwt:Secret"] (variable SALUDPAY_JWT_SECRET)
// Claims: sub=cedula, cedula, nombre
// Expiry: config["Jwt:ExpirySeconds"] (default 3600)
```

En `Program.cs`, configurar `AddAuthentication().AddJwtBearer(...)` con el secret de `appsettings.json`.

#### `appsettings.json` (ms-saludpay)

```json
{
  "Kestrel": {
    "Endpoints": { "Http": { "Url": "http://0.0.0.0:5000" } }
  },
  "ConnectionStrings": {
    "SaludPayDb": "Server=localhost;Database=db_salud_pay;User=sps_saludpay;Password=sps_pass;"
  },
  "Jwt": {
    "Secret": "saludpay-super-secret-key-min-256bits",
    "ExpirySeconds": 3600
  },
  "SPS": {
    "CompraCallbackUrl": "http://localhost:8082",
    "InternalApiKey": "internal-secret-key-sps"
  }
}
```

#### `Program.cs` (ms-saludpay) — estructura

```csharp
builder.Services.AddDbContext<SaludPayDbContext>(...);
builder.Services.AddScoped<IPagoSPSRepository, PagoSPSRepository>();
builder.Services.AddScoped<IUsuarioSaludPayRepository, UsuarioSaludPayRepository>();
builder.Services.AddScoped<ISaludPayService, SaludPayService>();
builder.Services.AddHttpClient();  // para el callback a ms-compra
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options => { /* configurar con Jwt:Secret */ });
builder.Services.AddControllers();

// Seed: si no hay usuario SaludPay, crear el de prueba (ver CONVENCIONES_ENTIDADES.md sección 15)
// Seed al arrancar con EnsureCreated() o Migrate()
```

#### Data Seeding ms-saludpay

Al arrancar, si `usuario_saludpay` está vacía, insertar el usuario de prueba de `docs/CONVENCIONES_ENTIDADES.md` sección 15.

---

### Proyecto 3: `ms-saludpay-frontend/` — ASP.NET Core 8 Razor Pages, Puerto 5001

**Namespace base:** `SPS.SaludPay.Frontend`  
**Artefacto:** `FrontendSaludPay.dll`  
**Nota:** Este proyecto es **solo presentación**. No contiene lógica de negocio. Toda la lógica va al backend (ms-saludpay puerto 5000) a través del `SaludPayApiClient`.

#### `SaludPayApiClient.cs`

```csharp
public class SaludPayApiClient {
    private readonly HttpClient _httpClient;
    private readonly string _baseUrl;

    public SaludPayApiClient(HttpClient httpClient, IConfiguration config) {
        _httpClient = httpClient;
        _baseUrl = config["SaludPayApi:BaseUrl"];
    }

    public async Task<(bool ok, string? token, string? error)> LoginAsync(string cedula, string contrasena) {
        // POST {_baseUrl}/ws/saludpay/auth/login
        // Retorna (true, token, null) o (false, null, errorMsg)
    }

    public async Task<List<PagoSPSDto>> ObtenerComprasAsync(string cedula, string token) {
        // GET {_baseUrl}/ws/saludpay/compras/{cedula}
        // Authorization: Bearer {token}
    }

    public async Task<(bool ok, string? error)> PagarAsync(long numeroCompra, decimal valor, string token) {
        // POST {_baseUrl}/ws/saludpay/pagar
        // Authorization: Bearer {token}
        // Body: { numeroCompra, valorPagado }
    }
}
```

#### Páginas Razor (en `Pages/`)

**`Login.cshtml` + `Login.cshtml.cs`**
```
- Formulario: Cédula (text) + Contraseña (password) + botón "Ingresar"
- OnPost: llama SaludPayApiClient.LoginAsync()
  - Si ok: guarda token en cookie de sesión, redirige a /MisCompras
  - Si error: muestra mensaje de error en la página
```

**`MisCompras.cshtml` + `MisCompras.cshtml.cs`**
```
- OnGet: verifica cookie de sesión (si no hay → redirect a /Login)
- Llama SaludPayApiClient.ObtenerComprasAsync(cedula, token)
- Muestra tabla: N° Compra | Valor Pendiente | Estado | Acción
- Botón "Pagar" por cada compra en estado PENDIENTE
- Al hacer clic en "Pagar": redirige a /Pagar?numeroCompra={id}&valor={valor}
```

**`Pagar.cshtml` + `Pagar.cshtml.cs`**
```
- Recibe query params: numeroCompra y valor
- Muestra: "Va a pagar $[valor] por la compra N° [numeroCompra]"
- Botón "Confirmar Pago"
- OnPost: llama SaludPayApiClient.PagarAsync(numeroCompra, valor, token)
  - Si ok: muestra mensaje "Pago procesado exitosamente" y enlace a /MisCompras
  - Si error: muestra mensaje de error
```

#### Manejo de sesión

Usar `ISession` de ASP.NET Core para guardar el token JWT de SaludPay y la cédula del usuario autenticado. Agregar `builder.Services.AddSession()` en `Program.cs`.

#### `appsettings.json` (ms-saludpay-frontend)

```json
{
  "Kestrel": {
    "Endpoints": { "Http": { "Url": "http://0.0.0.0:5001" } }
  },
  "SaludPayApi": {
    "BaseUrl": "http://localhost:5000"
  }
}
```

#### `Program.cs` (ms-saludpay-frontend)

```csharp
builder.Services.AddRazorPages();
builder.Services.AddHttpClient<SaludPayApiClient>();
builder.Services.AddSession(options => {
    options.IdleTimeout = TimeSpan.FromMinutes(30);
    options.Cookie.HttpOnly = true;
});
// NO agregar Entity Framework, NO agregar autenticación JWT aquí
```

---

## Instrucciones de Implementación (en este orden)

### ms-auth (Java)
1. Crear proyecto con Spring Initializr (Web, JPA, Security, MySQL Driver)
2. Configurar `application.properties`
3. Implementar entidad `UsuarioSPS` y enum `RolUsuario`
4. Implementar `RepoAuth`
5. Implementar `JwtUtil`
6. Implementar `ServiceAuth`
7. Implementar `WSAuthController`
8. Implementar `ApiResponse<T>`
9. Configurar `WebSecurityConfig` (todo permitAll)
10. Crear `data.sql` con usuario de prueba

### ms-saludpay (.NET API)
1. `dotnet new webapi -n MSSaludPay`
2. Agregar paquetes NuGet (EF Core, MySQL/SqlServer, JwtBearer, BCrypt.Net)
3. Crear modelos y DbContext
4. Crear repositorios (interfaz + implementación)
5. Implementar `SaludPayService` con lógica de pago y callback
6. Implementar `WSSaludPayController`
7. Configurar `Program.cs` (DI, JWT, HttpClient, EF)
8. Configurar `appsettings.json`
9. Crear seed de usuario en `Program.cs` (llamar a `EnsureCreated()` + insert si vacío)

### ms-saludpay-frontend (.NET Razor)
1. `dotnet new webapp -n FrontendSaludPay`
2. Crear `SaludPayApiClient`
3. Implementar páginas Razor (Login, MisCompras, Pagar)
4. Configurar `Program.cs` (Session, HttpClient, RazorPages)
5. Configurar `appsettings.json`

---

## Restricciones Críticas

- **ms-saludpay-frontend NO debe** tener lógica de negocio. Solo llama a ms-saludpay y renderiza datos.
- **ms-saludpay-frontend NO debe** conectarse a ninguna base de datos.
- **ms-auth NO valida** los JWTs de SaludPay; esos son tokens separados con secret separado.
- El JWT de SPS (generado por ms-auth) y el JWT de SaludPay (generado por ms-saludpay) tienen **secrets diferentes** y son para sistemas diferentes.
- **Nunca** devolver la contraseña (hash) en ninguna respuesta.
- El endpoint `POST /ws/saludpay/compras-pendientes` se protege con `X-Internal-Key`, no con JWT.
- Al hacer el callback HTTP desde ms-saludpay a ms-compra: si falla la llamada HTTP, **no revertir** el pago. Solo loguear el error.
- Usar `BCrypt.Net-Next` para hash de contraseñas en .NET, con work factor 12.

---

## Verificación de Éxito

El módulo está completo cuando:
- `mvn spring-boot:run` arranca ms-auth en puerto 8081 sin errores
- `POST http://localhost:8081/ws/auth/login` con credenciales válidas devuelve JWT
- `GET http://localhost:8081/ws/auth/validate` con JWT válido devuelve claims
- `dotnet run` arranca ms-saludpay en puerto 5000 sin errores
- `POST http://localhost:5000/ws/saludpay/auth/login` devuelve token SaludPay
- `POST http://localhost:5000/ws/saludpay/compras-pendientes` con X-Internal-Key válida registra la compra
- `dotnet run` arranca ms-saludpay-frontend en puerto 5001 sin errores
- La página `/Login` renderiza correctamente en el navegador
- La página `/MisCompras` muestra compras pendientes del cliente autenticado

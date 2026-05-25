# PROMPT MAESTRO — Módulo 2: Compra SPS + Mock SNS

---

Eres un agente de codificación experto en Spring Boot 3 y arquitecturas orientadas a eventos. Tu misión es implementar completamente el **Módulo 2** del Sistema SPS: el microservicio central de compra (`ms-compra`) que orquesta todo el flujo de negocio, y el mock de la SNS externa (`ms-sns`).

Lee el archivo `docs/CONVENCIONES_ENTIDADES.md` antes de comenzar. Respeta estrictamente todas las convenciones de nomenclatura, puertos, enums, contratos API y formatos de mensaje definidos allí.

---

## Contexto del Sistema

El Sistema de Compra de Planes de Salud (SPS) permite a los clientes adquirir planes médicos en línea. El flujo de compra es:

1. El cliente envía una solicitud de compra → el sistema responde 202 Accepted (no espera respuesta inmediata)
2. En segundo plano, el sistema valida cada plan ante la SNS externa (Superintendencia Nacional de Salud) mediante polling con un **Java Timer**
3. La SNS puede responder ENPROCESO (aún no decidida), APROBADO o RECHAZADO
4. Cuando todos los planes son APROBADO: se envía correo al cliente con URL de pago y se notifica a SaludPay
5. El cliente paga en SaludPay, que hace un callback HTTP a este módulo
6. Al confirmar pago: enviar correo de confirmación y publicar mensajes AMQP a SHC y SAM

**Importante:** La SNS es externa y se accede **sin MOM** (sin colas), usando HTTP directo con polling periódico. El Timer de Java es el mecanismo de retry y de polling.

---

## Tu Responsabilidad: DOS proyectos Spring Boot

### Proyecto A: `ms-compra/` — Puerto 8082

Empaqueta como WAR (artefacto: `MSCompraSPS.war`).

**Paquete base:** `com.sps.compra`

#### Entidades JPA (en `entity/`)

```java
// Compra.java
@Entity @Table(name = "compra")
public class Compra {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "numero_compra")
    private Long numeroCompra;

    @Column(name = "cedula_cliente", nullable = false, length = 20)
    private String cedulaCliente;

    @Column(name = "nombre_cliente", nullable = false, length = 200)
    private String nombreCliente;

    @Column(name = "correo_cliente", nullable = false, length = 150)
    private String correoCliente;

    @Column(name = "precio_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 40)
    private EstadoCompra estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_ultima_actualizacion", nullable = false)
    private LocalDateTime fechaUltimaActualizacion;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PlanCompra> planes;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaUltimaActualizacion = LocalDateTime.now();
    }
    @PreUpdate
    public void preUpdate() {
        this.fechaUltimaActualizacion = LocalDateTime.now();
    }
    // getters y setters
}

// PlanCompra.java
@Entity @Table(name = "plan_compra")
public class PlanCompra {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "numero_compra", nullable = false)
    private Compra compra;

    @Column(name = "codigo_plan", nullable = false, length = 20)
    private String codigoPlan;

    @Column(name = "nombre_plan", nullable = false, length = 100)
    private String nombrePlan;

    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_sns", nullable = false, length = 20)
    private EstadoSNS estadoSNS = EstadoSNS.PENDIENTE;

    @Column(name = "intentos_sns", nullable = false)
    private Integer intentosSNS = 0;

    @Column(name = "id_validacion_sns")
    private Long idValidacionSNS;  // ID retornado por SNS para consultas subsiguientes

    @Column(name = "ultimo_intento")
    private LocalDateTime ultimoIntento;
    // getters y setters
}

// PlanSalud.java
@Entity @Table(name = "plan_salud")
public class PlanSalud {
    @Id
    @Column(name = "codigo_plan", length = 20)
    private String codigoPlan;

    @Column(name = "nombre_plan", nullable = false, length = 100)
    private String nombrePlan;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "planSalud", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ServicioMedico> serviciosMedicos;
    // getters y setters
}

// ServicioMedico.java
@Entity @Table(name = "servicio_medico")
public class ServicioMedico {
    @Id
    @Column(name = "codigo_servicio", length = 20)
    private String codigoServicio;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoServicioMedico tipo;

    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_plan")
    private PlanSalud planSalud;
    // getters y setters
}
```

#### Enums (en `enums/`)

```java
// EstadoCompra.java
public enum EstadoCompra {
    PENDIENTE_VALIDACION_SNS,
    EN_PROCESO_SNS,
    APROBADO_SNS,
    RECHAZADO_SNS,
    PENDIENTE_PAGO,
    PAGADO,
    COMPLETADO,
    CANCELADO
}

// EstadoSNS.java
public enum EstadoSNS { PENDIENTE, EN_PROCESO, APROBADO, RECHAZADO }

// TipoServicioMedico.java
public enum TipoServicioMedico { CONSULTA, EXAMEN, HOSPITALIZACION }
```

#### Controlador `WSCompraController.java`

```java
@RestController
@RequestMapping("/ws/compra")
public class WSCompraController {

    // GET /ws/compra/planes → lista todos los planes activos
    @GetMapping("/planes")
    public ResponseEntity<ApiResponse<List<PlanSaludDTO>>> listarPlanes() { ... }

    // GET /ws/compra/planes/{codigo} → obtiene un plan específico
    @GetMapping("/planes/{codigo}")
    public ResponseEntity<ApiResponse<PlanSaludDTO>> obtenerPlan(@PathVariable String codigo) { ... }

    // POST /ws/compra → crea nueva compra (protegido por JWT)
    @PostMapping
    public ResponseEntity<ApiResponse<CompraResumenDTO>> crearCompra(
        @RequestBody CompraRequestDTO request,
        HttpServletRequest httpRequest  // para extraer cedula del JWT
    ) { ... }

    // GET /ws/compra/{numero} → consulta estado de compra (protegido por JWT)
    @GetMapping("/{numero}")
    public ResponseEntity<ApiResponse<CompraDTO>> obtenerCompra(@PathVariable Long numero) { ... }

    // GET /ws/compra/cliente/{cedula} → lista compras del cliente (protegido por JWT)
    @GetMapping("/cliente/{cedula}")
    public ResponseEntity<ApiResponse<List<CompraDTO>>> listarPorCliente(@PathVariable String cedula) { ... }

    // POST /ws/compra/{numero}/pago → callback de SaludPay (protegido por X-Internal-Key)
    @PostMapping("/{numero}/pago")
    public ResponseEntity<ApiResponse<Void>> recibirPago(
        @PathVariable Long numero,
        @RequestBody PagoNotificacionDTO pago,
        @RequestHeader("X-Internal-Key") String internalKey
    ) { ... }
}
```

Todas las respuestas usan el wrapper `ApiResponse<T>` definido en las convenciones.

#### Servicio `ServiceCompra.java` — Métodos principales

```java
@Service
@Transactional
public class ServiceCompra {

    public CompraResumenDTO crearCompra(CompraRequestDTO request, String cedulaCliente) {
        // 1. Cargar datos del cliente desde JWT (nombre y correo vienen en los claims)
        // 2. Cargar planes de la BD y calcular precio total
        // 3. Crear Compra con estado PENDIENTE_VALIDACION_SNS
        // 4. Crear PlanCompra[] con estadoSNS=PENDIENTE para cada plan
        // 5. Persistir
        // 6. Retornar DTO con numeroCompra y estado
    }

    public void procesarValidacionesSNSPendientes() {
        // Llamado periódicamente por el Timer
        // 1. Buscar compras en estado PENDIENTE_VALIDACION_SNS o EN_PROCESO_SNS
        // 2. Para cada compra, para cada PlanCompra con estadoSNS PENDIENTE o EN_PROCESO:
        //    a. Si idValidacionSNS es null: llamar ProxySNS.iniciarValidacion() → guardar idValidacionSNS
        //    b. Si idValidacionSNS no es null: llamar ProxySNS.consultarEstado(idValidacionSNS)
        //    c. Según respuesta: actualizar estadoSNS, incrementar intentosSNS
        //    d. Si intentosSNS >= MAX_INTENTOS_SNS: marcar como RECHAZADO
        // 3. Recalcular estado global de la Compra:
        //    - Todos APROBADO → notificarAprobacion(compra)
        //    - Alguno RECHAZADO y el resto no PENDIENTE → marcar compra RECHAZADO_SNS
        //    - En otro caso → estado EN_PROCESO_SNS
    }

    private void notificarAprobacion(Compra compra) {
        // 1. Actualizar compra.estado = APROBADO_SNS
        // 2. ProxyEmail.enviarCorreoAprobacion(...)
        // 3. ProxySaludPay.registrarCompraPendiente(...)
        // 4. Actualizar compra.estado = PENDIENTE_PAGO
    }

    public void procesarPago(Long numeroCompra, PagoNotificacionDTO pago, String internalKey) {
        // 1. Validar internalKey contra env variable INTERNAL_API_KEY
        // 2. Cargar Compra, verificar estado = PENDIENTE_PAGO
        // 3. Verificar valorPagado >= precioTotal
        // 4. Actualizar estado = PAGADO
        // 5. ProxyEmail.enviarCorreoConfirmacion(...)
        // 6. Actualizar estado = COMPLETADO
        // 7. IntegradorSHC.publicar(compra)
        // 8. IntegradorSAM.publicar(compra)
    }
}
```

#### Timer — `ValidacionSNSTimerTask.java`

```java
public class ValidacionSNSTimerTask extends TimerTask {
    private final ServiceCompra serviceCompra;

    public ValidacionSNSTimerTask(ServiceCompra serviceCompra) {
        this.serviceCompra = serviceCompra;
    }

    @Override
    public void run() {
        try {
            serviceCompra.procesarValidacionesSNSPendientes();
        } catch (Exception e) {
            // Log error pero NO relanzar; el Timer debe seguir ejecutándose
        }
    }
}
```

**Inicialización del Timer** — en un `@Component` con `@PostConstruct`:

```java
@Component
public class TimerConfig {
    @Autowired private ServiceCompra serviceCompra;
    @Value("${app.timer.intervalo.ms:30000}") private long intervaloMs;

    @PostConstruct
    public void iniciarTimer() {
        Timer timer = new Timer("ValidacionSNSTimer", true); // daemon=true
        timer.schedule(
            new ValidacionSNSTimerTask(serviceCompra),
            10000L,    // esperar 10s antes del primer disparo
            intervaloMs
        );
    }
}
```

**CRÍTICO:** Usar **`java.util.Timer`** y **`java.util.TimerTask`**. NO usar `@Scheduled`, NO usar `ScheduledExecutorService`, NO usar `@EnableScheduling`.

#### Proxy `ProxySNS.java`

```java
@Component
public class ProxySNS {
    @Value("${app.sns.url}") private String snsUrl;
    private final RestTemplate restTemplate;

    // Inicia validación de un plan. Retorna el idValidacion para consultas futuras.
    public Long iniciarValidacion(String codigoPlan) {
        // POST {snsUrl}/ws/sns/validar
        // Body: { codigoPlan, codigoEmpresaAseguradora: "EMP-SPS-001" }
        // Retorna: idValidacion del body de respuesta
    }

    // Consulta el estado de una validación iniciada previamente
    public EstadoSNS consultarEstado(Long idValidacion) {
        // GET {snsUrl}/ws/sns/estado/{idValidacion}
        // Retorna: el estadoValidacion mapeado a EstadoSNS
        // En caso de error de red o timeout: retornar EN_PROCESO (el Timer reintentará)
    }
}
```

Usar `RestTemplate` (no `WebClient`) para mantener código sincrónico compatible con el Timer.  
En caso de excepción de red: loguear el error y retornar `EstadoSNS.EN_PROCESO` para que el Timer reintente.

#### Proxy `ProxySaludPay.java`

```java
@Component
public class ProxySaludPay {
    @Value("${app.saludpay.url}") private String saludPayUrl;
    @Value("${app.internal.api.key}") private String internalApiKey;
    private final RestTemplate restTemplate;

    public void registrarCompraPendiente(String cedulaCliente, Long numeroCompra, BigDecimal valor) {
        // POST {saludPayUrl}/ws/saludpay/compras-pendientes
        // Header: X-Internal-Key: {internalApiKey}
        // Body: { cedulaCliente, numeroCompra, valorPendiente: valor }
        // En caso de error: loguear, NO lanzar excepción (la compra continúa)
    }
}
```

#### Proxy `ProxyEmail.java`

```java
@Component
public class ProxyEmail {
    @Autowired private JavaMailSender mailSender;
    @Value("${app.compra.base.url}") private String compraBaseUrl;

    public void enviarCorreoAprobacion(String correo, String nombre,
                                       Long numeroCompra, BigDecimal valor) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(correo);
        msg.setSubject("SPS - Su compra N° " + numeroCompra + " está lista para pagar");
        msg.setText(
            "Estimado/a " + nombre + ",\n\n" +
            "Sus planes han sido validados exitosamente.\n" +
            "Valor a pagar: $" + valor + "\n" +
            "Ingrese al portal de pago en: " + compraBaseUrl + "/saludpay/\n\n" +
            "Equipo SPS"
        );
        mailSender.send(msg);
    }

    public void enviarCorreoConfirmacion(String correo, String nombre, Long numeroCompra) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(correo);
        msg.setSubject("SPS - Compra N° " + numeroCompra + " completada");
        msg.setText(
            "Estimado/a " + nombre + ",\n\n" +
            "Su compra N° " + numeroCompra + " ha sido completada exitosamente.\n" +
            "¡Gracias por confiar en SPS!\n\n" +
            "Equipo SPS"
        );
        mailSender.send(msg);
    }
}
```

#### Integrador `IntegradorSHC.java`

```java
@Component
public class IntegradorSHC {
    @Autowired private RabbitTemplate rabbitTemplate;
    @Autowired private ObjectMapper objectMapper;

    public void publicar(Compra compra) {
        MensajeSHCDTO mensaje = buildMensajeSHC(compra);
        rabbitTemplate.convertAndSend("shc.exchange", "shc.compra.nueva", mensaje);
    }

    private MensajeSHCDTO buildMensajeSHC(Compra compra) {
        // Construir MensajeSHCDTO con todos los planes y servicios médicos
        // Ver formato exacto en docs/CONVENCIONES_ENTIDADES.md sección 11
    }
}
```

#### Integrador `IntegradorSAM.java`

```java
@Component
public class IntegradorSAM {
    @Autowired private RabbitTemplate rabbitTemplate;

    public void publicar(Compra compra) {
        MensajeSAMDTO mensaje = buildMensajeSAM(compra);
        rabbitTemplate.convertAndSend("sam.exchange", "sam.compra.nueva", mensaje);
    }
    // Ver formato exacto del mensaje en docs/CONVENCIONES_ENTIDADES.md sección 11
}
```

#### Seguridad `JwtFilter.java`

Filtro que extiende `OncePerRequestFilter`:
- Rutas públicas (sin JWT): `POST /ws/compra/{id}/pago`
- Todas las demás rutas `/ws/compra/**`: requieren JWT válido
- Extraer claims del JWT → poner `cedula` en `HttpServletRequest` attribute `"cedula_cliente"`
- El controlador lee ese atributo para saber quién hace la petición

#### `RabbitMQConfig.java`

```java
@Configuration
public class RabbitMQConfig {
    @Bean public Queue queueSHC() { return new Queue("shc.compras.queue", true); }
    @Bean public Queue queueSAM() { return new Queue("sam.compras.queue", true); }
    @Bean public DirectExchange exchangeSHC() { return new DirectExchange("shc.exchange"); }
    @Bean public DirectExchange exchangeSAM() { return new DirectExchange("sam.exchange"); }
    @Bean public Binding bindingSHC(Queue queueSHC, DirectExchange exchangeSHC) {
        return BindingBuilder.bind(queueSHC).to(exchangeSHC).with("shc.compra.nueva");
    }
    @Bean public Binding bindingSAM(Queue queueSAM, DirectExchange exchangeSAM) {
        return BindingBuilder.bind(queueSAM).to(exchangeSAM).with("sam.compra.nueva");
    }
    @Bean public MessageConverter jsonConverter() { return new Jackson2JsonMessageConverter(); }
    @Bean public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate rt = new RabbitTemplate(cf);
        rt.setMessageConverter(jsonConverter());
        return rt;
    }
}
```

#### Data Seeding — `data.sql` en resources

Insertar los planes y servicios médicos de prueba definidos en `docs/CONVENCIONES_ENTIDADES.md` sección 15. Usar `spring.sql.init.mode=always` junto con `spring.jpa.hibernate.ddl-auto=update`.

#### `application.properties` (ms-compra)

```properties
server.port=8082
spring.application.name=ms-compra
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always

spring.datasource.url=${DB_COMPRA_URL:jdbc:mysql://localhost:3306/db_compra}
spring.datasource.username=${DB_COMPRA_USER:root}
spring.datasource.password=${DB_COMPRA_PASS:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USER:guest}
spring.rabbitmq.password=${RABBITMQ_PASS:guest}

spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USER:}
spring.mail.password=${MAIL_PASS:}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

app.jwt.secret=${JWT_SECRET:supersecretkey256bitsminimum1234567890}
app.internal.api.key=${INTERNAL_API_KEY:internal-secret-key-sps}
app.sns.url=${SNS_URL:http://localhost:8083}
app.saludpay.url=${SALUDPAY_URL:http://localhost:5000}
app.compra.base.url=${COMPRA_BASE_URL:https://localhost}
app.timer.intervalo.ms=${TIMER_INTERVALO_MS:30000}
app.sns.max.intentos=${MAX_INTENTOS_SNS:10}
app.empresa.codigo=EMP-SPS-001
```

---

### Proyecto B: `ms-sns/` — Puerto 8083

Mock de la SNS externa. Empaqueta como WAR (artefacto: `SNS.war`).

**Paquete base:** `com.sps.sns`

#### Entidad `ValidacionSNS.java`

```java
@Entity @Table(name = "validacion_sns")
public class ValidacionSNS {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_plan", nullable = false, length = 20)
    private String codigoPlan;

    @Column(name = "codigo_empresa_aseguradora", nullable = false, length = 20)
    private String codigoEmpresaAseguradora;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", nullable = false, length = 20)
    private EstadoSNS estadoValidacion;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;
    // getters y setters
}
```

#### Controlador `WSSNSController.java`

```java
@RestController
@RequestMapping("/ws/sns")
public class WSSNSController {

    // POST /ws/sns/validar → inicia validación de un plan
    @PostMapping("/validar")
    public ResponseEntity<ApiResponse<ValidacionResponseDTO>> validar(
        @RequestBody ValidacionRequestDTO request) { ... }

    // GET /ws/sns/estado/{id} → consulta estado de una validación
    @GetMapping("/estado/{id}")
    public ResponseEntity<ApiResponse<ValidacionResponseDTO>> consultarEstado(
        @PathVariable Long id) { ... }
}
```

#### Servicio `ServiceSNS.java`

El servicio debe simular el comportamiento real de la SNS:

1. `iniciarValidacion(codigoPlan, codigoEmpresa)`:
   - Crea ValidacionSNS con estadoValidacion = `EN_PROCESO`
   - Persiste y guarda el `id`
   - Inicia un **`java.util.Timer`** (o reutiliza uno global) que, después de `SNS_TIEMPO_PROCESAMIENTO_MS` milisegundos, actualiza el estado:
     - Si `Math.random() > 0.3` → `APROBADO`
     - Si no → `RECHAZADO`
   - Retorna el id de la validación

2. `consultarEstado(id)`:
   - Carga ValidacionSNS por id
   - Retorna el estado actual (puede ser EN_PROCESO, APROBADO o RECHAZADO)

**Usa `java.util.Timer`** para el cambio de estado retrasado, no `Thread.sleep()`.

```java
private final Timer timerSNS = new Timer("SNSProcessingTimer", true);

public ValidacionResponseDTO iniciarValidacion(ValidacionRequestDTO req) {
    ValidacionSNS v = new ValidacionSNS();
    v.setCodigoPlan(req.getCodigoPlan());
    v.setCodigoEmpresaAseguradora(req.getCodigoEmpresaAseguradora());
    v.setEstadoValidacion(EstadoSNS.EN_PROCESO);
    v.setFechaSolicitud(LocalDateTime.now());
    v = repoSNS.save(v);
    final Long idValidacion = v.getId();

    timerSNS.schedule(new TimerTask() {
        @Override
        public void run() {
            EstadoSNS resultado = Math.random() > 0.3 ? EstadoSNS.APROBADO : EstadoSNS.RECHAZADO;
            repoSNS.findById(idValidacion).ifPresent(val -> {
                val.setEstadoValidacion(resultado);
                val.setFechaRespuesta(LocalDateTime.now());
                repoSNS.save(val);
            });
        }
    }, tiempoProcesamiento); // tiempoProcesamiento desde env SNS_TIEMPO_PROCESAMIENTO_MS

    return new ValidacionResponseDTO(idValidacion, EstadoSNS.EN_PROCESO);
}
```

#### `application.properties` (ms-sns)

```properties
server.port=8083
spring.application.name=ms-sns
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=${DB_SNS_URL:jdbc:mysql://localhost:3306/db_sns}
spring.datasource.username=${DB_SNS_USER:root}
spring.datasource.password=${DB_SNS_PASS:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
app.sns.tiempo.procesamiento.ms=${SNS_TIEMPO_PROCESAMIENTO_MS:15000}
```

---

## DTOs comunes (ms-compra)

```java
// CompraRequestDTO.java
public class CompraRequestDTO {
    private List<String> codigosPlanes;
    // getter, setter
}

// CompraResumenDTO.java
public class CompraResumenDTO {
    private Long numeroCompra;
    private String estado;
    // getters, setters, constructor
}

// CompraDTO.java — respuesta completa con planes
public class CompraDTO {
    private Long numeroCompra;
    private String cedulaCliente;
    private String nombreCliente;
    private BigDecimal precioTotal;
    private String estado;
    private String fechaCreacion;
    private List<PlanCompraDTO> planes;
}

// PagoNotificacionDTO.java — recibido desde SaludPay
public class PagoNotificacionDTO {
    private String cedulaCliente;
    private Long numeroCompra;
    private BigDecimal valorPagado;
}

// MensajeSHCDTO.java — enviado a la cola SHC
public class MensajeSHCDTO {
    private Long numeroCompra;
    private String cedulaCliente;
    private String nombreCliente;
    private String correoCliente;
    private List<PlanMensajeDTO> planes;
    private String fechaCompra;
}

// MensajeSAMDTO.java — enviado a la cola SAM
public class MensajeSAMDTO {
    private Long numeroCompra;
    private String cedulaCliente;
    private String nombreCliente;
    private List<ServicioMensajeDTO> serviciosMedicos;
    private String fechaCompra;
}
```

---

## Instrucciones de Implementación (en este orden)

1. Crear `ms-compra/` con Spring Initializr (Spring Web, Spring Data JPA, Spring AMQP, Spring Mail, Spring Security, MySQL Driver)
2. Crear `ms-sns/` con Spring Initializr (Spring Web, Spring Data JPA, MySQL Driver)
3. Implementar entidades y enums de ms-compra
4. Implementar repositories (interfaces JpaRepository)
5. Implementar DTOs
6. Implementar ProxySNS, ProxySaludPay, ProxyEmail
7. Implementar IntegradorSHC e IntegradorSAM
8. Implementar ServiceCompra con toda la lógica de negocio
9. Implementar ValidacionSNSTimerTask y TimerConfig
10. Implementar WSCompraController
11. Implementar JwtFilter y WebSecurityConfig
12. Implementar RabbitMQConfig
13. Agregar data.sql con seed de planes
14. Implementar ms-sns (entidad, repo, servicio con Timer, controlador)
15. Verificar application.properties de ambos proyectos

---

## Restricciones Críticas

- **Usar `java.util.Timer` y `java.util.TimerTask`** en AMBOS proyectos. Prohibido `@Scheduled`.
- **No compartir BD** con ningún otro módulo.
- **No implementar UI**: este módulo solo expone APIs REST.
- El endpoint `POST /ws/compra/{numero}/pago` se protege con `X-Internal-Key`, no con JWT.
- Si ProxySNS, ProxySaludPay o ProxyEmail lanzan excepción de red, **capturar y loguear**; nunca dejar que la excepción detenga el Timer.
- El Timer es `daemon=true` para que no impida el shutdown de la JVM.
- La validación JWT se hace **localmente** con el mismo `JWT_SECRET` que usa ms-auth; no llamar a ms-auth para validar.

---

## Verificación de Éxito

El módulo está completo cuando:
- `mvn spring-boot:run` arranca ms-compra en puerto 8082 sin errores
- `mvn spring-boot:run` arranca ms-sns en puerto 8083 sin errores
- `POST http://localhost:8082/ws/compra` con JWT válido devuelve 202
- El Timer de ms-compra llama a ms-sns cada 30 segundos (verificable en logs)
- El Timer de ms-sns actualiza el estado de una validación a APROBADO/RECHAZADO después de 15 segundos
- `GET http://localhost:8082/ws/compra/planes` devuelve los planes seeded

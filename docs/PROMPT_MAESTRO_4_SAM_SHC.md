# PROMPT MAESTRO — Módulo 4: SAM (Agenda Médica) + SHC (Historias Clínicas)

---

Eres un agente de codificación experto en Spring Boot 3 y mensajería AMQP con RabbitMQ. Tu misión es implementar completamente el **Módulo 4** del Sistema SPS: el Sistema de Historias Clínicas (`ms-shc`) y el Sistema de Agenda Médica (`ms-sam`), ambos en Spring Boot, que reciben información de compras completadas a través de colas AMQP de RabbitMQ.

Lee el archivo `docs/CONVENCIONES_ENTIDADES.md` antes de comenzar. Respeta estrictamente todas las convenciones de nomenclatura, puertos, formatos de mensaje AMQP y contratos definidos allí.

---

## Contexto del Sistema

Cuando un cliente completa el pago de su compra de planes de salud en SPS, el módulo de compras (Módulo 2) publica un mensaje AMQP en dos colas de RabbitMQ:
- **`shc.compras.queue`** → consumida por este módulo (`ms-shc`)
- **`sam.compras.queue`** → consumida por este módulo (`ms-sam`)

`ms-shc` almacena el historial clínico del cliente: qué planes y servicios médicos adquirió.  
`ms-sam` almacena la agenda médica: qué citas médicas deben ser agendadas para el cliente.

Ambos son sistemas independientes con sus propias bases de datos. No se comunican entre sí.

**RabbitMQ** debe estar instalado y corriendo en `localhost:5672`. Ambos módulos declaran sus exchanges y queues al arrancar (de forma idempotente).

---

## Tu Responsabilidad: DOS proyectos Spring Boot

### Proyecto A: `ms-shc/` — Puerto 8084

**Paquete base:** `com.sps.shc`  
**Artefacto:** `MSSHC.war`

#### Entidades JPA (en `entity/`)

```java
// HistoriaClinica.java
@Entity
@Table(name = "historia_clinica")
public class HistoriaClinica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cedula_cliente", nullable = false, length = 20)
    private String cedulaCliente;

    @Column(name = "nombre_cliente", nullable = false, length = 200)
    private String nombreCliente;

    @Column(name = "correo_cliente", nullable = false, length = 150)
    private String correoCliente;

    @Column(name = "numero_compra_origen", nullable = false, unique = true)
    private Long numeroCompraOrigen;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "historiaClinica", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PlanSHC> planes = new ArrayList<>();

    @PrePersist
    public void prePersist() { this.fechaRegistro = LocalDateTime.now(); }
    // getters y setters
}

// PlanSHC.java
@Entity
@Table(name = "plan_shc")
public class PlanSHC {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_historia_clinica", nullable = false)
    private HistoriaClinica historiaClinica;

    @Column(name = "codigo_plan", nullable = false, length = 20)
    private String codigoPlan;

    @Column(name = "nombre_plan", nullable = false, length = 100)
    private String nombrePlan;

    @OneToMany(mappedBy = "planSHC", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ServicioSHC> servicios = new ArrayList<>();
    // getters y setters
}

// ServicioSHC.java
@Entity
@Table(name = "servicio_shc")
public class ServicioSHC {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_shc", nullable = false)
    private PlanSHC planSHC;

    @Column(name = "codigo_servicio", nullable = false, length = 20)
    private String codigoServicio;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;
    // getters y setters
}
```

#### Repositorios (en `repository/`)

```java
// RepoHistoriaClinica.java
@Repository
public interface RepoHistoriaClinica extends JpaRepository<HistoriaClinica, Long> {
    List<HistoriaClinica> findByCedulaCliente(String cedula);
    Optional<HistoriaClinica> findByNumeroCompraOrigen(Long numeroCompra);
    boolean existsByNumeroCompraOrigen(Long numeroCompra);
}

// RepoPlanSHC.java
@Repository
public interface RepoPlanSHC extends JpaRepository<PlanSHC, Long> { }

// RepoServicioSHC.java
@Repository
public interface RepoServicioSHC extends JpaRepository<ServicioSHC, Long> { }
```

#### DTOs para el mensaje AMQP entrante (en `dto/`)

```java
// MensajeCompraDTO.java — mapea el JSON que llega de la cola shc.compras.queue
public class MensajeCompraDTO {
    private Long numeroCompra;
    private String cedulaCliente;
    private String nombreCliente;
    private String correoCliente;
    private List<PlanMensajeDTO> planes;
    private String fechaCompra;
    // getters y setters
}

// PlanMensajeDTO.java
public class PlanMensajeDTO {
    private String codigoPlan;
    private String nombrePlan;
    private List<ServicioMensajeDTO> serviciosMedicos;
    // getters y setters
}

// ServicioMensajeDTO.java
public class ServicioMensajeDTO {
    private String codigoServicio;
    private String nombre;
    private String tipo;
    // getters y setters
}

// HistoriaClinicaDTO.java — para respuestas REST
public class HistoriaClinicaDTO {
    private Long id;
    private String cedulaCliente;
    private String nombreCliente;
    private String correoCliente;
    private Long numeroCompraOrigen;
    private String fechaRegistro;
    private List<PlanSHCDTO> planes;
    // getters y setters
}
```

#### Listener AMQP `ListenerSHC.java`

```java
@Component
public class ListenerSHC {
    @Autowired
    private ServiceSHC serviceSHC;

    @RabbitListener(queues = "shc.compras.queue")
    public void recibirCompra(MensajeCompraDTO mensaje) {
        try {
            // Idempotencia: si ya procesamos esta compra, ignorar
            if (serviceSHC.yaExisteHistoria(mensaje.getNumeroCompra())) {
                return;
            }
            serviceSHC.crearHistoriaClinica(mensaje);
        } catch (Exception e) {
            // Loguear el error. Si se lanza excepción, RabbitMQ reintentará el mensaje.
            // Para evitar loop infinito, loguear y NO relanzar para mensajes ya procesados.
            throw e; // Relanzar para que RabbitMQ reintente si es error genuino
        }
    }
}
```

#### Servicio `ServiceSHC.java`

```java
@Service
@Transactional
public class ServiceSHC {
    @Autowired private RepoHistoriaClinica repoHistoriaClinica;

    public boolean yaExisteHistoria(Long numeroCompra) {
        return repoHistoriaClinica.existsByNumeroCompraOrigen(numeroCompra);
    }

    public void crearHistoriaClinica(MensajeCompraDTO mensaje) {
        HistoriaClinica historia = new HistoriaClinica();
        historia.setCedulaCliente(mensaje.getCedulaCliente());
        historia.setNombreCliente(mensaje.getNombreCliente());
        historia.setCorreoCliente(mensaje.getCorreoCliente());
        historia.setNumeroCompraOrigen(mensaje.getNumeroCompra());

        List<PlanSHC> planes = new ArrayList<>();
        for (PlanMensajeDTO planDto : mensaje.getPlanes()) {
            PlanSHC plan = new PlanSHC();
            plan.setCodigoPlan(planDto.getCodigoPlan());
            plan.setNombrePlan(planDto.getNombrePlan());
            plan.setHistoriaClinica(historia);

            List<ServicioSHC> servicios = new ArrayList<>();
            for (ServicioMensajeDTO svcDto : planDto.getServiciosMedicos()) {
                ServicioSHC svc = new ServicioSHC();
                svc.setCodigoServicio(svcDto.getCodigoServicio());
                svc.setNombre(svcDto.getNombre());
                svc.setTipo(svcDto.getTipo());
                svc.setPlanSHC(plan);
                servicios.add(svc);
            }
            plan.setServicios(servicios);
            planes.add(plan);
        }
        historia.setPlanes(planes);
        repoHistoriaClinica.save(historia);
    }

    public List<HistoriaClinicaDTO> listarPorCedula(String cedula) {
        return repoHistoriaClinica.findByCedulaCliente(cedula)
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public HistoriaClinicaDTO obtenerPorNumeroCompra(Long numeroCompra) {
        return repoHistoriaClinica.findByNumeroCompraOrigen(numeroCompra)
            .map(this::toDTO)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Historia no encontrada"));
    }

    private HistoriaClinicaDTO toDTO(HistoriaClinica h) {
        // Mapear entidad a DTO
    }
}
```

#### Controlador REST `WSShcController.java`

```java
@RestController
@RequestMapping("/ws/shc")
public class WSShcController {

    @Autowired private ServiceSHC serviceSHC;

    // GET /ws/shc/historias/{cedula} — lista historias del cliente (requiere JWT)
    @GetMapping("/historias/{cedula}")
    public ResponseEntity<ApiResponse<List<HistoriaClinicaDTO>>> listarHistorias(
            @PathVariable String cedula) {
        List<HistoriaClinicaDTO> historias = serviceSHC.listarPorCedula(cedula);
        return ResponseEntity.ok(ApiResponse.ok(historias, "Historias clínicas encontradas"));
    }

    // GET /ws/shc/historias/compra/{numeroCompra} — historia por compra (requiere JWT)
    @GetMapping("/historias/compra/{numeroCompra}")
    public ResponseEntity<ApiResponse<HistoriaClinicaDTO>> obtenerPorCompra(
            @PathVariable Long numeroCompra) {
        HistoriaClinicaDTO historia = serviceSHC.obtenerPorNumeroCompra(numeroCompra);
        return ResponseEntity.ok(ApiResponse.ok(historia, "Historia encontrada"));
    }
}
```

#### Configuración RabbitMQ `RabbitMQConfig.java` (ms-shc)

```java
@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue queueSHC() {
        return new Queue("shc.compras.queue", true); // durable=true
    }

    @Bean
    public DirectExchange exchangeSHC() {
        return new DirectExchange("shc.exchange");
    }

    @Bean
    public Binding bindingSHC(Queue queueSHC, DirectExchange exchangeSHC) {
        return BindingBuilder.bind(queueSHC).to(exchangeSHC).with("shc.compra.nueva");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
```

#### Filtro JWT `JwtFilter.java` (ms-shc)

Copia exacta del JwtFilter de ms-compra, ajustando el package. Valida JWT con el mismo `JWT_SECRET` (variable de entorno) que usa ms-auth. Protege todos los endpoints `/ws/shc/**`.

#### `application.properties` (ms-shc)

```properties
server.port=8084
spring.application.name=ms-shc
spring.jpa.hibernate.ddl-auto=update

spring.datasource.url=${DB_SHC_URL:jdbc:mysql://localhost:3306/db_shc}
spring.datasource.username=${DB_SHC_USER:root}
spring.datasource.password=${DB_SHC_PASS:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USER:guest}
spring.rabbitmq.password=${RABBITMQ_PASS:guest}

app.jwt.secret=${JWT_SECRET:supersecretkey256bitsminimum1234567890}
```

---

### Proyecto B: `ms-sam/` — Puerto 8085

**Paquete base:** `com.sps.sam`  
**Artefacto:** `MSSAM.war`

#### Entidades JPA (en `entity/`)

```java
// AgendaMedica.java
@Entity
@Table(name = "agenda_medica")
public class AgendaMedica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cedula_cliente", nullable = false, length = 20)
    private String cedulaCliente;

    @Column(name = "nombre_cliente", nullable = false, length = 200)
    private String nombreCliente;

    @Column(name = "numero_compra_origen", nullable = false, unique = true)
    private Long numeroCompraOrigen;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "agendaMedica", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CitaMedica> citas = new ArrayList<>();

    @PrePersist
    public void prePersist() { this.fechaRegistro = LocalDateTime.now(); }
    // getters y setters
}

// CitaMedica.java
@Entity
@Table(name = "cita_medica")
public class CitaMedica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agenda_medica", nullable = false)
    private AgendaMedica agendaMedica;

    @Column(name = "codigo_servicio", nullable = false, length = 20)
    private String codigoServicio;

    @Column(name = "nombre_servicio", nullable = false, length = 100)
    private String nombreServicio;

    @Column(name = "tipo_servicio", nullable = false, length = 30)
    private String tipoServicio;

    @Column(name = "codigo_plan", nullable = false, length = 20)
    private String codigoPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCita estado = EstadoCita.PENDIENTE;
    // getters y setters
}
```

#### Enum (en `enums/`)

```java
public enum EstadoCita { PENDIENTE, CONFIRMADA, CANCELADA }
```

#### Repositorios (en `repository/`)

```java
// RepoAgendaMedica.java
@Repository
public interface RepoAgendaMedica extends JpaRepository<AgendaMedica, Long> {
    List<AgendaMedica> findByCedulaCliente(String cedula);
    Optional<AgendaMedica> findByNumeroCompraOrigen(Long numeroCompra);
    boolean existsByNumeroCompraOrigen(Long numeroCompra);
}

// RepoCitaMedica.java
@Repository
public interface RepoCitaMedica extends JpaRepository<CitaMedica, Long> { }
```

#### DTOs para el mensaje AMQP entrante (en `dto/`)

```java
// MensajeCompraDTO.java — mapea el JSON de la cola sam.compras.queue
// ATENCIÓN: el formato SAM es diferente al de SHC
public class MensajeCompraDTO {
    private Long numeroCompra;
    private String cedulaCliente;
    private String nombreCliente;
    // SAM recibe lista plana de servicios (no anidada por plan)
    private List<ServicioMensajeDTO> serviciosMedicos;
    private String fechaCompra;
    // getters y setters
}

// ServicioMensajeDTO.java — para SAM (campos diferentes a los de SHC)
public class ServicioMensajeDTO {
    private String codigoServicio;
    private String nombreServicio;   // OJO: aquí se llama "nombreServicio", no "nombre"
    private String tipoServicio;     // OJO: aquí se llama "tipoServicio", no "tipo"
    private String codigoPlan;
    // getters y setters
}

// AgendaMedicaDTO.java — para respuestas REST
public class AgendaMedicaDTO {
    private Long id;
    private String cedulaCliente;
    private String nombreCliente;
    private Long numeroCompraOrigen;
    private String fechaRegistro;
    private List<CitaMedicaDTO> citas;
}

// CitaMedicaDTO.java
public class CitaMedicaDTO {
    private Long id;
    private String codigoServicio;
    private String nombreServicio;
    private String tipoServicio;
    private String codigoPlan;
    private String estado;
}
```

#### Listener AMQP `ListenerSAM.java`

```java
@Component
public class ListenerSAM {
    @Autowired
    private ServiceSAM serviceSAM;

    @RabbitListener(queues = "sam.compras.queue")
    public void recibirCompra(MensajeCompraDTO mensaje) {
        try {
            if (serviceSAM.yaExisteAgenda(mensaje.getNumeroCompra())) {
                return; // Idempotencia
            }
            serviceSAM.crearAgendaMedica(mensaje);
        } catch (Exception e) {
            throw e; // Relanzar para que RabbitMQ reintente
        }
    }
}
```

#### Servicio `ServiceSAM.java`

```java
@Service
@Transactional
public class ServiceSAM {
    @Autowired private RepoAgendaMedica repoAgendaMedica;

    public boolean yaExisteAgenda(Long numeroCompra) {
        return repoAgendaMedica.existsByNumeroCompraOrigen(numeroCompra);
    }

    public void crearAgendaMedica(MensajeCompraDTO mensaje) {
        AgendaMedica agenda = new AgendaMedica();
        agenda.setCedulaCliente(mensaje.getCedulaCliente());
        agenda.setNombreCliente(mensaje.getNombreCliente());
        agenda.setNumeroCompraOrigen(mensaje.getNumeroCompra());

        List<CitaMedica> citas = new ArrayList<>();
        for (ServicioMensajeDTO svcDto : mensaje.getServiciosMedicos()) {
            CitaMedica cita = new CitaMedica();
            cita.setCodigoServicio(svcDto.getCodigoServicio());
            cita.setNombreServicio(svcDto.getNombreServicio());
            cita.setTipoServicio(svcDto.getTipoServicio());
            cita.setCodigoPlan(svcDto.getCodigoPlan());
            cita.setEstado(EstadoCita.PENDIENTE);
            cita.setAgendaMedica(agenda);
            citas.add(cita);
        }
        agenda.setCitas(citas);
        repoAgendaMedica.save(agenda);
    }

    public List<AgendaMedicaDTO> listarPorCedula(String cedula) {
        return repoAgendaMedica.findByCedulaCliente(cedula)
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public AgendaMedicaDTO obtenerPorNumeroCompra(Long numeroCompra) {
        return repoAgendaMedica.findByNumeroCompraOrigen(numeroCompra)
            .map(this::toDTO)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agenda no encontrada"));
    }

    private AgendaMedicaDTO toDTO(AgendaMedica a) {
        // Mapear entidad a DTO
    }
}
```

#### Controlador REST `WSSamController.java`

```java
@RestController
@RequestMapping("/ws/sam")
public class WSSamController {

    @Autowired private ServiceSAM serviceSAM;

    // GET /ws/sam/agenda/{cedula} (requiere JWT)
    @GetMapping("/agenda/{cedula}")
    public ResponseEntity<ApiResponse<List<AgendaMedicaDTO>>> listarAgenda(
            @PathVariable String cedula) {
        List<AgendaMedicaDTO> agenda = serviceSAM.listarPorCedula(cedula);
        return ResponseEntity.ok(ApiResponse.ok(agenda, "Agenda médica encontrada"));
    }

    // GET /ws/sam/agenda/compra/{numeroCompra} (requiere JWT)
    @GetMapping("/agenda/compra/{numeroCompra}")
    public ResponseEntity<ApiResponse<AgendaMedicaDTO>> obtenerPorCompra(
            @PathVariable Long numeroCompra) {
        AgendaMedicaDTO agenda = serviceSAM.obtenerPorNumeroCompra(numeroCompra);
        return ResponseEntity.ok(ApiResponse.ok(agenda, "Agenda encontrada"));
    }
}
```

#### Configuración RabbitMQ `RabbitMQConfig.java` (ms-sam)

```java
@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue queueSAM() {
        return new Queue("sam.compras.queue", true);
    }

    @Bean
    public DirectExchange exchangeSAM() {
        return new DirectExchange("sam.exchange");
    }

    @Bean
    public Binding bindingSAM(Queue queueSAM, DirectExchange exchangeSAM) {
        return BindingBuilder.bind(queueSAM).to(exchangeSAM).with("sam.compra.nueva");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
```

#### `JwtFilter.java` (ms-sam)

Idéntico al de ms-shc. Mismo `JWT_SECRET`. Protege todos los endpoints `/ws/sam/**`.

#### `application.properties` (ms-sam)

```properties
server.port=8085
spring.application.name=ms-sam
spring.jpa.hibernate.ddl-auto=update

spring.datasource.url=${DB_SAM_URL:jdbc:mysql://localhost:3306/db_sam}
spring.datasource.username=${DB_SAM_USER:root}
spring.datasource.password=${DB_SAM_PASS:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USER:guest}
spring.rabbitmq.password=${RABBITMQ_PASS:guest}

app.jwt.secret=${JWT_SECRET:supersecretkey256bitsminimum1234567890}
```

---

## Dependencias Maven (ambos proyectos)

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
        <artifactId>spring-boot-starter-amqp</artifactId>
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
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
    </dependency>
</dependencies>
```

Empaquetar como WAR.

---

## Clase `ApiResponse<T>` (copiar de ms-compra o redefinir)

```java
public class ApiResponse<T> {
    private String status;
    private T data;
    private String message;
    private String timestamp;

    public static <T> ApiResponse<T> ok(T data, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.status = "OK"; r.data = data; r.message = message;
        r.timestamp = LocalDateTime.now().toString();
        return r;
    }
    // getters y setters
}
```

---

## Instrucciones de Implementación (en este orden)

### ms-shc
1. Crear proyecto con Spring Initializr (Web, JPA, AMQP, Security, MySQL Driver)
2. Configurar `application.properties`
3. Implementar entidades (HistoriaClinica, PlanSHC, ServicioSHC) con cascada correcta
4. Implementar repositorios
5. Implementar DTOs de entrada (MensajeCompraDTO, PlanMensajeDTO, ServicioMensajeDTO)
6. Implementar DTOs de salida (HistoriaClinicaDTO y sus nested)
7. Implementar `ServiceSHC` con lógica de creación y mapeo
8. Implementar `ListenerSHC` con anotación `@RabbitListener`
9. Implementar `WSShcController` con endpoints de consulta
10. Implementar `RabbitMQConfig`
11. Implementar `JwtFilter` y `WebSecurityConfig`
12. Implementar `ApiResponse<T>`

### ms-sam
1. Repetir pasos 1-12 para ms-sam con las entidades y DTOs propios de SAM
2. Prestar especial atención: el mensaje SAM tiene `serviciosMedicos` como lista plana (no anidada por plan), con campos `nombreServicio` y `tipoServicio` (no `nombre` y `tipo`)

---

## Diferencias Clave entre SHC y SAM

| Aspecto           | SHC                                  | SAM                                   |
|-------------------|--------------------------------------|---------------------------------------|
| Cola AMQP         | `shc.compras.queue`                  | `sam.compras.queue`                   |
| Exchange          | `shc.exchange`                       | `sam.exchange`                        |
| Routing key       | `shc.compra.nueva`                   | `sam.compra.nueva`                    |
| Estructura JSON   | Planes anidados con servicios        | Servicios planos con codigoPlan       |
| Campo servicio    | `nombre`, `tipo`                     | `nombreServicio`, `tipoServicio`      |
| Entidad raíz      | `HistoriaClinica`                    | `AgendaMedica`                        |
| Entidad hijo      | `PlanSHC` → `ServicioSHC`           | `CitaMedica` (directa a AgendaMedica) |
| Base de datos     | `db_shc`                             | `db_sam`                              |
| Puerto            | 8084                                 | 8085                                  |

---

## Restricciones Críticas

- **No publicar mensajes** AMQP. Solo consumir.
- **No comunicarse** directamente con ms-compra, ms-auth, ms-saludpay ni ms-sns.
- **No compartir** base de datos con ningún otro módulo.
- **No implementar** lógica de negocio compleja de agendamiento. Solo almacenar lo que llega.
- La **idempotencia** del listener es obligatoria: si el mismo mensaje llega dos veces (RabbitMQ puede reintentar), no se deben duplicar registros. Verificar por `numeroCompraOrigen` antes de insertar.
- El JWT que valida este módulo lo **emitió ms-auth** (Módulo 3). El secret debe ser el mismo.
- Configurar `spring.jackson.serialization.write-dates-as-timestamps=false` para serialización correcta de fechas.

---

## Verificación de Éxito

El módulo está completo cuando:
- `mvn spring-boot:run` arranca ms-shc en puerto 8084 sin errores
- `mvn spring-boot:run` arranca ms-sam en puerto 8085 sin errores
- RabbitMQ está corriendo y ambos módulos se conectan al arrancar (verificable en logs)
- Las colas `shc.compras.queue` y `sam.compras.queue` aparecen en la consola de RabbitMQ (http://localhost:15672)
- Al publicar manualmente un mensaje en `shc.compras.queue` con el formato correcto, `ListenerSHC` lo procesa y crea la `HistoriaClinica` en `db_shc`
- `GET http://localhost:8084/ws/shc/historias/{cedula}` con JWT válido devuelve las historias almacenadas
- Al publicar manualmente en `sam.compras.queue`, `ListenerSAM` crea la `AgendaMedica` en `db_sam`
- Si el mismo mensaje llega dos veces, no se crean registros duplicados (idempotencia)

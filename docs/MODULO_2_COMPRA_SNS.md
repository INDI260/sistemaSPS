# Módulo 2 — Compra SPS + SNS (Mock)

**Artefactos a producir:** `MSCompraSPS.war` · `SNS.war`  
**Carpetas del repositorio:** `ms-compra/` · `ms-sns/`  
**Es el núcleo del sistema.** Orquesta el flujo completo de compra: recepción, validación SNS asincrónica mediante Timer, notificación por correo, delegación de pago a SaludPay, y difusión asincrónica MOM hacia SHC y SAM.

---

## 1. Responsabilidades

| Componente       | Qué hace                                                                      |
|------------------|-------------------------------------------------------------------------------|
| WSCompra         | Controlador REST que expone los endpoints de compra y planes                  |
| ServiceCompra    | Lógica de negocio: estado de compra, flujo, validaciones                      |
| RepoCompra       | JPA repositories para Compra, PlanCompra, PlanSalud, ServicioMedico           |
| ProxySNS         | Cliente HTTP que llama al servicio SNS externo (mock)                         |
| ProxySaludPay    | Cliente HTTP que envía compras pendientes a AppServerSaludPay                 |
| ProxyEmail       | Cliente SMTP que envía correos al cliente vía JavaMail                        |
| Timer            | `java.util.Timer` que periódicamente procesa validaciones SNS pendientes      |
| IntegradorSHC    | Publica mensajes AMQP a la cola `shc.compras.queue`                          |
| IntegradorSAM    | Publica mensajes AMQP a la cola `sam.compras.queue`                          |
| WSSNS            | Controlador REST del mock SNS externo                                         |
| ServiceSNS       | Lógica del mock: simula ENPROCESO → APROBADO/RECHAZADO con delay              |
| RepoSNS          | JPA repository para ValidacionSNS                                             |

## 2. Fuera del Alcance

- **No** implementar autenticación de usuarios (eso es Módulo 3, ms-auth).
- **No** implementar la UI (eso es Módulo 1).
- **No** implementar el listener de mensajes en SHC ni SAM (eso es Módulo 4).
- **No** implementar el procesamiento del pago (eso es Módulo 3, ms-saludpay).
- **No** usar `@Scheduled` de Spring. Usar exclusivamente `java.util.Timer` + `java.util.TimerTask`.

---

## 3. Estructura de Carpetas

```
ms-compra/
├── pom.xml
└── src/main/
    ├── java/com/sps/compra/
    │   ├── MSCompraSPSApplication.java
    │   ├── config/
    │   │   ├── RabbitMQConfig.java
    │   │   ├── JwtFilter.java          ← valida JWT en requests
    │   │   └── WebSecurityConfig.java
    │   ├── controller/
    │   │   └── WSCompraController.java
    │   ├── service/
    │   │   ├── ServiceCompra.java
    │   │   └── ValidacionSNSTimerTask.java
    │   ├── proxy/
    │   │   ├── ProxySNS.java
    │   │   ├── ProxySaludPay.java
    │   │   └── ProxyEmail.java
    │   ├── integrador/
    │   │   ├── IntegradorSHC.java
    │   │   └── IntegradorSAM.java
    │   ├── repository/
    │   │   ├── RepoCompra.java
    │   │   ├── RepoPlanCompra.java
    │   │   ├── RepoPlanSalud.java
    │   │   └── RepoServicioMedico.java
    │   ├── entity/
    │   │   ├── Compra.java
    │   │   ├── PlanCompra.java
    │   │   ├── PlanSalud.java
    │   │   └── ServicioMedico.java
    │   ├── dto/
    │   │   ├── CompraDTO.java
    │   │   ├── CompraRequestDTO.java
    │   │   ├── PagoNotificacionDTO.java
    │   │   ├── PlanSaludDTO.java
    │   │   ├── MensajeSHCDTO.java
    │   │   └── MensajeSAMDTO.java
    │   └── enums/
    │       ├── EstadoCompra.java
    │       ├── EstadoSNS.java
    │       └── TipoServicioMedico.java
    └── resources/
        ├── application.properties
        └── data.sql                   ← seed de planes

ms-sns/
├── pom.xml
└── src/main/
    ├── java/com/sps/sns/
    │   ├── SNSApplication.java
    │   ├── controller/
    │   │   └── WSSNSController.java
    │   ├── service/
    │   │   └── ServiceSNS.java
    │   ├── repository/
    │   │   └── RepoSNS.java
    │   ├── entity/
    │   │   └── ValidacionSNS.java
    │   └── dto/
    │       ├── ValidacionRequestDTO.java
    │       └── ValidacionResponseDTO.java
    └── resources/
        └── application.properties
```

---

## 4. Flujo de Negocio Completo

```
PASO 1: Cliente envía solicitud de compra
─────────────────────────────────────────
POST /ws/compra
  → WSCompraController.crearCompra()
  → ServiceCompra.crearCompra(request)
      → Valida que todos los codigosPlanes existan en db_compra
      → Construye entidad Compra con estado = PENDIENTE_VALIDACION_SNS
      → Construye PlanCompra[] con estadoSNS = PENDIENTE para cada plan
      → Persiste en db_compra
  ← 202 Accepted + { numeroCompra, estado }

PASO 2: Timer procesa validaciones pendientes (cada 30 segundos)
────────────────────────────────────────────────────────────────
ValidacionSNSTimerTask.run()
  → RepoCompra.findByEstado(PENDIENTE_VALIDACION_SNS | EN_PROCESO_SNS)
  → Para cada Compra:
      → Para cada PlanCompra con estadoSNS = PENDIENTE o EN_PROCESO:
          → ProxySNS.validarPlan(codigoPlan, "EMP-SPS-001")
          → Respuesta puede ser:
              ENPROCESO → PlanCompra.intentosSNS++, continuar esperando
              APROBADO  → PlanCompra.estadoSNS = APROBADO
              RECHAZADO → PlanCompra.estadoSNS = RECHAZADO
          → Si intentosSNS > MAX_INTENTOS_SNS → marcar como RECHAZADO
      → Actualizar Compra.estado = EN_PROCESO_SNS
      → Si todos los planes APROBADO:
          → Compra.estado = APROBADO_SNS
          → ServiceCompra.notificarAprobacion(compra)  ← ver PASO 3
      → Si algún plan RECHAZADO:
          → Compra.estado = RECHAZADO_SNS
          → (Opcional: notificar al cliente por correo)

PASO 3: Notificación de aprobación SNS
───────────────────────────────────────
ServiceCompra.notificarAprobacion(compra)
  → ProxyEmail.enviarCorreoAprobacion(
        correo = compra.correoCliente,
        nombre = compra.nombreCliente,
        numeroCompra = compra.numeroCompra,
        valor = compra.precioTotal,
        urlPago = "https://localhost/saludpay/"
    )
  → ProxySaludPay.registrarCompraPendiente(
        cedulaCliente, numeroCompra, precioTotal
    )
  → Compra.estado = PENDIENTE_PAGO

PASO 4: SaludPay confirma el pago
──────────────────────────────────
POST /ws/compra/{numero}/pago   (header X-Internal-Key)
  → WSCompraController.recibirPago()
  → ServiceCompra.procesarPago(numero, valorPagado)
      → Verificar que valorPagado >= compra.precioTotal
      → Compra.estado = PAGADO
      → ProxyEmail.enviarCorreoConfirmacion(...)
      → Compra.estado = COMPLETADO
      → IntegradorSHC.publicar(compra)   ← AMQP a shc.compras.queue
      → IntegradorSAM.publicar(compra)   ← AMQP a sam.compras.queue
```

---

## 5. Implementación del Timer

```java
// ValidacionSNSTimerTask.java
public class ValidacionSNSTimerTask extends TimerTask {
    // Inyectado vía constructor (no @Autowired, Timer no es Spring-managed)
    private final ServiceCompra serviceCompra;

    @Override
    public void run() {
        serviceCompra.procesarValidacionesSNSPendientes();
    }
}

// En MSCompraSPSApplication.java o en un @PostConstruct de un @Component:
@PostConstruct
public void iniciarTimer() {
    long intervaloMs = Long.parseLong(env.getProperty("timer.intervalo.ms", "30000"));
    Timer timer = new Timer("ValidacionSNSTimer", true);  // daemon = true
    timer.schedule(
        new ValidacionSNSTimerTask(serviceCompra),
        5000L,      // delay inicial: 5 segundos
        intervaloMs // período de repetición
    );
}
```

---

## 6. Mock SNS — Comportamiento

El mock SNS (`ms-sns`) debe simular el comportamiento real:

1. Al recibir `POST /ws/sns/validar`:
   - Crea un registro `ValidacionSNS` con estado `EN_PROCESO`
   - Devuelve `{ idValidacion, estadoValidacion: "EN_PROCESO" }`

2. Internamente, el `ServiceSNS` usa su **propio** `java.util.Timer` para avanzar el estado:
   - Después de `SNS_TIEMPO_PROCESAMIENTO_MS` (ej. 15000 ms), cambia el estado a:
     - `APROBADO` (70% de los casos)
     - `RECHAZADO` (30% de los casos)
   - La decisión puede ser aleatoria: `Math.random() > 0.3 ? APROBADO : RECHAZADO`

3. El ProxySNS en ms-compra llama primero a `POST /ws/sns/validar` y guarda el `idValidacion` en `PlanCompra`. En los siguientes intentos del Timer de compra, llama a `GET /ws/sns/estado/{idValidacion}` para consultar el estado actual.

---

## 7. Dependencias Maven (ms-compra pom.xml)

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
        <artifactId>spring-boot-starter-mail</artifactId>
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
</dependencies>
```

Empaquetar como WAR (añadir `spring-boot-maven-plugin` con `<packaging>war</packaging>`).

---

## 8. Configuración RabbitMQ (RabbitMQConfig.java)

```java
// Declara exchanges y queues al arrancar
@Bean Queue queueSHC() { return new Queue("shc.compras.queue", true); }
@Bean Queue queueSAM() { return new Queue("sam.compras.queue", true); }
@Bean DirectExchange exchangeSHC() { return new DirectExchange("shc.exchange"); }
@Bean DirectExchange exchangeSAM() { return new DirectExchange("sam.exchange"); }
@Bean Binding bindingSHC(Queue queueSHC, DirectExchange exchangeSHC) {
    return BindingBuilder.bind(queueSHC).to(exchangeSHC).with("shc.compra.nueva");
}
@Bean Binding bindingSAM(Queue queueSAM, DirectExchange exchangeSAM) {
    return BindingBuilder.bind(queueSAM).to(exchangeSAM).with("sam.compra.nueva");
}
```

---

## 9. Seguridad y Validación JWT

`JwtFilter.java` debe:
1. Interceptar todas las peticiones excepto `POST /ws/compra/{numero}/pago` y rutas de actuator.
2. Para `POST /ws/compra/{numero}/pago`: validar header `X-Internal-Key` contra variable de entorno `INTERNAL_API_KEY`.
3. Para el resto: extraer y validar JWT del header `Authorization: Bearer <token>`.
4. Poner los claims del JWT en el `SecurityContextHolder`.

---

## 10. application.properties (ms-compra)

```properties
server.port=8082
spring.application.name=ms-compra
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

spring.datasource.url=${DB_COMPRA_URL}
spring.datasource.username=${DB_COMPRA_USER}
spring.datasource.password=${DB_COMPRA_PASS}

spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USER:guest}
spring.rabbitmq.password=${RABBITMQ_PASS:guest}

spring.mail.host=${MAIL_HOST}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USER}
spring.mail.password=${MAIL_PASS}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

app.jwt.secret=${JWT_SECRET}
app.internal.api.key=${INTERNAL_API_KEY}
app.sns.url=${SNS_URL:http://localhost:8083}
app.saludpay.url=${SALUDPAY_URL:http://localhost:5000}
app.compra.base.url=${COMPRA_BASE_URL:https://localhost}
app.timer.intervalo.ms=${TIMER_INTERVALO_MS:30000}
app.sns.max.intentos=${MAX_INTENTOS_SNS:10}
app.empresa.codigo=EMP-SPS-001
```

---

## 11. Integración con Otros Módulos

| Módulo destino | Cómo llama                      | Qué necesita              |
|----------------|---------------------------------|---------------------------|
| Módulo 3 Auth  | No llama directamente; valida JWT localmente con el mismo secret |
| Módulo 3 SaludPay | HTTP POST a puerto 5000     | `INTERNAL_API_KEY` compartida |
| Módulo 2 SNS   | HTTP GET/POST a puerto 8083     | Mock interno              |
| Módulo 4 SHC   | AMQP publish a RabbitMQ         | RabbitMQ en puerto 5672   |
| Módulo 4 SAM   | AMQP publish a RabbitMQ         | RabbitMQ en puerto 5672   |

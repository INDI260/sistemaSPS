# Módulo 4 — SAM (Agenda Médica) + SHC (Historias Clínicas)

**Artefactos a producir:** `MSSHC.war` · `MSSAM.war`  
**Carpetas del repositorio:** `ms-shc/` · `ms-sam/`  
**Infraestructura compartida:** RabbitMQ (SHCQueueServer + SAMQueueServer)  
**Ambos sistemas son receptores pasivos de información** que llega vía AMQP desde ms-compra cuando se completa una compra pagada.

---

## 1. Responsabilidades

| Componente   | Qué hace                                                                        |
|--------------|---------------------------------------------------------------------------------|
| ListenerSHC  | Consumidor AMQP de la cola `shc.compras.queue`; persiste historias clínicas     |
| ServiceSHC   | Lógica de creación de HistoriaClinica, PlanSHC y ServicioSHC                   |
| RepoSHC      | JPA repositories para las entidades de SHC                                      |
| WSSHC        | REST API de consulta (solo lectura) de historias clínicas                       |
| ListenerSAM  | Consumidor AMQP de la cola `sam.compras.queue`; persiste agendas médicas        |
| ServiceSAM   | Lógica de creación de AgendaMedica y CitaMedica                                 |
| RepoSAM      | JPA repositories para las entidades de SAM                                      |
| WSSSAM       | REST API de consulta (solo lectura) de agendas médicas                          |

## 2. Fuera del Alcance

- **No** enviar mensajes AMQP (solo consumir).
- **No** modificar el estado de compras (solo leer lo que llega).
- **No** implementar autenticación propia (validar JWT de SPS con el secret compartido).
- **No** comunicarse directamente con ms-compra, ms-auth, ms-saludpay ni ms-sns.
- **No** compartir base de datos con ningún otro módulo.
- La lógica de negocio compleja de agendamiento queda fuera del alcance; basta con almacenar los datos.

---

## 3. Infraestructura RabbitMQ

Ambos módulos dependen de RabbitMQ. La configuración de los exchanges y queues se define en `ms-compra` (Módulo 2), pero **cada módulo debe declararlos también** al arrancar (idempotente en RabbitMQ).

| Recurso AMQP          | SHC                      | SAM                      |
|-----------------------|--------------------------|--------------------------|
| Exchange (direct)     | `shc.exchange`           | `sam.exchange`           |
| Queue (durable)       | `shc.compras.queue`      | `sam.compras.queue`      |
| Routing key           | `shc.compra.nueva`       | `sam.compra.nueva`       |

---

## 4. Estructura de Carpetas

```
ms-shc/
├── pom.xml
└── src/main/
    ├── java/com/sps/shc/
    │   ├── MSShcApplication.java
    │   ├── config/
    │   │   ├── RabbitMQConfig.java
    │   │   └── JwtFilter.java          ← para proteger endpoints WS de consulta
    │   ├── controller/
    │   │   └── WSShcController.java
    │   ├── service/
    │   │   └── ServiceSHC.java
    │   ├── listener/
    │   │   └── ListenerSHC.java
    │   ├── repository/
    │   │   ├── RepoHistoriaClinica.java
    │   │   ├── RepoPlanSHC.java
    │   │   └── RepoServicioSHC.java
    │   ├── entity/
    │   │   ├── HistoriaClinica.java
    │   │   ├── PlanSHC.java
    │   │   └── ServicioSHC.java
    │   └── dto/
    │       ├── MensajeCompraDTO.java   ← mapea el mensaje AMQP entrante
    │       ├── PlanMensajeDTO.java
    │       └── ServicioMensajeDTO.java
    └── resources/
        └── application.properties

ms-sam/
├── pom.xml
└── src/main/
    ├── java/com/sps/sam/
    │   ├── MSSamApplication.java
    │   ├── config/
    │   │   ├── RabbitMQConfig.java
    │   │   └── JwtFilter.java
    │   ├── controller/
    │   │   └── WSSamController.java
    │   ├── service/
    │   │   └── ServiceSAM.java
    │   ├── listener/
    │   │   └── ListenerSAM.java
    │   ├── repository/
    │   │   ├── RepoAgendaMedica.java
    │   │   └── RepoCitaMedica.java
    │   ├── entity/
    │   │   ├── AgendaMedica.java
    │   │   └── CitaMedica.java
    │   └── dto/
    │       ├── MensajeCompraDTO.java   ← mapea el mensaje AMQP entrante
    │       └── ServicioMensajeDTO.java
    └── resources/
        └── application.properties
```

---

## 5. Formato del Mensaje AMQP Entrante

### SHC — `MensajeCompraDTO.java` (ms-shc)
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

### SAM — `MensajeCompraDTO.java` (ms-sam)
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

## 6. ListenerSHC — Lógica de Almacenamiento

```java
@Component
public class ListenerSHC {

    @RabbitListener(queues = "shc.compras.queue")
    public void recibirCompra(MensajeCompraDTO mensaje) {
        // Verificar idempotencia: si ya existe historia para esta compra, ignorar
        if (repoHistoriaClinica.existsByNumeroCompraOrigen(mensaje.getNumeroCompra())) {
            return;
        }
        serviceSHC.crearHistoriaClinica(mensaje);
    }
}
```

**`ServiceSHC.crearHistoriaClinica(mensaje)`** debe:
1. Crear `HistoriaClinica` con datos del cliente y `numeroCompraOrigen`
2. Para cada plan en `mensaje.planes`:
   - Crear `PlanSHC` asociado a la historia
   - Para cada servicio en `plan.serviciosMedicos`:
     - Crear `ServicioSHC` asociado al plan
3. Persistir todo en cascada

---

## 7. ListenerSAM — Lógica de Almacenamiento

```java
@Component
public class ListenerSAM {

    @RabbitListener(queues = "sam.compras.queue")
    public void recibirCompra(MensajeCompraDTO mensaje) {
        // Idempotencia
        if (repoAgendaMedica.existsByNumeroCompraOrigen(mensaje.getNumeroCompra())) {
            return;
        }
        serviceSAM.crearAgendaMedica(mensaje);
    }
}
```

**`ServiceSAM.crearAgendaMedica(mensaje)`** debe:
1. Crear `AgendaMedica` con datos del cliente y `numeroCompraOrigen`
2. Para cada servicio en `mensaje.serviciosMedicos`:
   - Crear `CitaMedica` con estado `PENDIENTE`, asociada a la agenda
3. Persistir todo en cascada

---

## 8. Endpoints REST de Consulta

### ms-shc (puerto 8084)

```
GET /ws/shc/historias/{cedula}
  → Requiere JWT válido en Authorization header
  → ServiceSHC.listarPorCedula(cedula)
  ← [ { id, cedulaCliente, nombreCliente, numeroCompraOrigen, fechaRegistro, planes: [...] } ]

GET /ws/shc/historias/compra/{numeroCompra}
  → Requiere JWT válido
  ← { id, cedulaCliente, nombreCliente, numeroCompraOrigen, planes: [...] }
```

### ms-sam (puerto 8085)

```
GET /ws/sam/agenda/{cedula}
  → Requiere JWT válido
  → ServiceSAM.listarPorCedula(cedula)
  ← [ { id, cedulaCliente, nombreCliente, numeroCompraOrigen, fechaRegistro, citas: [...] } ]

GET /ws/sam/agenda/compra/{numeroCompra}
  → Requiere JWT válido
  ← { id, cedulaCliente, nombreCliente, numeroCompraOrigen, citas: [...] }
```

---

## 9. Validación JWT

Ambos módulos deben incluir un `JwtFilter` idéntico al de ms-compra que:
1. Lea el `JWT_SECRET` de variable de entorno (mismo secret que usa ms-auth)
2. Valide el token en cada request a `/ws/shc/**` o `/ws/sam/**`
3. Rechace con 401 si el token es inválido o expirado

---

## 10. Dependencias Maven (ambos módulos)

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
</dependencies>
```

---

## 11. application.properties

### ms-shc
```properties
server.port=8084
spring.application.name=ms-shc
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=${DB_SHC_URL}
spring.datasource.username=${DB_SHC_USER}
spring.datasource.password=${DB_SHC_PASS}
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USER:guest}
spring.rabbitmq.password=${RABBITMQ_PASS:guest}
app.jwt.secret=${JWT_SECRET}
```

### ms-sam
```properties
server.port=8085
spring.application.name=ms-sam
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=${DB_SAM_URL}
spring.datasource.username=${DB_SAM_USER}
spring.datasource.password=${DB_SAM_PASS}
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USER:guest}
spring.rabbitmq.password=${RABBITMQ_PASS:guest}
app.jwt.secret=${JWT_SECRET}
```

---

## 12. RabbitMQConfig.java (igual en ambos módulos, ajustando nombres)

```java
@Configuration
public class RabbitMQConfig {

    // Para ms-shc:
    @Bean
    public Queue queueSHC() {
        return new Queue("shc.compras.queue", true);
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
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
```

---

## 13. Integración con Otros Módulos

| Módulo origen | Cómo interactúa          | Qué recibe                         |
|---------------|---------------------------|------------------------------------|
| Módulo 2      | AMQP via RabbitMQ         | Mensaje JSON de compra completada  |
| Módulo 3 Auth | JWT compartido (solo secret) | Valida requests entrantes a WS |

Este módulo **no llama** a ningún otro módulo. Solo escucha de RabbitMQ y expone consultas de solo lectura.

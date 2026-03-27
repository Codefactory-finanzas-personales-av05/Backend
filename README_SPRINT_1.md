# Gestion Financiera — Módulo de Autenticación
> Tecnología: Spring Boot 3.2 · Java 17 · Arquitectura Hexagonal

---

## Tabla de contenidos

1. [Descripción general](#descripción-general)
2. [Tipo de arquitectura](#tipo-de-arquitectura)
3. [Estructura del proyecto](#estructura-del-proyecto)
4. [Modelo de datos](#modelo-de-datos)
5. [Endpoints de la API](#endpoints-de-la-api)
6. [Flujo completo del sistema](#flujo-completo-del-sistema)
7. [Códigos de respuesta](#códigos-de-respuesta)
8. [Tecnologías utilizadas](#tecnologías-utilizadas)
9. [Cómo ejecutar el proyecto](#cómo-ejecutar-el-proyecto)
10. [Probar con Postman](#probar-con-postman)
11. [Base de datos H2 (modo desarrollo)](#base-de-datos-h2-modo-desarrollo)
12. [Migración a PostgreSQL (producción)](#migración-a-postgresql-producción)
13. [Variables de configuración](#variables-de-configuración)
14. [Decisiones de diseño](#decisiones-de-diseño)

---

## Descripción general

Este módulo implementa el sistema de autenticación para la aplicación de finanzas personales. Cubre el registro de usuarios, verificación de correo electrónico mediante código de 6 dígitos (2FA), inicio de sesión con JWT y actualización del perfil del cliente.

El proyecto fue desarrollado aplicando **Arquitectura Hexagonal** 
---

## Tipo de arquitectura

### Arquitectura Hexagonal (Ports and Adapters)

La arquitectura hexagonal —también conocida como *Ports and Adapters*, propuesta por Alistair Cockburn— organiza el sistema en tres zonas concéntricas:

```
┌─────────────────────────────────────────────────┐
│               INFRAESTRUCTURA                   │
│  (Web, JPA, Correo, Seguridad, Configuración)   │
│                                                 │
│    ┌───────────────────────────────────┐        │
│    │          APLICACIÓN               │        │
│    │  (Casos de uso, DTOs)             │        │
│    │                                   │        │
│    │    ┌─────────────────────┐        │        │
│    │    │      DOMINIO        │        │        │
│    │    │  (Modelos, Puertos) │        │        │
│    │    └─────────────────────┘        │        │
│    └───────────────────────────────────┘        │
└─────────────────────────────────────────────────┘
```

#### Las tres capas

| Capa | Responsabilidad | Ejemplo en este proyecto |
|---|---|---|
| **Dominio** | Modelos de negocio puros y definición de puertos. No depende de nada externo. | `Usuario`, `Cliente`, `CodigoVerificacion`, `CasoDeUsoAutenticacion` |
| **Aplicación** | Implementa los casos de uso orquestando los puertos. Contiene los DTOs. | `ServicioAutenticacion`, `PeticionLogin`, `RespuestaCliente` |
| **Infraestructura** | Adaptadores que conectan con el mundo externo (HTTP, BD, correo). | `ControladorAutenticacion`, `AdaptadorUsuario`, `AdaptadorCorreo` |

#### Puertos y Adaptadores

```
[Postman / Frontend]
        │
        ▼
[ControladorAutenticacion]  ← Adaptador de ENTRADA (Driving)
        │
        ▼
[CasoDeUsoAutenticacion]    ← Puerto de ENTRADA
        │
        ▼
[ServicioAutenticacion]     ← Implementación del caso de uso
        │
        ├──► [PuertoRepositorioUsuario]   ← Puerto de SALIDA
        │           │
        │           ▼
        │    [AdaptadorUsuario + JPA]     ← Adaptador de SALIDA (Driven)
        │
        ├──► [PuertoRepositorioCliente]
        │           │
        │           ▼
        │    [AdaptadorCliente + JPA]
        │
        └──► [PuertoCorreo]
                    │
                    ▼
             [AdaptadorCorreo]           ← (Por ahora imprime en consola)
```


---

## Estructura del proyecto

```
src/
└── main/
    ├── resources/
    │   └── application.yml
    └── java/com/finanzas/auth/
        │
        ├── FinanceAuthApplication.java          ← Punto de entrada
        │
        ├── dominio/                             ← Núcleo del negocio (sin dependencias externas)
        │   ├── modelo/
        │   │   ├── Usuario.java                 ← Modelo de dominio del usuario
        │   │   ├── Cliente.java                 ← Modelo de dominio del cliente
        │   │   └── CodigoVerificacion.java      ← Modelo del código 2FA
        │   └── puertos/
        │       ├── entrada/
        │       │   └── CasoDeUsoAutenticacion.java   ← Puerto de entrada (interfaz)
        │       └── salida/
        │           ├── PuertoRepositorioUsuario.java
        │           ├── PuertoRepositorioCliente.java
        │           ├── PuertoRepositorioCodigo.java
        │           └── PuertoCorreo.java
        │
        ├── aplicacion/                          ← Casos de uso + DTOs
        │   ├── casosdeuso/
        │   │   └── ServicioAutenticacion.java   ← Implementa CasoDeUsoAutenticacion
        │   └── dto/
        │       ├── peticion/
        │       │   ├── PeticionRegistro.java
        │       │   ├── PeticionVerificacion.java
        │       │   ├── PeticionLogin.java
        │       │   └── PeticionDescripcion.java
        │       └── respuesta/
        │           ├── RespuestaApi.java        ← Envoltorio genérico de respuestas
        │           ├── RespuestaRegistro.java
        │           ├── RespuestaLogin.java
        │           └── RespuestaCliente.java
        │
        ├── infraestructura/                     ← Todo lo externo
        │   ├── web/
        │   │   └── ControladorAutenticacion.java    ← Adaptador de entrada HTTP
        │   ├── persistencia/
        │   │   ├── entidad/
        │   │   │   ├── EntidadUsuario.java          ← Entidad JPA tabla usuarios
        │   │   │   ├── EntidadCliente.java          ← Entidad JPA tabla clientes
        │   │   │   └── EntidadCodigoVerificacion.java
        │   │   ├── repositorio/
        │   │   │   ├── RepositorioJpaUsuario.java   ← Spring Data JPA
        │   │   │   ├── RepositorioJpaCliente.java
        │   │   │   └── RepositorioJpaCodigo.java
        │   │   └── adaptador/
        │   │       ├── AdaptadorUsuario.java        ← Implementa PuertoRepositorioUsuario
        │   │       ├── AdaptadorCliente.java        ← Implementa PuertoRepositorioCliente
        │   │       ├── AdaptadorCodigo.java
        │   │       ├── ConvertidorUsuario.java      ← Convierte dominio ↔ entidad JPA
        │   │       └── ConvertidorCodigo.java
        │   ├── correo/
        │   │   └── AdaptadorCorreo.java             ← Implementa PuertoCorreo
        │   ├── seguridad/
        │   │   └── ConfiguracionSeguridad.java      ← Spring Security
        │   └── configuracion/
        │       └── ConfiguracionApp.java            ← CORS y beans generales
        │
        └── compartido/                          ← Utilidades compartidas entre capas
            ├── excepcion/
            │   ├── ExcepcionAutenticacion.java
            │   └── ManejadorExcepciones.java    ← Manejo global de errores
            └── utilidad/
                ├── UtilJwt.java                 ← Genera y valida tokens JWT
                └── GeneradorCodigo.java         ← Genera códigos de 6 dígitos
```

---

## Modelo de datos

### Diagrama de entidades

```
┌──────────────────────┐          ┌──────────────────────┐
│       usuarios       │          │       clientes       │
├──────────────────────┤          ├──────────────────────┤
│ id_usuario  (PK)     │          │ id_cliente  (PK)     │
│ correo               │◄────────►│ nombre               │
│ contrasena (hash)    │ id_cliente│ email               │
│ id_cliente  (FK)     │          │ descripcion (TEXT)   │
│ estado               │          └──────────────────────┘
│ correo_verificado    │
│ fecha_creacion       │
│ fecha_actualizacion  │
└──────────────────────┘
          │
          │ 1:N
          ▼
┌──────────────────────────┐
│   codigos_verificacion   │
├──────────────────────────┤
│ id             (PK)      │
│ usuario_id     (FK)      │
│ codigo         (6 dig.)  │
│ tipo           (ENUM)    │
│ fecha_expiracion         │
│ usado          (boolean) │
│ fecha_creacion           │
└──────────────────────────┘
```

### Tipos ENUM

**`usuarios.estado`**
```
PENDIENTE_VERIFICACION  → recién registrado, correo no verificado
ACTIVO                  → correo verificado, puede iniciar sesión
INACTIVO                → cuenta desactivada manualmente
BLOQUEADO               → bloqueado por intentos fallidos u otra razón
```

**`codigos_verificacion.tipo`**
```
VERIFICACION_CORREO      → código enviado al registrarse
RECUPERACION_CONTRASENA  → código para recuperar contraseña
DOS_FACTORES             → código para 2FA en login
```

---

## Endpoints de la API

**Base URL:** `http://localhost:8080/api/auth`

Todos los endpoints reciben y devuelven `application/json`.

---

### POST `/registro`

Registra un nuevo usuario y envía el código de verificación al correo.

**Request:**
```json
{
  "correo": "usuario@ejemplo.com",
  "contrasena": "Clave123!",
  "confirmarContrasena": "Clave123!"
}
```

> La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial (`@$!%*?&#._-`).

**Response 200 OK:**
```json
{
  "status": 200,
  "mensaje": "Registro exitoso. Revisa tu correo usuario@ejemplo.com para verificar tu cuenta.",
  "data": {
    "id": 1,
    "correo": "usuario@ejemplo.com",
    "correoVerificado": false,
    "mensaje": "Registro exitoso..."
  }
}
```

---

### POST `/verificar`

Valida el código de 6 dígitos enviado al correo.

**Request:**
```json
{
  "correo": "usuario@ejemplo.com",
  "codigo": "482931"
}
```

**Response 201 Created:**
```json
{
  "status": 201,
  "mensaje": "Correo verificado exitosamente. Ya puedes iniciar sesion!",
  "data": null
}
```

**Response 401 Unauthorized** (código incorrecto o expirado):
```json
{
  "status": 401,
  "mensaje": "El codigo es incorrecto o ya expiro",
  "data": null
}
```

---

### POST `/login`

Autentica al usuario y devuelve el token JWT junto con los datos del cliente.

**Request:**
```json
{
  "correo": "usuario@ejemplo.com",
  "contrasena": "Clave123!"
}
```

**Response 200 OK:**
```json
{
  "status": 200,
  "mensaje": "Inicio de sesion exitoso. Bienvenido!",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiraEn": 86400,
    "usuario": {
      "id": 1,
      "correo": "usuario@ejemplo.com",
      "estado": "ACTIVO",
      "cliente": {
        "idCliente": 1,
        "nombre": "",
        "email": "usuario@ejemplo.com",
        "descripcion": ""
      }
    }
  }
}
```

**Response 400 Bad Request** (correo o contraseña incorrectos):
```json
{
  "status": 400,
  "mensaje": "Correo o contrasena incorrectos",
  "data": null
}
```

---

### POST `/descripcion`

Recibe las credenciales del usuario y guarda la descripción en el perfil del cliente. Devuelve todos los datos del cliente **sin incluir la contraseña**.

**Request:**
```json
{
  "correo": "usuario@ejemplo.com",
  "contrasena": "Clave123!",
  "descripcion": "Desarrollador de software apasionado por las finanzas personales."
}
```

**Response 200 OK:**
```json
{
  "status": 200,
  "mensaje": "Descripcion guardada correctamente.",
  "data": {
    "idCliente": 1,
    "nombre": "",
    "email": "usuario@ejemplo.com",
    "descripcion": "Desarrollador de software apasionado por las finanzas personales.",
    "idUsuario": 1,
    "correo": "usuario@ejemplo.com",
    "estado": "ACTIVO"
  }
}
```

**Response 401 Unauthorized** (credenciales incorrectas):
```json
{
  "status": 401,
  "mensaje": "Usuario o contrasena incorrectos",
  "data": null
}
```

---

### POST `/reenviar-codigo?correo=xxx@yyy.com`

Reenvía el código de verificación si expiró o no llegó. El correo va como **query param**, no en el body.

**Response 200 OK:**
```json
{
  "status": 200,
  "mensaje": "Codigo de verificacion reenviado a usuario@ejemplo.com",
  "data": null
}
```

---

## Flujo completo del sistema

### Historia 1 — Registro y verificación

```
Cliente (Postman/Frontend)          Servidor                        Base de datos
        │                               │                                │
        │── POST /registro ────────────►│                                │
        │   {correo, contrasena}        │── ¿existe el correo? ─────────►│
        │                               │◄── No ────────────────────────│
        │                               │── Crear Cliente vacío ────────►│
        │                               │── Crear Usuario ──────────────►│
        │                               │── Generar código 6 dígitos     │
        │                               │── Guardar código ─────────────►│
        │                               │── Imprimir código en logs       │
        │◄── 200 {id, correo} ─────────│                                │
        │                               │                                │
        │   [usuario ve el código       │                                │
        │    en los logs de Spring]     │                                │
        │                               │                                │
        │── POST /verificar ───────────►│                                │
        │   {correo, codigo}            │── Buscar código activo ───────►│
        │                               │◄── Código encontrado ─────────│
        │                               │── ¿Está vigente? (15 min)      │
        │                               │── Marcar código como usado ───►│
        │                               │── Activar cuenta usuario ─────►│
        │◄── 201 ─────────────────────│                                │
```

### Historia 2 — Login

```
Cliente                             Servidor                        Base de datos
        │                               │                                │
        │── POST /login ───────────────►│                                │
        │   {correo, contrasena}        │── Buscar usuario por correo ──►│
        │                               │◄── Usuario encontrado ─────────│
        │                               │── Validar contraseña (BCrypt)  │
        │                               │── ¿correoVerificado = true?    │
        │                               │── ¿estado = ACTIVO?            │
        │                               │── Buscar datos del Cliente ───►│
        │                               │◄── Datos del cliente ──────────│
        │                               │── Generar token JWT             │
        │◄── 200 {token, cliente} ─────│                                │
        │                               │                                │
```

### Historia 3 — Guardar descripción

```
Cliente                             Servidor                        Base de datos
        │                               │                                │
        │── POST /descripcion ─────────►│                                │
        │   {correo, contrasena,        │── Buscar usuario ─────────────►│
        │    descripcion}               │◄── Usuario encontrado ─────────│
        │                               │── Validar contraseña (BCrypt)  │
        │                               │── Buscar cliente por idCliente►│
        │                               │── Actualizar descripcion ─────►│
        │◄── 200 {datos del cliente} ──│    (sin devolver contrasena)   │
        │                               │                                │
```

---

## Códigos de respuesta

| Código | Significado | Cuándo ocurre |
|---|---|---|
| `200 OK` | Éxito | Registro, login correcto, descripción guardada |
| `201 Created` | Creado | Verificación de correo exitosa |
| `400 Bad Request` | Error del cliente | Campos inválidos, correo/contraseña incorrectos en login |
| `401 Unauthorized` | No autorizado | Código de verificación incorrecto, credenciales incorrectas en /descripcion |
| `404 Not Found` | No encontrado | Perfil de cliente no existe |
| `409 Conflict` | Conflicto | Intentar registrar un correo que ya existe |
| `500 Internal Server Error` | Error del servidor | Error inesperado en el servidor |

---

## Tecnologías utilizadas

| Tecnología | Versión | Para qué se usa |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.2.3 | Framework base |
| Spring Security | 6.2 | Control de acceso y seguridad |
| Spring Data JPA | 3.2 | Acceso a base de datos |
| H2 Database | — | Base de datos en memoria para desarrollo |
| PostgreSQL | 14+ | Base de datos para producción |
| JJWT | 0.11.5 | Generación y validación de tokens JWT |
| Lombok | — | Reduce código repetitivo (getters, setters, builders) |
| BCrypt | — | Hasheo seguro de contraseñas |
| Maven | 3.9+ | Gestión de dependencias y build |

---

## Cómo ejecutar el proyecto

### Requisitos previos

- Java 17 instalado
- Maven 3.9+
- IDE: IntelliJ IDEA o VS Code con extensión Java

### Pasos

# Compilar y ejecutar
mvnw.cmd spring-boot:run

```

Si todo está bien, verás en la consola:
```
Started FinanceAuthApplication in 3.2 seconds
```

El servidor queda en: `http://localhost:8080`

---

## Probar con Postman

### Configuración inicial

1. Abrir Postman
2. Crear una colección llamada `Finance Auth API`
3. Crear un entorno con estas variables:

| Variable | Valor inicial |
|---|---|
| `base_url` | `http://localhost:8080` |
| `correo` | `tu@correo.com` |
| `token` | *(vacío, se llena automáticamente)* |

4. En el request de login, agregar en **Scripts → Post-response**:

```javascript
const json = pm.response.json();
if (json.data && json.data.accessToken) {
    pm.environment.set("token", json.data.accessToken);
    console.log("Token guardado correctamente");
}
```

### Orden de prueba recomendado

```
1. POST {{base_url}}/api/auth/registro
2. Ver el código en los logs de Spring Boot
3. POST {{base_url}}/api/auth/verificar
4. POST {{base_url}}/api/auth/login
5. POST {{base_url}}/api/auth/descripcion
```

---

## Base de datos H2 (modo desarrollo)

H2 es una base de datos que vive en memoria mientras el proyecto está corriendo. No requiere instalación. Se reinicia cada vez que se apaga el servidor.

**Acceso web:** `http://localhost:8080/h2-console`

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:finanzasdb` |
| User Name | `sa` |
| Password | *(vacío)* |



## Pruebas unitarias

El proyecto tiene cobertura estimada del **80%** distribuida en 4 archivos de prueba.

### Ejecutar las pruebas
```bash
# Correr todas las pruebas
mvn test

# Correr pruebas y generar reporte de cobertura
mvn test jacoco:report
```

El reporte de cobertura queda en `target/site/jacoco/index.html`. Ábrelo en el navegador para ver el detalle por clase.

### Archivos de prueba y qué cubren

| Archivo | Tipo | Pruebas | Qué cubre |
|---|---|---|---|
| `PruebasAutenticacion` | Integración | 3 | Flujo de registro y login con H2 real |
| `PruebasServicioAutenticacion` | Unitaria | 13 | Todos los casos de uso con mocks (Mockito) |
| `PruebasCodigoVerificacion` | Unitaria | 7 | Regla de negocio `estaVigente()` del dominio |
| `PruebasUtilJwt` | Unitaria | 6 | Generación y validación de tokens JWT |
| `PruebasGeneradorCodigo` | Unitaria | 4 | Generador de códigos de 6 dígitos |

### Diferencia entre pruebas de integración y unitarias

Las **pruebas unitarias** (`@ExtendWith(MockitoExtension.class)`) usan Mockito para simular los puertos de salida. No necesitan base de datos ni Spring. Son rápidas y prueban la lógica de forma aislada.

Las **pruebas de integración** (`@SpringBootTest`) levantan el contexto completo de Spring Boot con H2 en memoria. Son más lentas pero prueban el flujo de punta a punta.

### Casos probados en PruebasServicioAutenticacion

**Registro:** registro exitoso crea usuario y cliente, correo duplicado lanza CONFLICT, contraseña se hashea antes de guardar.

**Verificación:** código correcto activa la cuenta, código incorrecto lanza UNAUTHORIZED, código expirado lanza UNAUTHORIZED, correo ya verificado lanza BAD_REQUEST.

**Login:** login exitoso devuelve JWT y datos del cliente, correo inexistente lanza BAD_REQUEST, contraseña incorrecta lanza BAD_REQUEST, sin verificar lanza UNAUTHORIZED, cuenta bloqueada lanza UNAUTHORIZED.

**Descripción:** credenciales correctas actualiza el cliente, credenciales incorrectas lanza UNAUTHORIZED, correo inexistente lanza UNAUTHORIZED.

**Reenvío:** usuario pendiente genera nuevo código, correo ya verificado lanza BAD_REQUEST.


```

---

## Migración a PostgreSQL (producción)

Cuando el compañero de base de datos tenga PostgreSQL listo, hay que cambiar solo el archivo `application.yml`:

**Reemplazar el bloque de datasource:**

```yaml
# Quitar esto:
spring:
  autoconfigure:
    exclude: org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration
  datasource:
    url: jdbc:h2:mem:finanzasdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop

# Poner esto:
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/finanzas_db
    driver-class-name: org.postgresql.Driver
    username: postgres        # el que te dé tu compañero
    password: tu_password     # el que te dé tu compañero
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update        # crea las tablas automáticamente
    properties:
      hibernate:
        format_sql: true
```

**Cambiar la dependencia en `pom.xml`:**

```xml
<!-- Quitar: -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Poner: -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Datos de conexión que necesita el backend**


| Campo | Valor de ejemplo | Descripción |
|---|---|---|
| `Host` |	localhost |	IP o dominio del servidor PostgreSQL |
| `Puerto` |	5432 | Puerto por defecto de PostgreSQL |
| `Nombre BD` | 	finanzas_db |	Nombre de la base de datos a crear |
| `Usuario` |	finanzas_user |	Usuario con permisos sobre la BD |
| `Contraseña` |	(la que defina el compañero) |	Contraseña segura del usuario |


**Tabla `usuarios`:**

| Columna | Tipo | Restricciones |
|---|---|---|
| `id_usuario` | BIGSERIAL| PK, auto-increment |
| `correo` | VARCHAR(150) | NOT NULL, UNIQUE, Correo de login, no puede repetirse |
| `contrasena` | VARCHAR(255) | NOT NULL (viene hasheada con BCrypt) |
| `id_cliente` | BIGINT | NULL, FK → clientes, Referencia al perfil del cliente |
| `estado` | VARCHAR(50) | NOT NULL, PENDIENTE_VERIFICACION - ACTIVO - INACTIVO - BLOQUEADO |
| `correo_verificado` | BOOLEAN | NOT NULL, false hasta que el usuario verifique el correo |
| `fecha_creacion` | TIMESTAMP | NULL, Se llena automáticamente al crear el registro |
| `fecha_actualizacion` | TIMESTAMP | NULL, Se llena automáticamente al crear el registro |

**Tabla `codigos_verificacion`:**

| Columna | Tipo | Restricciones |
|---|---|---|
| `id` | BIGSERIAL|NOT NULL, PK, auto-increment |
| `usuario_id` | BIGINT |NOT NULL, FK → `usuarios.id`, Usuario al que pertenece este código |
| `codigo` | VARCHAR(10) | NOT NULL, 6 dígitos numéricos generados aleatoriamente |
| `tipo` | VARCHAR(50)| NOT NULL, `VERIFICACION_CORREO`, `RECUPERACION_CONTRASENA`, `DOS_FACTORES` |
| `fecha_expiracion` | TIMESTAMP | NOT NULL, Momento en que el código deja de ser válido (15 min) |
| `usado` | BOOLEAN | NOT NULL, false = código activo, true = ya fue utilizado |
| `fecha_creacion` | TIMESTAMP | NOT NULL, Momento en que se generó el código |

**Tabla `clientes`:**

| Columna | Tipo | Restricciones |
|---|---|---|
| `id_cliente` | BIGSERIAL | NOT NULL,PK, auto-increment |
| `nombre` | VARCHAR(150) | NULL, Nombre completo del cliente|
| `email` | VARCHAR(150) | NULL, Correo del cliente (puede diferir del correo de login) |
| `descripcion` | TEXT | NULL, Descripción libre del perfil del client |



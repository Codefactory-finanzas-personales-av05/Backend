
# Gestión Financiera — Documentación del Proyecto

> **Tecnología:** Spring Boot 3.2 · Java 17 · Arquitectura Hexagonal · PostgreSQL  

---

## Estado del proyecto

| Sprint | Descripción | Estado |
|---|---|---|
| **Sprint 1** | Módulo de autenticación (registro, verificación, login, perfil) | ✅ Completado |
| **Sprint 2** | *(próximamente)* | 🔜 Pendiente |
| **Sprint 3** | *(próximamente)* | 🔜 Pendiente |

---

# 🚀 SPRINT 1 — Módulo de Autenticación

---

## Tabla de contenidos — Sprint 1

1. [Descripción del sprint](#1-descripción-del-sprint)
2. [Historias de usuario implementadas](#2-historias-de-usuario-implementadas)
3. [Tipo de arquitectura](#3-tipo-de-arquitectura)
4. [Estructura del proyecto](#4-estructura-del-proyecto)
5. [Modelo de datos](#5-modelo-de-datos)
6. [Endpoints de la API](#6-endpoints-de-la-api)
7. [Flujo completo del sistema](#7-flujo-completo-del-sistema)
8. [Códigos de respuesta](#8-códigos-de-respuesta)
9. [Tecnologías utilizadas](#9-tecnologías-utilizadas)
10. [Cómo ejecutar el proyecto](#10-cómo-ejecutar-el-proyecto)
11. [Probar con Postman](#11-probar-con-postman)
12. [Base de datos H2 (desarrollo)](#12-base-de-datos-h2-desarrollo)
13. [Migración a PostgreSQL (producción)](#13-migración-a-postgresql-producción)
14. [Pruebas unitarias](#14-pruebas-unitarias)
15. [Variables de configuración](#15-variables-de-configuración)
16. [Integración con n8n (Envío de Correos)](#16-integración-con-n8n-envío-de-correos)
17. [Decisiones de diseño](#17-decisiones-de-diseño)

---

## 1. Descripción del sprint

El Sprint 1 implementa el módulo completo de autenticación para la aplicación de gestión financiera. Cubre el registro de usuarios, verificación de correo electrónico mediante código de 6 dígitos (2FA), inicio de sesión con JWT y actualización del perfil del cliente.

El proyecto fue desarrollado aplicando **Arquitectura Hexagonal (Ports and Adapters)** **Base de datos en desarrollo:** H2 en memoria (no requiere instalación).  
**Base de datos en producción:** PostgreSQL 14+.
**Orquestación de correos:** n8n.

---

## 2. Historias de usuario implementadas

| # | Historia | Endpoint | Resultado |
|---|---|---|---|
| HU-1 | El usuario puede registrarse con correo y contraseña | `POST /api/auth/registro` | `200` exitoso / `409` correo duplicado |
| HU-2 | El usuario puede verificar su correo con el código recibido | `POST /api/auth/verificar` | `201` exitoso / `401` código incorrecto |
| HU-3 | El usuario puede iniciar sesión con correo y contraseña | `POST /api/auth/login` | `200` con token / `400` credenciales incorrectas |
| HU-4 | El usuario puede guardar su descripción de perfil | `POST /api/auth/descripcion` | `200` con datos / `401` credenciales incorrectas |
| Extra | El usuario puede reenviar el código si expiró | `POST /api/auth/reenviar-codigo` | `200` reenviado |

---

## 3. Tipo de arquitectura

### Arquitectura Hexagonal (Ports and Adapters)

La arquitectura hexagonal, propuesta por Alistair Cockburn, organiza el sistema en tres zonas concéntricas. El objetivo principal es que el **dominio (lógica del negocio) no dependa de nada externo**.

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

#### Las cuatro capas del proyecto

| Capa | Carpeta | Responsabilidad |
|---|---|---|
| **Dominio** | `dominio/` | Modelos puros (`Usuario`, `Cliente`, `CodigoVerificacion`) y definición de puertos. Sin anotaciones de Spring ni JPA. |
| **Aplicación** | `aplicacion/` | Implementa los casos de uso (`ServicioAutenticacion`) y contiene los DTOs. |
| **Infraestructura** | `infraestructura/` | Adaptadores externos: controladores HTTP, entidades JPA, repositorios, webhooks (n8n), seguridad. |
| **Compartido** | `compartido/` | Utilidades transversales: manejo de excepciones, JWT, generador de códigos. |

#### Flujo de puertos y adaptadores

```
[Postman / Frontend]
        │
        ▼
[ControladorAutenticacion]  ← Adaptador de ENTRADA (Driving)
        │
        ▼
[CasoDeUsoAutenticacion]    ← Puerto de ENTRADA (interfaz)
        │
        ▼
[ServicioAutenticacion]     ← Implementación del caso de uso
        │
        ├──► [PuertoRepositorioUsuario]   ← Puerto de SALIDA (interfaz)
        │           └── [AdaptadorUsuario + JPA]     ← Adaptador de SALIDA
        │
        ├──► [PuertoRepositorioCliente]
        │           └── [AdaptadorCliente + JPA]
        │
        ├──► [PuertoRepositorioCodigo]
        │           └── [AdaptadorCodigo + JPA]
        │
        └──► [PuertoCorreo]
                    └── [AdaptadorN8n]    ← (Llamada HTTP a Webhook)
```

**Ventaja clave:** si mañana cambiamos de PostgreSQL a otro motor, o de n8n a AWS SES, solo se toca la capa de infraestructura. El dominio y los casos de uso no se modifican.

---

## 4. Estructura del proyecto

```
banco2026/
├── pom.xml
└── src/
    ├── main/
    │   ├── resources/
    │   │   └── application.yml                      ← Config general (H2 dev / PostgreSQL prod / webhooks)
    │   └── java/com/finanzas/auth/
    │       ├── FinanceAuthApplication.java           ← Punto de entrada
    │       │
    │       ├── dominio/                              ← Núcleo puro, sin dependencias externas
    │       │   ├── modelo/
    │       │   │   ├── Usuario.java                  ← Modelo de dominio del usuario
    │       │   │   ├── Cliente.java                  ← Modelo de dominio del cliente
    │       │   │   └── CodigoVerificacion.java       ← Modelo del código 2FA + regla estaVigente()
    │       │   └── puertos/
    │       │       ├── entrada/
    │       │       │   └── CasoDeUsoAutenticacion.java    ← Puerto de entrada (interfaz)
    │       │       └── salida/
    │       │           ├── PuertoRepositorioUsuario.java
    │       │           ├── PuertoRepositorioCliente.java
    │       │           ├── PuertoRepositorioCodigo.java
    │       │           └── PuertoCorreo.java
    │       │
    │       ├── aplicacion/                           ← Casos de uso + DTOs
    │       │   ├── casosdeuso/
    │       │   │   └── ServicioAutenticacion.java    ← Implementa CasoDeUsoAutenticacion
    │       │   └── dto/
    │       │       ├── peticion/
    │       │       │   ├── PeticionRegistro.java
    │       │       │   ├── PeticionVerificacion.java
    │       │       │   ├── PeticionLogin.java
    │       │       │   └── PeticionDescripcion.java
    │       │       └── respuesta/
    │       │           ├── RespuestaApi.java         ← Envoltorio genérico {status, mensaje, data}
    │       │           ├── RespuestaRegistro.java
    │       │           ├── RespuestaLogin.java       ← Incluye datos del Cliente
    │       │           └── RespuestaCliente.java     ← Sin contraseña
    │       │
    │       ├── infraestructura/                      ← Todo lo externo
    │       │   ├── web/
    │       │   │   └── ControladorAutenticacion.java ← Adaptador entrada HTTP
    │       │   ├── persistencia/
    │       │   │   ├── entidad/
    │       │   │   │   ├── EntidadUsuario.java       ← Tabla usuarios
    │       │   │   │   ├── EntidadCliente.java       ← Tabla clientes
    │       │   │   │   └── EntidadCodigoVerificacion.java
    │       │   │   ├── repositorio/
    │       │   │   │   ├── RepositorioJpaUsuario.java
    │       │   │   │   ├── RepositorioJpaCliente.java
    │       │   │   │   └── RepositorioJpaCodigo.java
    │       │   │   └── adaptador/
    │       │   │       ├── AdaptadorUsuario.java     ← Implementa PuertoRepositorioUsuario
    │       │   │       ├── AdaptadorCliente.java     ← Implementa PuertoRepositorioCliente
    │       │   │       ├── AdaptadorCodigo.java
    │       │   │       ├── ConvertidorUsuario.java   ← Convierte dominio ↔ entidad JPA
    │       │   │       └── ConvertidorCodigo.java
    │       │   ├── correo/
    │       │   │   └── AdaptadorN8n.java             ← Implementa PuertoCorreo (Petición HTTP a webhook)
    │       │   ├── seguridad/
    │       │   │   └── ConfiguracionSeguridad.java   ← Spring Security + BCrypt
    │       │   └── configuracion/
    │       │       └── ConfiguracionApp.java         ← CORS y RestTemplate
    │       │
    │       └── compartido/                           ← Utilidades compartidas
    │           ├── excepcion/
    │           │   ├── ExcepcionAutenticacion.java
    │           │   └── ManejadorExcepciones.java     ← Manejo global de errores
    │           └── utilidad/
    │               ├── UtilJwt.java                  ← Genera y valida tokens JWT
    │               └── GeneradorCodigo.java           ← Genera códigos numéricos seguros
    │
    └── test/
        ├── resources/
        │   └── application-test.yml                 ← Fuerza H2 en pruebas (PostgreSQL no se toca)
        └── java/com/finanzas/auth/
            ├── PruebasAutenticacion.java             ← Integración con @ActiveProfiles("test")
            ├── aplicacion/
            │   └── PruebasServicioAutenticacion.java ← Unitarias con Mockito (13 pruebas)
            ├── dominio/
            │   └── PruebasCodigoVerificacion.java    ← Unitarias del modelo (7 pruebas)
            └── compartido/
                ├── PruebasUtilJwt.java               ← Unitarias JWT (6 pruebas)
                └── PruebasGeneradorCodigo.java        ← Unitarias generador (4 pruebas)
```

---

## 5. Modelo de datos

### Diagrama de entidades

```
┌──────────────────────┐          ┌──────────────────────┐
│       clientes       │          │       usuarios       │
├──────────────────────┤          ├──────────────────────┤
│ id_cliente  (PK)     │◄─────────│ id_usuario  (PK)     │
│ nombre               │          │ correo       UNIQUE  │
│ email                │          │ contrasena   (hash)  │
│ descripcion (TEXT)   │          │ id_cliente   (FK)    │
└──────────────────────┘          │ estado       (ENUM)  │
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

### Detalle de tablas

**Tabla `clientes`** — perfil personal del usuario

| Columna | Tipo | Restricciones |
|---|---|---|
| `id_cliente` | BIGSERIAL | PK, auto-increment |
| `nombre` | VARCHAR(150) | NULL |
| `email` | VARCHAR(150) | NULL |
| `descripcion` | TEXT | NULL |

**Tabla `usuarios`** — credenciales de autenticación

| Columna | Tipo | Restricciones |
|---|---|---|
| `id_usuario` | BIGSERIAL | PK, auto-increment |
| `correo` | VARCHAR(150) | NOT NULL, UNIQUE |
| `contrasena` | VARCHAR(255) | NOT NULL — siempre hash BCrypt, nunca texto plano |
| `id_cliente` | BIGINT | NULL, FK → `clientes.id_cliente` |
| `estado` | VARCHAR(50) | NOT NULL — `PENDIENTE_VERIFICACION` / `ACTIVO` / `INACTIVO` / `BLOQUEADO` |
| `correo_verificado` | BOOLEAN | NOT NULL, default `false` |
| `fecha_creacion` | TIMESTAMP | Auto — se llena al crear |
| `fecha_actualizacion` | TIMESTAMP | Auto — se actualiza en cada cambio |

**Tabla `codigos_verificacion`** — códigos 2FA enviados al correo

| Columna | Tipo | Restricciones |
|---|---|---|
| `id` | BIGSERIAL | PK, auto-increment |
| `usuario_id` | BIGINT | NOT NULL, FK → `usuarios.id_usuario` |
| `codigo` | VARCHAR(10) | NOT NULL — 6 dígitos generados con SecureRandom |
| `tipo` | VARCHAR(50) | NOT NULL — `VERIFICACION_CORREO` / `RECUPERACION_CONTRASENA` / `DOS_FACTORES` |
| `fecha_expiracion` | TIMESTAMP | NOT NULL — 15 minutos después de la creación |
| `usado` | BOOLEAN | NOT NULL, default `false` |
| `fecha_creacion` | TIMESTAMP | NOT NULL |

> **Nota:** Las tablas se crean automáticamente al iniciar el proyecto (`ddl-auto: update`). No es necesario correr scripts SQL de creación.

---

## 6. Endpoints de la API

**Base URL:** `http://localhost:8080/api/auth`

Todos los endpoints reciben y devuelven `Content-Type: application/json`.

---

### POST `/registro`

Registra un nuevo usuario, crea su perfil de cliente y envía el código de verificación al correo a través del flujo de n8n.

**Request:**
```json
{
  "correo": "usuario@ejemplo.com",
  "contrasena": "Clave123!",
  "confirmarContrasena": "Clave123!"
}
```

> La contraseña requiere mínimo 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial (`@$!%*?&#._-`).

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

**Response 409 Conflict** (correo ya registrado):
```json
{
  "status": 409,
  "mensaje": "El correo ya esta registrado",
  "data": null
}
```

---

### POST `/verificar`

Valida el código de 6 dígitos enviado al correo del usuario.

**Request:**
```json
{
  "correo": "usuario@ejemplo.com",
  "codigo": "482931"
}
```

**Response 201 Created** (correcto):
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

Autentica al usuario y devuelve el token JWT junto con los datos del cliente. Si correo o contraseña son incorrectos devuelve `400` (según requerimiento del sprint).

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

Recibe las credenciales del usuario y guarda la descripción en su perfil de cliente. Devuelve todos los datos **sin incluir la contraseña**.

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

### POST `/reenviar-codigo?correo=usuario@ejemplo.com`

Reenvía el código si expiró o no llegó, activando nuevamente el webhook de n8n. El correo va como **query param**, no en el body.

**Response 200 OK:**
```json
{
  "status": 200,
  "mensaje": "Codigo de verificacion reenviado a usuario@ejemplo.com",
  "data": null
}
```

---

## 7. Flujo completo del sistema

### Historia 1 — Registro y verificación

```
Cliente (Postman/Frontend)          Servidor                        Base de datos / n8n
        │                               │                                │
        │── POST /registro ────────────►│                                │
        │   {correo, contrasena}        │── ¿existe el correo? ─────────►│ (BD)
        │                               │◄── No ────────────────────────│
        │                               │── Crear Cliente vacío ────────►│ (BD)
        │                               │── Crear Usuario ──────────────►│ (BD)
        │                               │── Generar código 6 dígitos     │
        │                               │── Guardar código ─────────────►│ (BD)
        │                               │── Enviar Webhook a n8n ───────►│ (n8n)
        │◄── 200 {id, correo} ─────────│                                │
        │                               │                                │
        │   [revisar la bandeja de      │                                │
        │    entrada del correo]        │                                │
        │                               │                                │
        │── POST /verificar ───────────►│                                │
        │   {correo, codigo}            │── Buscar código activo ───────►│ (BD)
        │                               │◄── Código encontrado ──────────│
        │                               │── ¿estaVigente()? (15 min)     │
        │                               │── Marcar código como usado ───►│ (BD)
        │                               │── Activar cuenta (ACTIVO) ────►│ (BD)
        │◄── 201 ──────────────────────│                                │
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
        │◄── 200 {datos sin contrasena}─│                                │
```

---

## 8. Códigos de respuesta

| Código HTTP | Significado | Cuándo ocurre en este proyecto |
|---|---|---|
| `200 OK` | Éxito | Registro, login correcto, descripción guardada |
| `201 Created` | Creado | Verificación de correo exitosa |
| `400 Bad Request` | Error del cliente | Campos inválidos, correo/contraseña incorrectos en login |
| `401 Unauthorized` | No autorizado | Código incorrecto, credenciales incorrectas en `/descripcion` |
| `404 Not Found` | No encontrado | Perfil de cliente no existe |
| `409 Conflict` | Conflicto | Intentar registrar un correo que ya existe |
| `500 Internal Server Error` | Error del servidor | Error inesperado no controlado |

---

## 9. Tecnologías utilizadas

| Tecnología | Versión | Para qué se usa |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.2.3 | Framework base |
| Spring Security | 6.2 | Control de acceso, BCrypt |
| Spring Data JPA | 3.2 | Acceso a base de datos sin SQL manual |
| n8n | — | Orquestador de flujos para envío de correos (Webhook) |
| H2 Database | — | BD en memoria para desarrollo y pruebas |
| PostgreSQL | 14+ | BD para producción |
| JJWT | 0.11.5 | Generación y validación de tokens JWT |
| Lombok | — | Reduce código repetitivo (getters, setters, builders) |
| BCrypt | — | Hasheo seguro de contraseñas (factor 12) |
| Mockito | — | Simulación de dependencias en pruebas unitarias |
| JaCoCo | 0.8.11 | Reporte de cobertura de pruebas |
| Maven | 3.9+ | Gestión de dependencias y build |

---

## 10. Cómo ejecutar el proyecto

### Requisitos previos

- Java 17 instalado
- Maven 3.9+
- IDE: IntelliJ IDEA o VS Code con extensión Java

### Pasos

```bash

# Compilar y ejecutar
./mvnw spring-boot:run

# En Windows:
mvnw.cmd spring-boot:run
```

Si todo está bien, en la consola aparece:
```
Started FinanceAuthApplication in 3.2 seconds
```

El servidor queda disponible en `http://localhost:8080`.

---

## 11. Probar con Postman

### Configuración inicial

1. Abrir Postman y crear una colección llamada `Finance Auth API`
2. Crear un entorno con estas variables:

| Variable | Valor inicial |
|---|---|
| `base_url` | `http://localhost:8080` |
| `correo` | `tu@correo.com` |
| `token` | *(vacío, se llena automáticamente)* |

3. En el request de login, agregar en **Scripts → Post-response**:

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
2. Revisar el correo electrónico para obtener el código (vía n8n)
3. POST {{base_url}}/api/auth/verificar
4. POST {{base_url}}/api/auth/login         ← el token se guarda automáticamente
5. POST {{base_url}}/api/auth/descripcion
```

---

## 12. Base de datos H2 (desarrollo)

H2 es una base de datos que vive en memoria mientras el proyecto está corriendo. No requiere instalación y se reinicia cada vez que se apaga el servidor.

**Acceso web:** `http://localhost:8080/h2-console`

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:finanzasdb` |
| User Name | `sa` |
| Password | *(vacío)* |

**Consultas útiles:**

```sql
-- Ver todos los usuarios
SELECT * FROM USUARIOS;

-- Ver el código de verificación generado
SELECT * FROM CODIGOS_VERIFICACION;

-- Ver los clientes y su perfil
SELECT * FROM CLIENTES;

-- Ver usuario con su cliente (join)
SELECT u.ID_USUARIO, u.CORREO, u.ESTADO, u.CORREO_VERIFICADO,
       c.ID_CLIENTE, c.NOMBRE, c.DESCRIPCION
FROM USUARIOS u
LEFT JOIN CLIENTES c ON u.ID_CLIENTE = c.ID_CLIENTE;
```

---

## 13. Migración a PostgreSQL (producción)

Cuando el compañero de base de datos tenga PostgreSQL configurado, realizar estos dos cambios:

### Cambio 1 — `application.yml`

Comentar el bloque de H2 y descomentar el de PostgreSQL:

```yaml
# DESARROLLO (H2) — comentar esto al pasar a producción:
# datasource:
#   url: jdbc:h2:mem:finanzasdb;DB_CLOSE_DELAY=-1
#   driver-class-name: org.h2.Driver
#   username: sa

# PRODUCCIÓN (PostgreSQL) — descomentar esto:
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/finanzas_db
    driver-class-name: org.postgresql.Driver
    username: postgres           # el que entregue el compañero de BD
    password: tu_password        # el que entregue el compañero de BD
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update           # crea las tablas automáticamente
    properties:
      hibernate:
        format_sql: true
```

### Cambio 2 — `pom.xml`

Descomentar el driver de PostgreSQL (el de H2 permanece en `scope: test` para que las pruebas sigan funcionando):

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

> **Importante:** H2 se mantiene en `scope: test` aunque en producción se use PostgreSQL. Esto garantiza que las pruebas unitarias e integración siempre corran con H2 en memoria, sin tocar la BD real.

### Datos que entrega el compañero de BD

| Campo | Descripción |
|---|---|
| Host | IP o nombre del servidor PostgreSQL |
| Puerto | Por defecto `5432` |
| Nombre de la BD | `finanzas_db` (o el que defina) |
| Usuario | Usuario con permisos sobre la BD |
| Contraseña | Contraseña del usuario |

### Script de creación (ejecutar en PostgreSQL)

```sql
CREATE DATABASE finanzas_db WITH ENCODING = 'UTF8';

CREATE USER finanzas_user WITH PASSWORD 'ContrasenaSegura123!';

GRANT ALL PRIVILEGES ON DATABASE finanzas_db TO finanzas_user;

\c finanzas_db
GRANT ALL ON SCHEMA public TO finanzas_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO finanzas_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO finanzas_user;
```

Las tablas se crean automáticamente al levantar el proyecto la primera vez.

---

## 14. Pruebas unitarias

El proyecto tiene una cobertura estimada del **80%**, distribuida en 5 archivos de prueba.

### Ejecutar las pruebas

```bash
# Correr todas las pruebas
mvn test

# Correr pruebas y generar reporte de cobertura
mvn test jacoco:report
```

El reporte de cobertura queda en `target/site/jacoco/index.html`. Abrirlo en el navegador para ver el detalle por clase y método.

### Configuración de pruebas con PostgreSQL

Las pruebas de integración usan `@ActiveProfiles("test")`, que activa el archivo `src/test/resources/application-test.yml`. Este archivo fuerza el uso de H2 en memoria durante los tests, **sin importar si en producción se usa PostgreSQL**. Esto garantiza que:

- Las pruebas corren siempre igual en cualquier entorno.
- No se requiere tener PostgreSQL instalado para correr los tests.
- La BD real de producción nunca se toca durante las pruebas.

### Archivos de prueba y qué cubren

| Archivo | Tipo | Pruebas | Qué cubre |
|---|---|---|---|
| `PruebasAutenticacion` | Integración | 3 | Flujo completo de registro y login con H2, usando `@ActiveProfiles("test")` |
| `PruebasServicioAutenticacion` | Unitaria | 13 | Todos los casos de uso con Mockito — sin BD, sin Spring |
| `PruebasCodigoVerificacion` | Unitaria | 7 | Regla de negocio `estaVigente()` del modelo de dominio |
| `PruebasUtilJwt` | Unitaria | 6 | Generación, validación y extracción de tokens JWT |
| `PruebasGeneradorCodigo` | Unitaria | 4 | Generador de códigos numéricos de 6 dígitos |

**Total: 33 pruebas**

### Diferencia entre tipos de prueba

Las **pruebas unitarias** (`@ExtendWith(MockitoExtension.class)`) simulan los puertos de salida con Mockito. No necesitan base de datos ni levantan Spring. Son rápidas y prueban la lógica de cada caso de uso de forma aislada.

Las **pruebas de integración** (`@SpringBootTest + @ActiveProfiles("test")`) levantan el contexto completo de Spring con H2 en memoria. Son más lentas pero verifican el flujo de punta a punta.

### Casos cubiertos en `PruebasServicioAutenticacion`

**Registro:** registro exitoso crea usuario y cliente, correo duplicado lanza CONFLICT, contraseña se hashea antes de guardar.

**Verificación:** código correcto activa la cuenta, código incorrecto lanza UNAUTHORIZED, código expirado lanza UNAUTHORIZED, correo ya verificado lanza BAD_REQUEST.

**Login:** login exitoso devuelve JWT y datos del cliente, correo inexistente lanza BAD_REQUEST, contraseña incorrecta lanza BAD_REQUEST, sin verificar lanza UNAUTHORIZED, cuenta bloqueada lanza UNAUTHORIZED.

**Descripción:** credenciales correctas actualiza el cliente, credenciales incorrectas lanza UNAUTHORIZED, correo inexistente lanza UNAUTHORIZED.

**Reenvío:** usuario pendiente genera nuevo código, correo ya verificado lanza BAD_REQUEST.

---

## 15. Variables de configuración

Todas las variables están en `src/main/resources/application.yml`:

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `server.port` | `8080` | Puerto del servidor |
| `app.jwt.secret` | `MiClaveSecreta...` | Clave para firmar los JWT — cambiar en producción |
| `app.jwt.expiration-ms` | `86400000` | Expiración del JWT (24 horas en ms) |
| `app.verificacion.minutos-expiracion` | `15` | Minutos antes de que el código de verificación expire |
| `app.n8n.webhook-url` | `http://localhost:5678/webhook/correo` | URL del Webhook de n8n para envío de correos |
| `app.frontend-url` | `http://localhost:3000` | URL del frontend (usada en CORS) |

---

## 16. Integración con n8n (Envío de Correos)

El envío de correos electrónicos (códigos de verificación) está desacoplado del backend mediante la plataforma de automatización **n8n**. 

Cuando se registra un usuario o se solicita un reenvío, el adaptador de infraestructura del backend realiza una petición HTTP `POST` al Webhook de n8n.

### Payload enviado a n8n
El backend envía la siguiente estructura JSON en el cuerpo de la petición:

```json
{
  "email": "usuario@ejemplo.com",
  "codigo": "123456",
  "asunto": "Tu código de verificación",
  "tipo": "VERIFICACION_CORREO"
}
```
*n8n recibe este payload, formatea el correo (mediante un nodo de HTML/Template) y lo despacha utilizando un nodo de SMTP (como Gmail, SendGrid, etc).*

---

## 17. Decisiones de diseño

**¿Por qué el dominio no tiene anotaciones JPA?** En arquitectura hexagonal el dominio debe ser independiente de cualquier framework. Por eso existen `Usuario.java` (dominio puro) y `EntidadUsuario.java` (con JPA en infraestructura). Los convertidores se encargan de traducir entre ambos.

**¿Por qué desacoplar el correo con n8n?** Permite cambiar el proveedor de correo, diseñar plantillas visuales o agregar lógica condicional (ej. alertas de fallos) directamente en el flujo visual de n8n sin tener que recompilar o modificar el código del backend en Java.

**¿Por qué BCrypt con factor 12?** Es el estándar recomendado para hashear contraseñas. Factor 12 ofrece un buen balance entre seguridad y velocidad de procesamiento.

**¿Por qué JWT sin estado (stateless)?** Configuramos `SessionCreationPolicy.STATELESS` para que el servidor no guarde sesiones. Cada request lleva su token. Esto hace la API más escalable.

**¿Por qué el código de verificación es numérico de 6 dígitos?** Es el estándar más reconocible para usuarios finales. Se genera con `SecureRandom` (criptográficamente seguro, a diferencia de `Random`).

**¿Por qué el login devuelve 400 y no 401 cuando las credenciales son incorrectas?** Según los requerimientos del sprint, el endpoint de login debe retornar `400 Bad Request` para credenciales incorrectas. Normalmente en REST se usaría `401`, pero se respetó la especificación.

**¿Por qué H2 se mantiene en `scope: test` aunque se use PostgreSQL en producción?** Para que las pruebas siempre corran en cualquier entorno sin necesitar una BD instalada. El `application-test.yml` fuerza H2 cuando el perfil `test` está activo.

---

---

---

# 🔜 SPRINT 2 — *(próximamente)*

> Esta sección se completará al iniciar el Sprint 2.

---

# 🔜 SPRINT 3 — *(próximamente)*

> Esta sección se completará al iniciar el Sprint 3.

---

*Documentación del proyecto — Fabrica escuela 20*
```
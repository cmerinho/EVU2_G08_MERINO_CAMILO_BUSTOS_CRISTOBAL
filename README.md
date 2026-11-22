# Sabor Gourmet – API REST de reservas

Evaluación Unidad 2 – Desarrollo de Software Web II (IPSS)
Grupo 08: Camilo Meriño · Cristóbal Bustos

API REST para gestionar clientes, mesas y reservas del restaurante "Sabor Gourmet".
Construida con Spring Boot y arquitectura Controller-Service-Repository (CSR).
Los datos se guardan en el archivo `datos.json` (sin base de datos).

## Requisitos

- Java 21 (JDK)
- No es necesario instalar Maven: el proyecto incluye Maven Wrapper (`mvnw` / `mvnw.cmd`).
- Postman, para probar los endpoints.

## Ejecución

1. Clonar el repositorio y entrar a la carpeta del proyecto:

   ```bash
   git clone https://github.com/cmerinho/EVU2_G08_MERINO_CAMILO_BUSTOS_CRISTOBAL.git
   cd EVU2_G08_MERINO_CAMILO_BUSTOS_CRISTOBAL
   ```

2. Levantar la aplicación **desde la raíz del proyecto** (donde está `pom.xml`):

   | Terminal | Comando |
   |---|---|
   | Git Bash / Linux / macOS | `./mvnw spring-boot:run` |
   | PowerShell / CMD | `.\mvnw.cmd spring-boot:run` |

3. La API queda disponible en `http://localhost:8080` cuando aparece el mensaje `Tomcat started on port 8080`.

4. Para detenerla: `Ctrl + C`.

## Persistencia de datos

- Los datos se leen y guardan en `datos.json`, en la raíz del proyecto (configurado en `src/main/resources/application.properties` con `app.datos.ruta=datos.json`).
- La ruta es relativa a la carpeta desde donde se ejecuta la aplicación. Por eso debe ejecutarse desde la raíz del proyecto.
- Si el archivo no existe, la aplicación lo crea vacío.
- Los datos se conservan al detener y volver a iniciar la aplicación.
- El archivo incluido trae datos de ejemplo: 3 clientes, 4 mesas (una inactiva) y 3 reservas (una cancelada).

## Pruebas con Postman

1. Abrir Postman → **Import** → seleccionar `postman/EVU2_Sabor_Gourmet.postman_collection.json`.
2. La colección usa la variable `baseUrl` (`http://localhost:8080`), definida en la propia colección.
3. Con la aplicación levantada, ejecutar los requests de cada carpeta: Clientes, Mesas, Reservas y Disponibilidad.

## Endpoints

### Clientes – `/api/clientes`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/clientes` | Lista todos los clientes | 200 | – |
| GET | `/api/clientes/{id}` | Obtiene un cliente | 200 | 404 |
| POST | `/api/clientes` | Crea un cliente | 201 | 400 |
| PUT | `/api/clientes/{id}` | Actualiza un cliente | 200 | 400, 404 |
| DELETE | `/api/clientes/{id}` | Elimina un cliente sin reservas | 204 | 404, 409 |

### Mesas – `/api/mesas`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/mesas` | Lista todas las mesas | 200 | – |
| GET | `/api/mesas/{id}` | Obtiene una mesa | 200 | 404 |
| POST | `/api/mesas` | Crea una mesa (número único) | 201 | 400, 409 |
| PUT | `/api/mesas/{id}` | Actualiza una mesa (incluye activarla o desactivarla) | 200 | 400, 404, 409 |
| DELETE | `/api/mesas/{id}` | Elimina una mesa sin reservas | 204 | 404, 409 |

### Reservas – `/api/reservas`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/reservas` | Lista todas las reservas | 200 | – |
| GET | `/api/reservas/{id}` | Obtiene una reserva | 200 | 404 |
| POST | `/api/reservas` | Crea una reserva | 201 | 400, 404, 409 |
| PUT | `/api/reservas/{id}` | Modifica una reserva | 200 | 400, 404, 409 |
| PATCH | `/api/reservas/{id}/cancelar` | Cancela una reserva | 200 | 404, 409 |

### Disponibilidad

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/mesas/disponibles?fecha=2026-12-05&hora=20:00&personas=4` | Mesas disponibles para esa fecha, hora y cantidad de personas | 200 | 400 |

### Cancelación de reservas

- **Método y ruta:** `PATCH /api/reservas/{id}/cancelar` (sin cuerpo).
- La reserva **no se elimina**: su estado cambia de `ACTIVA` a `CANCELADA` y queda como historial.
- Una reserva cancelada deja de ocupar la mesa, por lo que ese horario vuelve a estar disponible.
- Cancelar una reserva que ya estaba cancelada responde 409.

## Reglas de negocio

- Cada reserva ocupa la mesa durante 120 minutos (una reserva a las 20:00 ocupa hasta las 22:00).
- No se permiten reservas activas superpuestas en la misma mesa (409).
- No se puede reservar una mesa inactiva ni superar su capacidad (400).
- El cliente y la mesa de una reserva deben existir (404).
- No se puede repetir el número de mesa (409).
- No se puede eliminar un cliente o una mesa que tenga reservas (409).

## Ejemplos de cuerpo (JSON)

Cliente:

```json
{
  "rut": "12.345.678-9",
  "nombre": "Ana Pérez",
  "email": "ana.perez@correo.cl",
  "telefono": "+56912345678"
}
```

Mesa:

```json
{
  "numero": 1,
  "capacidad": 4,
  "ubicacion": "Terraza",
  "activa": true
}
```

Reserva (fecha `AAAA-MM-DD`, hora `HH:mm`):

```json
{
  "clienteId": 1,
  "mesaId": 1,
  "fecha": "2026-12-05",
  "hora": "20:00",
  "cantidadPersonas": 2
}
```

## Formato de errores

Todos los errores responden con el mismo formato, sin trazas internas:

```json
{
  "fecha": "2026-11-22T12:00:00",
  "estado": 404,
  "error": "Not Found",
  "mensaje": "La mesa con id 99 no existe en los registros.",
  "ruta": "/api/mesas/99",
  "detalles": {}
}
```

En errores de validación (400), `detalles` indica el campo y el problema, por ejemplo `"nombre": "El nombre es obligatorio"`.

## Estructura del proyecto

```
src/main/java/cl/ipss/saborgourmet/
├── controller/   Reciben las solicitudes HTTP y devuelven ResponseEntity
├── service/      Reglas de negocio y validaciones
├── repository/   Lectura y escritura de datos.json (AlmacenJson + repositorios)
├── model/        Cliente, Mesa, Reserva, EstadoReserva, Datos
└── exception/    Excepciones y manejador global de errores
```

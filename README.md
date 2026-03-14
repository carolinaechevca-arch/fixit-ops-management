# FixIt Ops Management API

Sistema de gestión operativa para la asignación inteligente de tareas de mantenimiento basado en **Arquitectura Hexagonal**, Domain Driven Design (DDD) y **SOLID**.

---

##   Arquitectura del Sistema

El proyecto sigue el patrón de **Puertos y Adaptadores**:

- **Domain:** Contiene la lógica de negocio pura, entidades, enums y servicios de dominio. No tiene dependencias de frameworks.
- **Application:** Orquesta los casos de uso a través de Puertos de Entrada (`in`) y define los contratos de persistencia en Puertos de Salida (`out`).
- **Infrastructure:** Implementaciones concretas de los adaptadores (JPA, REST, Mappers, Configuraciones de Spring).

---

## ️ Tecnologías Utilizadas

- **Java 21** & **Spring Boot 3.4.3**
- **Spring Data JPA** (Persistencia)
- **MapStruct** (Mapeo eficiente de objetos)
- **Lombok** (Reducción de código boilerplate)
- **OpenAPI 3 / Swagger** (Documentación interactiva)

---

##  Documentación de la API

###  Gestión de Técnicos (`/api/v1/technicians`)

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/v1/technicians` | Registra un nuevo técnico con su categoría (JUNIOR, SEMI_SENIOR, SENIOR, MASTER). |

```bash
curl -X 'POST' \
  'http://localhost:8080/api/v1/technicians' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
    "dni": "string",
    "name": "string",
    "category": "string"
  }'
```

---

###  Gestión de Tareas (`/api/v1/tasks`)

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/v1/tasks` | Crea una tarea y la asigna automáticamente por jerarquía. |
| `GET` | `/api/v1/tasks` | Lista todas las tareas registradas. |
| `GET` | `/api/v1/tasks/{id}` | Obtiene el detalle de una tarea específica. |
| `DELETE` | `/api/v1/tasks/{id}` | Elimina una tarea (Restringido a tareas PENDING). |
| `POST` | `/api/v1/tasks/{id}/assign-urgent` | Asigna manualmente una tarea urgente al mejor Master disponible. |
| `POST` | `/api/v1/tasks/auto-assign/urgent` | Procesa y asigna todas las tareas urgentes pendientes en la cola. |

```bash
# Crear tarea
curl -X 'POST' \
  'http://localhost:8080/api/v1/tasks' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "string",
    "description": "string",
    "priority": "string"
  }'

# Listar todas las tareas
curl -X 'GET' \
  'http://localhost:8080/api/v1/tasks' \
  -H 'accept: */*'

# Obtener tarea por ID
curl -X 'GET' \
  'http://localhost:8080/api/v1/tasks/1' \
  -H 'accept: */*'

# Eliminar tarea por ID
curl -X 'DELETE' \
  'http://localhost:8080/api/v1/tasks/1' \
  -H 'accept: */*'

# Asignar tarea urgente manualmente
curl -X 'POST' \
  'http://localhost:8080/api/v1/tasks/1/assign-urgent' \
  -H 'accept: */*' \
  -d ''

# Auto-asignar todas las tareas urgentes
curl -X 'POST' \
  'http://localhost:8080/api/v1/tasks/auto-assign/urgent' \
  -H 'accept: */*' \
  -d ''
```

---

##  Reglas de Asignación

El sistema aplica las siguientes restricciones de capacidad por puntos:

| Categoría | Capacidad máxima |
| :--- | :--- |
| **JUNIOR** | 8 puntos |
| **SEMI_SENIOR** | 13 puntos |
| **SENIOR** | 21 puntos |
| **MASTER** | Ilimitada (foco en tareas URGENT) |

> **Lógica de selección:** Para tareas estándar, el sistema prioriza el orden `JUNIOR → SEMI_SENIOR → SENIOR`. Para tareas **URGENTES**, se selecciona al Master con menor carga de tareas urgentes actuales; en caso de empate, se elige uno aleatoriamente.

---

##  Acceso a Swagger UI

Para probar los endpoints de manera interactiva, inicia la aplicación y accede a:

 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

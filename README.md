# FixIt Ops Management API

Sistema de gestión operativa para la asignación inteligente de tareas de mantenimiento basado en **Arquitectura Hexagonal**,  (Domain Driven Design) y **SOLID**.

---

##  Arquitectura del Sistema
El proyecto sigue el patrón de **Puertos y Adaptadores**:
* **Domain:** Contiene la lógica de negocio pura, entidades, enums y servicios de dominio. No tiene dependencias de frameworks.
* **Application:** Orquesta los casos de uso a través de Puertos de Entrada (`in`) y define los contratos de persistencia en Puertos de Salida (`out`).
* **Infrastructure:** Implementaciones concretas de los adaptadores (JPA, REST, Mappers, Configuraciones de Spring).

---

## Tecnologías Utilizadas
* **Java 21** & **Spring Boot 3.4.3**
* **Spring Data JPA** (Persistencia)
* **MapStruct** (Mapeo eficiente de objetos)
* **Lombok** (Reducción de código boilerplate)
* **OpenAPI 3 / Swagger** (Documentación interactiva)

---

##  Documentación de la API

###  Gestión de Técnicos (`/api/v1/technicians`)
| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/v1/technicians` | Registra un nuevo técnico con su categoría (JUNIOR, SEMI_SENIOR, SENIOR, MASTER). |

### 🛠️ Gestión de Tareas (`/api/v1/tasks`)
| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/v1/tasks` | Crea una tarea y la asigna automáticamente por jerarquía. |
| `GET` | `/api/v1/tasks` | Lista todas las tareas registradas. |
| `GET` | `/api/v1/tasks/{id}` | Obtiene el detalle de una tarea específica. |
| `DELETE` | `/api/v1/tasks/{id}` | Elimina una tarea (Restringido a tareas PENDING). |
| `POST` | `/api/v1/tasks/{id}/assign-urgent` | Asigna manualmente una tarea urgente al mejor Master disponible. |
| `POST` | `/api/v1/tasks/auto-assign/urgent` | Procesa y asigna todas las tareas urgentes pendientes en la cola. |

---

## ⚖️ Reglas de Asignación e Inteligencia
El sistema aplica las siguientes restricciones de capacidad por puntos:
* **JUNIOR:** Máximo **8 puntos**.
* **SEMI_SENIOR:** Máximo **13 puntos**.
* **SENIOR:** Máximo **21 puntos**.
* **MASTER:** Capacidad **Ilimitada** (Foco en tareas URGENT).

> **Lógica de Selección:** Para tareas estándar, el sistema prioriza el orden JUNIOR -> SEMI_SENIOR -> SENIOR. Para tareas **URGENTES**, se selecciona al Master con menor carga de tareas urgentes actuales; en caso de empate, se elige uno aleatoriamente.

---

## 🔗 Acceso a Swagger UI
Para probar los endpoints de manera interactiva, inicia la aplicación y accede a:
👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## ⚙️ Instalación y Ejecución
1. Clonar el repositorio.
2. Ejecutar `./mvnw clean install` para descargar dependencias.
3. Iniciar con `./mvnw spring-boot:run`.
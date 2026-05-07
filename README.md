# <p align="center">⚙️ PentaNet API</p>

<p align="center">
  <strong>Núcleo de Servicios REST y Arquitectura de Datos para PentaNet</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white" />
  <img src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
</p>

---

## 🏗️ Arquitectura del Sistema

Esta API actúa como el motor central de PentaNet, gestionando la lógica de negocio distribuida bajo un modelo **SaaS Multi-tenant**. 

### Capas del Proyecto:
*   **Controllers:** Endpoints REST documentados con Swagger.
*   **Services:** Lógica de negocio y validaciones de dominio.
*   **Repositories:** Abstracción de datos con Spring Data JPA.
*   **Models:** Entidades JPA con mapeo relacional complejo (ManyToOne, OneToMany).
*   **DTOs:** Objetos de transferencia para optimizar el payload (JSON) y ocultar datos sensibles.

---

## 🔒 Seguridad y Control de Acceso

El sistema implementa una arquitectura de seguridad robusta:
*   **JWT (Stateless):** Autenticación mediante tokens firmados.
*   **RBAC (Role-Based Access Control):** Permisos granulares (`ROLE_ADMIN`, `ROLE_SECRETARIA`, `ROLE_PROFESOR`, `ROLE_ALUMNO`).
*   **Filtros SaaS:** La mayoría de las consultas incluyen un filtro automático por `centro_id` para garantizar el aislamiento de datos entre instituciones.
*   **Bcrypt:** Encriptación de contraseñas de alta seguridad.

---

## 📡 Endpoints Destacados

La API ofrece más de 40 endpoints organizados por dominios:

*   **`/auth`**: Registro y Login con retorno de JWT.
*   **`/api/correos`**: Gestión de mensajería interna, estados de lectura y adjuntos en Base64.
*   **`/api/students`**: Gestión de matrículas, expedientes y horarios de alumnos.
*   **`/api/asistencia`**: Registro de faltas y puntualidad.
*   **`/api/calificaciones`**: Gestión de notas y criterios de evaluación.

> [!NOTE]
> La documentación interactiva completa está disponible vía **Swagger UI** en: `http://localhost:9999/swagger-ui.html`

---

## 🚀 Configuración para Desarrollo

1.  **Clonar y Configurar**:
    ```bash
    git clone ...
    ```
2.  **Base de Datos**:
    *   Importar el archivo `src/main/resources/BD.sql` en MySQL.
    *   Asegurarse de usar MySQL 8+ para soporte de `LONGTEXT`.
3.  **Propiedades**:
    *   Revisar `src/main/resources/application.properties` para las credenciales de BD.
4.  **Ejecutar**:
    ```bash
    ./mvnw spring-boot:run
    ```

---

## 📈 Características Avanzadas
*   **Data Truncation Handling:** Configurado para manejar grandes volúmenes de datos en mensajes (adjuntos).
*   **Global Exception Handling:** Sistema centralizado de captura de errores para respuestas JSON consistentes.
*   **Optimized Queries:** Uso de proyecciones y DTOs para evitar el problema de "N+1 queries".

---
<p align="center">
  Motorizado por <strong>Spring Boot</strong> | Diseñado para la escalabilidad.
</p>

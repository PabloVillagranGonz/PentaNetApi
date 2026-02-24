# 📧 PentaNet – Sistema de Gestión Académica con Mensajería

Penta es una aplicación multiplataforma desarrollada con Flutter (frontend) y Spring Boot + MySQL + JWT (backend), diseñada para la gestión académica de centros educativos musicales.

Permite administrar centros, cursos, asignaturas, profesores y alumnos, además de incluir un sistema interno de mensajería.

⸻

## 🚀 Tecnologías utilizadas

🔹 Backend
•	Java 17
•	Spring Boot
•	Spring Security + JWT
•	Spring Data JPA
•	MySQL
•	Swagger (OpenAPI)

🔹 Frontend
•	Flutter
•	Provider (State Management)
•	JWT Authentication
•	HTTP REST API

⸻

🔐 Autenticación y Seguridad
•	Sistema basado en JWT
•	Roles disponibles:
•	ADMIN
•	TEACHER
•	STUDENT
•	Seguridad configurada con SecurityFilterChain
•	Endpoints protegidos según rol

⸻

### 🏫 Funcionalidades principales

👑 Administrador
•	Crear centros
•	Crear cursos
•	Crear asignaturas
•	Crear profesores y alumnos
•	Asignar asignaturas a cursos
•	Asignar profesor, aula, día y hora
•	Gestión completa de usuarios

⸻

🎓 Alumno
•	Ver su centro
•	Ver su horario
•	Ver sus asignaturas
•	Enviar correos a sus profesores
•	Recibir correos

⸻

👨‍🏫 Profesor
•	Ver su horario
•	Enviar correos
•	Recibir correos
•	(Nueva funcionalidad) Ver sus alumnos al enviar correo

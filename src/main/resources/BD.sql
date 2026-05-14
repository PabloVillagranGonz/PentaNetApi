-- =====================================================
-- PENTANET - SCHEMA DEFINITIVO v4.3 (Incluye Calificaciones)
-- Optimizado para: Spring Boot, JPA, MySQL 8+ y Flutter
-- Idioma: Español
-- =====================================================

DROP DATABASE IF EXISTS PentaNet;
CREATE DATABASE IF NOT EXISTS PentaNet
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE PentaNet;

-- 1. CENTROS
CREATE TABLE centros (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         nombre VARCHAR(255) NOT NULL,
                         telefono VARCHAR(50),
                         email VARCHAR(255),
                         website VARCHAR(255),
                         horario_apertura VARCHAR(255),
                         direccion VARCHAR(255),
                         codigo_postal VARCHAR(20),
                         ciudad VARCHAR(100),
                         activo TINYINT(1) DEFAULT 1,
                         creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. INSTRUMENTOS
CREATE TABLE instrumentos (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              nombre VARCHAR(100) NOT NULL UNIQUE,
                              creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. CURSOS
CREATE TABLE cursos (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        centro_id BIGINT NOT NULL,
                        nombre VARCHAR(100) NOT NULL,
                        anio INT,
                        notas VARCHAR(255),
                        activo TINYINT(1) DEFAULT 1,
                        creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        KEY idx_cursos_anio (anio),
                        CONSTRAINT fk_cursos_centro FOREIGN KEY (centro_id) REFERENCES centros(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. USUARIOS
CREATE TABLE usuarios (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          centro_id BIGINT NULL,
                          instrumento_id BIGINT NULL,
                          curso_id BIGINT NULL,
                          nombre VARCHAR(100) NOT NULL,
                          apellidos VARCHAR(150) NOT NULL,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          rol ENUM('ADMIN','SECRETARIA','ALUMNO','PROFESOR') NOT NULL,
                          telefono VARCHAR(50),
                          dni VARCHAR(50) UNIQUE,
                          fecha_nacimiento DATE,
                          direccion VARCHAR(255),
                          foto_uri VARCHAR(255),
                          info_adicional TEXT,
                          activo TINYINT(1) DEFAULT 1,
                          creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          KEY idx_usuarios_rol (rol),
                          KEY idx_usuarios_centro (centro_id),
                          KEY idx_usuarios_curso (curso_id),
                          CONSTRAINT fk_usuarios_centro FOREIGN KEY (centro_id) REFERENCES centros(id) ON DELETE CASCADE,
                          CONSTRAINT fk_usuarios_instrumento FOREIGN KEY (instrumento_id) REFERENCES instrumentos(id) ON DELETE SET NULL,
                          CONSTRAINT fk_usuarios_curso FOREIGN KEY (curso_id) REFERENCES cursos(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 5. ESPACIOS
CREATE TABLE espacios (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         centro_id BIGINT NOT NULL,
                         nombre VARCHAR(50) NOT NULL,
                         tipo ENUM('AULA', 'CABINA', 'AUDITORIO', 'OTROS') NOT NULL,
                         capacidad INT DEFAULT 1,
                         activo TINYINT(1) DEFAULT 1,
                         creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         UNIQUE KEY unique_espacio_centro (centro_id, nombre),
                         KEY idx_espacios_tipo (tipo),
                         KEY idx_espacios_centro_tipo (centro_id, tipo),
                         CONSTRAINT fk_espacios_centro FOREIGN KEY (centro_id) REFERENCES centros(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. ASIGNATURAS
CREATE TABLE asignaturas (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             centro_id BIGINT NOT NULL,
                             nombre VARCHAR(100) NOT NULL,
                             descripcion TEXT,
                             duracion_minutos INT DEFAULT 60,
                             tipo ENUM('COLECTIVA','INDIVIDUAL') NOT NULL,
                             activo TINYINT(1) DEFAULT 1,
                             creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             KEY idx_asignaturas_tipo (tipo),
                             CONSTRAINT fk_asignaturas_centro FOREIGN KEY (centro_id) REFERENCES centros(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. ASIGNATURAS_CURSOS
CREATE TABLE asignaturas_cursos (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    curso_id BIGINT NOT NULL,
                                    asignatura_id BIGINT NOT NULL,
                                    horas_semanales DECIMAL(5,2) DEFAULT 0.00,
                                    notas_publicadas TINYINT(1) DEFAULT 0,
                                    UNIQUE KEY unique_asig_curso (curso_id, asignatura_id),
                                    CONSTRAINT fk_ac_curso FOREIGN KEY (curso_id) REFERENCES cursos(id) ON DELETE CASCADE,
                                    CONSTRAINT fk_ac_asignatura FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. ASIGNACIONES_DOCENTES
CREATE TABLE asignaciones_docentes (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       asignatura_id BIGINT NOT NULL,
                                       profesor_id BIGINT NOT NULL,
                                       curso_id BIGINT NOT NULL,
                                       rol_docente VARCHAR(50) DEFAULT 'Titular',
                                       creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       UNIQUE KEY unique_docente (profesor_id, asignatura_id, curso_id),
                                       CONSTRAINT fk_ad_profesor FOREIGN KEY (profesor_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                                       CONSTRAINT fk_ad_asignatura FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id) ON DELETE CASCADE,
                                       CONSTRAINT fk_ad_curso FOREIGN KEY (curso_id) REFERENCES cursos(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 9. SESIONES_CLASE
CREATE TABLE sesiones_clase (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                asignatura_id BIGINT NOT NULL,
                                profesor_id BIGINT NOT NULL,
                                curso_id BIGINT NOT NULL,
                                alumno_id BIGINT NULL,
                                espacio_id BIGINT NOT NULL,
                                dia_semana INT NOT NULL,
                                hora_inicio TIME NOT NULL,
                                hora_fin TIME NOT NULL,
                                notas VARCHAR(255),
                                creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                CHECK (hora_fin > hora_inicio),
                                CHECK (dia_semana BETWEEN 1 AND 7),
                                UNIQUE KEY unique_espacio_horario (espacio_id, dia_semana, hora_inicio, hora_fin),
                                KEY idx_sc_profesor (profesor_id),
                                KEY idx_sc_curso (curso_id),
                                KEY idx_sc_dia_hora (dia_semana, hora_inicio),
                                KEY idx_sc_alumno (alumno_id),
                                CONSTRAINT fk_sc_asignatura FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id) ON DELETE CASCADE,
                                CONSTRAINT fk_sc_profesor FOREIGN KEY (profesor_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                                CONSTRAINT fk_sc_curso FOREIGN KEY (curso_id) REFERENCES cursos(id) ON DELETE CASCADE,
                                CONSTRAINT fk_sc_alumno FOREIGN KEY (alumno_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                                CONSTRAINT fk_sc_espacio FOREIGN KEY (espacio_id) REFERENCES espacios(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. RESERVAS
CREATE TABLE reservas (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          centro_id BIGINT NOT NULL,
                          usuario_id BIGINT NOT NULL,
                          espacio_id BIGINT NOT NULL,
                          inicio DATETIME NOT NULL,
                          fin DATETIME NOT NULL,
                          fin_real DATETIME NULL,
                          finalizada_antes TINYINT(1) DEFAULT 0,
                          creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          CHECK (fin > inicio),
                          KEY idx_res_espacio_fecha (espacio_id, inicio, fin),
                          CONSTRAINT fk_res_centro FOREIGN KEY (centro_id) REFERENCES centros(id) ON DELETE CASCADE,
                          CONSTRAINT fk_res_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                          CONSTRAINT fk_res_espacio FOREIGN KEY (espacio_id) REFERENCES espacios(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. GRUPOS_MENSAJES
CREATE TABLE grupos_mensajes (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 asignatura_id BIGINT NOT NULL,
                                 creado_por BIGINT NULL,
                                 creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_gm_asignatura FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_gm_creador FOREIGN KEY (creado_por) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 12. MENSAJES
CREATE TABLE mensajes (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          remitente_id BIGINT NOT NULL,
                          destinatario_id BIGINT NULL,
                          grupo_id BIGINT NULL,
                          asunto VARCHAR(255) NOT NULL,
                          cuerpo LONGTEXT NOT NULL,
                          fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          KEY idx_msg_fecha (fecha_envio DESC),
                          KEY idx_msg_remitente (remitente_id),
                          KEY idx_msg_destinatario (destinatario_id),
                          KEY idx_msg_grupo (grupo_id),
                          CONSTRAINT fk_msj_remitente FOREIGN KEY (remitente_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                          CONSTRAINT fk_msj_destinatario FOREIGN KEY (destinatario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
                          CONSTRAINT fk_msj_grupo FOREIGN KEY (grupo_id) REFERENCES grupos_mensajes(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 13. USUARIOS_MENSAJES
CREATE TABLE usuarios_mensajes (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   mensaje_id BIGINT NOT NULL,
                                   usuario_id BIGINT NOT NULL,
                                   leido TINYINT(1) DEFAULT 0,
                                   eliminado TINYINT(1) DEFAULT 0,
                                   archivado TINYINT(1) DEFAULT 0,
                                   fecha_lectura TIMESTAMP NULL,
                                   fecha_eliminacion TIMESTAMP NULL,
                                   creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   KEY idx_um_mensaje (mensaje_id),
                                   KEY idx_um_usuario_estado (usuario_id, eliminado, archivado),
                                   KEY idx_um_usuario_leido (usuario_id, leido),
                                   CONSTRAINT fk_um_mensaje FOREIGN KEY (mensaje_id) REFERENCES mensajes(id) ON DELETE CASCADE,
                                   CONSTRAINT fk_um_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 14. ASISTENCIA
CREATE TABLE asistencia (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            sesion_id BIGINT NOT NULL,
                            alumno_id BIGINT NOT NULL,
                            fecha DATE NOT NULL,
                            estado ENUM('PRESENTE','AUSENTE','RETRASO') NOT NULL,
                            creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE KEY unique_asistencia (sesion_id, alumno_id, fecha),
                            FOREIGN KEY (sesion_id) REFERENCES sesiones_clase(id) ON DELETE CASCADE,
                            FOREIGN KEY (alumno_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- 15. CRITERIOS_EVALUACION
-- Define las columnas de evaluación (ej: Examen Parcial 40%)
-- =====================================================
CREATE TABLE criterios_evaluacion (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      asignatura_id BIGINT NOT NULL,
                                      curso_id BIGINT NOT NULL,
                                      nombre VARCHAR(100) NOT NULL,
                                      peso DECIMAL(5,2) NOT NULL, -- Porcentaje (ej: 40.00 para 40%)
                                      creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      CONSTRAINT fk_ce_asignatura FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id) ON DELETE CASCADE,
                                      CONSTRAINT fk_ce_curso FOREIGN KEY (curso_id) REFERENCES cursos(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- 16. CALIFICACIONES
-- Guarda la nota específica de un alumno en un criterio concreto
-- =====================================================
CREATE TABLE calificaciones (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                criterio_id BIGINT NOT NULL,
                                alumno_id BIGINT NOT NULL,
                                nota DECIMAL(4,2) NOT NULL, -- Nota del 0.00 al 10.00
                                comentarios TEXT,
                                fecha_evaluacion DATE DEFAULT (CURRENT_DATE),
                                creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                UNIQUE KEY unique_calificacion_alumno (criterio_id, alumno_id),
                                CONSTRAINT fk_cal_criterio FOREIGN KEY (criterio_id) REFERENCES criterios_evaluacion(id) ON DELETE CASCADE,
                                CONSTRAINT fk_cal_alumno FOREIGN KEY (alumno_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB;
-- =====================================================
-- DATOS INICIALES (SEMILLA / SEEDER)
-- =====================================================

-- 1. Usuario SuperAdmin (God Mode)
-- Email: admin@pentanet.es
-- Contraseña: 123456
-- OJO: centro_id es NULL porque es el superadministrador global
INSERT INTO usuarios (centro_id, nombre, apellidos, email, password, rol, activo) 
VALUES (NULL, 'Super', 'Administrador', 'admin@pentanet.es', '$2a$12$oSUCCxVHbcDOwRbpn5x4j.h1HmhNyS27fxh2LCxBm/HYJ0BbYIhGm', 'ADMIN', 1);

-- 2. Instrumentos de Conservatorio
INSERT INTO instrumentos (nombre) VALUES 
('Piano'),
('Violin'),
('Viola'),
('Violonchelo'),
('Contrabajo'),
('Flauta Travesera'),
('Oboe'),
('Clarinete'),
('Fagot'),
('Saxofon'),
('Trompa'),
('Trompeta'),
('Trombon'),
('Tuba'),
('Percusion'),
('Arpa'),
('Guitarra Clasica'),
('Guitarra Flamenca'),
('Guitarra Electrica'),
('Bajo Electrico'),
('Canto'),
('Organo'),
('Clave'),
('Acordeon'),
('Flauta de Pico'),
('Viola da Gamba'),
('Instrumentos de Pua'),
('Laud'),
('Vihuela'),
('Bateria');

-- 3. Centros (2 centros de prueba)
INSERT INTO centros (nombre, telefono, email, direccion, ciudad, codigo_postal) VALUES 
('Conservatorio de Valladolid', '912345678', 'valladolid@pentanet.es', 'Calle Atocha 1', 'Valladolid', '28012'),
('Escuela de Musica de Barcelona', '931234567', 'bcn@pentanet.es', 'Carrer Marina 2', 'Barcelona', '08013');

-- 4. Usuarios por Centro (Todos tienen contraseña '123456')
-- Centro 1 (Madrid)
INSERT INTO usuarios (centro_id, nombre, apellidos, email, password, rol, activo) VALUES 
(1, 'Admin', 'Valladolid', 'admin1@pentanet.es', '$2a$12$oSUCCxVHbcDOwRbpn5x4j.h1HmhNyS27fxh2LCxBm/HYJ0BbYIhGm', 'ADMIN', 1),
(1, 'Secretario', 'Valladolid', 'secretario1@pentanet.es', '$2a$12$oSUCCxVHbcDOwRbpn5x4j.h1HmhNyS27fxh2LCxBm/HYJ0BbYIhGm', 'SECRETARIA', 1),
(1, 'Laura', 'Valladolid', 'lauragarrido@pentanet.es', '$2a$12$oSUCCxVHbcDOwRbpn5x4j.h1HmhNyS27fxh2LCxBm/HYJ0BbYIhGm', 'PROFESOR', 1),
(1, 'Pablo', 'Valladolid', 'pablo@pentanet.es', '$2a$12$oSUCCxVHbcDOwRbpn5x4j.h1HmhNyS27fxh2LCxBm/HYJ0BbYIhGm', 'ALUMNO', 1);

-- Centro 2 (Barcelona)
INSERT INTO usuarios (centro_id, nombre, apellidos, email, password, rol, activo) VALUES 
(2, 'Admin', 'Barcelona', 'admin2@pentanet.es', '$2a$12$oSUCCxVHbcDOwRbpn5x4j.h1HmhNyS27fxh2LCxBm/HYJ0BbYIhGm', 'ADMIN', 1),
(2, 'Secretario', 'Barcelona', 'secretario2@pentanet.es', '$2a$12$oSUCCxVHbcDOwRbpn5x4j.h1HmhNyS27fxh2LCxBm/HYJ0BbYIhGm', 'SECRETARIA', 1),
(2, 'Profesor', 'Barcelona', 'profesor2@pentanet.es', '$2a$12$oSUCCxVHbcDOwRbpn5x4j.h1HmhNyS27fxh2LCxBm/HYJ0BbYIhGm', 'PROFESOR', 1),
(2, 'Alumno', 'Barcelona', 'alumno2@pentanet.es', '$2a$12$oSUCCxVHbcDOwRbpn5x4j.h1HmhNyS27fxh2LCxBm/HYJ0BbYIhGm', 'ALUMNO', 1);

-- 5. CURSOS (Centro 1)
INSERT INTO cursos (centro_id, nombre, anio, activo) VALUES
(1, '1 Profesional', 1, 1),
(1, '2 Profesional', 2, 1),
(1, '3 Profesional', 3, 1),
(1, '4 Profesional', 4, 1),
(1, '5 Profesional', 5, 1),
(1, '6 Profesional', 6, 1);

-- 6. ESPACIOS (Centro 1) - 6 Aulas y 6 Cabinas
INSERT INTO espacios (centro_id, nombre, tipo, capacidad, activo) VALUES
(1, 'Aula 1', 'AULA', 20, 1),
(1, 'Aula 2', 'AULA', 20, 1),
(1, 'Aula 3', 'AULA', 20, 1),
(1, 'Aula 4', 'AULA', 20, 1),
(1, 'Aula 5', 'AULA', 20, 1),
(1, 'Aula 6', 'AULA', 20, 1),
(1, 'Cabina 1', 'CABINA', 2, 1),
(1, 'Cabina 2', 'CABINA', 2, 1),
(1, 'Cabina 3', 'CABINA', 2, 1),
(1, 'Cabina 4', 'CABINA', 2, 1),
(1, 'Cabina 5', 'CABINA', 2, 1),
(1, 'Cabina 6', 'CABINA', 2, 1);

-- 7. ASIGNATURAS (Centro 1)
INSERT INTO asignaturas (centro_id, nombre, descripcion, duracion_minutos, tipo, activo) VALUES
(1, 'Armonia', 'Asignatura grupal de armonia', 60, 'COLECTIVA', 1),
(1, 'Historia de la Musica', 'Asignatura grupal de historia de la musica', 60, 'COLECTIVA', 1),
(1, 'Banda', 'Practica grupal de banda', 90, 'COLECTIVA', 1),
(1, 'Instrumento Principal', 'Clase individual de instrumento', 60, 'INDIVIDUAL', 1);

-- 8. ASIGNATURAS_CURSOS (Relacionar las asignaturas con 1º Profesional - Curso ID 1)
INSERT INTO asignaturas_cursos (curso_id, asignatura_id, horas_semanales, notas_publicadas) VALUES
(1, 1, 2.00, 0), -- Armonia
(1, 2, 2.00, 0), -- Historia de la Musica
(1, 3, 3.00, 0), -- Banda
(1, 4, 1.00, 0); -- Instrumento Principal

-- 9. ASIGNACIONES_DOCENTES (Profesor Laura ID 4)
INSERT INTO asignaciones_docentes (asignatura_id, profesor_id, curso_id, rol_docente) VALUES
(1, 4, 1, 'Titular'),
(2, 4, 1, 'Titular'),
(3, 4, 1, 'Titular'),
(4, 4, 1, 'Titular');

-- 10. ACTUALIZAR ALUMNO PABLO (Asignarlo al Curso 1 - 1º Profesional y al Instrumento Piano - ID 1)
-- El ID de Pablo es 5 (asumiendo SuperAdmin 1, Centro 1 tiene 4 usuarios: 2, 3, 4, 5)
UPDATE usuarios SET curso_id = 1, instrumento_id = 1 WHERE id = 5;

-- 11. SESIONES_CLASE (Horarios)
-- Sesiones grupales (alumno_id = NULL)
INSERT INTO sesiones_clase (asignatura_id, profesor_id, curso_id, alumno_id, espacio_id, dia_semana, hora_inicio, hora_fin) VALUES
(1, 4, 1, NULL, 1, 1, '16:00:00', '17:00:00'), -- Armonia en Aula 1 el Lunes
(2, 4, 1, NULL, 2, 2, '17:00:00', '18:00:00'), -- Historia en Aula 2 el Martes
(3, 4, 1, NULL, 3, 3, '18:00:00', '19:30:00'); -- Banda en Aula 3 el Miercoles

-- Sesión individual para Pablo (ID 5) en Instrumento (Asignatura 4)
INSERT INTO sesiones_clase (asignatura_id, profesor_id, curso_id, alumno_id, espacio_id, dia_semana, hora_inicio, hora_fin) VALUES
(4, 4, 1, 5, 7, 4, '16:00:00', '17:00:00'); -- Instrumento en Cabina 1 el Jueves para Pablo

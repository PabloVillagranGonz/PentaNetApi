-- =====================================================
-- PENTANET - SCHEMA DEFINITIVO v4.2
-- Optimizado para: Spring Boot, JPA, MySQL 8+ y Flutter
-- Idioma: Español
-- =====================================================

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
                        creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        KEY idx_cursos_anio (anio),
                        CONSTRAINT fk_cursos_centro
                            FOREIGN KEY (centro_id) REFERENCES centros(id)
                                ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. USUARIOS
CREATE TABLE usuarios (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          centro_id BIGINT NOT NULL,
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
                          CONSTRAINT fk_usuarios_centro
                              FOREIGN KEY (centro_id) REFERENCES centros(id)
                                  ON DELETE CASCADE,
                          CONSTRAINT fk_usuarios_instrumento
                              FOREIGN KEY (instrumento_id) REFERENCES instrumentos(id)
                                  ON DELETE SET NULL,
                          CONSTRAINT fk_usuarios_curso
                              FOREIGN KEY (curso_id) REFERENCES cursos(id)
                                  ON DELETE SET NULL
) ENGINE=InnoDB;

-- 5. ESPACIOS
CREATE TABLE espacio (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          centro_id BIGINT NOT NULL,
                          nombre VARCHAR(50) NOT NULL,
                          tipo ENUM('AULA', 'CABINA', 'AUDITORIO', 'OTROS') NOT NULL,
                          capacidad INT DEFAULT 1,
                          creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          UNIQUE KEY unique_espacio_centro (centro_id, nombre),
                          KEY idx_espacios_tipo (tipo),
                          KEY idx_espacios_centro_tipo (centro_id, tipo),
                          CONSTRAINT fk_espacios_centro
                              FOREIGN KEY (centro_id) REFERENCES centros(id)
                                  ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. ASIGNATURAS
CREATE TABLE asignaturas (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             centro_id BIGINT NOT NULL,
                             nombre VARCHAR(100) NOT NULL,
                             descripcion TEXT,
                             duracion_minutos INT DEFAULT 60,
                             tipo ENUM('COLECTIVA','INDIVIDUAL') NOT NULL,
                             creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             KEY idx_asignaturas_tipo (tipo),
                             CONSTRAINT fk_asignaturas_centro
                                 FOREIGN KEY (centro_id) REFERENCES centros(id)
                                     ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. ASIGNATURAS_CURSOS
CREATE TABLE asignaturas_cursos (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    curso_id BIGINT NOT NULL,
                                    asignatura_id BIGINT NOT NULL,
                                    horas_semanales DECIMAL(5,2) DEFAULT 0.00,
                                    UNIQUE KEY unique_asig_curso (curso_id, asignatura_id),
                                    CONSTRAINT fk_ac_curso
                                        FOREIGN KEY (curso_id) REFERENCES cursos(id)
                                            ON DELETE CASCADE,
                                    CONSTRAINT fk_ac_asignatura
                                        FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id)
                                            ON DELETE CASCADE
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
                                       CONSTRAINT fk_ad_profesor
                                           FOREIGN KEY (profesor_id) REFERENCES usuarios(id)
                                               ON DELETE CASCADE,
                                       CONSTRAINT fk_ad_asignatura
                                           FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id)
                                               ON DELETE CASCADE,
                                       CONSTRAINT fk_ad_curso
                                           FOREIGN KEY (curso_id) REFERENCES cursos(id)
                                               ON DELETE CASCADE
) ENGINE=InnoDB;

-- 9. SESIONES_CLASE (CORREGIDO)
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
                                CONSTRAINT fk_sc_asignatura
                                    FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id)
                                        ON DELETE CASCADE,
                                CONSTRAINT fk_sc_profesor
                                    FOREIGN KEY (profesor_id) REFERENCES usuarios(id)
                                        ON DELETE RESTRICT,
                                CONSTRAINT fk_sc_curso
                                    FOREIGN KEY (curso_id) REFERENCES cursos(id)
                                        ON DELETE CASCADE,
                                CONSTRAINT fk_sc_alumno
                                    FOREIGN KEY (alumno_id) REFERENCES usuarios(id)
                                        ON DELETE CASCADE,
                                CONSTRAINT fk_sc_espacio
                                    FOREIGN KEY (espacio_id) REFERENCES espacio(id)
                                        ON DELETE RESTRICT
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
                          CONSTRAINT fk_res_centro
                              FOREIGN KEY (centro_id) REFERENCES centros(id)
                                  ON DELETE CASCADE,
                          CONSTRAINT fk_res_usuario
                              FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
                                  ON DELETE RESTRICT,
                          CONSTRAINT fk_res_espacio
                              FOREIGN KEY (espacio_id) REFERENCES espacio(id)
                                  ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. GRUPOS_MENSAJES
CREATE TABLE grupos_mensajes (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 asignatura_id BIGINT NOT NULL,
                                 creado_por BIGINT NULL,
                                 creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_gm_asignatura
                                     FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id)
                                         ON DELETE CASCADE,
                                 CONSTRAINT fk_gm_creador
                                     FOREIGN KEY (creado_por) REFERENCES usuarios(id)
                                         ON DELETE SET NULL
) ENGINE=InnoDB;

-- 12. MENSAJES
CREATE TABLE mensajes (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          remitente_id BIGINT NOT NULL,
                          destinatario_id BIGINT NULL,
                          grupo_id BIGINT NULL,
                          asunto VARCHAR(255) NOT NULL,
                          cuerpo TEXT NOT NULL,
                          fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          KEY idx_msg_fecha (fecha_envio DESC),
                          KEY idx_msg_remitente (remitente_id),
                          KEY idx_msg_destinatario (destinatario_id),
                          KEY idx_msg_grupo (grupo_id),
                          CONSTRAINT fk_msj_remitente
                              FOREIGN KEY (remitente_id) REFERENCES usuarios(id)
                                  ON DELETE CASCADE,
                          CONSTRAINT fk_msj_destinatario
                              FOREIGN KEY (destinatario_id) REFERENCES usuarios(id)
                                  ON DELETE SET NULL,
                          CONSTRAINT fk_msj_grupo
                              FOREIGN KEY (grupo_id) REFERENCES grupos_mensajes(id)
                                  ON DELETE SET NULL
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
                                   CONSTRAINT fk_um_mensaje
                                       FOREIGN KEY (mensaje_id) REFERENCES mensajes(id)
                                           ON DELETE CASCADE,
                                   CONSTRAINT fk_um_usuario
                                       FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
                                           ON DELETE CASCADE
) ENGINE=InnoDB;

-- 14. Asistencia
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
);
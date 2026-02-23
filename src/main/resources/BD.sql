-- =====================================================
-- CENTROSNET - SCHEMA FINAL COMPLETO v1.1
-- MySQL 8+
-- =====================================================

CREATE DATABASE IF NOT EXISTS centrosnet
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE centrosnet;

-- =====================================================
-- CENTERS
-- =====================================================

CREATE TABLE centers (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         name VARCHAR(255) NOT NULL,
                         phone VARCHAR(50),
                         email VARCHAR(255),
                         website VARCHAR(255),
                         opening_hours VARCHAR(255),
                         address VARCHAR(255),
                         postal_code VARCHAR(20),
                         town VARCHAR(100),
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (id)
) ENGINE=InnoDB;

-- =====================================================
-- INSTRUMENTS
-- =====================================================

CREATE TABLE instruments (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             name VARCHAR(100) NOT NULL,
                             PRIMARY KEY (id),
                             UNIQUE KEY unique_instrument_name (name)
) ENGINE=InnoDB;

-- =====================================================
-- COURSES
-- =====================================================

CREATE TABLE courses (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         center_id BIGINT NOT NULL,
                         name VARCHAR(100) NOT NULL,
                         year INT,
                         notes VARCHAR(255),
                         PRIMARY KEY (id),
                         KEY idx_courses_center (center_id),
                         CONSTRAINT fk_courses_center
                             FOREIGN KEY (center_id) REFERENCES centers(id)
                                 ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- USERS
-- =====================================================

CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       center_id BIGINT,
                       instrument_id BIGINT,
                       course_id BIGINT,
                       nombre VARCHAR(100) NOT NULL,
                       apellidos VARCHAR(150) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       phone VARCHAR(50),
                       dni VARCHAR(50),
                       birthdate DATE,
                       address VARCHAR(255),
                       photo_uri VARCHAR(255),
                       additional_info TEXT,
                       active TINYINT(1) DEFAULT 1,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       UNIQUE KEY unique_user_email (email),
                       KEY idx_users_center (center_id),
                       KEY idx_users_instrument (instrument_id),
                       KEY idx_users_course (course_id),
                       CONSTRAINT fk_users_center
                           FOREIGN KEY (center_id) REFERENCES centers(id)
                               ON DELETE CASCADE,
                       CONSTRAINT fk_users_instrument
                           FOREIGN KEY (instrument_id) REFERENCES instruments(id)
                               ON DELETE SET NULL,
                       CONSTRAINT fk_users_course
                           FOREIGN KEY (course_id) REFERENCES courses(id),
                       CONSTRAINT chk_users_role
                           CHECK (role IN ('ADMIN','SECRETARIA','STUDENT','TEACHER'))
) ENGINE=InnoDB;

-- =====================================================
-- ROOMS
-- =====================================================

CREATE TABLE rooms (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       center_id BIGINT NOT NULL,
                       name VARCHAR(100) NOT NULL,
                       notes VARCHAR(255),
                       PRIMARY KEY (id),
                       UNIQUE KEY unique_room_center (center_id, name),
                       CONSTRAINT fk_rooms_center
                           FOREIGN KEY (center_id) REFERENCES centers(id)
                               ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- SUBJECTS
-- =====================================================

CREATE TABLE subjects (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          center_id BIGINT NOT NULL,
                          room_id BIGINT,
                          teacher_id BIGINT,
                          code VARCHAR(50),
                          name VARCHAR(100) NOT NULL,
                          description TEXT,
                          duration_minutes INT DEFAULT 0,
                          PRIMARY KEY (id),
                          KEY idx_subjects_center (center_id),
                          KEY idx_subjects_room (room_id),
                          KEY idx_subjects_teacher (teacher_id),
                          CONSTRAINT fk_subjects_center
                              FOREIGN KEY (center_id) REFERENCES centers(id)
                                  ON DELETE CASCADE,
                          CONSTRAINT fk_subjects_room
                              FOREIGN KEY (room_id) REFERENCES rooms(id)
                                  ON DELETE SET NULL,
                          CONSTRAINT fk_subjects_teacher
                              FOREIGN KEY (teacher_id) REFERENCES users(id)
                                  ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- COURSE_SUBJECTS
-- =====================================================

CREATE TABLE course_subjects (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 course_id BIGINT NOT NULL,
                                 subject_id BIGINT NOT NULL,
                                 hours_per_week DECIMAL(5,2) DEFAULT 0.00,
                                 PRIMARY KEY (id),
                                 UNIQUE KEY unique_course_subject (course_id, subject_id),
                                 KEY idx_cs_subject (subject_id),
                                 CONSTRAINT fk_cs_course
                                     FOREIGN KEY (course_id) REFERENCES courses(id)
                                         ON DELETE CASCADE,
                                 CONSTRAINT fk_cs_subject
                                     FOREIGN KEY (subject_id) REFERENCES subjects(id)
                                         ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- ENROLLMENTS
-- =====================================================

CREATE TABLE enrollments (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             student_id BIGINT,
                             subject_id BIGINT NOT NULL,
                             course_id BIGINT,
                             enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             status VARCHAR(50) DEFAULT 'active',
                             PRIMARY KEY (id),
                             UNIQUE KEY unique_enrollment (student_id, subject_id, course_id),
                             KEY idx_enrollment_subject (subject_id),
                             KEY idx_enrollment_course (course_id),
                             KEY idx_enrollment_student (student_id),
                             CONSTRAINT fk_enrollment_student
                                 FOREIGN KEY (student_id) REFERENCES users(id)
                                     ON DELETE SET NULL,
                             CONSTRAINT fk_enrollment_subject
                                 FOREIGN KEY (subject_id) REFERENCES subjects(id)
                                     ON DELETE CASCADE,
                             CONSTRAINT fk_enrollment_course
                                 FOREIGN KEY (course_id) REFERENCES courses(id)
                                     ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- CLASS_SESSIONS
-- =====================================================

CREATE TABLE class_sessions (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                subject_id BIGINT NOT NULL,
                                teacher_id BIGINT,
                                course_id BIGINT,
                                room_id BIGINT,
                                day_of_week INT NOT NULL,
                                start_time TIME NOT NULL,
                                end_time TIME NOT NULL,
                                notes VARCHAR(255),
                                PRIMARY KEY (id),
                                KEY idx_cs_subject (subject_id),
                                KEY idx_cs_teacher (teacher_id),
                                KEY idx_cs_course (course_id),
                                KEY idx_cs_room (room_id),
                                CONSTRAINT fk_class_subject
                                    FOREIGN KEY (subject_id) REFERENCES subjects(id)
                                        ON DELETE CASCADE,
                                CONSTRAINT fk_class_teacher
                                    FOREIGN KEY (teacher_id) REFERENCES users(id)
                                        ON DELETE SET NULL,
                                CONSTRAINT fk_class_course
                                    FOREIGN KEY (course_id) REFERENCES courses(id)
                                        ON DELETE SET NULL,
                                CONSTRAINT fk_class_room
                                    FOREIGN KEY (room_id) REFERENCES rooms(id)
                                        ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- TEACHING_ASSIGNMENTS
-- =====================================================

CREATE TABLE teaching_assignments (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      teacher_id BIGINT,
                                      subject_id BIGINT NOT NULL,
                                      course_id BIGINT,
                                      role VARCHAR(50) DEFAULT 'teacher',
                                      PRIMARY KEY (id),
                                      UNIQUE KEY unique_teaching (teacher_id, subject_id, course_id),
                                      KEY idx_ta_subject (subject_id),
                                      KEY idx_ta_course (course_id),
                                      CONSTRAINT fk_ta_teacher
                                          FOREIGN KEY (teacher_id) REFERENCES users(id)
                                              ON DELETE SET NULL,
                                      CONSTRAINT fk_ta_subject
                                          FOREIGN KEY (subject_id) REFERENCES subjects(id)
                                              ON DELETE CASCADE,
                                      CONSTRAINT fk_ta_course
                                          FOREIGN KEY (course_id) REFERENCES courses(id)
                                              ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- AULAS
-- =====================================================

CREATE TABLE aulas (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       center_id BIGINT NOT NULL,
                       numero INT NOT NULL,
                       tipo VARCHAR(50),
                       instrumento_actual BIGINT,
                       PRIMARY KEY (id),
                       UNIQUE KEY unique_aula_center (center_id, numero),
                       KEY idx_aulas_center (center_id),
                       KEY idx_aula_instrumento (instrumento_actual),
                       CONSTRAINT fk_aulas_center
                           FOREIGN KEY (center_id) REFERENCES centers(id)
                               ON DELETE CASCADE,
                       CONSTRAINT fk_aulas_instrumento
                           FOREIGN KEY (instrumento_actual) REFERENCES instruments(id)
                               ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- RESERVAS
-- =====================================================

CREATE TABLE reservas (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          center_id BIGINT NOT NULL,
                          usuario_id BIGINT NOT NULL,
                          aula_id BIGINT NOT NULL,
                          inicio DATETIME NOT NULL,
                          fin DATETIME NOT NULL,
                          fin_real DATETIME,
                          finalizada_antes TINYINT(1) DEFAULT 0,
                          PRIMARY KEY (id),
                          KEY idx_reservas_center (center_id),
                          KEY idx_reservas_usuario (usuario_id),
                          KEY idx_reservas_aula_finreal (aula_id, fin_real),
                          KEY idx_reservas_usuario_finreal (usuario_id, fin_real),
                          CONSTRAINT fk_reservas_center
                              FOREIGN KEY (center_id) REFERENCES centers(id)
                                  ON DELETE CASCADE,
                          CONSTRAINT fk_reservas_usuario
                              FOREIGN KEY (usuario_id) REFERENCES users(id)
                                  ON DELETE RESTRICT,
                          CONSTRAINT fk_reservas_aula
                              FOREIGN KEY (aula_id) REFERENCES aulas(id)
                                  ON DELETE RESTRICT,
                          CONSTRAINT chk_reservas_fechas
                              CHECK (fin > inicio)
) ENGINE=InnoDB;


-- =====================================================
-- MENSAJE DE GRUPO
-- =====================================================

CREATE TABLE message_groups (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                subject_id BIGINT NULL,
                                course_id BIGINT NULL,
                                center_id BIGINT NULL,

                                created_by BIGINT NULL,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_mg_subject FOREIGN KEY (subject_id)
                                    REFERENCES subjects(id) ON DELETE CASCADE,

                                CONSTRAINT fk_mg_course FOREIGN KEY (course_id)
                                    REFERENCES courses(id) ON DELETE CASCADE,

                                CONSTRAINT fk_mg_center FOREIGN KEY (center_id)
                                    REFERENCES centers(id) ON DELETE CASCADE,

                                CONSTRAINT fk_mg_creator FOREIGN KEY (created_by)
                                    REFERENCES users(id) ON DELETE SET NULL,

                                CONSTRAINT chk_only_one_group_type CHECK (
                                    (subject_id IS NOT NULL AND course_id IS NULL AND center_id IS NULL)
                                        OR
                                    (subject_id IS NULL AND course_id IS NOT NULL AND center_id IS NULL)
                                        OR
                                    (subject_id IS NULL AND course_id IS NULL AND center_id IS NOT NULL)
                                    )
) ENGINE=InnoDB;

-- =====================================================
-- CORREOS
-- =====================================================
CREATE TABLE correos (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         user_id BIGINT NULL,
                         destinatario_id BIGINT NULL,
                         message_group_id BIGINT NULL,

                         asunto VARCHAR(255) NOT NULL,
                         cuerpo TEXT NOT NULL,

                         fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                         PRIMARY KEY (id),

    -- =========================
    -- Índices
    -- =========================
                         KEY idx_correos_user (user_id),
                         KEY idx_correos_destinatario (destinatario_id),
                         KEY idx_correos_group (message_group_id),

    -- =========================
    -- Foreign Keys
    -- =========================
                         CONSTRAINT fk_correos_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(id)
                                 ON DELETE SET NULL,

                         CONSTRAINT fk_correos_destinatario
                             FOREIGN KEY (destinatario_id)
                                 REFERENCES users(id)
                                 ON DELETE SET NULL,

                         CONSTRAINT fk_correos_message_group
                             FOREIGN KEY (message_group_id)
                                 REFERENCES message_groups(id)
                                 ON DELETE SET NULL

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- USUARIOS_CORREOS
-- =====================================================

CREATE TABLE usuarios_correos (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  correo_id BIGINT NOT NULL,
                                  usuario_id BIGINT,
                                  leido TINYINT(1) DEFAULT 0,
                                  eliminado TINYINT(1) DEFAULT 0,
                                  archivado TINYINT(1) DEFAULT 0,
                                  fecha_leido TIMESTAMP NULL,
                                  fecha_eliminado TIMESTAMP NULL,
                                  PRIMARY KEY (id),
                                  KEY idx_uc_correo (correo_id),
                                  KEY idx_uc_usuario (usuario_id, eliminado, archivado),
                                  CONSTRAINT fk_uc_correo
                                      FOREIGN KEY (correo_id) REFERENCES correos(id)
                                          ON DELETE CASCADE,
                                  CONSTRAINT fk_uc_usuario
                                      FOREIGN KEY (usuario_id) REFERENCES users(id)
                                          ON DELETE SET NULL
) ENGINE=InnoDB;
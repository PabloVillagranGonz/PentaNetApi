-- =========================
-- DATABASE CentrosNet
-- =========================
CREATE DATABASE IF NOT EXISTS centrosnet
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE centrosnet;

-- =========================
-- CENTERS
-- =========================
CREATE TABLE centers (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         phone VARCHAR(50),
                         email VARCHAR(255),
                         website VARCHAR(255),
                         opening_hours VARCHAR(255),
                         address VARCHAR(255),
                         postal_code VARCHAR(20),
                         town VARCHAR(100),
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- INSTRUMENTS
-- =========================
CREATE TABLE instruments (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             name VARCHAR(100) UNIQUE NOT NULL
);

-- =========================
-- ROOMS
-- =========================
CREATE TABLE rooms (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       center_id BIGINT NOT NULL,
                       name VARCHAR(100) NOT NULL,
                       notes VARCHAR(255),
                       UNIQUE (center_id, name),
                       FOREIGN KEY (center_id) REFERENCES centers(id) ON DELETE CASCADE
);

-- =========================
-- USERS
-- =========================
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       center_id BIGINT,
                       instrument_id BIGINT,
                       nombre VARCHAR(100) NOT NULL,
                       apellidos VARCHAR(150) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       tipo VARCHAR(50) NOT NULL,
                       phone VARCHAR(50),
                       dni VARCHAR(50),
                       birthdate DATE,
                       address VARCHAR(255),
                       photo_uri VARCHAR(255),
                       additional_info TEXT,
                       active TINYINT(1) DEFAULT 1,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (center_id) REFERENCES centers(id) ON DELETE CASCADE,
                       FOREIGN KEY (instrument_id) REFERENCES instruments(id) ON DELETE SET NULL
);

-- =========================
-- COURSES
-- =========================
CREATE TABLE courses (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         center_id BIGINT NOT NULL,
                         name VARCHAR(100) NOT NULL,
                         year INT,
                         notes VARCHAR(255),
                         FOREIGN KEY (center_id) REFERENCES centers(id) ON DELETE CASCADE
);

-- =========================
-- SUBJECTS
-- =========================
CREATE TABLE subjects (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          center_id BIGINT NOT NULL,
                          room_id BIGINT,
                          teacher_id BIGINT,
                          code VARCHAR(50),
                          name VARCHAR(100) NOT NULL,
                          description TEXT,
                          duration_minutes INT DEFAULT 0,
                          FOREIGN KEY (center_id) REFERENCES centers(id) ON DELETE CASCADE,
                          FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL,
                          FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL
);

-- =========================
-- COURSE MEMBERS
-- =========================
CREATE TABLE course_members (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                course_id BIGINT NOT NULL,
                                student_id BIGINT,
                                joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                UNIQUE (course_id, student_id),
                                FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                                FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE SET NULL
);

-- =========================
-- COURSE SUBJECTS
-- =========================
CREATE TABLE course_subjects (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 course_id BIGINT NOT NULL,
                                 subject_id BIGINT NOT NULL,
                                 hours_per_week DECIMAL(5,2) DEFAULT 0.0,
                                 UNIQUE (course_id, subject_id),
                                 FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                                 FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

-- =========================
-- ENROLLMENTS
-- =========================
CREATE TABLE enrollments (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             student_id BIGINT,
                             subject_id BIGINT NOT NULL,
                             course_id BIGINT,
                             enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             status VARCHAR(50) DEFAULT 'active',
                             UNIQUE (student_id, subject_id, course_id),
                             FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE SET NULL,
                             FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
                             FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL
);

-- =========================
-- TEACHING ASSIGNMENTS
-- =========================
CREATE TABLE teaching_assignments (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      teacher_id BIGINT,
                                      subject_id BIGINT NOT NULL,
                                      course_id BIGINT,
                                      role VARCHAR(50) DEFAULT 'teacher',
                                      FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL,
                                      FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
                                      FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL
);

-- =========================
-- CLASS SESSIONS
-- =========================
CREATE TABLE class_sessions (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                subject_id BIGINT NOT NULL,
                                teacher_id BIGINT,
                                course_id BIGINT,
                                room_id BIGINT,
                                day_of_week INT NOT NULL,
                                start_time TIME NOT NULL,
                                end_time TIME NOT NULL,
                                notes VARCHAR(255),
                                FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
                                FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL,
                                FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL,
                                FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
);

-- =========================
-- GRADES (HISTÓRICAS)
-- =========================
CREATE TABLE grades (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        student_id BIGINT,
                        subject_id BIGINT NOT NULL,
                        teacher_id BIGINT,
                        grade_type VARCHAR(50),
                        value DECIMAL(5,2),
                        weight DECIMAL(5,2) DEFAULT 1.0,
                        pass_fail TINYINT(1) DEFAULT 0,
                        date_recorded TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        remarks TEXT,
                        FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE SET NULL,
                        FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
                        FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL
);

-- =========================
-- CORREOS
-- =========================
CREATE TABLE correos (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT,
                         destinatario_id BIGINT,
                         asunto VARCHAR(255) NOT NULL,
                         cuerpo TEXT NOT NULL,
                         fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                         FOREIGN KEY (destinatario_id) REFERENCES users(id) ON DELETE SET NULL
);

-- =========================
-- USUARIOS_CORREOS
-- =========================
CREATE TABLE usuarios_correos (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  correo_id BIGINT NOT NULL,
                                  usuario_id BIGINT,
                                  leido TINYINT(1) DEFAULT 0,
                                  eliminado TINYINT(1) DEFAULT 0,
                                  archivado TINYINT(1) DEFAULT 0,
                                  fecha_leido TIMESTAMP NULL,
                                  fecha_eliminado TIMESTAMP NULL,
                                  FOREIGN KEY (correo_id) REFERENCES correos(id) ON DELETE CASCADE,
                                  FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE SET NULL
);

-- =========================
-- TEACHER SUBJECTS
-- =========================
CREATE TABLE teacher_subjects (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  teacher_id BIGINT,
                                  subject_id BIGINT NOT NULL,
                                  UNIQUE (teacher_id, subject_id),
                                  FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL,
                                  FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

-- =========================
-- TEACHER COURSES
-- =========================
CREATE TABLE teacher_courses (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 teacher_id BIGINT,
                                 course_id BIGINT NOT NULL,
                                 role VARCHAR(50),
                                 UNIQUE (teacher_id, course_id),
                                 FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL,
                                 FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);


-- =========================
-- AULAS (MULTI-CENTRO)
-- =========================
CREATE TABLE aulas (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

                       center_id BIGINT NOT NULL,

                       numero INT NOT NULL,
                       tipo VARCHAR(50),
                       estado ENUM('libre', 'ocupada') NOT NULL DEFAULT 'libre',
                       instrumento_actual BIGINT NULL,

                       UNIQUE (center_id, numero),

                       CONSTRAINT fk_aula_center
                           FOREIGN KEY (center_id)
                               REFERENCES centers(id)
                               ON DELETE CASCADE,

                       CONSTRAINT fk_aula_instrumento
                           FOREIGN KEY (instrumento_actual)
                               REFERENCES instruments(id)
                               ON DELETE SET NULL
);
-- =====================================================
-- TABLA: reservas
-- =====================================================
CREATE TABLE reservas (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,

                          center_id BIGINT NOT NULL,
                          usuario_id BIGINT NOT NULL,
                          aula_id BIGINT NOT NULL,

                          inicio DATETIME NOT NULL,
                          fin DATETIME NOT NULL,
                          fin_real DATETIME DEFAULT NULL,

                          finalizada_antes BOOLEAN DEFAULT FALSE,

-- =========================
-- FOREIGN KEYS
-- =========================

                          CONSTRAINT fk_reserva_center
                              FOREIGN KEY (center_id)
                                  REFERENCES centers(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_reserva_usuario
                              FOREIGN KEY (usuario_id)
                                  REFERENCES users(id)
                                  ON DELETE RESTRICT,

                          CONSTRAINT fk_reserva_aula
                              FOREIGN KEY (aula_id)
                                  REFERENCES aulas(id)
                                  ON DELETE RESTRICT
);

-- =========================
-- INDEXES
-- =========================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_courses_center ON courses(center_id);
CREATE INDEX idx_subjects_center ON subjects(center_id);
CREATE INDEX idx_enrollments_student ON enrollments(student_id);


-- =========================
-- DATOS DE PRUEBA CentrosNet
-- =========================

USE centrosnet;

-- =========================
-- CENTER
-- =========================
INSERT INTO centers (name, phone, email, town)
VALUES ('Conservatorio Profesional Valladolid', '983000000', 'info@conservatoriova.es', 'Valladolid');

-- =========================
-- INSTRUMENTS
-- =========================
INSERT INTO instruments (name) VALUES
                                   ('Piano'),
                                   ('Violín'),
                                   ('Trombón');

-- =========================
-- ROOMS
-- =========================
INSERT INTO rooms (center_id, name)
VALUES
    (1, 'Aula 101'),
    (1, 'Aula 202');

-- =========================
-- USERS (TEACHERS)
-- =========================
INSERT INTO users (center_id, nombre, apellidos, email, password, tipo)
VALUES
    (1, 'Ana', 'Martín', 'ana.martin@centrosnet.com', '1234', 'teacher'),
    (1, 'Luis', 'Gómez', 'luis.gomez@centrosnet.com', '1234', 'teacher');

-- =========================
-- USERS (STUDENTS)
-- =========================
INSERT INTO users (center_id, nombre, apellidos, email, password, tipo)
VALUES
    (1, 'Pablo', 'Villagrán', 'pablo@centrosnet.com', '1234', 'student'),
    (1, 'Lucía', 'Hernández', 'lucia@centrosnet.com', '1234', 'student');

-- =========================
-- COURSES
-- =========================
INSERT INTO courses (center_id, name, year)
VALUES
    (1, 'Profesional 1', 1),
    (1, 'Profesional 2', 2);

-- =========================
-- SUBJECTS
-- =========================
INSERT INTO subjects (center_id, name, duration_minutes)
VALUES
    (1, 'Instrumento', 60),
    (1, 'Lenguaje Musical', 60),
    (1, 'Banda', 90);

-- =========================
-- COURSE SUBJECTS
-- =========================
INSERT INTO course_subjects (course_id, subject_id)
VALUES
    (1, 1),
    (1, 2),
    (1, 3);

-- =========================
-- ENROLLMENTS
-- =========================
INSERT INTO enrollments (student_id, subject_id, course_id)
VALUES
    (3, 1, 1),
    (3, 2, 1),
    (3, 3, 1),
    (4, 1, 1),
    (4, 2, 1);

-- =========================
-- TEACHING ASSIGNMENTS
-- =========================
INSERT INTO teaching_assignments (teacher_id, subject_id, course_id)
VALUES
    (1, 1, 1), -- Ana → Instrumento
    (2, 2, 1), -- Luis → Lenguaje Musical
    (2, 3, 1); -- Luis → Banda

-- =========================
-- CLASS SESSIONS (SCHEDULE)
-- =========================
INSERT INTO class_sessions (
    subject_id,
    teacher_id,
    course_id,
    room_id,
    day_of_week,
    start_time,
    end_time
)
VALUES
    (1, 1, 1, 1, 1, '16:00:00', '17:00:00'), -- Lunes Instrumento
    (2, 2, 1, 2, 2, '17:00:00', '18:00:00'), -- Martes Lenguaje Musical
    (3, 2, 1, 1, 4, '18:00:00', '19:30:00'); -- Jueves Banda

-- =========================
-- AULAS CABINAS ESTUDIO (Centro 1)
-- =========================

INSERT INTO aulas (center_id, numero, tipo, estado)
VALUES
    (1, 300, 'cabina_estudio', 'libre'),
    (1, 301, 'cabina_estudio', 'libre'),
    (1, 302, 'cabina_estudio', 'libre'),
    (1, 303, 'cabina_estudio', 'libre'),
    (1, 304, 'cabina_estudio', 'libre'),
    (1, 305, 'cabina_estudio', 'libre'),
    (1, 306, 'cabina_estudio', 'libre'),
    (1, 307, 'cabina_estudio', 'libre'),
    (1, 308, 'cabina_estudio', 'libre'),
    (1, 309, 'cabina_estudio', 'libre');
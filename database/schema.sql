-- ============================================================
-- trasplantes.db - Script de creación de esquema y datos de prueba
-- Organ Transplant AutoMatch System
-- ============================================================

PRAGMA foreign_keys = ON;

DROP TABLE IF EXISTS historial;
DROP TABLE IF EXISTS organos;
DROP TABLE IF EXISTS pacientes;

-- ------------------------------------------------------------
-- Tabla: pacientes
-- ------------------------------------------------------------
CREATE TABLE pacientes (
                           id_paciente         INTEGER PRIMARY KEY AUTOINCREMENT,
                           nombre              TEXT NOT NULL,
                           tipo_sangre         TEXT NOT NULL CHECK (tipo_sangre IN ('A+','A-','B+','B-','AB+','AB-','O+','O-')),
                           organo_requerido    TEXT NOT NULL CHECK (organo_requerido IN ('HIGADO','CORAZON','RINON')),
                           fecha_inscripcion   TEXT NOT NULL,
                           activo              INTEGER NOT NULL DEFAULT 1,

    -- Campos MELD (usados si organo_requerido = 'HIGADO')
                           bilirrubina         REAL,
                           creatinina          REAL,
                           inr                 REAL,

    -- Urgencia UNOS (usada si organo_requerido = 'CORAZON')
                           urgencia_corazon    TEXT CHECK (urgencia_corazon IN ('1A','1B','2')),

    -- Histocompatibilidad HLA (usada si organo_requerido = 'RINON'), 0 a 6 alelos compatibles
                           hla_alelos          INTEGER CHECK (hla_alelos BETWEEN 0 AND 6),

    -- Tiempo en lista de espera en dias, usado como criterio de desempate
                           dias_espera         INTEGER NOT NULL DEFAULT 0
);

-- ------------------------------------------------------------
-- Tabla: organos
-- ------------------------------------------------------------
CREATE TABLE organos (
                         id_organo           INTEGER PRIMARY KEY AUTOINCREMENT,
                         tipo_organo         TEXT NOT NULL CHECK (tipo_organo IN ('HIGADO','CORAZON','RINON')),
                         tipo_sangre         TEXT NOT NULL CHECK (tipo_sangre IN ('A+','A-','B+','B-','AB+','AB-','O+','O-')),
                         donante_nombre      TEXT,
                         fecha_disponible    TEXT NOT NULL,
                         tiempo_isquemia_max  INTEGER NOT NULL DEFAULT 0, -- horas maximas viables
                         hla_alelos_donante  INTEGER CHECK (hla_alelos_donante BETWEEN 0 AND 6),
                         estado              TEXT NOT NULL DEFAULT 'DISPONIBLE' CHECK (estado IN ('DISPONIBLE','ASIGNADO','DESCARTADO'))
);

-- ------------------------------------------------------------
-- Tabla: historial
-- ------------------------------------------------------------
CREATE TABLE historial (
                           id_historial        INTEGER PRIMARY KEY AUTOINCREMENT,
                           id_paciente          INTEGER NOT NULL,
                           id_organo            INTEGER NOT NULL,
                           fecha_match          TEXT NOT NULL,
                           puntuacion_total     REAL NOT NULL,
                           detalle_puntuacion   TEXT, -- JSON o texto con el desglose
                           FOREIGN KEY (id_paciente) REFERENCES pacientes(id_paciente),
                           FOREIGN KEY (id_organo) REFERENCES organos(id_organo)
);

-- ------------------------------------------------------------
-- Datos de prueba: pacientes
-- ------------------------------------------------------------
INSERT INTO pacientes (nombre, tipo_sangre, organo_requerido, fecha_inscripcion, activo, bilirrubina, creatinina, inr, urgencia_corazon, hla_alelos, dias_espera) VALUES
                                                                                                                                                                      ('Carlos Mendoza',   'O+',  'HIGADO',  '2025-01-10', 1, 3.2, 1.8, 1.9, NULL, NULL, 240),
                                                                                                                                                                      ('Ana Torres',       'A+',  'HIGADO',  '2025-03-05', 1, 5.6, 2.4, 2.3, NULL, NULL, 190),
                                                                                                                                                                      ('Luis Ramirez',     'O-',  'HIGADO',  '2024-11-20', 1, 2.1, 1.2, 1.4, NULL, NULL, 300),
                                                                                                                                                                      ('Maria Fernandez',  'B+',  'CORAZON', '2025-02-14', 1, NULL, NULL, NULL, '1A', NULL, 60),
                                                                                                                                                                      ('Jorge Salinas',    'AB+', 'CORAZON', '2025-04-01', 1, NULL, NULL, NULL, '1B', NULL, 100),
                                                                                                                                                                      ('Patricia Gomez',   'O+',  'CORAZON', '2024-12-15', 1, NULL, NULL, NULL, '2',  NULL, 260),
                                                                                                                                                                      ('Ricardo Nunez',    'A-',  'RINON',   '2024-10-01', 1, NULL, NULL, NULL, NULL, 5, 340),
                                                                                                                                                                      ('Sofia Castillo',   'O+',  'RINON',   '2025-01-22', 1, NULL, NULL, NULL, NULL, 3, 230),
                                                                                                                                                                      ('Diego Herrera',    'B-',  'RINON',   '2025-05-10', 1, NULL, NULL, NULL, NULL, 6, 30),
                                                                                                                                                                      ('Elena Vargas',     'AB-', 'HIGADO',  '2025-06-01', 1, 4.0, 1.5, 1.8, NULL, NULL, 10);

-- ------------------------------------------------------------
-- Datos de prueba: organos
-- ------------------------------------------------------------
INSERT INTO organos (tipo_organo, tipo_sangre, donante_nombre, fecha_disponible, tiempo_isquemia_max, hla_alelos_donante, estado) VALUES
                                                                                                                                      ('HIGADO',  'O+',  'Donante Anonimo 1', '2025-09-10', 12, NULL, 'DISPONIBLE'),
                                                                                                                                      ('CORAZON', 'B+',  'Donante Anonimo 2', '2025-09-11', 6,  NULL, 'DISPONIBLE'),
                                                                                                                                      ('RINON',   'A-',  'Donante Anonimo 3', '2025-09-09', 24, 5,    'DISPONIBLE'),
                                                                                                                                      ('HIGADO',  'AB-', 'Donante Anonimo 4', '2025-09-11', 12, NULL, 'DISPONIBLE'),
                                                                                                                                      ('RINON',   'O+',  'Donante Anonimo 5', '2025-09-08', 24, 4,    'DISPONIBLE');

-- ------------------------------------------------------------
-- Datos de prueba: historial (ejemplo de match ya realizado)
-- ------------------------------------------------------------
INSERT INTO historial (id_paciente, id_organo, fecha_match, puntuacion_total, detalle_puntuacion) VALUES
    (3, 1, '2025-08-01', 92.5, '{"meld":18.4,"compatibilidad_sangre":100,"dias_espera":300,"nota":"Match historico de ejemplo"}');
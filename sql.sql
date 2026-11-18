-- =========================================================
-- SCHOOL MANAGEMENT SYSTEM
-- MYSQL DATABASE BACKEND
-- =========================================================

-- This script does NOT drop your existing database.
-- This script does NOT delete S001.
-- This script does NOT change your MySQL password.
-- This script does NOT change port 3306.
-- This script does NOT change Spring Boot port 8080.

CREATE DATABASE IF NOT EXISTS school_management
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

USE school_management;


-- =========================================================
-- BACKUP CURRENT STUDENT TABLE
-- =========================================================

CREATE TABLE IF NOT EXISTS
STUDENT_backup_before_backend
LIKE STUDENT;

INSERT IGNORE INTO
STUDENT_backup_before_backend
(
    Studentid,
    studentname,
    studentaddress,
    birthday
)
SELECT
    Studentid,
    studentname,
    studentaddress,
    birthday
FROM STUDENT;


-- =========================================================
-- STUDENT
-- =========================================================

CREATE TABLE IF NOT EXISTS STUDENT
(
    Studentid VARCHAR(255) NOT NULL,

    studentname VARCHAR(255) NULL,

    studentaddress VARCHAR(255) NULL,

    birthday VARCHAR(255) NULL,

    parent_name VARCHAR(255) NULL,

    parent_contact_no VARCHAR(50) NULL,

    PRIMARY KEY (Studentid)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;


-- Safely add parent_name if it is not already there.

SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = 'school_management'
            AND TABLE_NAME = 'STUDENT'
            AND COLUMN_NAME = 'parent_name'
        ),

        'SELECT 1',

        'ALTER TABLE STUDENT
         ADD COLUMN parent_name
         VARCHAR(255) NULL'
    )
);

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;


-- Safely add parent_contact_no
-- if it is not already there.

SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = 'school_management'
            AND TABLE_NAME = 'STUDENT'
            AND COLUMN_NAME = 'parent_contact_no'
        ),

        'SELECT 1',

        'ALTER TABLE STUDENT
         ADD COLUMN parent_contact_no
         VARCHAR(50) NULL'
    )
);

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;


-- =========================================================
-- TEACHER
-- =========================================================

CREATE TABLE IF NOT EXISTS teacher
(
    teacher_id VARCHAR(100) NOT NULL,

    teacher_name VARCHAR(255) NOT NULL,

    nic VARCHAR(30) NULL,

    address VARCHAR(500) NULL,

    PRIMARY KEY (teacher_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;


-- =========================================================
-- STUDENT GRADE
-- =========================================================

CREATE TABLE IF NOT EXISTS student_grade
(
    student_id VARCHAR(255) NOT NULL,

    grade VARCHAR(50) NOT NULL,

    PRIMARY KEY (student_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;


-- =========================================================
-- STUDENT PROFILE
-- =========================================================

CREATE TABLE IF NOT EXISTS student_profile
(
    student_id VARCHAR(255) NOT NULL,

    student_name VARCHAR(255) NULL,

    student_birth_date VARCHAR(50) NULL,

    student_address VARCHAR(500) NULL,

    achievements TEXT NULL,

    behaviour TEXT NULL,

    average_marks DOUBLE NOT NULL DEFAULT 0,

    PRIMARY KEY (student_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;


-- =========================================================
-- NON ACADEMIC STAFF
-- =========================================================

CREATE TABLE IF NOT EXISTS non_academic
(
    employee_id VARCHAR(100) NOT NULL,

    employee_name VARCHAR(255) NOT NULL,

    PRIMARY KEY (employee_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;


-- =========================================================
-- PHYSICAL PROPERTIES
-- =========================================================

CREATE TABLE IF NOT EXISTS physical_properties
(
    property_id VARCHAR(100) NOT NULL,

    property_name VARCHAR(255) NOT NULL,

    PRIMARY KEY (property_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;


-- =========================================================
-- ATTENDANCE
-- =========================================================

CREATE TABLE IF NOT EXISTS attendance
(
    attendance_id BIGINT NOT NULL
        AUTO_INCREMENT,

    student_id VARCHAR(255) NOT NULL,

    attendance_date VARCHAR(50) NOT NULL,

    class_name VARCHAR(100) NOT NULL,

    attendance_time VARCHAR(50) NOT NULL,

    PRIMARY KEY (attendance_id),

    INDEX idx_attendance_student
        (student_id),

    INDEX idx_attendance_date
        (attendance_date)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;


-- =========================================================
-- TEACHER ASSIGNMENT
-- =========================================================

CREATE TABLE IF NOT EXISTS teacher_assignment
(
    assignment_id VARCHAR(100) NOT NULL,

    subject_name VARCHAR(255) NOT NULL,

    assignment_date VARCHAR(50) NOT NULL,

    assignment_link VARCHAR(1000) NULL,

    PRIMARY KEY (assignment_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;


-- =========================================================
-- ASSIGNMENT MARKS
-- =========================================================

CREATE TABLE IF NOT EXISTS assignment_marks
(
    id BIGINT NOT NULL
        AUTO_INCREMENT,

    student_id VARCHAR(255) NOT NULL,

    student_name VARCHAR(255) NOT NULL,

    marks INT NOT NULL,

    assignment_id VARCHAR(100) NOT NULL,

    mark_date VARCHAR(50) NOT NULL,

    PRIMARY KEY (id),

    INDEX idx_assignment_marks_student
        (student_id),

    INDEX idx_assignment_marks_assignment
        (assignment_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;


-- =========================================================
-- SUBJECT MARKS
-- =========================================================

CREATE TABLE IF NOT EXISTS subject_marks
(
    id BIGINT NOT NULL
        AUTO_INCREMENT,

    student_id VARCHAR(255) NOT NULL,

    student_name VARCHAR(255) NOT NULL,

    marks INT NOT NULL,

    subject_id VARCHAR(100) NOT NULL,

    subject_name VARCHAR(255) NOT NULL,

    PRIMARY KEY (id),

    INDEX idx_subject_marks_student
        (student_id),

    INDEX idx_subject_marks_subject
        (subject_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;


-- =========================================================
-- GRADE 1 ADMISSION
-- =========================================================

CREATE TABLE IF NOT EXISTS admission_grade1
(
    admission_id VARCHAR(100) NOT NULL,

    marks INT NOT NULL DEFAULT 0,

    assign_date DATE NULL,

    birth_date DATE NULL,

    distance_km DOUBLE
        NOT NULL DEFAULT 0,

    parent_in_school BOOLEAN
        NOT NULL DEFAULT FALSE,

    sibling_in_school BOOLEAN
        NOT NULL DEFAULT FALSE,

    parent_teacher_or_principal BOOLEAN
        NOT NULL DEFAULT FALSE,

    parent_officer BOOLEAN
        NOT NULL DEFAULT FALSE,

    parent_abroad BOOLEAN
        NOT NULL DEFAULT FALSE,

    PRIMARY KEY (admission_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;


-- =========================================================
-- INTERMEDIATE ADMISSION
-- =========================================================

CREATE TABLE IF NOT EXISTS admission_intermediate
(
    admission_id VARCHAR(100) NOT NULL,

    marks INT NOT NULL DEFAULT 0,

    max_student_count INT
        NOT NULL DEFAULT 0,

    school_cut_off INT
        NOT NULL DEFAULT 0,

    students_g2 INT
        NOT NULL DEFAULT 0,

    students_g3 INT
        NOT NULL DEFAULT 0,

    students_g4 INT
        NOT NULL DEFAULT 0,

    students_g5 INT
        NOT NULL DEFAULT 0,

    students_g6 INT
        NOT NULL DEFAULT 0,

    students_g7 INT
        NOT NULL DEFAULT 0,

    students_g8 INT
        NOT NULL DEFAULT 0,

    students_g9 INT
        NOT NULL DEFAULT 0,

    students_g10 INT
        NOT NULL DEFAULT 0,

    PRIMARY KEY (admission_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;


-- =========================================================
-- A/L ADMISSION
-- =========================================================

CREATE TABLE IF NOT EXISTS admission_al
(
    admission_id VARCHAR(100) NOT NULL,

    marks INT NOT NULL DEFAULT 0,

    stream INT NOT NULL,

    criteria_met BOOLEAN
        NOT NULL DEFAULT FALSE,

    PRIMARY KEY (admission_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;


-- =========================================================
-- DATABASE VERIFICATION
-- =========================================================

SHOW TABLES;

DESCRIBE STUDENT;

SELECT *
FROM STUDENT;

SELECT COUNT(*) AS total_students
FROM STUDENT;

SELECT *
FROM STUDENT_backup_before_backend;

-- SMS Java/MySQL extension: creates missing existing account tables and new sms_* feature tables.
-- Does not drop or rename original project tables.
-- FIRST: back up school_management in MySQL Workbench (Server > Data Export).
-- Run with MySQL Workbench while connected as a user allowed to CREATE TABLE.
CREATE DATABASE IF NOT EXISTS school_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE school_management;

-- These are the EXISTING entity/table names from the prior report:
-- UserAccount -> APP_USER; SystemSetup -> SYSTEM_SETUP;
-- TeacherScope -> TEACHER_SCOPE. No duplicate SMS user/account entities.
-- CREATE IF NOT EXISTS will NOT change existing tables if already present.
-- If they exist with different columns, STOP and inspect your entity source.

CREATE TABLE IF NOT EXISTS APP_USER (
  user_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(80) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL,
  teacher_id VARCHAR(50) NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_app_user_username(username),
  UNIQUE KEY uk_app_user_teacher(teacher_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS SYSTEM_SETUP (
  setup_id INT NOT NULL PRIMARY KEY,
  admin_bootstrap_completed BOOLEAN NOT NULL DEFAULT FALSE,
  completed_at DATETIME(6) NULL
) ENGINE=InnoDB;
INSERT IGNORE INTO SYSTEM_SETUP(setup_id,admin_bootstrap_completed,completed_at)
 VALUES(1,FALSE,NULL);
-- If an ADMIN was previously created, permanently disable first-admin registration.
UPDATE SYSTEM_SETUP SET admin_bootstrap_completed=TRUE,
 completed_at=COALESCE(completed_at,CURRENT_TIMESTAMP(6))
 WHERE setup_id=1 AND EXISTS(SELECT 1 FROM APP_USER WHERE role='ADMIN');

CREATE TABLE IF NOT EXISTS TEACHER_SCOPE (
  scope_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  teacher_id VARCHAR(50) NOT NULL,
  grade_level INT NOT NULL,
  subject_id VARCHAR(50) NOT NULL,
  UNIQUE KEY uk_teacher_scope(teacher_id,grade_level,subject_id),
  INDEX idx_scope_teacher(teacher_id)
) ENGINE=InnoDB;

-- Existing `STUDENT` table remains untouched. student_id is a logical reference
-- (no foreign key until the actual existing Student table schema is verified).
CREATE TABLE IF NOT EXISTS sms_student_enrollment (
  student_id VARCHAR(60) NOT NULL PRIMARY KEY,
  grade_number INT NOT NULL,
  seat_type VARCHAR(16) NOT NULL DEFAULT 'ORDINARY',
  active BOOLEAN NOT NULL DEFAULT TRUE,
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT sms_enrollment_grade_chk CHECK(grade_number BETWEEN 1 AND 13),
  CONSTRAINT sms_enrollment_seat_chk CHECK(seat_type IN ('ORDINARY','SCHOLARSHIP')),
  INDEX sms_enrollment_grade_idx (grade_number,active,seat_type)
) ENGINE=InnoDB;

-- New durable academic records. Original Java classes SubjectMarks,
-- StudentProfile, Attendance and TeacherAssignment are NOT renamed or replaced.
CREATE TABLE IF NOT EXISTS sms_subject_marks (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  student_id VARCHAR(60) NOT NULL,
  subject_id VARCHAR(60) NOT NULL,
  grade_number INT NOT NULL,
  mark DECIMAL(6,2) NOT NULL,
  term_name VARCHAR(60) NOT NULL,
  entered_by VARCHAR(80) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT sms_marks_valid_chk CHECK(mark >= 0 AND mark <= 100),
  UNIQUE KEY sms_marks_unique (student_id,subject_id,term_name),
  INDEX sms_marks_lookup (student_id,subject_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sms_profile_note (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  student_id VARCHAR(60) NOT NULL,
  grade_number INT NOT NULL,
  subject_id VARCHAR(60) NOT NULL,
  achievements TEXT,
  behaviour TEXT,
  entered_by VARCHAR(80) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX sms_profile_student_idx(student_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sms_attendance (
  student_id VARCHAR(60) NOT NULL,
  attendance_date DATE NOT NULL,
  grade_number INT NOT NULL,
  subject_id VARCHAR(60) NOT NULL,
  present BOOLEAN NOT NULL,
  marked_by VARCHAR(80) NOT NULL,
  PRIMARY KEY (student_id,attendance_date,subject_id),
  INDEX sms_att_grade_idx(grade_number,attendance_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sms_teacher_assignment (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(160) NOT NULL,
  description TEXT,
  grade_number INT NOT NULL,
  subject_id VARCHAR(60) NOT NULL,
  link_url VARCHAR(2000),
  stored_file VARCHAR(120),
  original_filename VARCHAR(255),
  content_type VARCHAR(100),
  uploaded_by VARCHAR(80) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX sms_assign_class_idx(grade_number,subject_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sms_property (
  property_id VARCHAR(60) NOT NULL PRIMARY KEY,
  property_name VARCHAR(160) NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  condition_note VARCHAR(500),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT sms_property_quantity_chk CHECK(quantity >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sms_admission_setting (
  setting_key VARCHAR(60) NOT NULL PRIMARY KEY,
  setting_value INT NOT NULL,
  CONSTRAINT sms_setting_nonnegative_chk CHECK(setting_value >= 0)
) ENGINE=InnoDB;
INSERT IGNORE INTO sms_admission_setting(setting_key,setting_value) VALUES
 ('GRADE1_CAPACITY',0),('GRADE6_SCHOLARSHIP_EXTRA_SEATS',0),('GRADE6_SCHOLARSHIP_CUTOFF',0);

-- Grade 1: store every application; select first N by submitted_at,id.
CREATE TABLE IF NOT EXISTS sms_grade1_application (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  admission_id VARCHAR(60) NOT NULL UNIQUE,
  student_name VARCHAR(160),
  birth_date DATE NOT NULL,
  assign_date DATE,
  distance DECIMAL(8,2),
  marks INT,
  parent_in_school BOOLEAN NOT NULL DEFAULT FALSE,
  sibling_in_school BOOLEAN NOT NULL DEFAULT FALSE,
  parent_tea_prin BOOLEAN NOT NULL DEFAULT FALSE,
  parent_officer BOOLEAN NOT NULL DEFAULT FALSE,
  parent_abroad BOOLEAN NOT NULL DEFAULT FALSE,
  status VARCHAR(20) NOT NULL DEFAULT 'WAITLISTED',
  submitted_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX sms_grade1_order_idx(submitted_at,id)
) ENGINE=InnoDB;

-- Intermediate: special grade-6 seats separate from ordinary seats.
CREATE TABLE IF NOT EXISTS sms_intermediate_application (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  student_id VARCHAR(60) NOT NULL,
  grade_number INT NOT NULL,
  scholarship_mark INT,
  parent_transfer_km DECIMAL(8,2) NOT NULL DEFAULT 0,
  home_move_km DECIMAL(8,2) NOT NULL DEFAULT 0,
  status VARCHAR(24) NOT NULL DEFAULT 'WAITLISTED',
  submitted_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT sms_inter_grade_chk CHECK(grade_number BETWEEN 2 AND 10),
  UNIQUE KEY sms_inter_unique(student_id,grade_number),
  INDEX sms_inter_rank(grade_number,submitted_at,id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sms_al_stream_capacity (
  stream_name VARCHAR(24) NOT NULL PRIMARY KEY,
  seat_limit INT NOT NULL DEFAULT 0,
  CONSTRAINT sms_al_capacity_chk CHECK(seat_limit >= 0)
) ENGINE=InnoDB;
INSERT IGNORE INTO sms_al_stream_capacity(stream_name,seat_limit)
 VALUES ('SCIENCE',0),('TECHNOLOGY',0),('COMMERCE',0),('ARTS',0);

CREATE TABLE IF NOT EXISTS sms_al_application (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  student_id VARCHAR(60) NOT NULL,
  stream_name VARCHAR(24) NOT NULL,
  current_school_student BOOLEAN NOT NULL DEFAULT FALSE,
  status VARCHAR(20) NOT NULL DEFAULT 'WAITLISTED',
  submitted_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  UNIQUE KEY sms_al_student_stream(student_id,stream_name),
  INDEX sms_al_rank(stream_name,current_school_student,submitted_at,id),
  CONSTRAINT sms_al_stream_fk FOREIGN KEY(stream_name)
    REFERENCES sms_al_stream_capacity(stream_name)
) ENGINE=InnoDB;

-- Safe inspection after setup:
SELECT TABLE_NAME FROM information_schema.TABLES
WHERE TABLE_SCHEMA='school_management' AND TABLE_NAME LIKE 'sms\\_%'
ORDER BY TABLE_NAME;

-- READ-ONLY inspection queries after the backend is configured.
USE school_management;
SELECT username,role,teacher_code,active,created_at FROM sms_user_account;
SELECT * FROM sms_teacher_scope ORDER BY teacher_code,grade_number,subject_id;
SELECT * FROM sms_student_enrollment ORDER BY grade_number,student_id;
SELECT * FROM sms_admission_setting;
SELECT admission_id,status,submitted_at FROM sms_grade1_application ORDER BY submitted_at,id;
SELECT grade_number,status,COUNT(*) AS application_count FROM sms_intermediate_application GROUP BY grade_number,status;
SELECT stream_name,seat_limit FROM sms_al_stream_capacity;
SELECT stream_name,status,COUNT(*) AS applications FROM sms_al_application GROUP BY stream_name,status;
SELECT COALESCE(SUM(quantity),0) AS total_property_units FROM sms_property;

-- Verify that only one-time admin bootstrap is possible:
SELECT setup_id,admin_bootstrap_completed FROM SYSTEM_SETUP;
-- Existing real tables are NOT renamed:
SELECT table_name FROM information_schema.tables
 WHERE table_schema=DATABASE() AND table_name IN
 ('STUDENT','TEACHER','SUBJECTMARKS','APP_USER','SYSTEM_SETUP','TEACHER_SCOPE');
SELECT * FROM teacher;

drop 




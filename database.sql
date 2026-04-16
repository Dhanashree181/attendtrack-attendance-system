DROP DATABASE IF EXISTS student_attendance_db;

CREATE DATABASE student_attendance_db;

USE student_attendance_db;

-- Users
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'FACULTY', 'STUDENT') NOT NULL,
    linked_id VARCHAR(50)
);

-- Sections
CREATE TABLE sections (
    section_id INT PRIMARY KEY AUTO_INCREMENT,
    section_name VARCHAR(20) UNIQUE NOT NULL
);

-- Students  (is_active for soft-delete)
CREATE TABLE students (
    roll_number VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    section_id INT NOT NULL,
    email VARCHAR(100) UNIQUE,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (section_id) REFERENCES sections (section_id) ON DELETE CASCADE
);

-- Faculty  (is_active for soft-delete)
CREATE TABLE faculty (
    faculty_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

-- Courses  (is_active for soft-delete)
CREATE TABLE courses (
    course_id INT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

-- Timetable
CREATE TABLE timetable (
    timetable_id INT PRIMARY KEY AUTO_INCREMENT,
    section_id INT NOT NULL,
    course_id INT NOT NULL,
    faculty_id INT NOT NULL,
    day_of_week ENUM(
        'MONDAY',
        'TUESDAY',
        'WEDNESDAY',
        'THURSDAY',
        'FRIDAY',
        'SATURDAY'
    ) NOT NULL,
    period_number INT NOT NULL CHECK (period_number BETWEEN 1 AND 6),
    FOREIGN KEY (section_id) REFERENCES sections (section_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses (course_id) ON DELETE CASCADE,
    FOREIGN KEY (faculty_id) REFERENCES faculty (faculty_id) ON DELETE CASCADE,
    UNIQUE KEY unique_section_slot (
        section_id,
        day_of_week,
        period_number
    ),
    UNIQUE KEY unique_faculty_slot (
        faculty_id,
        day_of_week,
        period_number
    )
);

-- Class Session
CREATE TABLE class_session (
    session_id INT PRIMARY KEY AUTO_INCREMENT,
    timetable_id INT NOT NULL,
    session_date DATE NOT NULL,
    FOREIGN KEY (timetable_id) REFERENCES timetable (timetable_id) ON DELETE CASCADE,
    UNIQUE KEY unique_session (timetable_id, session_date)
);

-- Attendance
CREATE TABLE attendance (
    attendance_id INT PRIMARY KEY AUTO_INCREMENT,
    roll_number VARCHAR(20),
    session_id INT,
    status ENUM('PRESENT', 'ABSENT') DEFAULT 'ABSENT',
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (roll_number) REFERENCES students (roll_number) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES class_session (session_id) ON DELETE CASCADE,
    UNIQUE KEY unique_attendance (roll_number, session_id)
);

-- Seed Admin
INSERT INTO
    users (username, password, role)
VALUES ('admin', 'admin123', 'ADMIN');
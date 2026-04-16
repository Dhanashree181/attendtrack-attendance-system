# Project Report
# ATTENDTRACK – Student Attendance Management System

**Course:** Database Management / Java Programming (Capstone Project)  
**Date:** March 2026  
**Technology:** Java Swing + JDBC + MySQL

---

## Table of Contents
1. [Abstract](#1-abstract)
2. [Introduction](#2-introduction)
3. [Objectives](#3-objectives)
4. [System Requirements](#4-system-requirements)
5. [System Architecture](#5-system-architecture)
6. [Database Design](#6-database-design)
7. [Module Description](#7-module-description)
8. [Key Features](#8-key-features)
9. [Exception Handling](#9-exception-handling)
10. [Conclusion](#10-conclusion)
11. [Future Scope](#11-future-scope)

---

## 1. Abstract

**ATTENDTRACK – Student Attendance Management System** is a robust Java Swing desktop application designed to automate the recording and tracking of student attendance across academic sections, courses, and faculty members. The system features a modern Graphical User Interface (GUI) with role-based dashboards for Administrators, Faculty, and Students. It utilizes JDBC to interact with a MySQL database, implements the DAO design pattern for clean data separation, and incorporates advanced features such as real-time low-attendance alerts, a weekly timetable grid, and secure password management.

---

## 2. Introduction

Manual attendance tracking is prone to errors and lacks efficient reporting capabilities. ATTENDTRACK digitizes this process with a structured database-backed system. By transitioning from a console-based interface to a comprehensive GUI, the application now offers a user-friendly and intuitive experience for all stakeholders.

The system models a real academic environment:
- **Sections:** Students are organized into specific class sections.
- **Timetables:** Dynamic scheduling for sections, courses, and faculty.
- **Sessions:** Specific class instances conducted on a given date.
- **Attendance:** Accurate recording of student status per session.

---

## 3. Objectives

1. Develop a multi-role GUI desktop application (Admin / Faculty / Student).
2. Centralize academic data management in a relational MySQL database.
3. Automate class session creation through period-level attendance marking.
4. Implement strict business rules, such as preventing future-date attendance.
5. Ensure data integrity using soft-delete patterns for courses, faculty, and students.
6. Provide visual attendance reports with real-time status warnings (Good/Warning/Low).
7. Optimize the codebase by removing legacy console logic and dead code.

---

## 4. System Requirements

### Hardware
- Any system capable of running Java 8+ and MySQL 8.0.

### Software
- **JDK:** 8 or higher
- **MySQL:** 8.0
- **Database Driver:** MySQL Connector/J (JDBC Type-4)
- **Framework:** Java Swing / AWT
- **Build Directory:** `bin/` (Separates compiled `.class` files from source)

---

## 5. System Architecture

The system follows a **Layered Architecture** with a clear separation of concerns:

```
┌─────────────────────────────────────┐
│          GUI Layer (Swing)          │
│  (Dashboards, Panels, Dialogs)      │
└──────────────┬──────────────────────┘
               │ calls
┌──────────────▼──────────────────────┐
│           DAO Layer                 │
│  (AdminDAO, FacultyDAO, StudentDAO) │
└──────────────┬──────────────────────┘
               │ JDBC
┌──────────────▼──────────────────────┐
│         MySQL Database              │
│   (student_attendance_db)           │
└─────────────────────────────────────┘
```

### Key Design Patterns
- **DAO Pattern:** Isolates database operations from the UI logic.
- **Singleton Pattern:** Used for database connection management via `DBConnection`.
- **Observer-like Listeners:** GUI components react to user actions and data updates.

---

## 6. Database Design

### Core Tables
- **users:** Credentials and RBAC roles.
- **sections:** Academic class sections.
- **courses:** Subject details with soft-delete support.
- **faculty:** Faculty profiles with email and status.
- **students:** Student enrollment data linked to sections.
- **timetable:** Master schedule linking section, course, faculty, day, and period.
- **class_session:** Instances of classes on specific dates.
- **attendance:** Record of student presence per session.

### Integrity Mechanisms
- **Unique Constraints:** Prevents duplicate attendance for the same student-session.
- **Soft Deletion:** `is_active` flag preserves historical data while managing active entities.

---

## 7. Module Description

### 7.1 Admin Module
Provides full control over the institution's data. Features include entity CRUD, timetable scheduling with a weekly grid view, and historical attendance correction.

### 7.2 Faculty Module
Focused on daily operations. Faculty can view their specific daily schedule, mark attendance for any valid date, and track the percentage of their students per course.

### 7.3 Student Module
A personal portal where students can monitor their attendance. Features a high-visibility **Notice Board** for threshold warnings and a drill-down view for date-wise attendance details.

---

## 8. Key Features

### 8.1 Real-time Attendance Warning
Calculates cumulative attendance automatically. If percentage falls below 75%, a warning is displayed on the student dashboard.

### 8.2 Weekly Timetable Grid
A specialized grid view for administrators to visualize a section's weekly load at a glance, facilitating conflict-free scheduling.

### 8.3 Integrated Password Security
Every user module includes a "Change Password" panel with old-password verification, ensuring account security remains localized to the GUI environment.

---

## 9. Exception Handling

The system employs robust error handling:
- **SQLException Handling:** Caught at the DAO level and reported to the UI via user-friendly `JOptionPane` messages.
- **Input Validation:** GUI fields use regex and length checks to prevent invalid data entry (e.g., student roll numbers or email formats).
- **Resource Management:** All JDBC connections and statements are managed via `try-with-resources`.

---

## 10. Conclusion

ATTENDTRACK successfully bridges the gap between manual recording and modern data systems. By implementing a clean GUI and a normalized database structure, the project demonstrates proficiency in Java desktop development, database management systems, and object-oriented design principles.

---

## 11. Future Scope

- **Email Integration:** Automated SMTP alerts for low attendance.
- **Biometric API:** Integration with fingerprint scanners for automated marking.
- **Advanced Analytics:** SEM (Structural Equation Modeling) or similar trends for administrative reporting.
- **Cloud Migration:** Moving the database to a cloud provider like RDS for global accessibility.

---

*Report prepared for academic submission — ATTENDTRACK Project, 2026.*

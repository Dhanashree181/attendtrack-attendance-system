# ATTENDTRACK – Student Attendance Management System

**ATTENDTRACK** is a Java Swing-based desktop application designed for efficient, role-based student attendance management. The project provides tailored interfaces for Administrators, Faculty, and Students, ensuring streamlined workflows and accurate data tracking.

---

## 🚀 Overview

- **Type:** Java Swing Desktop Application
- **Database:** MySQL 8.0
- **Architecture:** DAO (Data Access Object) Pattern
- **Primary Interface:** Graphical User Interface (GUI)

---

## 🛠️ Module Features

### 👤 Admin Module
* **Entity Management:** Full CRUD (Add, Edit, Soft-delete/Reactivate) for Students, Faculty, Courses, and Sections.
* **Timetable Management:** Add, Edit, and Delete timetable slots. Includes a section-wise weekly grid view for easy scheduling.
* **Attendance Management:** 
    * Search and view attendance records by student roll number.
    * View overall attendance summaries for all students.
    * Correct/Edit existing attendance records.

### 👨‍🏫 Faculty Module
* **Dashboard:** Real-time view of today's scheduled classes.
* **Mark Attendance:** Intuitive interface to mark attendance by selecting a date and period slot. Supports bulk marking.
* **Course Attendance:** Detailed student-wise attendance summary for assigned courses.
* **Security:** Integrated "Change Password" feature.

### 🎓 Student Module
* **Dashboard:** Personal details and a dynamic **Notice Board** that displays attendance status and low-attendance warnings.
* **View Attendance:** Subject-wise attendance summary with status indicators. Click any row for a detailed date-wise breakdown.
* **Security:** Integrated "Change Password" feature.

---

## 📐 Key Design Decisions

* **Soft-Delete Pattern:** Students, Faculty, and Courses use an `is_active` flag. Records are never permanently deleted, allowing for reactivation and historical data integrity.
* **Direct DAO Pattern:** For project simplicity and performance, the GUI layers interact directly with specialized DAO classes (no redundant service layer).
* **Role-Based Access Control (RBAC):**
    * **Admins** have full control, including attendance corrections.
    * **Faculty** are restricted to marking attendance and viewing their course stats.
    * **Students** have read-only access to their own attendance data.
* **Status Thresholds:**
    * **Good:** Attendance ≥ 75.00%
    * **Warning:** 65.00% ≤ Attendance < 75.00% (Orange indicator)
    * **Low:** Attendance < 65.00% (Red indicator)

---

## 📂 Project Structure

```
Capstone/
├── MainGUI.java            # Main entry point (GUI Launch)
├── bin/                    # Compiled .class files (Build output)
├── dao/                    # Database Access Layer (JDBC)
│   ├── AdminDAO.java
│   ├── FacultyDAO.java
│   ├── StudentDAO.java
│   └── UserDAO.java
├── database/
│   └── DBConnection.java   # MySQL connection factory
├── gui/                    # GUI Controllers and Frames
│   ├── LoginFrame.java
│   ├── AdminDashboard.java
│   ├── FacultyDashboard.java
│   └── StudentDashboard.java
├── gui/panels/             # Specialized UI components/tabs
├── models/                 # Entity classes (POJOs)
├── lib/                    # External libraries (MySQL Connector)
└── docs/                   # Detailed documentation
```

---

## ⚙️ Setup Instructions

### 1. Prerequisites
- JDK 8 or higher
- MySQL Server 8.0+
- MySQL Connector/J (included in `lib/`)

### 2. Database Configuration
Update `database/DBConnection.java` with your MySQL credentials:
```java
private static final String URL      = "jdbc:mysql://localhost:3306/student_attendance_db";
private static final String USER     = "root";
private static final String PASSWORD = "your_password";
```

### 3. Initialize Database
Run the schema and demo data scripts provided in the root directory (`database.sql` and `demo_data.sql`).

### 4. Compile & Run

#### Step A: Create Build Directory
```powershell
mkdir bin
```

#### Step B: Compile to bin Folder
```powershell
javac -d bin -cp ".;lib/*" MainGUI.java
```

#### Step C: Run from bin Folder
```powershell
java -cp "bin;lib/*" MainGUI
```

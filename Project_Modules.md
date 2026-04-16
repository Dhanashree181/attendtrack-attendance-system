# Project Modules

## Architecture Overview

ATTENDTRACK follows a **Layered Architecture** with a clear separation between the User Interface and the Data Access Layer.

```
       GUI LAYER (Swing)
  (Admin, Faculty, Student Dashboards)
               ↓
       DATA ACCESS LAYER (JDBC)
  (AdminDAO, FacultyDAO, StudentDAO, UserDAO)
               ↓
       DATABASE LAYER (MySQL)
```

---

## 🔐 Module 1 — Authentication (`gui.LoginFrame`, `dao.UserDAO`)

Handles application access and role-based routing.

*   **Components:** `LoginFrame` (Login UI), `SessionManager` (Current user state).
*   **Key Logic:**
    *   `UserDAO.login(user, pass)`: Validates credentials against the `users` table.
    *   `SessionManager.setCurrentUser(user)`: Stores the logged-in session globally.
    *   **Routing:** Directs users to `AdminDashboard`, `FacultyDashboard`, or `StudentDashboard` based on their role.

---

## 🛠️ Module 2 — Admin Module (`gui.AdminDashboard`, `dao.AdminDAO`)

Comprehensive system administration hub.

### Sub-Modules & Panels:
*   **Entity Management:** `StudentManagePanel`, `FacultyManagePanel`, `CourseManagePanel`, `SectionManagePanel`.
    *   Supports: Add, Edit, Soft-delete (via `is_active` flag), and Reactivate.
*   **Timetable Management:** `TimetableManagePanel`.
    *   `AdminDAO.addTimetableSlot(...)`: Schedules a course-section-faculty triad.
    *   **Weekly Grid:** Provides a matrix view of the section's schedule.
*   **Attendance Oversight:** `AdminAttendancePanel`.
    *   `AdminDAO.getAllStudents()`: View overall stats.
    *   `AdminDAO.updateAttendanceRecord(...)`: Allows corrections to previously marked attendance.

---

## 👨‍🏫 Module 3 — Faculty Module (`gui.FacultyDashboard`, `dao.FacultyDAO`)

Operational module for attendance marking and course tracking.

*   **Today's Timetable:** Integrated into the dashboard via `FacultyDashboardPanel`.
    *   `FacultyDAO.getTodaySlots(facultyId, day)`: Fetches scheduled classes for the current system date.
*   **Attendance Marking:** `MarkAttendancePanel`.
    *   `FacultyDAO.getOrCreateSession(...)`: Ensures a class session exists for the given slot and date.
    *   `FacultyDAO.markAttendance(...)`: Atomic UPSERT for student attendance records.
*   **Analytics:** `CourseAttendancePanel`.
    *   Calculates per-student percentage per course section.

---

## 🎓 Module 4 — Student Module (`gui.StudentDashboard`, `dao.StudentDAO`)

Personal attendance portal for students.

*   **Dashboard & Notice Board:** `StudentDashboard` home screen.
    *   Displays current enrollment details.
    *   **Warning System:** Triggers alerts if any subject's attendance falls below the 75% threshold.
*   **Detailed View:** `StudentAttendancePanel`.
    *   `StudentDAO.getAttendancePerCourse(roll)`: Aggregates summary stats.
    *   `StudentDAO.getDetailedAttendance(roll, course)`: Returns a date-wise history of PRESENT/ABSENT marks for a specific subject.

---

## 🗃️ Module 5 — Data Access Layer (`dao/`)

| DAO | Key Methods | Responsibilities |
|---|---|---|
| `UserDAO` | `login`, `addUser`, `changePassword` | Credential security and session entry. |
| `AdminDAO` | `addStudent`, `softDeleteFaculty`, `updateTimetableSlot` | Master data orchestration and historical edits. |
| `FacultyDAO` | `getOrCreateSession`, `markAttendance`, `getTodaySlots` | Operational data entry and session management. |
| `StudentDAO` | `getAttendancePerCourse`, `getDetailedAttendance` | Aggregate reporting and student history. |

---

## ⚙️ Module 6 — Database Management (`database/`)

*   **DBConnection:** A singleton-pattern utility that manages the JDBC `Connection` pool to the MySQL server.
*   **Schema Persistence:** Uses standard SQL constraints (Foreign Keys, Unique Indices) to ensure data integrity across Timetables, Sessions, and Attendance records.

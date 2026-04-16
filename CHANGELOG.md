# CHANGELOG

All notable changes to the ATTENDTRACK project will be documented in this file.

## [1.1.0] - 2026-03-20

### Added
- **Java Swing GUI:** Migrated the entire application interface from console-based to Graphical User Interface (GUI).
    - `LoginFrame`: New graphical login screen with role-based routing.
    - `AdminDashboard`, `FacultyDashboard`, `StudentDashboard`: Main application hubs using `JTabbedPane` for feature organization.
    - Specialized panels for all entity management (Students, Faculty, Courses, Sections).
    - Integrated Weekly Timetable Grid view for Admins.
    - Attendance "Notice Board" for students with dynamic warnings.
- **Optimized Build Process:** Introduced a separate `bin/` directory for compiled `.class` files, keeping the source code clean.
- **Enhanced Visual Feedback:** Added color-coded status indicators for attendance percentages (Red for Low, Orange for Warning).

### Removed
- **Legacy Console Code:** 
    - Dedicated console menu classes (`AdminMenu`, `FacultyMenu`, `StudentMenu`, `MainMenu`).
    - Console-specific utility classes (`ValidationUtil`, `PasswordUtil`).
- **Dead Code Cleanup:**
    - Removed unused methods in `FacultyDAO` and `StudentDAO` that were only required for console menus.
    - Deleted `FacultyTodayPanel.java` after merging its logic into the main dashboard.

### Changed
- **Entry Point:** Application now launches via `MainGUI.java`.
- **Soft-Delete Implementation:** Standardized the use of `is_active` across all entities with a "Reactivate" option in the UI.

---

## [1.0.0] - 2026-03-14
- Initial release (Console-based version).
- Core DAO and MySQL database structure.

---

## 🚀 Known Limitations & Future Scope

### Known Limitations
- **Local Database:** Currently requires a local MySQL instance; no cloud database integration.
- **Report Export:** Dashboard views are available, but there is no direct "Export to PDF/Excel" feature for attendance reports.
- **Session Timeout:** No automatic session expiration for logged-in users.

### Future Scope
- **Email Notifications:** Integration of an SMTP server to send automatic low-attendance alerts to students and parents.
- **Biometric Integration:** Support for fingerprint or face-recognition based attendance marking.
- **Mobile Companion:** Development of a lightweight mobile app for students to check attendance on the go.
- **Advanced Analytics:** Charts and graphs for administrators to track attendance trends across different semesters.

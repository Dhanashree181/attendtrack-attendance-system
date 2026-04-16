# Exception Handling Strategy

The system uses a consistent, layered approach to exception management: **DAOs throw, GUI catch and display**.

---

## 1. Database Exceptions (`SQLException`)

**Where:** All DAO methods (`UserDAO`, `AdminDAO`, `FacultyDAO`, `StudentDAO`).

**Approach:**
- Every DAO method declares `throws SQLException` — it propagates the exception up without swallowing it.
- The GUI layer (Dashboards and Panels) wraps DAO calls in `try-catch (SQLException e)` blocks.
- Errors are displayed to the user via `JOptionPane.showMessageDialog()` with an "Error" icon.
- This ensures the application remains responsive even if a database operation fails.

**Example flow:**
```
AdminDAO.addCourse() → throws SQLException
        ↓
CourseManagePanel.addCourse() → catches and displays popup: "Error: Course already exists."
```

---

## 2. Resource Management (try-with-resources)

**Where:** Every JDBC operation in every DAO.

**Approach:**
- `Connection`, `PreparedStatement`/`Statement`, and `ResultSet` are all declared in the `try(...)` header.
- Java automatically closes them in reverse order on both normal exit and on exception, preventing connection leaks.

```java
try (Connection conn = DBConnection.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    // ...
}
```

---

## 3. Input Validation

**Where:** GUI Panels (`MarkAttendancePanel`, `StudentManagePanel`, etc.).

**Approach:**
- Before calling DAO methods, the GUI performs lightweight validation:
    - **Empty Fields:** Checks if required text fields are blank.
    - **Data Types:** Uses `Integer.parseInt()` for numeric fields, catching `NumberFormatException` to show a "Please enter a valid number" popup.
    - **Regex:** Validates email formats and roll number patterns.
- If validation fails, a warning dialog is shown and the DAO call is skipped.

---

## 4. Missing JDBC Driver (`ClassNotFoundException`)

**Where:** `DBConnection` static initialiser block.

**Approach:**
- `Class.forName("com.mysql.cj.jdbc.Driver")` is wrapped in `try-catch (ClassNotFoundException)`.
- If the driver is missing, a diagnostic message is printed to the system error log, and subsequent connection attempts will report a "Database connection failed" error in the UI.

---

## 5. Future Date Rejection (Business Logic)

**Where:** `MarkAttendancePanel`.

**Approach:**
- The date picker or input field is checked against `LocalDate.now()`.
- If a future date is selected, the application displays a warning: "Cannot mark attendance for a future date" and prevents the submission.

---

## 6. Duplicate Attendance (Database Constraint)

**Where:** `attendance` table DDL + `FacultyDAO.markAttendance()`.

**Approach:**
- The `attendance` table has a `UNIQUE KEY (roll_number, session_id)`.
- The DAO uses `INSERT … ON DUPLICATE KEY UPDATE status = ?`.
- This ensures that if a faculty member re-marks attendance for the same slot, the database updates the existing row instead of throwing a duplicate-entry error.

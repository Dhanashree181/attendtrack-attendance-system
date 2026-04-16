package dao;

import models.Faculty;
import models.TimetableSlot;
import models.Attendance;
import models.Student;
import database.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for faculty-related database operations.
 * Covers timetable queries, class session management, student retrieval
 * by section, and attendance marking.
 */
public class FacultyDAO {

    // ==============================================
    // TIMETABLE: Get today's slots for this faculty
    // Optionally filter by sectionId (0 = all sections)
    // ==============================================
    public List<TimetableSlot> getTodaySlots(int facultyId, String dayOfWeek, int sectionId) throws SQLException {
        List<TimetableSlot> list = new ArrayList<>();
        String sql = "SELECT t.timetable_id, s.section_id, s.section_name, " +
                     "c.course_id, c.course_code, c.course_name, " +
                     "f.faculty_id, f.name AS faculty_name, " +
                     "t.day_of_week, t.period_number " +
                     "FROM timetable t " +
                     "JOIN sections s ON t.section_id = s.section_id " +
                     "JOIN courses c ON t.course_id = c.course_id " +
                     "JOIN faculty f ON t.faculty_id = f.faculty_id " +
                     "WHERE t.faculty_id = ? AND t.day_of_week = ? AND c.is_active = TRUE" +
                     (sectionId > 0 ? " AND t.section_id = ?" : "") +
                     " ORDER BY t.period_number";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, facultyId);
            pstmt.setString(2, dayOfWeek.toUpperCase());
            if (sectionId > 0) pstmt.setInt(3, sectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new TimetableSlot(
                        rs.getInt("timetable_id"),
                        rs.getInt("section_id"), rs.getString("section_name"),
                        rs.getInt("course_id"), rs.getString("course_code"), rs.getString("course_name"),
                        rs.getInt("faculty_id"), rs.getString("faculty_name"),
                        rs.getString("day_of_week"), rs.getInt("period_number")
                    ));
                }
            }
        }
        return list;
    }

    // Overload: no section filter (all sections)
    public List<TimetableSlot> getTodaySlots(int facultyId, String dayOfWeek) throws SQLException {
        return getTodaySlots(facultyId, dayOfWeek, 0);
    }


    // ==============================================
    // TIMETABLE: One entry per unique (course, section) pair
    // Used by CourseAttendancePanel dropdown so each course-section
    // appears only once regardless of how many periods/week it has.
    // ==============================================
    public List<TimetableSlot> getDistinctCourseSlots(int facultyId) throws SQLException {
        List<TimetableSlot> list = new ArrayList<>();
        String sql = "SELECT MIN(t.timetable_id) AS timetable_id, " +
                     "s.section_id, s.section_name, " +
                     "c.course_id, c.course_code, c.course_name, " +
                     "f.faculty_id, f.name AS faculty_name, " +
                     "'' AS day_of_week, 0 AS period_number " +
                     "FROM timetable t " +
                     "JOIN sections s ON t.section_id = s.section_id " +
                     "JOIN courses c  ON t.course_id  = c.course_id  " +
                     "JOIN faculty f  ON t.faculty_id = f.faculty_id " +
                     "WHERE t.faculty_id = ? " +
                     "GROUP BY c.course_id, s.section_id, f.faculty_id " +
                     "ORDER BY c.course_code, s.section_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, facultyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new TimetableSlot(
                        rs.getInt("timetable_id"),
                        rs.getInt("section_id"), rs.getString("section_name"),
                        rs.getInt("course_id"), rs.getString("course_code"), rs.getString("course_name"),
                        rs.getInt("faculty_id"), rs.getString("faculty_name"),
                        rs.getString("day_of_week"), rs.getInt("period_number")
                    ));
                }
            }
        }
        return list;
    }

    // ==============================================
    // FACULTY: Get faculty record by ID
    // ==============================================
    public Faculty getFacultyById(int facultyId) throws SQLException {
        String sql = "SELECT * FROM faculty WHERE faculty_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Faculty f = new Faculty(rs.getInt("faculty_id"), rs.getString("name"), rs.getString("email"));
                    f.setActive(rs.getBoolean("is_active"));
                    return f;
                }
            }
        }
        return null;
    }

    // ==============================================
    // SESSION: Auto-create or retrieve session
    // ==============================================
    public int getOrCreateSession(int timetableId, java.sql.Date date) throws SQLException {
        // Use a single connection + transaction to avoid a race condition where two
        // concurrent requests could each see no existing session and both INSERT.
        String findSql   = "SELECT session_id FROM class_session WHERE timetable_id = ? AND session_date = ?";
        String insertSql = "INSERT IGNORE INTO class_session (timetable_id, session_date) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Step 1: check for an existing session
                try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                    ps.setInt(1, timetableId);
                    ps.setDate(2, date);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            conn.commit();
                            return rs.getInt("session_id");
                        }
                    }
                }
                // Step 2: insert (IGNORE silently skips on duplicate key)
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, timetableId);
                    ps.setDate(2, date);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            conn.commit();
                            return keys.getInt(1);
                        }
                    }
                }
                // Step 3: INSERT IGNORE produced 0 rows (lost race) – SELECT again
                try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                    ps.setInt(1, timetableId);
                    ps.setDate(2, date);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            conn.commit();
                            return rs.getInt("session_id");
                        }
                    }
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return -1;
    }

    // ==============================================
    // STUDENTS: Get all students in a section
    // ==============================================
    public List<Student> getStudentsBySection(int sectionId) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE section_id = ? AND is_active = TRUE ORDER BY roll_number";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(
                        rs.getString("roll_number"),
                        rs.getString("name"),
                        rs.getInt("section_id"),
                        rs.getString("email")
                    ));

                }
            }
        }
        return list;
    }

    // ==============================================
    // ATTENDANCE: Mark (insert or update)
    // ==============================================
    public void markAttendance(Attendance attendance) throws SQLException {
        String sql = "INSERT INTO attendance (roll_number, session_id, status) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, attendance.getRollNumber());
            pstmt.setInt(2, attendance.getSessionId());
            pstmt.setString(3, attendance.getStatus());
            pstmt.setString(4, attendance.getStatus());
            pstmt.executeUpdate();
        }
    }

    // ==============================================
    // VIEW ATTENDANCE: Per course for a period slot
    // ==============================================
    public int getStudentPresentCount(String rollNumber, int courseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance a " +
                     "JOIN class_session cs ON a.session_id = cs.session_id " +
                     "JOIN timetable t ON cs.timetable_id = t.timetable_id " +
                     "WHERE a.roll_number = ? AND t.course_id = ? AND a.status = 'PRESENT'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            pstmt.setInt(2, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Returns the total number of class sessions held for a specific course
     * AND section combination, so that attendance percentages are calculated
     * correctly when the same course is taught in multiple sections.
     */
    public int getTotalSessionsCount(int courseId, int sectionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM class_session cs " +
                     "JOIN timetable t ON cs.timetable_id = t.timetable_id " +
                     "WHERE t.course_id = ? AND t.section_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, sectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }
}

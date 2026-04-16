package dao;

import models.Student;
import models.TimetableSlot;
import database.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for student-related database operations.
 * Provides methods to add, retrieve, and query attendance statistics
 * for students. Used by both the student-facing menu and the admin module.
 */
public class StudentDAO {

    public void addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (roll_number, name, section_id, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getRollNumber());
            pstmt.setString(2, student.getName());
            pstmt.setInt(3, student.getSectionId());
            if (student.getEmail() != null && !student.getEmail().isEmpty())
                pstmt.setString(4, student.getEmail());
            else
                pstmt.setNull(4, Types.VARCHAR);
            pstmt.executeUpdate();
        }
    }

    public Student getStudentByRollNumber(String rollNumber) throws SQLException {
        String sql = "SELECT * FROM students WHERE roll_number = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Student s = new Student(
                        rs.getString("roll_number"),
                        rs.getString("name"),
                        rs.getInt("section_id"),
                        rs.getString("email")
                    );
                    s.setStatus(rs.getString("status"));
                    return s;
                }
            }
        }
        return null;
    }


    /**
     * Returns subject-wise attendance percentage for a student.
     * Joins through timetable to correctly count period-wise sessions.
     */
    public Map<String, double[]> getAttendancePerCourse(String rollNumber) throws SQLException {
        // LinkedHashMap preserves insertion order
        Map<String, double[]> stats = new LinkedHashMap<>();

        String sql = "SELECT c.course_code, c.course_name, " +
                     "COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) AS present_count, " +
                     "COUNT(cs.session_id) AS total_sessions " +
                     "FROM courses c " +
                     "JOIN timetable t ON c.course_id = t.course_id " +
                     "JOIN sections sec ON t.section_id = sec.section_id " +
                     "JOIN students st ON st.section_id = sec.section_id AND st.roll_number = ? " +
                     "LEFT JOIN class_session cs ON t.timetable_id = cs.timetable_id " +
                     "LEFT JOIN attendance a ON cs.session_id = a.session_id AND a.roll_number = ? " +
                     "GROUP BY c.course_id, c.course_code, c.course_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            pstmt.setString(2, rollNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("course_code") + " - " + rs.getString("course_name");
                    int present = rs.getInt("present_count");
                    int total = rs.getInt("total_sessions");
                    double pct = (total > 0) ? ((double) present / total) * 100 : 0.0;
                    stats.put(key, new double[]{present, total, pct});
                }
            }
        }
        return stats;
    }

    /**
     * Returns today's timetable slots for a given section and day of week.
     * Used by the student dashboard to display today's class schedule.
     */
    public List<TimetableSlot> getTodayTimetable(int sectionId, String dayOfWeek) throws SQLException {
        List<TimetableSlot> list = new ArrayList<>();
        String sql = "SELECT t.timetable_id, s.section_id, s.section_name, " +
                     "c.course_id, c.course_code, c.course_name, " +
                     "f.faculty_id, f.name AS faculty_name, t.day_of_week, t.period_number " +
                     "FROM timetable t " +
                     "JOIN sections s ON t.section_id = s.section_id " +
                     "JOIN courses c ON t.course_id = c.course_id " +
                     "JOIN faculty f ON t.faculty_id = f.faculty_id " +
                     "WHERE t.section_id = ? AND t.day_of_week = ? AND c.is_active = TRUE " +
                     "ORDER BY t.period_number";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.setString(2, dayOfWeek.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
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

    /**
     * Returns date-wise detailed attendance for a specific course for the student.
     */
    public List<String[]> getDetailedAttendance(String rollNumber, String courseCode) throws SQLException {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT cs.session_date, t.period_number, t.day_of_week, COALESCE(a.status, 'ABSENT') AS status " +
                     "FROM class_session cs " +
                     "JOIN timetable t ON cs.timetable_id = t.timetable_id " +
                     "JOIN courses c ON t.course_id = c.course_id " +
                     "JOIN students st ON st.section_id = t.section_id " +
                     "LEFT JOIN attendance a ON cs.session_id = a.session_id AND a.roll_number = st.roll_number " +
                     "WHERE st.roll_number = ? AND c.course_code = ? " +
                     "ORDER BY cs.session_date DESC, t.period_number DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber); ps.setString(2, courseCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new String[]{
                        rs.getString("session_date"),
                        String.valueOf(rs.getInt("period_number")),
                        rs.getString("day_of_week"), rs.getString("status")});
            }
        }
        return result;
    }
}

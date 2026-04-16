package dao;

import models.Course;
import models.Faculty;
import models.Section;
import models.TimetableSlot;
import models.Student;
import database.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for all administrative database operations.
 * Manages sections, courses, faculty, students, timetable slots,
 * and attendance record edits. Enforces is_active soft-delete for
 * courses, faculty, and students.
 */
public class AdminDAO {

    // ======================== SECTION ========================

    public void addSection(Section section) throws SQLException {
        String sql = "INSERT INTO sections (section_name) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, section.getSectionName());
            ps.executeUpdate();
        }
    }

    public void updateSection(Section section) throws SQLException {
        String sql = "UPDATE sections SET section_name = ? WHERE section_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, section.getSectionName());
            ps.setInt(2, section.getSectionId());
            ps.executeUpdate();
        }
    }

    public List<Section> getAllSections() throws SQLException {
        List<Section> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM sections ORDER BY section_name")) {
            while (rs.next())
                list.add(new Section(rs.getInt("section_id"), rs.getString("section_name")));
        }
        return list;
    }

    // ======================== COURSE ========================

    public void addCourse(Course course) throws SQLException {
        String sql = "INSERT INTO courses (course_code, course_name) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, course.getCourseCode());
            ps.setString(2, course.getCourseName());
            ps.executeUpdate();
        }
    }

    public List<Course> getAllCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY course_code";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Course c = new Course(rs.getInt("course_id"), rs.getString("course_code"), rs.getString("course_name"));
                c.setActive(rs.getBoolean("is_active"));
                list.add(c);
            }
        }
        return list;
    }

    public Course getCourseById(int id) throws SQLException {
        String sql = "SELECT * FROM courses WHERE course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Course c = new Course(rs.getInt("course_id"), rs.getString("course_code"), rs.getString("course_name"));
                    c.setActive(rs.getBoolean("is_active"));
                    return c;
                }
            }
        }
        return null;
    }

    public void updateCourse(Course course) throws SQLException {
        String sql = "UPDATE courses SET course_name = ? WHERE course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, course.getCourseName());
            ps.setInt(2, course.getCourseId());
            ps.executeUpdate();
        }
    }

    public void softDeleteCourse(int courseId) throws SQLException {
        String sql = "UPDATE courses SET is_active = FALSE WHERE course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.executeUpdate();
        }
    }

    public void reactivateCourse(int courseId) throws SQLException {
        String sql = "UPDATE courses SET is_active = TRUE WHERE course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.executeUpdate();
        }
    }

    // ======================== FACULTY ========================

    public int addFaculty(Faculty faculty) throws SQLException {
        String sql = "INSERT INTO faculty (name, email) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, faculty.getName());
            ps.setString(2, faculty.getEmail());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) return k.getInt(1); }
        }
        return -1;
    }

    public List<Faculty> getAllFaculty() throws SQLException {
        List<Faculty> list = new ArrayList<>();
        String sql = "SELECT * FROM faculty ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Faculty f = new Faculty(rs.getInt("faculty_id"), rs.getString("name"), rs.getString("email"));
                f.setActive(rs.getBoolean("is_active"));
                list.add(f);
            }
        }
        return list;
    }

    public Faculty getFacultyById(int id) throws SQLException {
        String sql = "SELECT * FROM faculty WHERE faculty_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
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

    public void updateFaculty(Faculty faculty) throws SQLException {
        String sql = "UPDATE faculty SET name = ?, email = ? WHERE faculty_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, faculty.getName());
            ps.setString(2, faculty.getEmail());
            ps.setInt(3, faculty.getFacultyId());
            ps.executeUpdate();
        }
    }

    public void softDeleteFaculty(int facultyId) throws SQLException {
        String sql = "UPDATE faculty SET is_active = FALSE WHERE faculty_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            ps.executeUpdate();
        }
    }

    public void reactivateFaculty(int facultyId) throws SQLException {
        String sql = "UPDATE faculty SET is_active = TRUE WHERE faculty_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            ps.executeUpdate();
        }
    }

    // ======================== STUDENT ========================

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY roll_number";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Student s = new Student(rs.getString("roll_number"), rs.getString("name"),
                    rs.getInt("section_id"), rs.getString("email"));
                s.setStatus(rs.getString("status"));
                s.setActive(rs.getBoolean("is_active"));
                list.add(s);
            }
        }
        return list;
    }

    public Student getStudentByRoll(String roll) throws SQLException {
        String sql = "SELECT * FROM students WHERE roll_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roll);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = new Student(rs.getString("roll_number"), rs.getString("name"),
                        rs.getInt("section_id"), rs.getString("email"));
                    s.setStatus(rs.getString("status"));
                    s.setActive(rs.getBoolean("is_active"));
                    return s;
                }
            }
        }
        return null;
    }

    public void updateStudent(Student student) throws SQLException {
        String sql = "UPDATE students SET name = ?, email = ?, section_id = ? WHERE roll_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getName());
            if (student.getEmail() != null && !student.getEmail().isEmpty())
                ps.setString(2, student.getEmail());
            else
                ps.setNull(2, Types.VARCHAR);
            ps.setInt(3, student.getSectionId());
            ps.setString(4, student.getRollNumber());
            ps.executeUpdate();
        }
    }

    public void softDeleteStudent(String rollNumber) throws SQLException {
        String sql = "UPDATE students SET is_active = FALSE, status = 'INACTIVE' WHERE roll_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber);
            ps.executeUpdate();
        }
    }

    public void reactivateStudent(String rollNumber) throws SQLException {
        String sql = "UPDATE students SET is_active = TRUE, status = 'ACTIVE' WHERE roll_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber);
            ps.executeUpdate();
        }
    }

    // ======================== TIMETABLE ========================

    public void addTimetableSlot(int sectionId, int courseId, int facultyId,
                                  String dayOfWeek, int periodNumber) throws SQLException {
        String sql = "INSERT INTO timetable (section_id, course_id, faculty_id, day_of_week, period_number) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId); ps.setInt(2, courseId); ps.setInt(3, facultyId);
            ps.setString(4, dayOfWeek.toUpperCase()); ps.setInt(5, periodNumber);
            ps.executeUpdate();
        }
    }

    public void updateTimetableSlot(int timetableId, int courseId, int facultyId) throws SQLException {
        String sql = "UPDATE timetable SET course_id = ?, faculty_id = ? WHERE timetable_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setInt(2, facultyId);
            ps.setInt(3, timetableId);
            ps.executeUpdate();
        }
    }

    public void deleteTimetableSlot(int timetableId) throws SQLException {
        String sql = "DELETE FROM timetable WHERE timetable_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, timetableId); ps.executeUpdate();
        }
    }

    public List<TimetableSlot> getAllTimetableSlots() throws SQLException {
        List<TimetableSlot> list = new ArrayList<>();
        String sql = "SELECT t.timetable_id, s.section_id, s.section_name, " +
                     "c.course_id, c.course_code, c.course_name, " +
                     "f.faculty_id, f.name AS faculty_name, t.day_of_week, t.period_number " +
                     "FROM timetable t " +
                     "JOIN sections s ON t.section_id = s.section_id " +
                     "JOIN courses c ON t.course_id = c.course_id " +
                     "JOIN faculty f ON t.faculty_id = f.faculty_id " +
                     "ORDER BY s.section_name, t.day_of_week, t.period_number";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new TimetableSlot(rs.getInt("timetable_id"),
                    rs.getInt("section_id"), rs.getString("section_name"),
                    rs.getInt("course_id"), rs.getString("course_code"), rs.getString("course_name"),
                    rs.getInt("faculty_id"), rs.getString("faculty_name"),
                    rs.getString("day_of_week"), rs.getInt("period_number")));
        }
        return list;
    }

    public List<TimetableSlot> getTimetableSlotsBySection(int sectionId) throws SQLException {
        List<TimetableSlot> list = new ArrayList<>();
        String sql = "SELECT t.timetable_id, s.section_id, s.section_name, " +
                     "c.course_id, c.course_code, c.course_name, " +
                     "f.faculty_id, f.name AS faculty_name, t.day_of_week, t.period_number " +
                     "FROM timetable t " +
                     "JOIN sections s ON t.section_id = s.section_id " +
                     "JOIN courses c ON t.course_id = c.course_id " +
                     "JOIN faculty f ON t.faculty_id = f.faculty_id " +
                     "WHERE t.section_id = ? " +
                     "ORDER BY t.day_of_week, t.period_number";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new TimetableSlot(rs.getInt("timetable_id"),
                        rs.getInt("section_id"), rs.getString("section_name"),
                        rs.getInt("course_id"), rs.getString("course_code"), rs.getString("course_name"),
                        rs.getInt("faculty_id"), rs.getString("faculty_name"),
                        rs.getString("day_of_week"), rs.getInt("period_number")));
            }
        }
        return list;
    }

    // ======================== ATTENDANCE VIEW & EDIT ========================

    public List<String[]> getAttendanceByRollAndCourse(String rollNumber, int courseId) throws SQLException {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT a.attendance_id, cs.session_date, t.period_number, t.day_of_week, COALESCE(a.status, 'ABSENT') AS status " +
                     "FROM class_session cs " +
                     "JOIN timetable t ON cs.timetable_id = t.timetable_id " +
                     "JOIN students st ON st.section_id = t.section_id " +
                     "LEFT JOIN attendance a ON cs.session_id = a.session_id AND a.roll_number = st.roll_number " +
                     "WHERE st.roll_number = ? AND t.course_id = ? ORDER BY cs.session_date DESC, t.period_number DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber); ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int attId = rs.getInt("attendance_id");
                    String aidStr = (rs.wasNull() || attId == 0) ? "N/A" : String.valueOf(attId);
                    result.add(new String[]{
                        aidStr,
                        rs.getString("session_date"),
                        String.valueOf(rs.getInt("period_number")),
                        rs.getString("day_of_week"), rs.getString("status")});
                }
            }
        }
        return result;
    }

    public List<String[]> getAttendanceSummaryByRoll(String rollNumber) throws SQLException {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT c.course_code, c.course_name, " +
                     "  COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) AS present_count, " +
                     "  COUNT(cs.session_id) AS total_count " +
                     "FROM courses c " +
                     "JOIN timetable t ON c.course_id = t.course_id " +
                     "JOIN sections sec ON t.section_id = sec.section_id " +
                     "JOIN students st ON st.section_id = sec.section_id AND st.roll_number = ? " +
                     "LEFT JOIN class_session cs ON t.timetable_id = cs.timetable_id " +
                     "LEFT JOIN attendance a ON cs.session_id = a.session_id AND a.roll_number = ? " +
                     "GROUP BY c.course_id, c.course_code, c.course_name " +
                     "ORDER BY c.course_code";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber);
            ps.setString(2, rollNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int present = rs.getInt("present_count");
                    int total   = rs.getInt("total_count");
                    String pct  = total > 0
                        ? String.format("%.1f%%", (present * 100.0 / total))
                        : "N/A";
                    result.add(new String[]{
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        String.valueOf(present),
                        String.valueOf(total),
                        pct});
                }
            }
        }
        return result;
    }

    public void updateAttendance(int attendanceId, String newStatus) throws SQLException {
        String sql = "UPDATE attendance SET status = ? WHERE attendance_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.toUpperCase()); ps.setInt(2, attendanceId);
            ps.executeUpdate();
        }
    }
}

package models;

/**
 * Represents a single slot in the timetable, linking a section, course,
 * faculty member, day of week, and period number.
 * Used for both timetable display and attendance session creation.
 */
public class TimetableSlot {
    private int timetableId;
    private int sectionId;
    private String sectionName;
    private int courseId;
    private String courseCode;
    private String courseName;
    private int facultyId;
    private String facultyName;
    private String dayOfWeek;
    private int periodNumber;

    public TimetableSlot() {}

    public TimetableSlot(int timetableId, int sectionId, String sectionName,
                         int courseId, String courseCode, String courseName,
                         int facultyId, String facultyName,
                         String dayOfWeek, int periodNumber) {
        this.timetableId = timetableId;
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.dayOfWeek = dayOfWeek;
        this.periodNumber = periodNumber;
    }

    public int getTimetableId() { return timetableId; }
    public void setTimetableId(int timetableId) { this.timetableId = timetableId; }

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getFacultyId() { return facultyId; }
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public int getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(int periodNumber) { this.periodNumber = periodNumber; }

    @Override
    public String toString() {
        return String.format("P%d | %-10s | %-8s | %-25s | %s",
            periodNumber, dayOfWeek, courseCode, courseName, sectionName);
    }
}

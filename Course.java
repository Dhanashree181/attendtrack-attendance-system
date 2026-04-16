package models;

/**
 * Represents an academic course offered in the system.
 * Supports soft-delete via the {@code isActive} flag.
 */
public class Course {
    private int    courseId;
    private String courseCode;
    private String courseName;
    private boolean isActive;

    public Course() {}

    public Course(int courseId, String courseCode, String courseName) {
        this.courseId   = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.isActive   = true;
    }

    public int     getCourseId()              { return courseId; }
    public void    setCourseId(int id)        { this.courseId = id; }

    public String  getCourseCode()            { return courseCode; }
    public void    setCourseCode(String c)    { this.courseCode = c; }

    public String  getCourseName()            { return courseName; }
    public void    setCourseName(String n)    { this.courseName = n; }

    public boolean isActive()                 { return isActive; }
    public void    setActive(boolean active)  { this.isActive = active; }

    @Override
    public String toString() {
        return String.format("[ID:%-3d] %-6s %s", courseId, courseCode, courseName);
    }
}

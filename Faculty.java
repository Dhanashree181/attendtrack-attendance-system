package models;

/**
 * Represents a faculty member who teaches courses.
 * Supports soft-delete via the {@code isActive} flag.
 */
public class Faculty {
    private int    facultyId;
    private String name;
    private String email;
    private boolean isActive;

    public Faculty() {}

    public Faculty(int facultyId, String name, String email) {
        this.facultyId = facultyId;
        this.name      = name;
        this.email     = email;
        this.isActive  = true;
    }

    public int     getFacultyId()             { return facultyId; }
    public void    setFacultyId(int id)       { this.facultyId = id; }

    public String  getName()                  { return name; }
    public void    setName(String name)       { this.name = name; }

    public String  getEmail()                 { return email; }
    public void    setEmail(String email)     { this.email = email; }

    public boolean isActive()                 { return isActive; }
    public void    setActive(boolean active)  { this.isActive = active; }

    @Override
    public String toString() {
        return String.format("[ID:%-3d] %-25s %s", facultyId, name, email);
    }
}

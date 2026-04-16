package models;

/**
 * Represents a student enrolled in a section.
 * Supports soft-delete via the {@code isActive} flag.
 */
public class Student {
    private String rollNumber;
    private String name;
    private int    sectionId;
    private String email;
    private String status;
    private boolean isActive;

    public Student() {}

    /** Full constructor */
    public Student(String rollNumber, String name, int sectionId, String email) {
        this.rollNumber = rollNumber;
        this.name       = name;
        this.sectionId  = sectionId;
        this.email      = email;
        this.status     = "ACTIVE";
    }

    /** Minimal (no email) */
    public Student(String rollNumber, String name, int sectionId) {
        this(rollNumber, name, sectionId, null);
    }

    public String getRollNumber() { return rollNumber; }
    public void   setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getName() { return name; }
    public void   setName(String name) { this.name = name; }

    public int  getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

    public String getEmail() { return email; }
    public void   setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void   setStatus(String status) { this.status = status; }

    public boolean isActive() { return isActive; }
    public void    setActive(boolean active) { this.isActive = active; }

    @Override
    public String toString() {
        return String.format("%-12s %-25s SectionID:%-4d Email:%-20s Status:%s",
            rollNumber, name, sectionId,
            (email != null ? email : "—"),
            (status != null ? status : "ACTIVE"));
    }
}

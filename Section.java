package models;

/**
 * Represents a class section (e.g., CSE-A, CSE-B).
 * Students and timetable slots are grouped by section.
 */
public class Section {
    private int sectionId;
    private String sectionName;

    public Section() {}

    public Section(int sectionId, String sectionName) {
        this.sectionId = sectionId;
        this.sectionName = sectionName;
    }

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    @Override
    public String toString() {
        return "[" + sectionId + "] " + sectionName;
    }
}

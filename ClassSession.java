package models;

import java.sql.Date;

/**
 * Represents a concrete class session — a timetable slot that was
 * actually conducted on a specific date. Each session can have
 * attendance records associated with it.
 */
public class ClassSession {
    private int sessionId;
    private int timetableId;   // Now links to timetable slot, not course directly
    private Date sessionDate;

    public ClassSession() {}

    public ClassSession(int sessionId, int timetableId, Date sessionDate) {
        this.sessionId = sessionId;
        this.timetableId = timetableId;
        this.sessionDate = sessionDate;
    }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getTimetableId() { return timetableId; }
    public void setTimetableId(int timetableId) { this.timetableId = timetableId; }

    public Date getSessionDate() { return sessionDate; }
    public void setSessionDate(Date sessionDate) { this.sessionDate = sessionDate; }

    @Override
    public String toString() {
        return "[Session " + sessionId + "] TimetableSlot=" + timetableId + " Date=" + sessionDate;
    }
}

package models;

/**
 * Represents an attendance record for a student in a class session.
 * Status is either "PRESENT" or "ABSENT".
 */
public class Attendance {
    private int attendanceId;
    private String rollNumber;
    private int sessionId;
    private String status; // "PRESENT" or "ABSENT"

    public Attendance() {}

    public Attendance(int attendanceId, String rollNumber, int sessionId, String status) {
        this.attendanceId = attendanceId;
        this.rollNumber = rollNumber;
        this.sessionId = sessionId;
        this.status = status;
    }

    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Attendance [ID=" + attendanceId + ", Student=" + rollNumber + ", Status=" + status + "]";
    }
}

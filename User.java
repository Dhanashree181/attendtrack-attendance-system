package models;

/**
 * Represents an application user (Admin, Faculty, or Student).
 * Each user is linked to their respective entity via {@code linkedId}
 * (e.g., faculty_id, roll_number).
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String role; // ADMIN, FACULTY, STUDENT
    private String linkedId;

    public User() {}

    public User(int userId, String username, String password, String role, String linkedId) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.linkedId = linkedId;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getLinkedId() { return linkedId; }
    public void setLinkedId(String linkedId) { this.linkedId = linkedId; }
}

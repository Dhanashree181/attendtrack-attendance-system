package gui;

import gui.panels.*;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Main window for Faculty users. Uses a JTabbedPane to organize feature panels.
 *
 * Tab 0 – Dashboard        : faculty info + today's timetable (FacultyDashboardPanel)
 * Tab 1 – Mark Attendance  : mark attendance for a class session
 * Tab 2 – Course Attendance: view student attendance per course
 * Tab 3 – Change Password  : shared ChangePasswordPanel
 */
public class FacultyDashboard extends JFrame {

    public FacultyDashboard() {
        User user = SessionManager.getCurrentUser();
        setTitle("ATTENDTRACK - Faculty Dashboard");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());

        // Header bar (identical structure to AdminDashboard)
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(8, 12, 8, 12));
        header.setBackground(new Color(20, 60, 100));

        int facultyId = Integer.parseInt(user.getLinkedId());
        String displayTitle = "ATTENDTRACK  |  Faculty: " + user.getUsername();
        try {
            models.Faculty f = new dao.FacultyDAO().getFacultyById(facultyId);
            if (f != null) {
                displayTitle = "ATTENDTRACK  |  Faculty: " + f.getName() + " (FA" + String.format("%02d", facultyId) + ")";
            }
        } catch (Exception ignored) {}

        JLabel title = new JLabel(displayTitle);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        header.add(title, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(this::handleLogout);
        header.add(logoutBtn, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard",        new FacultyDashboardPanel(facultyId));
        tabs.addTab("Mark Attendance",  new MarkAttendancePanel(facultyId));
        tabs.addTab("Course Attendance",new CourseAttendancePanel(facultyId));
        tabs.addTab("Change Password",  new ChangePasswordPanel());
        root.add(tabs, BorderLayout.CENTER);

        add(root);
    }

    private void handleLogout(ActionEvent e) {
        SessionManager.clearSession();
        new LoginFrame().setVisible(true);
        dispose();
    }
}

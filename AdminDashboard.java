package gui;

import gui.panels.*;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Main window for Admin users. Uses a JTabbedPane to organize all feature panels.
 */
public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        User user = SessionManager.getCurrentUser();
        setTitle("ATTENDTRACK - Admin Dashboard");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());

        // Header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(8, 12, 8, 12));
        header.setBackground(new Color(33, 37, 41));
        JLabel title = new JLabel("ATTENDTRACK  |  Admin: " + user.getUsername());
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        header.add(title, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(this::handleLogout);
        header.add(logoutBtn, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Students",        new StudentManagePanel());
        tabs.addTab("Faculty",         new FacultyManagePanel());
        tabs.addTab("Courses",         new CourseManagePanel());
        tabs.addTab("Sections",        new SectionManagePanel());
        tabs.addTab("Timetable",       new TimetableManagePanel());
        tabs.addTab("Attendance",      new AdminAttendancePanel());
        tabs.addTab("Change Password", new ChangePasswordPanel());
        root.add(tabs, BorderLayout.CENTER);

        add(root);
    }

    private void handleLogout(ActionEvent e) {
        SessionManager.clearSession();
        new LoginFrame().setVisible(true);
        dispose();
    }
}

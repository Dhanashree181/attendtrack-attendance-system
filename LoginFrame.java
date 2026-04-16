package gui;

import dao.UserDAO;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Login screen for the ATTENDTRACK GUI application.
 * Authenticates via UserDAO and routes to role-based dashboard.
 */
public class LoginFrame extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final UserDAO userDao;

    public LoginFrame() {
        userDao = new UserDAO();
        setTitle("ATTENDTRACK - Login");
        setSize(420, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());

        // Header
        JLabel header = new JLabel("ATTENDTRACK", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 22));
        header.setBorder(new EmptyBorder(20, 0, 5, 0));
        root.add(header, BorderLayout.NORTH);

        JLabel sub = new JLabel("Student Attendance Management System", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 11));

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(10, 40, 10, 40));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 5, 7, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        form.add(sub, g);

        g.gridy = 1; g.gridx = 0; g.weightx = 0; g.gridwidth = 1;
        form.add(new JLabel("Username:"), g);
        usernameField = new JTextField(16);
        g.gridx = 1; g.weightx = 1;
        form.add(usernameField, g);

        g.gridy = 2; g.gridx = 0; g.weightx = 0;
        form.add(new JLabel("Password:"), g);
        passwordField = new JPasswordField(16);
        g.gridx = 1; g.weightx = 1;
        form.add(passwordField, g);

        root.add(form, BorderLayout.CENTER);

        // Button
        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        JButton loginBtn = new JButton("Login");
        loginBtn.setPreferredSize(new Dimension(110, 30));
        loginBtn.addActionListener(this::handleLogin);
        btnPanel.add(loginBtn);
        root.add(btnPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(loginBtn);
        add(root);
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDao.login(username, password);
            if (user != null) {
                SessionManager.setCurrentUser(user);
                openDashboard(user);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                usernameField.requestFocus();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDashboard(User user) {
        switch (user.getRole().toUpperCase()) {
            case "ADMIN":
                new AdminDashboard().setVisible(true);
                break;
            case "FACULTY":
                new FacultyDashboard().setVisible(true);
                break;
            case "STUDENT":
                new StudentDashboard().setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown role: " + user.getRole(), "Error", JOptionPane.ERROR_MESSAGE);
                SessionManager.clearSession();
                new LoginFrame().setVisible(true);
        }
    }
}

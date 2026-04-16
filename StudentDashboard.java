package gui;

import dao.StudentDAO;
import gui.panels.ChangePasswordPanel;
import gui.panels.StudentAttendancePanel;
import models.Student;
import models.TimetableSlot;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Main window for Student users.
 * Uses a JTabbedPane to organise the dashboard, attendance view,
 * and password change – consistent with AdminDashboard and FacultyDashboard.
 *
 * Tab 0 – Dashboard   : student info (TL) + notice board (TR) + today's timetable (bottom)
 * Tab 1 – Attendance  : subject-wise attendance table (StudentAttendancePanel)
 * Tab 2 – Change Pwd  : shared ChangePasswordPanel
 */
public class StudentDashboard extends JFrame {

    private static final Color HEADER_BG = new Color(30, 80, 50);   // same green family as original

    private final StudentDAO studentDao = new StudentDAO();
    private final User        currentUser;
    private final Student     student;

    // Notice board
    private final JEditorPane noticeArea = new JEditorPane();

    // Today's timetable model (declared here so loadTimetable can reach it)
    private final DefaultTableModel ttModel;

    public StudentDashboard() {
        currentUser = SessionManager.getCurrentUser();
        student     = fetchStudent();

        setTitle("ATTENDTRACK – Student Dashboard");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());

        // ── Header bar (identical structure to Admin/Faculty) ──────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(8, 12, 8, 12));
        header.setBackground(HEADER_BG);

        String name = student != null ? student.getName() : currentUser.getUsername();
        String id   = student != null ? student.getRollNumber() : currentUser.getLinkedId();
        JLabel title = new JLabel("ATTENDTRACK  |  Student: " + name + " (" + id + ")");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        header.add(title, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(this::handleLogout);
        header.add(logoutBtn, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // ── Tabs ────────────────────────────────────────────────────────────
        ttModel = new DefaultTableModel(
            new String[]{"Period", "Course Code", "Course Name", "Faculty"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard",       buildDashboardPanel());
        tabs.addTab("View Attendance", new StudentAttendancePanel());
        tabs.addTab("Change Password", new ChangePasswordPanel());
        root.add(tabs, BorderLayout.CENTER);

        add(root);

        // Load dashboard data
        loadNoticeBoard();
        loadTodayTimetable();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Dashboard tab: top half = info (left) + notice (right),
    //                bottom half = today's timetable
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top half: student info + notice board side-by-side
        JPanel topHalf = new JPanel(new GridLayout(1, 2, 10, 0));
        topHalf.setPreferredSize(new Dimension(0, 200));
        topHalf.add(buildStudentInfoPanel());
        topHalf.add(buildNoticeBoardPanel());
        panel.add(topHalf, BorderLayout.NORTH);

        // Bottom half: today's timetable
        panel.add(buildTimetablePanel(), BorderLayout.CENTER);

        return panel;
    }

    // ── Student Info card (top-left) ──────────────────────────────────────
    private JPanel buildStudentInfoPanel() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Student Details"),
            new EmptyBorder(6, 10, 6, 10)
        ));

        String name      = student != null ? student.getName()      : currentUser.getUsername();
        String id        = student != null ? student.getRollNumber() : currentUser.getLinkedId();
        String section   = "—";
        if (student != null) {
            try {
                for (models.Section s : new dao.AdminDAO().getAllSections()) {
                    if (s.getSectionId() == student.getSectionId()) {
                        section = s.getSectionName();
                        break;
                    }
                }
            } catch (java.sql.SQLException ignored) {}
        }
        String email     = (student != null && student.getEmail() != null && !student.getEmail().isEmpty())
                           ? student.getEmail() : "—";

        card.add(boldRow("ATTENDTRACK"));
        card.add(Box.createVerticalStrut(2));
        card.add(infoRow("Student Dashboard"));
        card.add(Box.createVerticalStrut(8));
        card.add(new JSeparator());
        card.add(Box.createVerticalStrut(8));
        card.add(fieldRow("Name",    name));
        card.add(Box.createVerticalStrut(5));
        card.add(fieldRow("ID",      id));
        card.add(Box.createVerticalStrut(5));
        card.add(fieldRow("Section", section));
        card.add(Box.createVerticalStrut(5));
        card.add(fieldRow("Email",   email));

        return card;
    }

    private JLabel boldRow(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 15));
        l.setForeground(HEADER_BG);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel infoRow(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.PLAIN, 12));
        l.setForeground(new Color(80, 80, 80));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel fieldRow(String label, String value) {
        JLabel l = new JLabel("<html><b>" + label + ":</b>&nbsp;&nbsp;" + value + "</html>");
        l.setFont(new Font("Arial", Font.PLAIN, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ── Notice Board (top-right) ──────────────────────────────────────────
    private JPanel buildNoticeBoardPanel() {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBorder(BorderFactory.createTitledBorder("Notice Board"));

        noticeArea.setEditable(false);
        noticeArea.setContentType("text/html");
        noticeArea.setBorder(new EmptyBorder(4, 6, 4, 6));

        card.add(new JScrollPane(noticeArea), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        refreshBtn.addActionListener(e -> loadNoticeBoard());
        btnRow.add(refreshBtn);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    // ── Today's Timetable (bottom half) ──────────────────────────────────
    private JPanel buildTimetablePanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));

        String dayName = LocalDate.now().getDayOfWeek().toString();
        dayName = dayName.charAt(0) + dayName.substring(1).toLowerCase();
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

        TitledBorder tb = BorderFactory.createTitledBorder(
            "Today's Timetable  —  " + dayName + ", " + dateStr);
        tb.setTitleFont(new Font("Arial", Font.BOLD, 12));
        panel.setBorder(tb);

        JTable table = new JTable(ttModel);
        table.setRowHeight(26);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Centre the Period column
        DefaultTableCellRenderer centre = new DefaultTableCellRenderer();
        centre.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centre);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadTodayTimetable());
        btnBar.add(refreshBtn);
        panel.add(btnBar, BorderLayout.SOUTH);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Data loading
    // ─────────────────────────────────────────────────────────────────────

    private void loadNoticeBoard() {
        String roll = currentUser.getLinkedId();
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: monospace; font-size: 11px; margin: 4px; color: #333333;'>");

        try {
            Map<String, double[]> stats = studentDao.getAttendancePerCourse(roll);

            if (stats.isEmpty()) {
                sb.append("<span style='color: #666666;'>No Classes Yet<br><br>Attendance records will appear<br>once classes are conducted.</span>");
                sb.append("</body></html>");
                noticeArea.setText(sb.toString());
                return;
            }

            long withClasses = stats.values().stream().filter(d -> d[1] > 0).count();
            if (withClasses == 0) {
                sb.append("<span style='color: #666666;'>No Classes Yet<br><br>No sessions have been conducted<br>for any subject.</span>");
                sb.append("</body></html>");
                noticeArea.setText(sb.toString());
                return;
            }

            int overallPresent = 0;
            int overallTotal = 0;
            for (double[] d : stats.values()) {
                overallPresent += (int) d[0];
                overallTotal += (int) d[1];
            }
            double overallPct = (overallTotal > 0) ? (overallPresent * 100.0) / overallTotal : 0.0;
            String ovrPctStr = String.format("%.2f%%", overallPct);
            if (ovrPctStr.endsWith(".00%")) ovrPctStr = ovrPctStr.replace(".00%", ".0%");

            long lowCount = stats.values().stream().filter(d -> d[1] > 0 && d[2] < 75.0).count();

            sb.append(String.format("Overall Attendance: %s<br>", ovrPctStr));
            sb.append("────────────────────────<br>");

            if (lowCount == 0) {
                sb.append("All subjects above 75%.<br>You are on track.");
            } else {
                sb.append("Subjects needing attention:<br>");

                for (Map.Entry<String, double[]> e : stats.entrySet()) {
                    double[] d = e.getValue();
                    if (d[1] > 0 && d[2] < 75.0) {
                        String courseCode = e.getKey().split(" - ", 2)[0];
                        String status, colorStr;
                        if (d[2] < 65.0) {
                            status = "LOW";
                            colorStr = "red";
                        } else {
                            status = "Warning";
                            colorStr = "#E65100"; // Deep Orange
                        }
                        String line = String.format("   %-8s %-7s [%s]", courseCode, String.format("%.1f%%", d[2]), status);
                        sb.append(String.format("<span style='color: %s;'>%s</span><br>", colorStr, line.replace(" ", "&nbsp;")));
                    }
                }
                sb.append("Attend more classes to meet<br>the 75% requirement.");
            }

        } catch (SQLException ex) {
            sb.append("<span style='color: #666666;'>Error loading attendance:<br>").append(ex.getMessage()).append("</span>");
        }

        sb.append("</body></html>");
        noticeArea.setText(sb.toString());
        noticeArea.setCaretPosition(0);
    }

    /** Populate the timetable for the student's section and today's day. */
    private void loadTodayTimetable() {
        ttModel.setRowCount(0);
        if (student == null) {
            ttModel.addRow(new Object[]{"—", "—", "Student record not found", "—"});
            return;
        }
        String today = LocalDate.now().getDayOfWeek().name();
        try {
            List<TimetableSlot> slots = studentDao.getTodayTimetable(student.getSectionId(), today);
            if (slots.isEmpty()) {
                ttModel.addRow(new Object[]{"—", "—", "No classes scheduled today", "—"});
            } else {
                for (TimetableSlot s : slots) {
                    ttModel.addRow(new Object[]{
                        "P" + s.getPeriodNumber(),
                        s.getCourseCode(),
                        s.getCourseName(),
                        s.getFacultyName()
                    });
                }
            }
        } catch (SQLException ex) {
            ttModel.addRow(new Object[]{"—", "—", "Error: " + ex.getMessage(), "—"});
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────

    private Student fetchStudent() {
        try {
            return studentDao.getStudentByRollNumber(currentUser.getLinkedId());
        } catch (SQLException e) {
            return null;
        }
    }

    private void handleLogout(ActionEvent e) {
        SessionManager.clearSession();
        new LoginFrame().setVisible(true);
        dispose();
    }
}

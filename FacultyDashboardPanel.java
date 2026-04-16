package gui.panels;

import dao.FacultyDAO;
import models.Faculty;
import models.TimetableSlot;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dashboard tab for Faculty users.
 * Top-left: Faculty info card (name, ID, email, today's date).
 * Bottom half: Today's timetable for this faculty member.
 * Consistent in structure with the student dashboard panel.
 */
public class FacultyDashboardPanel extends JPanel {

    private static final Color HEADER_BG = new Color(20, 60, 100);   // faculty blue

    private final FacultyDAO      facultyDao = new FacultyDAO();
    private final int             facultyId;
    private final DefaultTableModel ttModel;

    public FacultyDashboardPanel(int facultyId) {
        this.facultyId = facultyId;
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Today's timetable model (shared between builder and loader)
        ttModel = new DefaultTableModel(
            new String[]{"Period", "Course Code", "Course Name", "Section"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        // Top half: info card (left) – no notice board for faculty, just info
        JPanel topHalf = new JPanel(new GridLayout(1, 1, 10, 0));
        topHalf.setPreferredSize(new Dimension(0, 180));
        topHalf.add(buildFacultyInfoPanel());
        add(topHalf, BorderLayout.NORTH);

        // Bottom half: today's timetable
        add(buildTimetablePanel(), BorderLayout.CENTER);

        // Load data
        loadTodayTimetable();
    }

    // ── Faculty Info card (top-left) ─────────────────────────────────────
    private JPanel buildFacultyInfoPanel() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Faculty Details"),
            new EmptyBorder(6, 10, 6, 10)
        ));

        // Fetch faculty record
        Faculty faculty = null;
        try { faculty = facultyDao.getFacultyById(facultyId); } catch (SQLException ignored) {}

        String name  = faculty != null ? faculty.getName()  : "Faculty ID: " + facultyId;
        String email = faculty != null && faculty.getEmail() != null ? faculty.getEmail() : "—";
        String idStr = String.format("FA%02d", facultyId);

        card.add(boldLabel("ATTENDTRACK"));
        card.add(Box.createVerticalStrut(2));
        card.add(plainLabel("Faculty Dashboard"));
        card.add(Box.createVerticalStrut(8));
        card.add(new JSeparator());
        card.add(Box.createVerticalStrut(8));
        card.add(fieldRow("Name",  name));
        card.add(Box.createVerticalStrut(5));
        card.add(fieldRow("ID",    idStr));
        card.add(Box.createVerticalStrut(5));
        card.add(fieldRow("Email", email));
        card.add(Box.createVerticalStrut(5));
        card.add(fieldRow("Date",
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy"))));

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

    // ── Data loading ──────────────────────────────────────────────────────
    public void loadTodayTimetable() {
        ttModel.setRowCount(0);
        String today = LocalDate.now().getDayOfWeek().name();
        try {
            List<TimetableSlot> slots = facultyDao.getTodaySlots(facultyId, today);
            if (slots.isEmpty()) {
                ttModel.addRow(new Object[]{"—", "—", "No classes scheduled today", "—"});
            } else {
                for (TimetableSlot s : slots) {
                    ttModel.addRow(new Object[]{
                        "P" + s.getPeriodNumber(),
                        s.getCourseCode(),
                        s.getCourseName(),
                        s.getSectionName()
                    });
                }
            }
        } catch (SQLException ex) {
            ttModel.addRow(new Object[]{"—", "—", "Error: " + ex.getMessage(), "—"});
        }
    }

    // ── Label helpers ─────────────────────────────────────────────────────
    private JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 15));
        l.setForeground(HEADER_BG);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel plainLabel(String text) {
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
}

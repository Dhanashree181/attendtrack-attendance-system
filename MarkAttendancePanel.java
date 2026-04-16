package gui.panels;

import dao.FacultyDAO;
import models.Attendance;
import models.Student;
import models.TimetableSlot;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Faculty panel to mark attendance for a timetable slot.
 *
 * Flow:
 *  1. The slot combo is populated for TODAY's day by default.
 *  2. If a past date is typed in the date field and Enter is pressed
 *     (or focus leaves the field), the slot combo automatically refreshes
 *     to show slots for THAT DATE's day-of-week.
 *  3. "Reload Slots" also refreshes based on the date field.
 *  4. "Load Students" loads the section list — no DB session is created yet.
 *  5. "Save Attendance" creates the session + writes all records atomically.
 */
public class MarkAttendancePanel extends JPanel {
    private final FacultyDAO facultyDao = new FacultyDAO();
    private final int facultyId;

    private final JComboBox<String> slotCombo;
    private final JTextField dateField;
    private final DefaultTableModel studentModel;
    private final JTable studentTable;
    private List<TimetableSlot> currentSlots;   // slots for the currently selected day
    private List<Student> loadedStudents;
    // Stored when "Load Students" is clicked; session is NOT created until "Save Attendance".
    private int  pendingTimetableId = -1;
    private Date pendingSessionDate = null;

    public MarkAttendancePanel(int facultyId) {
        this.facultyId = facultyId;
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ── Top controls ────────────────────────────────────────────────────
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6); g.anchor = GridBagConstraints.WEST;

        // Row 0: date field  (date first so faculty picks day before seeing slots)
        g.gridx = 0; g.gridy = 0;
        topPanel.add(new JLabel("Date (YYYY-MM-DD, blank = today):"), g);
        dateField = new JTextField(12);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(dateField, g);

        g.gridx = 2; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        JButton refreshSlotsBtn = new JButton("Load Class");
        topPanel.add(refreshSlotsBtn, g);

        // Row 1: slot combo
        g.gridx = 0; g.gridy = 1; g.fill = GridBagConstraints.NONE;
        topPanel.add(new JLabel("Select Period:"), g);
        slotCombo = new JComboBox<>();
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(slotCombo, g);

        g.gridx = 2; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        JButton loadStudentsBtn = new JButton("Load Students");
        topPanel.add(loadStudentsBtn, g);
        add(topPanel, BorderLayout.NORTH);

        // ── Student table with checkboxes ───────────────────────────────────
        String[] cols = {"Roll Number", "Name", "Present"};
        studentModel = new DefaultTableModel(cols, 0) {
            public Class<?> getColumnClass(int col) { return col == 2 ? Boolean.class : String.class; }
            public boolean isCellEditable(int r, int c) { return c == 2; }
        };
        studentTable = new JTable(studentModel);
        studentTable.setRowHeight(26);
        studentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(studentTable), BorderLayout.CENTER);

        // ── Bottom buttons ──────────────────────────────────────────────────
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton markAllBtn = new JButton("Mark All Present");
        JButton saveBtn    = new JButton("Save Attendance");
        markAllBtn.addActionListener(e -> markAllPresent());
        saveBtn.addActionListener(e -> saveAttendance());
        bottomBar.add(markAllBtn); bottomBar.add(saveBtn);
        add(bottomBar, BorderLayout.SOUTH);

        // ── Listeners ───────────────────────────────────────────────────────
        // Refresh slot combo whenever the date field is committed (Enter or focus-lost)
        dateField.addActionListener(e -> refreshSlotsForDate());
        dateField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) { refreshSlotsForDate(); }
        });
        refreshSlotsBtn.addActionListener(e -> refreshSlotsForDate());
        loadStudentsBtn.addActionListener(e -> loadStudents());

        // Initial load: today's slots
        refreshSlotsForDate();
    }

    // ── Slot combo refresh ───────────────────────────────────────────────────
    /**
     * Parses the date field to determine the day-of-week, then loads timetable
     * slots for that day into the combo box.
     * Blank or invalid date → uses today.
     * Future date → uses today and warns.
     */
    private void refreshSlotsForDate() {
        LocalDate targetDate = parseDateField();
        String dayOfWeek = targetDate.getDayOfWeek().name(); // e.g. "FRIDAY"

        slotCombo.removeAllItems();
        studentModel.setRowCount(0);   // clear stale student list when date changes
        loadedStudents = null;
        pendingTimetableId = -1;
        pendingSessionDate = null;

        try {
            currentSlots = facultyDao.getTodaySlots(facultyId, dayOfWeek);
            if (currentSlots.isEmpty()) {
                String friendly = dayOfWeek.charAt(0) + dayOfWeek.substring(1).toLowerCase();
                slotCombo.addItem("No classes on " + friendly + " (" + targetDate + ")");
            } else {
                for (TimetableSlot t : currentSlots)
                    slotCombo.addItem("P" + t.getPeriodNumber()
                        + " | " + t.getCourseCode()
                        + " | " + t.getSectionName());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading slots: " + e.getMessage());
        }
    }

    /** Returns the date in the date field, or today if blank/invalid/future. */
    private LocalDate parseDateField() {
        String dateStr = dateField.getText().trim();
        if (dateStr.isEmpty()) return LocalDate.now();
        try {
            LocalDate d = LocalDate.parse(dateStr);
            if (d.isAfter(LocalDate.now())) {
                JOptionPane.showMessageDialog(this,
                    "Cannot use a future date. Defaulting to today.",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
                dateField.setText("");
                return LocalDate.now();
            }
            return d;
        } catch (DateTimeParseException ex) {
            // silently fall back to today while typing; warn only on explicit reload
            return LocalDate.now();
        }
    }

    // ── Load students ────────────────────────────────────────────────────────
    private void loadStudents() {
        if (currentSlots == null || currentSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No slots available for the selected date.", "No Slots",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idx = slotCombo.getSelectedIndex();
        if (idx < 0 || idx >= currentSlots.size()) return;
        TimetableSlot selected = currentSlots.get(idx);

        // The date is already validated as ≤ today and matching the slot's day
        // (slot combo was refreshed for this date's day-of-week).
        LocalDate targetDate = parseDateField();
        pendingSessionDate   = Date.valueOf(targetDate);
        pendingTimetableId   = selected.getTimetableId();

        try {
            loadedStudents = facultyDao.getStudentsBySection(selected.getSectionId());
            studentModel.setRowCount(0);
            if (loadedStudents.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No active students in this section.");
                return;
            }
            for (Student s : loadedStudents)
                studentModel.addRow(new Object[]{s.getRollNumber(), s.getName(), Boolean.FALSE});
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // ── Mark all present ─────────────────────────────────────────────────────
    private void markAllPresent() {
        for (int i = 0; i < studentModel.getRowCount(); i++)
            studentModel.setValueAt(Boolean.TRUE, i, 2);
    }

    // ── Save attendance ──────────────────────────────────────────────────────
    private void saveAttendance() {
        if (loadedStudents == null || loadedStudents.isEmpty()
                || pendingTimetableId < 0 || pendingSessionDate == null) {
            JOptionPane.showMessageDialog(this, "Please load students first.");
            return;
        }

        int present = 0, absent = 0;
        for (int i = 0; i < studentModel.getRowCount(); i++) {
            if ((Boolean) studentModel.getValueAt(i, 2)) present++;
            else absent++;
        }

        String summary = String.format("Attendance Summary:\nPresent: %d\nAbsent: %d\nTotal: %d\n\nSubmit this attendance record?", 
            present, absent, loadedStudents.size());
            
        int confirm = JOptionPane.showConfirmDialog(this, summary, "Confirm Attendance", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) {
            return; // Exit here, leaving the checkboxes editable
        }

        try {
            int sessionId = facultyDao.getOrCreateSession(pendingTimetableId, pendingSessionDate);
            for (int i = 0; i < loadedStudents.size(); i++) {
                boolean isPresent = (Boolean) studentModel.getValueAt(i, 2);
                String status = isPresent ? "PRESENT" : "ABSENT";
                facultyDao.markAttendance(
                    new Attendance(0, loadedStudents.get(i).getRollNumber(), sessionId, status));
            }
            JOptionPane.showMessageDialog(this, "Attendance saved successfully!");
            refreshSlotsForDate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + e.getMessage());
        }
    }
}

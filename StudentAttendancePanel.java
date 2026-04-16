package gui.panels;

import dao.StudentDAO;
import gui.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;

/**
 * Panel for students to view their subject-wise attendance record.
 * Consistent with CourseAttendancePanel (faculty) in style.
 */
public class StudentAttendancePanel extends JPanel {

    private final StudentDAO studentDao = new StudentDAO();
    private final DefaultTableModel model;

    public StudentAttendancePanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header label
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel header = new JLabel("Subject-wise Attendance", SwingConstants.LEFT);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBorder(new EmptyBorder(0, 0, 4, 0));
        topPanel.add(header, BorderLayout.NORTH);
        
        JLabel instrLabel = new JLabel(" Select the row to see date-wise attendance");
        instrLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        instrLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        topPanel.add(instrLabel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"Course Code", "Course Name", "Present", "Total Classes", "Percentage", "Status"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s && c == 5) {
                    String st = String.valueOf(v);
                    if ("Low".equals(st)) comp.setForeground(Color.RED);
                    else if ("Warning".equals(st)) comp.setForeground(Color.ORANGE);
                    else comp.setForeground(t.getForeground());
                } else if (!s) {
                    comp.setForeground(t.getForeground());
                }
                return comp;
            }
        });
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Refresh button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String code = (String) model.getValueAt(row, 0);
                    if (!"OVERALL".equals(code) && !code.equals("—")) {
                        showDateWiseAttendance(code, (String) model.getValueAt(row, 1));
                    }
                }
            }
        });

        loadData();
    }

    private void showDateWiseAttendance(String courseCode, String courseName) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detailed Attendance: " + courseCode + " - " + courseName, true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);

        String[] cols = {"Date", "Period", "Day", "Status"};
        DefaultTableModel detModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable detTable = new JTable(detModel);
        detTable.setRowHeight(24);
        detTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        detTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if (column == 3 && "ABSENT".equalsIgnoreCase(String.valueOf(value))) {
                        c.setForeground(Color.RED);
                        c.setBackground(table.getBackground());
                    } else {
                        c.setBackground(table.getBackground());
                        c.setForeground(table.getForeground());
                    }
                }
                return c;
            }
        });

        String roll = SessionManager.getCurrentUser().getLinkedId();
        try {
            java.util.List<String[]> records = studentDao.getDetailedAttendance(roll, courseCode);
            for (String[] rec : records) {
                detModel.addRow(rec);
            }
            if (records.isEmpty()) {
                detModel.addRow(new Object[]{"—", "—", "No records found", "—"});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading records: " + ex.getMessage());
        }

        dialog.add(new JScrollPane(detTable), BorderLayout.CENTER);
        
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        bottom.add(closeBtn);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    public void loadData() {
        model.setRowCount(0);
        String roll = SessionManager.getCurrentUser().getLinkedId();
        try {
            Map<String, double[]> stats = studentDao.getAttendancePerCourse(roll);
            if (stats.isEmpty()) {
                model.addRow(new Object[]{"—", "No attendance records found", "—", "—", "—", "—"});
            } else {
                int overallPresent = 0;
                int overallTotal = 0;
                
                for (Map.Entry<String, double[]> entry : stats.entrySet()) {
                    double[] d    = entry.getValue();
                    int present   = (int) d[0];
                    int total     = (int) d[1];
                    double pct    = d[2];
                    
                    String pctStr;
                    String status;
                    if (total == 0) {
                        pctStr = "N/A";
                        status = "N/A";
                    } else {
                        pctStr = String.format("%.2f%%", pct);
                        if (pct >= 75.0) status = "Good";
                        else if (pct >= 65.0) status = "Warning";
                        else status = "Low";
                    }
                    
                    overallPresent += present;
                    overallTotal += total;

                    // Split "CODE - Name" key
                    String[] parts = entry.getKey().split(" - ", 2);
                    model.addRow(new Object[]{
                        parts.length > 0 ? parts[0] : entry.getKey(),
                        parts.length > 1 ? parts[1] : "",
                        present,
                        total,
                        pctStr,
                        status
                    });
                }
                
                if (overallTotal >= 0) {
                    String pctStr;
                    String status;
                    if (overallTotal == 0) {
                        pctStr = "N/A";
                        status = "N/A";
                    } else {
                        double overallPct = (overallPresent * 100.0) / overallTotal;
                        pctStr = String.format("%.2f%%", overallPct);
                        if (overallPct >= 75.0) status = "Good";
                        else if (overallPct >= 65.0) status = "Warning";
                        else status = "Low";
                    }
                    model.addRow(new Object[]{
                        "OVERALL",
                        "Total Attendance",
                        overallPresent,
                        overallTotal,
                        pctStr,
                        status
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

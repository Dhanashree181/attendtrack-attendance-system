package gui.panels;

import dao.AdminDAO;
import dao.StudentDAO;
import dao.UserDAO;
import models.Section;
import models.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Admin panel for managing students. Supports Add, Edit, and Deactivate.
 */
public class StudentManagePanel extends JPanel {
    private final AdminDAO adminDao = new AdminDAO();
    private final StudentDAO studentDao = new StudentDAO();
    private final UserDAO userDao = new UserDAO();
    private final DefaultTableModel model;
    private final JTable table;

    public StudentManagePanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Roll Number", "Name", "Section", "Email", "Status"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s && c == 4) {
                    if ("Inactive".equals(String.valueOf(v))) comp.setForeground(Color.RED);
                    else comp.setForeground(t.getForeground());
                } else if (!s) {
                    comp.setForeground(t.getForeground());
                }
                return comp;
            }
        });
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Button bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton addBtn   = new JButton("Add Student");
        JButton editBtn  = new JButton("Edit Student");
        JButton deactBtn = new JButton("Deactivate");
        JButton refBtn   = new JButton("Refresh");
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                String status = (String) model.getValueAt(table.getSelectedRow(), 4);
                if ("Active".equals(status)) deactBtn.setText("Deactivate");
                else deactBtn.setText("Reactivate");
            }
        });

        addBtn.addActionListener(e -> showAddDialog());
        editBtn.addActionListener(e -> showEditDialog());
        deactBtn.addActionListener(e -> toggleStatusSelected());
        refBtn.addActionListener(e -> loadData());
        btnBar.add(addBtn); btnBar.add(editBtn); btnBar.add(deactBtn); btnBar.add(refBtn);
        add(btnBar, BorderLayout.SOUTH);

        loadData();
    }

    public void loadData() {
        model.setRowCount(0);
        try {
            List<Section> secs = adminDao.getAllSections();
            for (Student s : adminDao.getAllStudents()) {
                String secName = String.valueOf(s.getSectionId());
                for (Section sec : secs) {
                    if (sec.getSectionId() == s.getSectionId()) {
                        secName = sec.getSectionName();
                        break;
                    }
                }
                model.addRow(new Object[]{s.getRollNumber(), s.getName(), secName,
                    s.getEmail() != null ? s.getEmail() : "—", s.isActive() ? "Active" : "Inactive"});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        try {
            List<Section> secs = adminDao.getAllSections();
            if (secs.isEmpty()) { JOptionPane.showMessageDialog(this, "No sections available. Add a section first."); return; }

            JTextField rollField = new JTextField(10);
            JTextField nameField = new JTextField(20);
            JTextField emailField = new JTextField(20);
            String[] secNames = secs.stream().map(s -> s.getSectionId() + " - " + s.getSectionName()).toArray(String[]::new);
            JComboBox<String> secCombo = new JComboBox<>(secNames);

            JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
            p.add(new JLabel("Roll Number (e.g. A001):")); p.add(rollField);
            p.add(new JLabel("Name:")); p.add(nameField);
            p.add(new JLabel("Email (optional):")); p.add(emailField);
            p.add(new JLabel("Section:")); p.add(secCombo);

            int res = JOptionPane.showConfirmDialog(this, p, "Add Student", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;

            String roll = rollField.getText().trim().toUpperCase();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            int secId = secs.get(secCombo.getSelectedIndex()).getSectionId();

            if (!roll.matches("[A-Za-z][0-9]{3}")) { JOptionPane.showMessageDialog(this, "Invalid roll: one letter + 3 digits."); return; }
            if (!name.matches("[A-Za-z][A-Za-z .'-]{0,99}")) { JOptionPane.showMessageDialog(this, "Invalid name."); return; }
            if (!email.isEmpty() && !email.matches("[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}")) { email = null; }

            studentDao.addStudent(new Student(roll, name, secId, email.isEmpty() ? null : email));
            String username = roll.toLowerCase();
            String defPwd = "pass123";
            userDao.addUser(new models.User(0, username, defPwd, "STUDENT", roll));
            JOptionPane.showMessageDialog(this, "Student added!\nLogin: " + username + " / " + defPwd);
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a student to edit."); return; }
        String roll = (String) model.getValueAt(row, 0);
        try {
            Student st = adminDao.getStudentByRoll(roll);
            if (st == null || !st.isActive()) { JOptionPane.showMessageDialog(this, "Cannot edit an inactive student."); return; }
            List<Section> secs = adminDao.getAllSections();

            JTextField nameField  = new JTextField(st.getName(), 20);
            JTextField emailField = new JTextField(st.getEmail() != null ? st.getEmail() : "", 20);
            String[] secNames = secs.stream().map(s -> s.getSectionId() + " - " + s.getSectionName()).toArray(String[]::new);
            JComboBox<String> secCombo = new JComboBox<>(secNames);
            for (int i = 0; i < secs.size(); i++) if (secs.get(i).getSectionId() == st.getSectionId()) { secCombo.setSelectedIndex(i); break; }

            JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
            p.add(new JLabel("Name:")); p.add(nameField);
            p.add(new JLabel("Email (blank to clear):")); p.add(emailField);
            p.add(new JLabel("Section:")); p.add(secCombo);

            int res = JOptionPane.showConfirmDialog(this, p, "Edit Student: " + roll, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;

            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            if (name.matches("[A-Za-z][A-Za-z .'-]{0,99}")) st.setName(name);
            st.setEmail(email.isEmpty() ? null : (email.matches("[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}") ? email : st.getEmail()));
            st.setSectionId(secs.get(secCombo.getSelectedIndex()).getSectionId());

            adminDao.updateStudent(st);
            JOptionPane.showMessageDialog(this, "Student updated successfully.");
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleStatusSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a student."); return; }
        String roll = (String) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);
        String status = (String) model.getValueAt(row, 4);

        if ("Active".equals(status)) {
            int confirm = JOptionPane.showConfirmDialog(this, "Deactivate " + name + " (" + roll + ")?\nAttendance history will be preserved.", "Confirm Deactivate", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                adminDao.softDeleteStudent(roll);
                JOptionPane.showMessageDialog(this, "Student deactivated.");
                loadData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this, "Reactivate " + name + " (" + roll + ")?", "Confirm Reactivate", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                adminDao.reactivateStudent(roll);
                JOptionPane.showMessageDialog(this, "Student reactivated. Login access restored.");
                loadData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

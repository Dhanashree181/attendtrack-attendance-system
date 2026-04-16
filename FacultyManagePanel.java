package gui.panels;

import dao.AdminDAO;
import dao.UserDAO;
import models.Faculty;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

/**
 * Admin panel for managing faculty. Supports Add, Edit, and Deactivate.
 */
public class FacultyManagePanel extends JPanel {
    private final AdminDAO adminDao = new AdminDAO();
    private final UserDAO userDao = new UserDAO();
    private final DefaultTableModel model;
    private final JTable table;

    public FacultyManagePanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Faculty ID", "Name", "Email", "Status"};
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
                if (!s && c == 3) {
                    if ("Inactive".equals(String.valueOf(v))) comp.setForeground(Color.RED);
                    else comp.setForeground(t.getForeground());
                } else if (!s) {
                    comp.setForeground(t.getForeground());
                }
                return comp;
            }
        });
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton addBtn   = new JButton("Add Faculty");
        JButton editBtn  = new JButton("Edit Faculty");
        JButton deactBtn = new JButton("Deactivate");
        JButton refBtn   = new JButton("Refresh");
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                String status = (String) model.getValueAt(table.getSelectedRow(), 3);
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
            for (Faculty f : adminDao.getAllFaculty())
                model.addRow(new Object[]{f.getFacultyId(), f.getName(), f.getEmail(), f.isActive() ? "Active" : "Inactive"});
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        JTextField nameField  = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Name:")); p.add(nameField);
        p.add(new JLabel("Email:")); p.add(emailField);

        int res = JOptionPane.showConfirmDialog(this, p, "Add Faculty", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String name  = nameField.getText().trim();
        String email = emailField.getText().trim();
        if (!name.matches("[A-Za-z][A-Za-z .'-]{0,99}")) { JOptionPane.showMessageDialog(this, "Invalid name."); return; }
        if (!email.matches("[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}")) { JOptionPane.showMessageDialog(this, "Invalid email."); return; }

        try {
            int id = adminDao.addFaculty(new Faculty(0, name, email));
            String username = "FA0" + id;
            String defPwd = "pass123";
            userDao.addUser(new models.User(0, username, defPwd, "FACULTY", String.valueOf(id)));
            JOptionPane.showMessageDialog(this, "Faculty added!\nLogin: " + username + " / " + defPwd);
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a faculty to edit."); return; }
        int fid = (int) model.getValueAt(row, 0);
        try {
            Faculty f = adminDao.getFacultyById(fid);
            if (f == null || !f.isActive()) { JOptionPane.showMessageDialog(this, "Cannot edit an inactive faculty."); return; }

            JTextField nameField  = new JTextField(f.getName(), 20);
            JTextField emailField = new JTextField(f.getEmail(), 20);
            JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
            p.add(new JLabel("Name:")); p.add(nameField);
            p.add(new JLabel("Email:")); p.add(emailField);

            int res = JOptionPane.showConfirmDialog(this, p, "Edit Faculty ID: " + fid, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;

            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            if (name.matches("[A-Za-z][A-Za-z .'-]{0,99}")) f.setName(name);
            if (email.matches("[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}")) f.setEmail(email);

            adminDao.updateFaculty(f);
            JOptionPane.showMessageDialog(this, "Faculty updated successfully.");
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleStatusSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a faculty."); return; }
        int fid    = (int) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);
        String status = (String) model.getValueAt(row, 3);

        if ("Active".equals(status)) {
            int confirm = JOptionPane.showConfirmDialog(this, "Deactivate " + name + "?\nLogin access will be removed.", "Confirm Deactivate", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                adminDao.softDeleteFaculty(fid);
                userDao.deleteUser("FACULTY", String.valueOf(fid));
                JOptionPane.showMessageDialog(this, "Faculty deactivated. Login access removed.");
                loadData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this, "Reactivate " + name + "?", "Confirm Reactivate", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                adminDao.reactivateFaculty(fid);
                String username = "FA0" + fid;
                String defPwd = "pass123";
                // Verify user isn't already there just in case
                if (userDao.login(username, defPwd) == null) {
                    userDao.addUser(new models.User(0, username, defPwd, "FACULTY", String.valueOf(fid)));
                }
                JOptionPane.showMessageDialog(this, "Faculty reactivated.\nLogin access restored.");
                loadData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

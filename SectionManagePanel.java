package gui.panels;

import dao.AdminDAO;
import models.Section;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

/**
 * Admin panel to view and add sections.
 */
public class SectionManagePanel extends JPanel {
    private final AdminDAO adminDao = new AdminDAO();
    private final DefaultTableModel model;
    private final JTable table;

    public SectionManagePanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Section ID", "Section Name"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton addBtn = new JButton("Add Section");
        JButton editBtn = new JButton("Edit Section");
        JButton refBtn = new JButton("Refresh");
        addBtn.addActionListener(e -> showAddDialog());
        editBtn.addActionListener(e -> showEditDialog());
        refBtn.addActionListener(e -> loadData());
        btnBar.add(addBtn); btnBar.add(editBtn); btnBar.add(refBtn);
        add(btnBar, BorderLayout.SOUTH);

        loadData();
    }

    public void loadData() {
        model.setRowCount(0);
        try {
            for (Section s : adminDao.getAllSections())
                model.addRow(new Object[]{s.getSectionId(), s.getSectionName()});
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        JTextField nameField = new JTextField(12);
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Section Name (e.g. CSE-A):")); p.add(nameField);

        int res = JOptionPane.showConfirmDialog(this, p, "Add Section", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim().toUpperCase();
        if (!name.matches("[A-Z]{2,5}-[A-Z]")) { JOptionPane.showMessageDialog(this, "Invalid format. Use: 2-5 uppercase letters, dash, 1 letter. (e.g. CSE-A)"); return; }
        try {
            adminDao.addSection(new Section(0, name));
            JOptionPane.showMessageDialog(this, "Section '" + name + "' added.");
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a section to edit."); return; }
        
        int secId = (Integer) model.getValueAt(row, 0);
        String currentName = (String) model.getValueAt(row, 1);
        
        JTextField nameField = new JTextField(currentName, 12);
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Section Name (e.g. CSE-A):")); p.add(nameField);

        int res = JOptionPane.showConfirmDialog(this, p, "Edit Section", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim().toUpperCase();
        if (!name.matches("[A-Z]{2,5}-[A-Z]")) { JOptionPane.showMessageDialog(this, "Invalid format. Use: 2-5 uppercase letters, dash, 1 letter. (e.g. CSE-A)"); return; }
        try {
            adminDao.updateSection(new Section(secId, name));
            JOptionPane.showMessageDialog(this, "Section updated.");
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

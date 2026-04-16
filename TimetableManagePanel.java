package gui.panels;

import dao.AdminDAO;
import models.Course;
import models.Faculty;
import models.Section;
import models.TimetableSlot;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin panel to view, add, and delete timetable slots.
 * Features a weekly grid view by default, and a detailed list drawer.
 */
public class TimetableManagePanel extends JPanel {
    private final AdminDAO adminDao = new AdminDAO();
    
    // Grid components
    private JComboBox<SectionItem> sectionCombo;
    private DefaultTableModel gridModel;
    private JTable gridTable;
    
    // Drawer components
    private JPanel drawerPanel;
    private DefaultTableModel listModel;
    private JTable listTable;
    
    private List<TimetableSlot> allSlots;

    public TimetableManagePanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Top Bar ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        JButton addBtn = new JButton("Add Slot");
        JButton editBtn = new JButton("Edit Slot");
        JButton delBtn = new JButton("Delete Slot");
        JButton refreshBtn = new JButton("Refresh");
        
        addBtn.addActionListener(e -> showAddDialog());
        editBtn.addActionListener(e -> showEditDialog());
        delBtn.addActionListener(e -> showDeleteDialog());
        refreshBtn.addActionListener(e -> loadData());
        
        topBar.add(addBtn);
        topBar.add(editBtn);
        topBar.add(delBtn);
        topBar.add(refreshBtn);

        JButton toggleDrawerBtn = new JButton("Section Wise View (Drawer)");
        toggleDrawerBtn.addActionListener(e -> {
            drawerPanel.setVisible(!drawerPanel.isVisible());
            revalidate();
            repaint();
        });
        topBar.add(toggleDrawerBtn);
        add(topBar, BorderLayout.NORTH);

        // --- Center: Detailed Slots View (Default) ---
        String[] listCols = {"Slot ID", "Section", "Day", "Period", "Course Code", "Course Name", "Faculty"};
        listModel = new DefaultTableModel(listCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        listTable = new JTable(listModel);
        listTable.setRowHeight(24);
        listTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(listTable), BorderLayout.CENTER);

        // --- East: Weekly Grid View (Drawer) ---
        drawerPanel = new JPanel(new BorderLayout(5, 5));
        drawerPanel.setPreferredSize(new Dimension(500, 0));
        drawerPanel.setBorder(BorderFactory.createTitledBorder("Weekly Section Grid"));
        
        JPanel drawerTopBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        drawerTopBar.add(new JLabel("Select Section:"));
        sectionCombo = new JComboBox<>();
        sectionCombo.addActionListener(e -> refreshGridView());
        drawerTopBar.add(sectionCombo);
        drawerPanel.add(drawerTopBar, BorderLayout.NORTH);

        String[] gridCols = {"Day", "P1", "P2", "P3", "P4", "P5", "P6"};
        gridModel = new DefaultTableModel(gridCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) { return String.class; }
        };
        gridTable = new JTable(gridModel);
        gridTable.setRowHeight(40);
        gridTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        gridTable.getColumnModel().getColumn(0).setPreferredWidth(120);

        // Center align grid cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i = 0; i < gridTable.getColumnCount(); i++) {
            gridTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        drawerPanel.add(new JScrollPane(gridTable), BorderLayout.CENTER);
        
        drawerPanel.setVisible(false); // Hidden by default
        add(drawerPanel, BorderLayout.EAST);

        loadSections();
        loadData();
    }
    
    private void loadSections() {
        try {
            SectionItem selected = (SectionItem) sectionCombo.getSelectedItem();
            int selectedId = (selected != null) ? selected.id : -1;

            List<Section> sections = adminDao.getAllSections();
            sectionCombo.removeAllItems();
            
            SectionItem toSelect = null;
            for (Section s : sections) {
                SectionItem item = new SectionItem(s.getSectionId(), s.getSectionName());
                sectionCombo.addItem(item);
                if (s.getSectionId() == selectedId) {
                    toSelect = item;
                }
            }
            
            if (toSelect != null) {
                sectionCombo.setSelectedItem(toSelect);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading sections: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadData() {
        try {
            loadSections(); // Refresh the dropdown
            allSlots = adminDao.getAllTimetableSlots();
            
            // Populate List View
            listModel.setRowCount(0);
            for (TimetableSlot t : allSlots) {
                listModel.addRow(new Object[]{
                    t.getTimetableId(), t.getSectionName(), t.getDayOfWeek().substring(0,3),
                    t.getPeriodNumber(), t.getCourseCode(), t.getCourseName(), t.getFacultyName()
                });
            }
            
            // Refresh Grid View based on selected section
            refreshGridView();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshGridView() {
        if (allSlots == null) return;
        SectionItem selected = (SectionItem) sectionCombo.getSelectedItem();
        if (selected == null) return;
        
        gridModel.setRowCount(0);
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
        
        // Filter slots for the selected section
        List<TimetableSlot> sectionSlots = allSlots.stream()
            .filter(t -> t.getSectionId() == selected.id)
            .collect(Collectors.toList());
            
        for (String day : days) {
            Object[] row = new Object[7]; // Day + 6 periods
            row[0] = day;
            for(int i=1; i<=6; i++) row[i] = "..."; // Default value
            
            // Fill actual slots
            for (TimetableSlot slot : sectionSlots) {
                if (slot.getDayOfWeek().equalsIgnoreCase(day)) {
                    int p = slot.getPeriodNumber();
                    if (p >= 1 && p <= 6) {
                        row[p] = slot.getCourseCode();
                    }
                }
            }
            gridModel.addRow(row);
        }
    }

    private void showAddDialog() {
        try {
            List<Section> secs     = adminDao.getAllSections();
            List<Course>  courses  = adminDao.getAllCourses().stream().filter(Course::isActive).collect(Collectors.toList());
            List<Faculty> faculty  = adminDao.getAllFaculty();
            if (secs.isEmpty() || courses.isEmpty() || faculty.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please ensure sections, courses, and faculty exist first.");
                return;
            }

            String[] secItems     = secs.stream().map(s -> s.getSectionId() + " - " + s.getSectionName()).toArray(String[]::new);
            String[] courseItems  = courses.stream().map(c -> c.getCourseId() + " - " + c.getCourseCode() + " " + c.getCourseName()).toArray(String[]::new);
            String[] facultyItems = faculty.stream().map(f -> f.getFacultyId() + " - " + f.getName()).toArray(String[]::new);
            String[] days = {"MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"};
            String[] periods = {"1","2","3","4","5","6"};

            JComboBox<String> secCb  = new JComboBox<>(secItems);
            JComboBox<String> crsCb  = new JComboBox<>(courseItems);
            JComboBox<String> facCb  = new JComboBox<>(facultyItems);
            JComboBox<String> dayCb  = new JComboBox<>(days);
            JComboBox<String> perCb  = new JComboBox<>(periods);
            
            // Pre-select the dropdown to current section in the main combo box
            SectionItem currentSec = (SectionItem) sectionCombo.getSelectedItem();
            if(currentSec != null) {
                for(int i=0; i<secs.size(); i++) {
                    if(secs.get(i).getSectionId() == currentSec.id) {
                        secCb.setSelectedIndex(i);
                        break;
                    }
                }
            }

            JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
            p.add(new JLabel("Section:"));  p.add(secCb);
            p.add(new JLabel("Course:"));   p.add(crsCb);
            p.add(new JLabel("Faculty:"));  p.add(facCb);
            p.add(new JLabel("Day:"));      p.add(dayCb);
            p.add(new JLabel("Period:"));   p.add(perCb);

            int res = JOptionPane.showConfirmDialog(this, p, "Add Timetable Slot", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;

            int secId  = secs.get(secCb.getSelectedIndex()).getSectionId();
            int crsId  = courses.get(crsCb.getSelectedIndex()).getCourseId();
            int facId  = faculty.get(facCb.getSelectedIndex()).getFacultyId();
            String day = (String) dayCb.getSelectedItem();
            int period = Integer.parseInt((String) perCb.getSelectedItem());

            adminDao.addTimetableSlot(secId, crsId, facId, day, period);
            JOptionPane.showMessageDialog(this, "Slot added: " + day + " Period-" + period);
            loadData(); // Will refresh both list and grid
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDeleteDialog() {
        String input = JOptionPane.showInputDialog(this, "Enter Timetable Slot ID to delete:");
        if (input == null || input.trim().isEmpty()) return;
        try {
            int tid = Integer.parseInt(input.trim());
            int confirm = JOptionPane.showConfirmDialog(this, "Delete Timetable Slot ID " + tid + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            adminDao.deleteTimetableSlot(tid);
            JOptionPane.showMessageDialog(this, "Slot deleted.");
            loadData();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Slot ID.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showEditDialog() {
        int selectedRow = listTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a slot to edit");
            return;
        }

        int slotId = (Integer) listModel.getValueAt(selectedRow, 0);
        String section = (String) listModel.getValueAt(selectedRow, 1);
        String day = (String) listModel.getValueAt(selectedRow, 2);
        int period = (Integer) listModel.getValueAt(selectedRow, 3);
        String currentCourseName = (String) listModel.getValueAt(selectedRow, 5);
        String currentFaculty = (String) listModel.getValueAt(selectedRow, 6);

        try {
            List<Course> activeCourses = adminDao.getAllCourses().stream()
                .filter(Course::isActive)
                .collect(Collectors.toList());
                
            List<Faculty> activeFaculty = adminDao.getAllFaculty().stream()
                .filter(Faculty::isActive)
                .collect(Collectors.toList());

            if (activeCourses.isEmpty() || activeFaculty.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No active courses or faculty found.");
                return;
            }

            String[] courseItems = activeCourses.stream().map(c -> c.getCourseId() + " - " + c.getCourseCode() + " " + c.getCourseName()).toArray(String[]::new);
            String[] facultyItems = activeFaculty.stream().map(f -> f.getFacultyId() + " - " + f.getName()).toArray(String[]::new);

            JComboBox<String> crsCb = new JComboBox<>(courseItems);
            JComboBox<String> facCb = new JComboBox<>(facultyItems);

            for (int i = 0; i < activeCourses.size(); i++) {
                if (activeCourses.get(i).getCourseName().equals(currentCourseName)) {
                    crsCb.setSelectedIndex(i);
                    break;
                }
            }
            for (int i = 0; i < activeFaculty.size(); i++) {
                if (activeFaculty.get(i).getName().equals(currentFaculty)) {
                    facCb.setSelectedIndex(i);
                    break;
                }
            }

            JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
            p.add(new JLabel("Section:")); p.add(new JLabel(section));
            p.add(new JLabel("Day:")); p.add(new JLabel(day));
            p.add(new JLabel("Period:")); p.add(new JLabel(String.valueOf(period)));
            p.add(new JLabel("Course:")); p.add(crsCb);
            p.add(new JLabel("Faculty:")); p.add(facCb);

            int res = JOptionPane.showConfirmDialog(this, p, "Edit Timetable Slot", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;

            int crsId = activeCourses.get(crsCb.getSelectedIndex()).getCourseId();
            int facId = activeFaculty.get(facCb.getSelectedIndex()).getFacultyId();

            adminDao.updateTimetableSlot(slotId, crsId, facId);
            JOptionPane.showMessageDialog(this, "Slot updated successfully.");
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Helper class for Section Combo Box
    private static class SectionItem {
        int id;
        String name;
        public SectionItem(int id, String name) {
            this.id = id;
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        }
    }
}

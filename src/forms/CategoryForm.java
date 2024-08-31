package forms;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CategoryForm extends JFrame {
    private JTable tblData;
    private JTextField txtID;
    private JTextField txtCategory;
    private JButton btnCancel;
    private JButton btnCreate;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JLabel lblTitle;
    private JLabel lblID;
    private JLabel lblName;
    private JPanel panelData;
    private JPanel panelControl;
    private JPanel panelButtons;
    private JPanel panelMain;

    DefaultTableModel tableModel = new DefaultTableModel();

    public CategoryForm() {
        initializedTable();
        setTitle("Category Form");
        txtID.setEditable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        setContentPane(panelMain);

        btnCancel.addActionListener(e -> {
            clearControls(panelControl);
            loadCategory(tableModel);
            this.setVisible(false);
            MenuForm menuForm = new MenuForm();
            menuForm.setVisible(true);
        });

        btnCreate.addActionListener(e -> {
            if (txtID.getText().isEmpty()) {
                String category = txtCategory.getText();
                insertCategory(category);
                clearControls(panelControl);
                loadCategory(tableModel);
            }
        });

        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblData.getSelectedRow();
                if (selectedRow != -1) {
                    Object selectedCode = tblData.getValueAt(selectedRow, 0);
                    loadCategoryByID(selectedCode.toString());
                }
            }
        });

        btnUpdate.addActionListener(e -> {
            if (!txtID.getText().isEmpty()) {
                String id = txtID.getText();
                String category = txtCategory.getText();
                updateCategory(id, category);
                clearControls(panelControl);
                loadCategory(tableModel);
            }
        });

        btnDelete.addActionListener(e -> {
            if (!txtID.getText().isEmpty()) {
                String id = txtID.getText();
                deleteCategory(id);
                clearControls(panelControl);
                loadCategory(tableModel);
            }
        });
    }

    private void initializedTable() {
        tableModel.addColumn("ID");
        tableModel.addColumn("Category");
        loadCategory(tableModel);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblData.setModel(tableModel);
        tblData.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(tblData);
        scrollPane.setPreferredSize(new Dimension(tblData.getPreferredScrollableViewportSize().width, 300));
        JPanel panelStudent = new JPanel(new BorderLayout());
        panelStudent.add(scrollPane, BorderLayout.CENTER);
        panelData.add(panelStudent);
    }

    private void clearControls(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JTextField) {
                ((JTextField) component).setText("");
            } else if (component instanceof JComboBox) {
                ((JComboBox<?>) component).setSelectedIndex(0);
            } else if (component instanceof JTextArea) {
                ((JTextArea) component).setText("");
            }
        }
    }

    private void loadCategory(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        String query = "SELECT categoriesID, categoriesName FROM categories ORDER BY categoriesID";
        try (Connection con = DBConnection.getConnection();
             Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String id = resultSet.getString("categoriesID");
                String category = resultSet.getString("categoriesName");

                tableModel.addRow(new Object[]{id, category});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data from database", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCategoryByID(String code) {
        String query = "SELECT categoriesID, categoriesName FROM categories WHERE categoriesID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setString(1, code);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                txtID.setText(resultSet.getString("categoriesID"));
                txtCategory.setText(resultSet.getString("categoriesName"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data from database: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertCategory(String category) {
        String insertSQL = "INSERT INTO categories(categoriesName) VALUES (?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(insertSQL)) {

            pstmt.setString(1, category);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Record inserted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to insert the record.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database: " + e.getMessage());
        }
    }

    private void updateCategory(String id, String category) {
        String updateSQL = "UPDATE categories SET categoriesName = ? WHERE categoriesID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(updateSQL)) {

            pstmt.setString(1, category);
            pstmt.setString(2, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Record updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update the record.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteCategory(String id) {
        String deleteSQL = "DELETE FROM categories WHERE categoriesID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(deleteSQL)) {

            pstmt.setString(1, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Record deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete the record.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private String generateCategoryID() {
        return "001"; // Replace with actual logic
    }
}

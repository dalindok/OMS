package forms;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CustomerForm extends JFrame {
    private JTable tblData;
    private JTextField txtName;
    private JTextField txtContact;
    private JTextField txtDistrict;
    private JTextField txtCity;
    private JButton btnCancel;
    private JButton btnCreate;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JLabel lblTitle;
    private JLabel lblName;
    private JLabel lblContact;
    private JLabel lblDistrict;
    private JLabel lblCity;
    private JPanel panelControls;
    private JPanel panelData;
    private JPanel panelTitle;
    private JPanel panelMain;
    private JLabel lblID;
    private JTextField txtID;

    DefaultTableModel tableModel = new DefaultTableModel();

    public CustomerForm() {
        initializedTable();
        setTitle("Customer Form");
        txtID.setEditable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        setContentPane(panelMain);

        btnCancel.addActionListener(e -> {
            clearControls(panelControls);
            loadCustomer(tableModel);
            CustomerForm.this.setVisible(false);// Hide this form
            MenuForm manuForm = new MenuForm();
            manuForm.setVisible(true);
        });

        btnCreate.addActionListener(e -> {
            if (txtID.getText().equals("")) {
                String name = txtName.getText();
                String contact = txtContact.getText();
                String district = txtDistrict.getText();
                String city = txtCity.getText();

                insertCustomer(name, contact, district, city);
                clearControls(panelControls);
                loadCustomer(tableModel);
            }
        });

        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblData.getSelectedRow();
                if (selectedRow != -1) {
                    Object selectedCode = tblData.getValueAt(selectedRow, 0);
                    loadCustomerByID(selectedCode.toString());
                }
            }
        });

        btnUpdate.addActionListener(e -> {
            if (!txtID.getText().isEmpty()) {
                String id = txtID.getText();
                String name = txtName.getText();
                String contact = txtContact.getText();
                String district = txtDistrict.getText();
                String city = txtCity.getText();
                updateCustomer(id, name, contact, district, city);
                clearControls(panelControls);
                loadCustomer(tableModel);
            }
        });

        btnDelete.addActionListener(e -> {
            if (!txtID.getText().isEmpty()) {
                String id = txtID.getText();
                deleteCustomer(id);
                clearControls(panelControls);
                loadCustomer(tableModel);
            }
        });
    }

    private void initializedTable() {
        tableModel.addColumn("ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Contact");
        tableModel.addColumn("District");
        tableModel.addColumn("City");
        loadCustomer(tableModel);
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
            }
        }
    }

    private void loadCustomer(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        String query = "SELECT customerID, customerName, customerContact, customerDistrict, customerCity FROM customer ORDER BY customerID";
        try (Connection con = DBConnection.getConnection();
             Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String id = resultSet.getString("customerID");
                String name = resultSet.getString("customerName");
                String contact = resultSet.getString("customerContact");
                String district = resultSet.getString("customerDistrict");
                String city = resultSet.getString("customerCity");
                tableModel.addRow(new Object[]{id, name, contact, district, city});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data from database", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCustomerByID(String id) {
        String query = "SELECT customerID, customerName, customerContact, customerDistrict, customerCity FROM customer WHERE customerID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setString(1, id);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                txtID.setText(resultSet.getString("customerID"));
                txtName.setText(resultSet.getString("customerName"));
                txtContact.setText(resultSet.getString("customerContact"));
                txtDistrict.setText(resultSet.getString("customerDistrict"));
                txtCity.setText(resultSet.getString("customerCity"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data from database: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertCustomer(String name, String contact, String district, String city) {
        String insertSQL = "INSERT INTO customer(customerName, customerContact, customerDistrict, customerCity) VALUES ( ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(insertSQL)) {

            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            pstmt.setString(3, district);
            pstmt.setString(4, city);
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

    private void updateCustomer(String id, String name, String contact, String district, String city) {
        String updateSQL = "UPDATE customer SET customerName = ?, customerContact = ?, customerDistrict = ?, customerCity = ? WHERE customerID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(updateSQL)) {

            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            pstmt.setString(3, district);
            pstmt.setString(4, city);
            pstmt.setString(5, id);

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

    private void deleteCustomer(String id) {
        String deleteSQL = "DELETE FROM customer WHERE customerID = ?";
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
}

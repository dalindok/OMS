package forms;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProductForm extends JFrame {
    private JPanel panelTitle;
    private JPanel panelData;
    private JLabel lblProduct;
    private JTextField txtProductID;
    private JLabel lblProductID;
    private JComboBox<String> cboCateID;  // JComboBox for category ID
    private JLabel lblCateID;
    private JLabel lblName;
    private JTextField txtName;
    private JLabel lblPrice;
    private JTextField txtPrice;
    private JLabel lblUpdate;
    private JTextField txtUpdate;  // JTextField for Update Date
    private JButton btnCreate;
    private JButton btnUpdate;
    private JButton btnCancel;
    private JButton btnDelete;
    private JTable tblData;
    private JPanel panelControl;
    private JPanel panelButton;
    private JPanel Mainpanel;
    private JLabel lblDescription;
    private JTextArea txtADescription;
    private JTextField txtDate;  // JTextField for displaying the formatted date
    private DefaultTableModel tableModel;

    public ProductForm() {
        tableModel = new DefaultTableModel();
        initializedTable();
        setTitle("Product Form");
        txtProductID.setEditable(false);
        txtDate.setEditable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        setContentPane(Mainpanel);

        loadCategoriesIntoComboBox(); // Load categories into JComboBox

        btnCancel.addActionListener(e -> {
            clearControls(panelControl);
            loadProduct(tableModel);
            ProductForm.this.setVisible(false);
            MenuForm menuForm = new MenuForm();
            menuForm.setVisible(true);
        });

        btnCreate.addActionListener(e -> {
            if (txtProductID.getText().isEmpty()) {
//                String productID = generateProductID(); // Generate a new product ID
                String[] selectedCategory = cboCateID.getSelectedItem().toString().split(" - ");
                int categoryID = Integer.parseInt(selectedCategory[0]);
                String productName = txtName.getText();
                String productDescription = txtADescription.getText();
                double price = Double.parseDouble(txtPrice.getText());
                String updatedAt = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                insertProduct( categoryID, productName, productDescription, price, updatedAt);
                clearControls(panelControl);
                loadProduct(tableModel);
            }
        });

        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblData.getSelectedRow();
                if (selectedRow != -1) {
                    Object selectedCode = tblData.getValueAt(selectedRow, 0);
                    loadProductByID(selectedCode.toString());
                }
            }
        });

        btnUpdate.addActionListener(e -> {
            if (!txtProductID.getText().isEmpty()) {
                String ID = txtProductID.getText();
                // Extract the selected category ID from the JComboBox
                String[] selectedCategory = cboCateID.getSelectedItem().toString().split(" - ");
                int categoryID = Integer.parseInt(selectedCategory[0]);
                String productName = txtName.getText();
                String productDescription = txtADescription.getText();
                double price = Double.parseDouble(txtPrice.getText());
                String updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                updateProduct(ID, categoryID, productName, productDescription, price, updatedAt);
                clearControls(panelControl);
                loadProduct(tableModel);
            }
        });


        btnDelete.addActionListener(e -> {
            if (!txtProductID.getText().isEmpty()) {
                String productID = txtProductID.getText();
                deleteProduct(productID);
                clearControls(panelControl);
                loadProduct(tableModel);
            }
        });
    }

    private void initializedTable() {
        tableModel.addColumn("Product ID");
        tableModel.addColumn("Category ID");
        tableModel.addColumn("Product Name");
        tableModel.addColumn("Description");
        tableModel.addColumn("Price");
        tableModel.addColumn("Updated At");
        loadProduct(tableModel);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblData.setModel(tableModel);
        tblData.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(tblData);
        scrollPane.setPreferredSize(new Dimension(tblData.getPreferredScrollableViewportSize().width, 300));
        JPanel panelProduct = new JPanel(new BorderLayout());
        panelProduct.add(scrollPane, BorderLayout.CENTER);
        panelData.add(panelProduct);
    }

    private void clearControls(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JTextField) {
                ((JTextField) component).setText("");
            } else if (component instanceof JComboBox) {
                ((JComboBox<?>) component).setSelectedIndex(0);
            } else if (component instanceof JTextArea) {
                ((JTextArea) component).setText("");
            } else if (component instanceof JFormattedTextField) {
                ((JFormattedTextField) component).setValue(null);
            }
        }
    }

    private void loadCategoriesIntoComboBox() {
        String query = "SELECT categoriesID, categoriesName FROM categories";
        try (Connection con = DBConnection.getConnection();
             Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int categoryID = resultSet.getInt("categoriesID");
                String categoryName = resultSet.getString("categoriesName");
                cboCateID.addItem(categoryID + " - " + categoryName); // Display both ID and name
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading categories into combo box", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProduct(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        try {
            Connection con = DBConnection.getConnection();
            String query = "SELECT product.productID, product.categoriesID, product.productName, product.productDescription, product.productPrice, product.updatedat FROM product INNER JOIN categories ON product.categoriesID = categories.categoriesID ORDER BY productID";
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String productID = resultSet.getString("productID");
                String categoryID = resultSet.getString("categoriesID");
                String productName = resultSet.getString("productName");
                String description = resultSet.getString("productDescription");
                String price = resultSet.getString("productPrice");
                String updatedAt = resultSet.getString("updatedat");
                tableModel.addRow(new Object[]{productID, categoryID, productName, description, price, updatedAt});
            }
            con.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data from database", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProductByID(String id) {
        String query = "SELECT productID, categoriesID, productName, productDescription, productPrice, updatedat FROM product WHERE productID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement statement = con.prepareStatement(query)) {

            // Set the parameter for the product ID
            statement.setString(1, id);

            // Execute the query
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Load the data into the form fields
                txtProductID.setText(resultSet.getString("productID"));
                txtProductID.setEditable(false);
                txtName.setText(resultSet.getString("productName"));
                txtADescription.setText(resultSet.getString("productDescription"));
                txtPrice.setText(resultSet.getString("productPrice"));

                // Get the categoriesID from the result set
                int categoryID = resultSet.getInt("categoriesID");

                // Set the correct category in the JComboBox
                for (int i = 0; i < cboCateID.getItemCount(); i++) {
                    String item = cboCateID.getItemAt(i);
                    if (item.startsWith(categoryID + " -")) {  // Check if the item starts with the category ID
                        cboCateID.setSelectedIndex(i);
                        break;
                    }
                }
                Timestamp updatedAtTimestamp = resultSet.getTimestamp("updatedat");
                if (updatedAtTimestamp != null) {
                    LocalDateTime updatedAt = updatedAtTimestamp.toLocalDateTime();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    txtDate.setText(updatedAt.format(formatter));  // Set formatted date to JTextField
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data from database", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void insertProduct(int categoryID, String productName, String productDescription, double price, String updatedAt) {
        String insertSQL = "INSERT INTO product(categoriesID, productName, productDescription, productPrice, updatedat) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(insertSQL)) {

            pstmt.setString(1, String.valueOf(categoryID));
            pstmt.setString(2, productName);
            pstmt.setString(3, productDescription);
            pstmt.setDouble(4, price);
            pstmt.setString(5, updatedAt);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Product inserted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting product into database", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void updateProduct(String ID, int categoryID, String productName, String productDescription, double price, String updatedAt) {
        String updateSQL = "UPDATE product SET categoriesID = ?, productName = ?, productDescription = ?, productPrice = ?, updatedat = ? WHERE productID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(updateSQL)) {

            pstmt.setInt(1, categoryID);
            pstmt.setString(2, productName);
            pstmt.setString(3, productDescription);
            pstmt.setDouble(5, price);
            pstmt.setString(4, updatedAt);
            pstmt.setString(6, ID);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Record updated successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to update the record.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }


    private void deleteProduct(String productID) {
        String deleteSQL = "DELETE FROM product WHERE productID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(deleteSQL)) {

            pstmt.setString(1, productID);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting product from database", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


private String generateProductID() {
    String newProductID = null;
    String query = "SELECT MAX(productID) AS maxID FROM product";
    try (Connection con = DBConnection.getConnection();
         Statement statement = con.createStatement();
         ResultSet resultSet = statement.executeQuery(query)) {

        if (resultSet.next()) {
            String maxID = resultSet.getString("maxID");
            if (maxID != null) {
                int id = Integer.parseInt(maxID) + 1;
                newProductID = String.format("%03d", id); // Formatting ID as a 3-digit number
            } else {
                newProductID = "001";
            }
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return newProductID;
    }

}

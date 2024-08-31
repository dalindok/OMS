package forms;

import cls.KeyValue;
import db.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class CreateOrderForm extends JFrame {

    private JPanel MainPanel;
    private JPanel productPanel;
    private JPanel panelCustomer;
    private JPanel orderPanel;
    private JPanel buttonPanel;
    private JTextField txtTotal;
    private JComboBox<KeyValue> cboStatus;
    private JComboBox<KeyValue> cboCustomer;
    private JButton addNewCustomerButton;
    private JButton btnCreate;
    private JButton btnDelete;
    private JButton btnCancel;
    private JLabel lblTitle;
    private JLabel lblSelectPro;

    private Map<Integer, SelectProduct> selectedProducts = new HashMap<>();

    private KeyValue[] items = {
            new KeyValue(1, "Confirmed"),
            new KeyValue(2, "Complete"),
            new KeyValue(3, "Pending"),
            new KeyValue(4, "Cancel")
    };

    private void initializeComboBox() {
        for (KeyValue kw : items) cboStatus.addItem(kw);
    }

    public CreateOrderForm() {
        setTitle("Create Order");
        setContentPane(MainPanel);
        setMinimumSize(new Dimension(800, 400));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initializeComponents();
        loadProductsIntoPanelProduct();
        loadCustomerIntoComboBox();

        btnCancel.addActionListener(e -> {
            clearControls(MainPanel);
            CreateOrderForm.this.setVisible(false);
            MenuForm menuForm = new MenuForm();
            menuForm.setVisible(true);
        });

        addNewCustomerButton.addActionListener(e ->{
            CreateOrderForm.this.setVisible(false);
            CustomerForm customerForm = new CustomerForm();
            customerForm.setVisible(true);
        });

        btnCreate.addActionListener(e -> {
            // Check if required fields are filled
            if (selectedProducts.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select at least one product.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int customerID = getKeyFromSelectedItem(cboCustomer);
            int statusID = getKeyFromSelectedItem(cboStatus);
            double orderTotalPrice = Double.parseDouble(txtTotal.getText());
            String createdAt = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


            insertOrder(customerID,statusID, orderTotalPrice, selectedProducts, createdAt);
            clearControls(MainPanel);
        });

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

    private static void setSelectedItemByKey(JComboBox<KeyValue> comboBox, int key) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            KeyValue item = comboBox.getItemAt(i);
            if (item.getKey() == key) {
                comboBox.setSelectedItem(item);
                break;
            }
        }
    }

    private int getKeyFromSelectedItem(JComboBox<KeyValue> comboBox) {
        KeyValue selectedItem = (KeyValue) comboBox.getSelectedItem();
        return selectedItem != null ? selectedItem.getKey() : 0;
    }

    private void initializeComponents() {
        productPanel.setLayout(new BoxLayout(productPanel, BoxLayout.Y_AXIS));
        txtTotal.setEditable(false);
//        addNewCustomerButton.addActionListener(e -> openCustomerForm());
        initializeComboBox();
    }

    private void loadProductsIntoPanelProduct() {
        String query = "SELECT productID, productName, productPrice FROM product";
        try (Connection con = DBConnection.getConnection();
             Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            productPanel.removeAll();
            selectedProducts.clear();

            while (resultSet.next()) {
                int productID = resultSet.getInt("productID");
                String productName = resultSet.getString("productName");
                double productPrice = resultSet.getDouble("productPrice");

                JPanel individualProductPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                JCheckBox productCheckBox = new JCheckBox(productName);
                productCheckBox.setName(String.valueOf(productID));

                JLabel lblQuantity = new JLabel("Quantity:");
                JSpinner quantityField = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));

                productCheckBox.addItemListener(e -> {
                    if (productCheckBox.isSelected()) {
                        int qty = (int) quantityField.getValue();
                        selectedProducts.put(productID, new SelectProduct(qty, qty * productPrice));
                    } else {
                        selectedProducts.remove(productID);
                    }
                    updateTotalPrice();
                });

                quantityField.addChangeListener(e -> {
                    if (productCheckBox.isSelected()) {
                        int qty = (int) quantityField.getValue();
                        selectedProducts.put(productID, new SelectProduct(qty, qty * productPrice));
                        updateTotalPrice();
                    }
                });

                individualProductPanel.add(productCheckBox);
                individualProductPanel.add(lblQuantity);
                individualProductPanel.add(quantityField);
                productPanel.add(individualProductPanel);
            }

            productPanel.revalidate();
            productPanel.repaint();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading products into form", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTotalPrice() {
        double total = selectedProducts.values().stream().mapToDouble(od -> od.totalPrice).sum();
        txtTotal.setText(String.format("%.2f", total));
    }

    private void openCustomerForm() {
        JOptionPane.showMessageDialog(this, "Opening Customer Form...");
        new CustomerForm().setVisible(true);
    }

    private void loadCustomerIntoComboBox() {
        String query = "SELECT customerID, customerName FROM customer";
        try (Connection con = DBConnection.getConnection();
             Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int customerID = resultSet.getInt("customerID");
                String customerName = resultSet.getString("customerName");
                cboCustomer.addItem(new KeyValue(customerID, customerName));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading customers into combo box", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertOrder(int customerID,int statusID, double orderTotalPrice, Map<Integer, SelectProduct> selectedProducts, String createAt) {
        String insertSQL = "INSERT INTO `order` (customerID, statusID, orderTotalPrice, createdat) VALUES (?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement orderStmt = null;
        ResultSet generatedKeys = null;

        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            orderStmt = con.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, customerID);
            orderStmt.setInt(2, statusID);
            orderStmt.setDouble(3, orderTotalPrice);
            orderStmt.setString(4, createAt);

            int rowsAffected = orderStmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            // Retrieve the generated orderID
            generatedKeys = orderStmt.getGeneratedKeys();
            int orderID = 0;
            if (generatedKeys.next()) {
                orderID = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating order failed, no ID obtained.");
            }

            // Insert order details
            insertOrderDetail(con, orderID, selectedProducts);
            con.commit();
        } catch (SQLException ex) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            JOptionPane.showMessageDialog(this, "Error inserting order into database", "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException ignore) {}
            if (orderStmt != null) try { orderStmt.close(); } catch (SQLException ignore) {}
            if (con != null) try { con.close(); } catch (SQLException ignore) {}
        }
    }

    private void insertOrderDetail(Connection con, int orderID, Map<Integer, SelectProduct> selectedProducts) throws SQLException {
//        System.out.println("orderID : " + orderID);
        String insertOrderDetailSQL = "INSERT INTO `orderdetail` (orderID, productID, quantity, totalprice) VALUES (?, ?, ? ,?)";

        PreparedStatement orderDetailStmt = null;

        try {
            orderDetailStmt = con.prepareStatement(insertOrderDetailSQL);
            for (Map.Entry<Integer, SelectProduct> entry : selectedProducts.entrySet()) {
                int productID = entry.getKey();
                SelectProduct product = entry.getValue();

                orderDetailStmt.setInt(1, orderID);
                orderDetailStmt.setInt(2, productID);
                orderDetailStmt.setInt(3, product.quantity);
                orderDetailStmt.setDouble(4, product.totalPrice);
                orderDetailStmt.addBatch();
            }

            int[] batchResults = orderDetailStmt.executeBatch();

            // Optional: Check if all inserts were successful
            for (int result : batchResults) {
                if (result == Statement.EXECUTE_FAILED) {
                    throw new SQLException("Inserting order details failed.");
                }
            }
            JOptionPane.showMessageDialog(this, "Create Order successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } finally {
            if (orderDetailStmt != null) try { orderDetailStmt.close(); } catch (SQLException ignore) {}
        }
    }
    class SelectProduct {
        int quantity;
        double totalPrice;

        SelectProduct(int quantity, double totalPrice) {
            this.quantity = quantity;
            this.totalPrice = totalPrice;
        }
    }
}

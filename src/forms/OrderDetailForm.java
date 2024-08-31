package forms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import db.DBConnection;

public class OrderDetailForm extends  JFrame {
    private JPanel panelOrder;
    private JPanel panelCustomer;
    private JButton btnBack;
    private JLabel lblDate;
    private JPanel panelProduct;
    private JTable tblProduct;
    private JLabel lblStatus;
    private JLabel lblTotal;
    private JLabel lblCustomerName;
    private JLabel lblPhone;
    private JLabel lblAddress;
    private JLabel lblCustomerDetail;
    private JLabel lblProductDetail;
    private JLabel lblOrderDetail;
    private JPanel Mainpanel;
    private JTextField txtDate;
    private JTextField txtStatus;
    private JTextField txtPrice;
    private JTextField txtCusName;
    private JTextField txtCusContact;
    private JTextField txtCusAddress;
    private DefaultTableModel tableModel;

    public  OrderDetailForm(String orderID, String customerID, String orderStatus, String orderTotalPrice, String orderDate){
        tableModel = new DefaultTableModel();
        initOrderInfo(orderStatus, orderTotalPrice, orderDate);
        initCustomerInfo(customerID);
        initProductInfo(orderID);
//        tableModel = new DefaultTableModel();
//        initializedTable();
        setTitle("Order Detail Form");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        setContentPane(Mainpanel);
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OrderDetailForm.this.setVisible(false);
                OrderListForm orderListForm = new OrderListForm();
                orderListForm.setVisible(true);
            }
        });
    }


    private void initOrderInfo(String orderStatus, String orderTotalPrice, String orderDate){
        // Set default or initial values for the order information fields
        txtDate.setEditable(false); // Assume date is read-only
        txtStatus.setEditable(false); // Assume status is read-only
        txtPrice.setEditable(false); // Assume price is read-only

        txtDate.setText(orderDate);
        txtStatus.setText(orderStatus);
        txtPrice.setText(orderTotalPrice);
    }
    private void initCustomerInfo(String customerId){
        // Set default or initial values for the customer information fields
        txtCusName.setEditable(false); // Assume customer name is read-only
        txtCusContact.setEditable(false); // Assume customer contact is read-only
        txtCusAddress.setEditable(false);// Assume customer address is read-only

        String query = "SELECT `customer`.customerName, `customer`.customerContact, `customer`.customerCity, `customer`.customerDistrict FROM `customer` WHERE `customer`.customerID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement statement = con.prepareStatement(query)) {
            statement.setInt(1, Integer.parseInt(customerId));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                txtCusName.setText(resultSet.getString("customerName"));
                txtCusContact.setText(resultSet.getString("customerContact"));
                txtCusAddress.setText(resultSet.getString("customerDistrict") + " , " + resultSet.getString("customerCity"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void initProductInfo(String orderId){
        tableModel.addColumn("Product ID");
        tableModel.addColumn("Product Name");
        tableModel.addColumn("Quantity");
        tableModel.addColumn("Price");
        tableModel.addColumn("Total Price");

        tblProduct.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblProduct.setModel(tableModel);
        tblProduct.setRowHeight(30);

        // Load data into the model
       loadProductFromDB(orderId, tableModel);

        // Add JTable to JScrollPane
        JScrollPane scrollPane = new JScrollPane(tblProduct);
        scrollPane.setPreferredSize(new Dimension(tblProduct.getPreferredScrollableViewportSize().width, 300));

        // Add JScrollPane to the panel
        panelProduct.setLayout(new BorderLayout());
        panelProduct.add(scrollPane, BorderLayout.CENTER);

//        Mainpanel.add(panelProduct);
    }

    private void loadProductFromDB(String orderId, DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        String query = "SELECT `orderdetail`.productID, `orderdetail`.quantity, `orderdetail`.totalprice, `product`.productName, `product`.productPrice " +
                "FROM `orderdetail` " +
                "INNER JOIN `product` ON `orderdetail`.productID = `product`.productID " +
                "WHERE `orderdetail`.orderID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement statement = con.prepareStatement(query)) {

            // Set the parameter for the order ID
            statement.setInt(1, Integer.parseInt(orderId));

            // Execute the query
            ResultSet resultSet = statement.executeQuery();

            // Iterate through the result set and add rows to the table model
            while (resultSet.next()) {
                String productId = resultSet.getString("productID");
                String productName = resultSet.getString("productName");
                int quantity = resultSet.getInt("quantity");
                double totalPrice = resultSet.getDouble("totalprice");
                double productPrice = resultSet.getDouble("productPrice");

                tableModel.addRow(new Object[]{productId, productName, quantity, "$" + productPrice, "$" + totalPrice});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data from database", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

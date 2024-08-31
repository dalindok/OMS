package forms;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class OrderListForm extends JFrame {
    private JButton btnBackToManu;
    private JButton btnConfirm;
    private JButton btnCancel;
    private JButton btnComplete;
    private JButton btnDetail;
    private DefaultTableModel tableModel;
    private JTable tblData;
    private JPanel panelButton;
    private JPanel Mainpanel;
    private JPanel pamelTitle;
    private JLabel lblTitle;
    private JPanel panelData;
    private String orderId;
    private String customerId;
    private String status;
    private String orderDate;
    private String totalPrices;

    public  OrderListForm(){
        tableModel = new DefaultTableModel();
        initializedTable();
        setTitle("List Order Form");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        setContentPane(Mainpanel);

        // Initially hide all buttons
        btnConfirm.setVisible(false);
        btnCancel.setVisible(false);
        btnComplete.setVisible(false);
        btnDetail.setVisible(false);

        btnBackToManu.addActionListener(e -> {
            this.setVisible(false);
            MenuForm menuForm = new MenuForm();
            menuForm.setVisible(true);
        });

        btnConfirm.addActionListener(e -> {
            // Update order status to CONFIRM in the database
            updateOrderStatus(orderId, 1);
        });

        btnCancel.addActionListener(e -> {
            // Update order status to CANCEL in the database
            updateOrderStatus(orderId, 4);
        });

        btnComplete.addActionListener(e -> {
            // Update order status to COMPLETE in the database
            updateOrderStatus(orderId, 2);
        });

        btnDetail.addActionListener(e -> {
            OrderDetailForm orderDetailForm = new OrderDetailForm(orderId, customerId, status, totalPrices, orderDate);
            this.setVisible(false);
            orderDetailForm.setVisible(true);
        });

        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblData.getSelectedRow();
                if (selectedRow != -1) {
                    btnDetail.setVisible(true);
                    Object statusName = tblData.getValueAt(selectedRow, 2);
                    updateButtonVisibility(statusName.toString());

                    String query = "SELECT `order`.customerID FROM `order` WHERE `order`.orderID = ?";
                    Object orderID = tblData.getValueAt(selectedRow, 0);
                    try (Connection con = DBConnection.getConnection();
                         PreparedStatement statement = con.prepareStatement(query)) {
                        statement.setInt(1, Integer.parseInt((String) orderID));
                        ResultSet resultSet = statement.executeQuery();
                        if (resultSet.next()) {
                            customerId = resultSet.getString("customerID");
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    Object total = tblData.getValueAt(selectedRow, 3);
                    Object date = tblData.getValueAt(selectedRow, 4);
                    orderId = orderID.toString();
                    status = statusName.toString();
                    totalPrices = total.toString();
                    orderDate = date.toString();
//                    loadOrderByID(selectedCode.toString());
                }
            }
        });

    }
    private void initializedTable() {
        // Initialize table and model
        tableModel.addColumn("Order ID");
        tableModel.addColumn("Customer Name");
        tableModel.addColumn("Status");
        tableModel.addColumn("Total");
        tableModel.addColumn("Date");

        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblData.setModel(tableModel);
        tblData.setRowHeight(30);

        // Load data into the model
        loadOrderList(tableModel);

        // Add JTable to JScrollPane
        JScrollPane scrollPane = new JScrollPane(tblData);
        scrollPane.setPreferredSize(new Dimension(tblData.getPreferredScrollableViewportSize().width, 300));

        // Add JScrollPane to the panel
        panelData.setLayout(new BorderLayout());
        panelData.add(scrollPane, BorderLayout.CENTER);

        Mainpanel.add(panelData, BorderLayout.CENTER);
    }

    private void loadOrderList(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        try {
            Connection con = DBConnection.getConnection();
            String query = "SELECT `order`.orderID, `order`.customerID, `order`.statusID, `order`.orderTotalPrice, `order`.createdat, `customer`.customerName,`status`.statusName FROM `order` INNER JOIN `customer` ON `order`.customerID = `customer`.customerID INNER JOIN `status` ON `order`.statusID = `status`.statusID  ORDER BY `order`.orderID";
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
//            System.out.println(resultSet.getString("customerName"));
                String orderID = resultSet.getString("orderID");
                String customerName = resultSet.getString("customerName");
                String customerID = resultSet.getString("customerID");
                String statusName = resultSet.getString("statusName");
                String orderTotalPrice = resultSet.getString("orderTotalPrice");
                String orderDate = resultSet.getString("createdat");
                tableModel.addRow(new Object[]{orderID, customerName, statusName.toUpperCase(), "$" + orderTotalPrice, orderDate});
            }
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data from database", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void updateButtonVisibility(String status) {
        // Initially hide all buttons
        btnConfirm.setVisible(false);
        btnCancel.setVisible(false);
        btnComplete.setVisible(false);
        // Show buttons based on the status
        if (status.equals("PENDING")) {
            btnConfirm.setVisible(true);
            btnCancel.setVisible(true);
        } else if (status.equals("CONFIRMED ")) {
            btnComplete.setVisible(true);
        }

    }
    private void updateOrderStatus(String orderId, int statusId) {
        String query = "UPDATE `order` SET `statusID` = ? WHERE `orderID` = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement statement = con.prepareStatement(query)) {

            statement.setString(1, String.valueOf(statusId));
            statement.setString(2, orderId);

            statement.executeUpdate();
            loadOrderList(tableModel);
            btnConfirm.setVisible(false);
            btnCancel.setVisible(false);
            btnComplete.setVisible(false);
            btnDetail.setVisible(false);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

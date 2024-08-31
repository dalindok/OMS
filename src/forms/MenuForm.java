package forms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuForm extends JFrame {
    private JPanel panelControls;
    private JButton btnCustomer;
    private JButton btnOrderList;
    private JButton btnProduct;
    private JButton btnCategory;
    private JPanel Mainpanel;
    private JLabel lblMenu;
    private JButton btnCreateOrder;

    public MenuForm() {
        setTitle("Menu");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(Mainpanel);
        setVisible(true);

        btnCreateOrder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MenuForm.this.setVisible(false);
                CreateOrderForm createOrderForm = new CreateOrderForm();
                createOrderForm.setVisible(true);
            }
        });
        btnOrderList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MenuForm.this.setVisible(false);
                OrderListForm orderListForm = new OrderListForm();
                orderListForm.setVisible(true);
            }
        });
        btnCustomer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MenuForm.this.setVisible(false);
                CustomerForm customerForm = new CustomerForm();
                customerForm.setVisible(true);
            }
        });
        btnProduct.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MenuForm.this.setVisible(false);
                ProductForm productForm = new ProductForm();
                productForm.setVisible(true);
            }
        });
        btnCategory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MenuForm.this.setVisible(false);
                CategoryForm  categoryForm = new CategoryForm();
                categoryForm.setVisible(true);
            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}

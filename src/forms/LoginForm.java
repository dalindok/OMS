package forms;

import cls.AuthenticationState;
import cls.User;
import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginForm extends JFrame {
    private JPanel MainPanel;
    private JTextField txtUserName;
    private JButton btnLogin;
    private JButton btnReset;
    private JLabel lblLogin;
    private JLabel lblUserName;
    private JLabel lblPassword;
    private JPasswordField txtPassword;
    private JLabel lblMsg;

    public LoginForm() {
        setTitle("Login");
        setContentPane(MainPanel);
        setMinimumSize(new Dimension(400, 300));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                an exception refers to an event that occurs during the execution of a program,
                try {
                    String username = txtUserName.getText();
                    String password = String.valueOf(txtPassword.getPassword());
//                     // Establish the connection
                    Connection con = DBConnection.getConnection();
                    Statement stm = con.createStatement();
//                    if user and password match (it replace ?) condition true
                    String query = "SELECT * FROM USER WHERE userName=? AND userPassword=?";
                    PreparedStatement preparedStatement = con.prepareStatement(query);
                    preparedStatement.setString(1,username);
                    preparedStatement.setString(2,password);
//                    // Execute the query
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()){
                        User.setUserName(rs.getString("userName"));
                        User.setuserPassword(rs.getString("userPassword"));
                        AuthenticationState.setAuthenticated(true);
//                        Open the main form and close the login form
                        MenuForm menuForm = new MenuForm();
//                          Close the current form
                        dispose();
                    }else {
                        AuthenticationState.setAuthenticated(false);
                        lblMsg.setText("Login failed");
                        lblMsg.setForeground(Color.RED);
                    }
                }catch (SQLException ex){
                    ex.printStackTrace();
//                    lblMsg.setText("Database error: " + ex.getMessage());
                }
            }
        });
    }

}


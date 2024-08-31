import db.DBConnection;
import forms.*;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        Connection con = DBConnection.getConnection();
        if (con!=null){
            System.out.println("Connection is established successfully.");
        }
        else{
            System.out.println("Can't connect to database");
        }
Da
        LoginForm loginForm = new LoginForm();
        loginForm.setVisible(true);
    }
}
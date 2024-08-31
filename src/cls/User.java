package cls;
public class User {
    private static String userName;
    private static String userPassword;
    private static int userID;
    public static void setUserName(String user_name){
        userName = user_name;
    }
    public static String getUserName(){
        return userName;
    }
    public static void setRoleID(int user_id){
        userID = user_id;
    }
    public static int getUserID(){
        return userID;
    }

    public static void setuserPassword(String user_password) {
        userPassword = user_password;
    }
    public static String getuserPassword(){
        return userPassword;
    }
}


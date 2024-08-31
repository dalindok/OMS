package cls;
public class AuthenticationState {
    private static boolean authenticated = false;
    public static boolean isAuthenticated() {
        return authenticated;
    }
    public static void setAuthenticated(boolean status) {
        authenticated = status;
    }
}
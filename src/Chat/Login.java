package Chat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Login {
    private Scanner scanner;
    private Connect connection;
    private List<String> userDetails;

    /**
     * Takes user's id and password and pass it to userCheck() method
     * @return
     */
    public List<String> loginIn() {
        scanner = new Scanner(System.in);
        System.out.print("Enter your ID: ");
        String Id = scanner.nextLine();
        System.out.print("Enter your Password: ");
        String password = scanner.nextLine();
        List<String> userCheck = checkUser(Id, password);

        if (userCheck != null) {
            return userCheck;
        }
        return null;

    }

    /**
     * This method works for automatic log in after signing up
     * @param data
     * @return
     */
    public List<String> loginIn(List<String> data) {
        List<String> userCheck = checkUser(data.get(0), data.get(1));
        if (userCheck != null) {
            return userCheck;
        }
        return null;

    }

    /**
     * This method if the user is in the database or not.
     * returns the name and ID of the user.
     * @param Id
     * @param password
     * @return
     */
    private List<String> checkUser(String Id, String password){
        userDetails = new ArrayList<>();
        connection = new Connect();
        try {
            ResultSet userInfo = null;
            try {
                userInfo = connection.createStatement().executeQuery("SELECT * from userdata");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            while (userInfo.next()) {
                if (userInfo.getString("UserID").equals(Id) && userInfo.getString("Password").equals(password)){
                     userDetails.add(userInfo.getString("ID"));
                     userDetails.add(userInfo.getString("FirstName") + " " + userInfo.getString("LastName"));
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return userDetails;
    }
}

package Chat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SignUp {
    private Connect connection;
    private Scanner scanner;
    private List<String> data;


    public List<String> signUp() {

        scanner = new Scanner(System.in);
        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine();
        System.out.print("Enter your user id: ");
        String userId = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        /**
         * Creating new user id everytime, if not it replace the old one in database.
         */
        userId = userId + creatingUserID();
        System.out.println("Your user id is: " + userId + ", remember it for future logins");

        List<String> data = createAccount(firstName, lastName, userId , password);
        return data;
    }

    /**
     * Creating an account for new Users
     * @param firstName
     * @param lastName
     * @param userId
     * @param password
     * @return
     */
    public List<String> createAccount(String firstName, String lastName, String userId, String password){
        connection = new Connect();
        data = new ArrayList<>();
        String createUserAccount = "INSERT INTO userdata (UserId, FirstName, LastName, Password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pStmt = connection.getConnection().prepareStatement(createUserAccount)) {
            pStmt.setString(1, userId);
            pStmt.setString(2, firstName);
            pStmt.setString(3, lastName);
            pStmt.setString(4, password);
            pStmt.executeUpdate();
            data.add(userId);
            data.add(password);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return data;
    }

    /**
     * This helps me to create new user id everytime, taking the last ID (that is primary key) from database and adding 1.
     * @return
     */
    private int creatingUserID() {
        try {
            connection = new Connect();
            ResultSet gettingUserID = connection.createStatement().executeQuery("SELECT MAX(ID) AS last_id FROM userdata");
            if (gettingUserID.next()) {
                int id = gettingUserID.getInt("last_id");
                return id + 1;
            } else {
                return -1;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

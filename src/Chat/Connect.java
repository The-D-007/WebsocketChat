package Chat;

import java.sql.*;

public class Connect {
    private Connection connection;
    private final String URL = "jdbc:mysql://localhost:3306/server";
    private final String userName = "client";
    private final String password = "JavaProject";

    /**
     * Here the connection to database happens
     */
    public  Connect(){
        try{
            this.connection = DriverManager.getConnection(URL, userName, password);
        } catch (SQLException e){
           System.out.println("Server is down...404");
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public Statement createStatement() throws SQLException {
        return this.connection.createStatement();
    }

    public static void main(String[] args) {
        new Connect();
    }
}

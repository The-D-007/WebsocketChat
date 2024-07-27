package Chat;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class ConnectedClient {
    private Connect connection;
    private Socket clientSocket;
    private DataInputStream input;
    private DataOutputStream messageOutput;
    private String userName;
    private String ID;
    private String userFullName;
    private static final List<String> onlineUsers = new ArrayList<>();
    private static final Map<String, ConnectedClient> clientMap = new HashMap<>();

    public ConnectedClient(){}
    /**
     * This method shows the client has connected and make an object to read the message that user will send.
     * @param clientSocket
     * @param userName
     */
    public ConnectedClient(Socket clientSocket, String ID, String userName){
        this.clientSocket = clientSocket;
        this.userName = userName;
        this.ID = ID;
        onlineUsers.add(ID);
        this.userFullName = userName + " - "+ ID;
        clientMap.put(userFullName, this);
        try {
            System.out.println("Client " + userName + " - "+ ID + " Connected." );
            this.input = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            this.messageOutput = new DataOutputStream(clientSocket.getOutputStream());

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Function is used for reading the user messages.
     * Based on the conditions new method or printing works
     */
    public void readMessages(){
        String line = "";
        while (!line.equals(Server.STOP_STRING)) {
            try {
                line = input.readUTF();
                if (line.equals(Server.USERS)) {
                    userInServer();
                    continue;
                }
                if (line.equals(Server.ALL_USERS)) {
                    allUsersInServer();
                    continue;
                }
                if (line.equals(Server.BOT)) {
                    bot();
                    continue;
                }
                if (line.equals("Private Chat")){
                    startPrivateChat();
                    continue;
                }if (line.equals("Hello BOT")) {
                    continue;
                }
                else { //This is mainly used for user to send message to server (group chat).
                    System.out.println("Client " + userFullName + ": " + line);
                }
            } catch (IOException e) {
                break;
            }
        }
        // When the user leaves.
        onlineUsers.remove(ID);
        System.out.println("Client " + userFullName +  " Disconnected." );
    }

    /**
     * Checks online users in the server.
     */
    private void userInServer(){
        try {
            messageOutput.writeUTF("All the online users are: ");
            connection = new Connect();
            ResultSet userInfo;
            try {
                userInfo = connection.createStatement().executeQuery("SELECT ID, FirstName, LastName from userdata");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            Map<String, String> userMap = new HashMap<>();
            while (userInfo.next()) {
                String id = userInfo.getString("ID");
                String fullName = userInfo.getString("FirstName") + " " + userInfo.getString("LastName") + " - "+ id;
                userMap.put(id, fullName);
            }
            for (String user : onlineUsers) {
                if (userMap.containsKey(user)) {
                    messageOutput.writeUTF(userMap.get(user));
                }
            }
        }catch (SQLException | IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Prints all the user who have account on this server
     */
    private void allUsersInServer(){
        try {
            connection = new Connect();
            messageOutput.writeUTF("All the users in the server.");
            ResultSet userInfo;
            try {
                userInfo = connection.createStatement().executeQuery("SELECT ID, FirstName, LastName from userdata");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            while (userInfo.next()) {
                String id = userInfo.getString("ID");
                String fullName = userInfo.getString("FirstName") + " " + userInfo.getString("LastName") + " - "+ id;
                if (onlineUsers.contains(id)){
                    messageOutput.writeUTF(fullName + " (Online)");
                } else{
                    messageOutput.writeUTF(fullName + " (Offline)");
                }
            }
        }catch (SQLException | IOException e){
            e.printStackTrace();
        }
    }


    /**
     * A bot to help users
     */
    private void bot(){
        try {
            messageOutput.writeUTF("Use 'users' to see all available users \n  or use 'all users' to see all the users.");
            messageOutput.writeUTF("Type 'Hello BOT'");
            messageOutput.writeUTF("Type Private Chat");
            String command = input.readUTF();
            handleBotCommand(command);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Do the tasks that are assigned to the bot
     * @param command
     */
    private void handleBotCommand(String command) {
        if (command.equalsIgnoreCase("users")) {
            userInServer();
        } else if(command.equalsIgnoreCase("all users")) {
            allUsersInServer();
        } else if (command.equalsIgnoreCase("Hello BOT")) {
            try {
                messageOutput.writeUTF("My name is BOT, I welcome " + userFullName + " to the server.");
            } catch (IOException e) {
                System.out.println("The BOT is not responding...");
            }
        }
    }

    /**
     * This function starts private chat between users
     */
    private void startPrivateChat(){
        try {
            messageOutput.writeUTF("Enter the digits after '-' of username: ");
            String number = input.readUTF();
            connection = new Connect();
            ResultSet userInfo;
        if (onlineUsers.contains(number)) {
            try {
                userInfo = connection.createStatement().executeQuery("SELECT ID, FirstName, LastName from userdata");
                Map<String, String> userMap = new HashMap<>();
                while (userInfo.next()) {
                    String id = userInfo.getString("ID");
                    String fullName = userInfo.getString("FirstName") + " " + userInfo.getString("LastName")  + " - "+ id;
                    userMap.put(id, fullName);
                }
                    if (userMap.containsKey(number)) {
                        String user = userMap.get(number);
                        messageOutput.writeUTF("Starting private chat with user " + user + ", use 'exit' to leave.");
                        new PrivateMessageClass(user);
                    }

            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        } else {
            messageOutput.writeUTF("User " + number + " is not online.");
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     private class PrivateMessageClass{
        private String otherUser;
        private String fullId = userFullName;

        private PrivateMessageClass(String otherUser){
            this.otherUser = otherUser;
            run();
        }

         /**
          * Asks getUserOutput() method to check whether the otherUser can receive private messages or not.
          * If it can, starts the conversation and keep it on until 'exit' is used by the user.
          */
         private void run() {
            try{
                DataOutputStream dataOutput = getUserOutput(otherUser);
                if (dataOutput == null){
                    messageOutput.writeUTF("User " + otherUser + " is not available for private chat.");
                    return;
                }
                String privateMessage;
                while (!(privateMessage = input.readUTF()).equalsIgnoreCase("exit")) {
                    dataOutput.writeUTF("Private message from " + fullId + ": " + privateMessage);
                }
                messageOutput.writeUTF("Private chat with user " + otherUser + " ended.");
            } catch (IOException e) {
                System.out.println(fullId + " left :(");
            }
        }


         /**
          * This checks if the user is online and available for private chatting
          * @param userID
          * @return
          */
         private DataOutputStream getUserOutput(String userID) {
            ConnectedClient targetClient = clientMap.get(userID);
            if (targetClient != null) {
                return targetClient.messageOutput;
            }
            return null;
        }

    }

    /**
     * Will close the connection
     */
    public void close(){
        try {
            clientSocket.close();
            input.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

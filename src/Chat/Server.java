package Chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {

    private ServerSocket server;
    public static final int PORT = 3030;
    public static final String STOP_STRING = "bye";
    public static final String USERS = "users";
    public static final String ALL_USERS = "all users";
    public static final  String BOT = "BOT";

    /**
     * This starts the server and then forward to startConnection() method that accepts the clients.
     */
    public Server(){
        //This loop keeps my server alive even after a client terminates their connection.
        while (true) {
            try {
                server = new ServerSocket(PORT);
                while (true) {
                    try {
                        startConnection();
                    } catch (SocketException e) {
                        System.out.println("User disconnected");
                    }
                }
            } catch (Exception e) {
                System.out.println("Server encountered an error. ");
                System.out.println("Server is restarting...");
                }
        }
    }

    /**
     *Accepts the users
     */
    private void startConnection() throws IOException {
        Socket clientSocket = server.accept();
        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        /**
         * I am sending ID and userName from client class,  aboutUserDetails() method.
         * These values help me to manage the online users that are in server.
         */
        String ID = in.readUTF();
        String userName = in.readUTF();

        if(clientSocket.isConnected()) {
                /**
                 * The use of Thread helps the server to handle multiple clients
                 */
                new Thread(() -> {
                    try {
                    ConnectedClient client = new ConnectedClient(clientSocket, ID, userName);
                    out.writeUTF("The server welcomes you: " + userName + " - " +ID + " :)");
                    out.writeUTF("Type 'BOT' to chat with the bot");
                    client.readMessages();
                    client.close();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
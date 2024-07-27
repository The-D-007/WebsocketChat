package Chat;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;


public class Client {
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream messageInput;
    private Scanner input;
    private Scanner scanner;

    /**
     * To connect new client
     */
    public Client() {
        try {
            socket = new Socket("localhost", Server.PORT);
            output = new DataOutputStream(socket.getOutputStream());
            messageInput = new DataInputStream(socket.getInputStream());
            input = new Scanner(System.in);
            scanner = new Scanner(System.in);

            Start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method gives user choice.
     */
    public void Start(){
        List<String> userDetails;
        while (true) {
            System.out.println("\n***Welcome***");
            System.out.println("1. Login");
            System.out.println("2. Sign Up");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int option = 0;
            try {
                option = scanner.nextInt();
                scanner.nextLine();
                if (option == 1) {
                    userDetails = new Login().loginIn();
                    aboutUserDetails(userDetails);
                    break;

                } else if (option == 2) {
                    List<String> data= new SignUp().signUp();

                    //This is an automatic method where after sign up user will log in automatically.
                    userDetails = new Login().loginIn(data);
                    aboutUserDetails(userDetails);
                } else {
                    System.out.println("Bye");
                    break;
                }

            } catch (InputMismatchException e) {
                System.out.println("Please put integers.");
                scanner.next();
            }
        }
    }

    /**
     *This method confirm if the user can chat or not.
     * @param userDetails
     */
    private void aboutUserDetails(List<String> userDetails){
        if (!userDetails.isEmpty()) {
            String ID = userDetails.get(0);
            String name = userDetails.get(1);
            String fullName = name + " - " + ID;
            try {
                output.writeUTF(ID);
                output.writeUTF(name);
                System.out.println(messageInput.readUTF());
                System.out.println(messageInput.readUTF());

                //Starts new thread for every new client
                new Thread(() -> readMessages()).start();
                writeMessage(fullName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("Please provide right id and password or Sign up.");
        }
    }

    /**
     * Writes the user messages on the server, we can say group chat.
     * @throws IOException
     */
    private void writeMessage(String fullName)  {
        String line = "";
        try {
            while (!line.equals(Server.STOP_STRING)) {
                line = input.nextLine();
                if (socket != null && !socket.isClosed()) {
                    output.writeUTF(line);
                } else {
                    System.out.println("Socket is closed. Unable to send message.");
                    break;
                }
            }
            System.out.println(fullName + " disconnected from the server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This reads messages that are send privately to the user
     */
    public void readMessages() {
        try {
            while (true) {
                String message = messageInput.readUTF();
                    System.out.println(message);
            }
        } catch (EOFException e) {
            System.out.println("Chat again :)");
        } catch (SocketException e) {
            System.out.println("Please login again");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Closes the connection
     * @throws IOException
     */
    private void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (output != null) {
            output.close();
        }
        if (input != null) {
            input.close();
        }
        if (messageInput != null) {
            messageInput.close();
        }
    }


    public static void main(String[] args) {
        new Client();
    }
}
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server {

    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);
        System.out.print("Enter the port on which to listen: ");
        int port = -1;
        do {
            String maybePort = keyboard.nextLine();
            try {
                port = Integer.parseInt(maybePort);
            }
            catch (Exception e) {
                System.out.println("Invalid port. Try again.");
            }
        } while (port < 0);

        try (ServerSocket serverSock = new ServerSocket(port)) {
            while (true) {
                Socket newClient = serverSock.accept();

                try {
                    System.out.println("Starting connection to new client: " + newClient);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(newClient.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(newClient.getOutputStream()));

                    System.out.println("Creating new thread for client: " + newClient);

                    ServerThread newThread = new ServerThread(reader, writer, newClient);
                    newThread.start();
                }
                catch (Exception ex) {
                    System.out.println("Error creating new thread for client: " + ex.getMessage())
                }
            }
        }
        catch (Exception ex) {
            System.out.println("There was an error while running the server: " + ex.getMessage());
        }
    }



}

class ServerThread extends Thread {
    private Color[][] board = new Color[8][8];
    private final Color serverColor = Color.AI;
    private BufferedReader reader;
    private BufferedWriter writer;
    private Socket clientSocket;

    public ServerThread(BufferedReader reader, BufferedWriter writer, Socket clientSocket) {
        this.reader = reader;
        this.writer = writer;
        this.clientSocket = clientSocket;
    }

    public void run() {}

    private void get_player_move() {

    }

    private int[][] check_updated_coordinates() {
        return new int[][] {};
    }

    private void send_client_updated_moves() {

    }

    private void make_server_move() {
    }

    private int[][] get_server_updated_coords() {
        return new int[][] {};
    }

    private void send_update_to_client() {

    }
}
import java.io.*;
import java.net.*;
import java.util.ArrayList;
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
    private final Color aiColor = Color.AI;
    private OthelloPlayer aiPlayer;
    private BufferedReader reader;
    private BufferedWriter writer;
    private Socket clientSocket;

    public ServerThread(BufferedReader reader, BufferedWriter writer, Socket clientSocket) {
        this.reader = reader;
        this.writer = writer;
        this.clientSocket = clientSocket;
        this.aiPlayer = new OthelloPlayer(aiColor);
    }

    public void run() {
        int[] lastMove = null;
        int[] playerCoord = getPlayerMove();

        // Check if last move was pass and player move is pass then game is over

        ArrayList<int[]> playerUpdatedCoords = OthelloPlayer.place(aiPlayer.getBoard(), playerCoord, aiColor);
        aiPlayer.updateBoard(playerUpdatedCoords);

        // Send playerUpdatedCoords to player
    }

    //Client should send data as "coord, coord"
    private int[] getPlayerMove() {
        int[] playerMove = null;

        do
        {
            try {
                String str_player_move = reader.readLine();

                // Split the data sent from the client by ','
                String[] coordNums = str_player_move.split(",");

                playerMove = new int[2];
                playerMove[0] = Integer.parseInt(coordNums[0]);
                playerMove[1] = Integer.parseInt(coordNums[1]);
            }
            catch (Exception ex) {

            }
        } while (playerMove == null);

        return playerMove;
    }

    private int[][] checkUpdatedCoordinates() {
        return new int[][] {};
    }

    private void sendClientUpdatedMoves() {

    }

    private void makeServerMove() {
    }

    private int[][] getServerUpdatedCoords() {
        return new int[][] {};
    }

    private void sendUpdateToClient() {

    }
}
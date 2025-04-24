import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
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
                    System.out.println("Error creating new thread for client: " + ex.getMessage());
                }
            }
        }
        catch (Exception ex) {
            System.out.println("There was an error while running the server: " + ex.getMessage());
        }
    }
}

class ServerThread extends Thread {
    private static final int sleepTimeAfterSendUpdate = 5;

    private static final int[] noValidMove = new int[] {-1, 0};
    private static final int[] playerLoses = new int[] {-1, -1};
    private static final int[] playerWins = new int [] {-1, -2};

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
        int[] playerCoord = null;

        do {
            playerCoord = getPlayerMove();

            // Check if last move was pass and player move is pass then game is over
            if (Arrays.equals(lastMove, noValidMove) && Arrays.equals(playerCoord, noValidMove)) {
                // end game logic
                endGame();
                break;
            }

            ArrayList<int[]> playerUpdatedCoords = OthelloPlayer.place(aiPlayer.getBoard(), playerCoord, Color.PLAYER);
            aiPlayer.updateBoard(playerUpdatedCoords);

            // Send playerUpdatedCoords to player
            sendClientUpdatedCoords(playerUpdatedCoords);

            try {
                Thread.sleep(sleepTimeAfterSendUpdate);
            }
            catch (InterruptedException ex) {
                System.out.println("Problem sleeping to let the user look at what coordinates are updated: " + ex.getMessage() + ".\nContinuing execution.");
            }

            // AI Logic to get the best move and call it on that
            int[]  serverMove = makeServerMove();
            lastMove = serverMove;

            if (Arrays.equals(serverMove, noValidMove) && Arrays.equals(playerCoord, noValidMove)) {
                endGame();
                break;
            }

            ArrayList<int[]> serverUpdatedCoords = OthelloPlayer.place(aiPlayer.getBoard(), serverMove, Color.AI);

            sendServerUpdatedCoords(serverMove, serverUpdatedCoords);
        } while (true); //keep playing until someone loses or wins
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


    private void sendClientUpdatedCoords(ArrayList<int[]> coordsChanged) {
        String coordsToSend = "";

        for (int coordIndex = 0; coordIndex < coordsChanged.size(); coordIndex++) {
            int[] currentCoordinate = coordsChanged.get(coordIndex);

            // coordsToSend will look like "2,3,1|5,8,0|0,1,2"
            // You can get the coords using String.split("|") and then individual numbers in the coord with String.split(",")
            coordsToSend += currentCoordinate[0] + "," + currentCoordinate[1] + "," + currentCoordinate[2] + "|";
        }
        coordsToSend = coordsToSend.substring(0, coordsToSend.length() - 1 - 1); //remove final "|"

        boolean successfullySent = true;

        do {
            try {
                writer.write(coordsToSend);
                writer.flush();
                successfullySent = true;
            } catch (IOException ex) {
                successfullySent = false;
            }
        } while (!successfullySent);

    }

    // AI logic
    // Return coord to play
    private int[] makeServerMove() {
        return new int[] {};
    }


    private void sendServerUpdatedCoords(int[] serverMove, ArrayList<int[]> coordsChanged) {

    }

    // Needs to send who wins or loses to the client and then end the connection
    private void endGame() {
        Color winner = OthelloPlayer.winner(aiPlayer.getBoard());
        // Need to meet on if the server sends the winner to the player or if both get it independently
        // Server will send the coord either {-1, -1} or {-1, -2} to signal who wins

    }
}
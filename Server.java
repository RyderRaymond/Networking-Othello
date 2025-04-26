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

        System.out.println("Listening for new connections");
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
    private static final int[] playerWins = new int[] {-1, -2};
    private static final int[] tie = new int[] {-1, -3};


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
            sendClientUpdatedCoords(playerCoord, playerUpdatedCoords);

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

        boolean readerIsClosed = false;
        boolean writerIsClosed = false;

        do {
            try {
                System.out.println("Closing socket for client: " + clientSocket);
                reader.close();
                readerIsClosed = true;
                writer.close();
                writerIsClosed = true;
                clientSocket.close();
            }
            catch (IOException ex) {
                System.out.println("Error closing reader, writer, or socket: " + ex.getMessage());
            }
        } while (!readerIsClosed && !writerIsClosed && !clientSocket.isClosed());
    }

    //Client should send data as "coord, coord"
    //Example: "2,5"
    private int[] getPlayerMove() {
        int[] playerMove = null;

        do //keep trying to read in case we get an IO error and the move isn't correctly received
        {
            try {
                String str_player_move = reader.readLine();

                // Split the data sent from the client by ','
                // So if the reader reads "4,6" it parses to an array index 0 = 4, index 1 = 6
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

    // Sends the coordinates that the client's move updated
    // See parseListOfCoordinates to see what the string looks like

    // Basically, every time the client tries to read from the server the coordinates the Player's last move updated,
    // and whenver the client is trying to read the server's updated coordinates, it needs to read a single coordinate as one line,
    // and then determine if that is a valid coordinate or if it is a notification that someone has won.
    // If someone won, display who won
    // If no one won and this is a real coordinate, display the coordinates changed
    private void sendClientUpdatedCoords(int[] playerMoveAgain, ArrayList<int[]> coordsChanged) {
        String playerLastMove = "" + playerMoveAgain[0] + "," + playerMoveAgain[1];

        String coordsToSend = parseListOfCoordinates(coordsChanged);

        boolean successfullySent = true;

        do {
            try {
                writer.write(playerLastMove);
                writer.newLine();
                writer.write(coordsToSend);
                writer.newLine();
                writer.flush();
                successfullySent = true;
            } catch (IOException ex) {
                successfullySent = false;
            }
        } while (!successfullySent);
    }

    // When the client reads the server's updated coordinates,
    private void sendServerUpdatedCoords(int[] serverMove, ArrayList<int[]> coordsChanged) {
        String serverCoord = "" + serverMove[0] + ',' + serverMove[1];
        String coordsToSend = parseListOfCoordinates(coordsChanged);

        boolean successfullySent = true;
        do {
            try {
                writer.write(serverCoord);
                writer.newLine();
                writer.write(coordsToSend);
                writer.newLine();
                writer.flush();
            }
            catch (IOException ex) {
                successfullySent = false;
            }
        } while (!successfullySent);
    }

    // Takes in the ArrayList of changed coordinates on the board and creates a string that will be sent to the client
    // The string will look like "2,3,1|5,8,0|" for coordinate (2, 3) being changed to Color.AI and (5, 8) being set to Empty
    private String parseListOfCoordinates(ArrayList<int[]> coordsChanged) {
        String coordsToSend = "";

        for (int coordIndex = 0; coordIndex < coordsChanged.size(); coordIndex++) {
            int[] currentCoordinate = coordsChanged.get(coordIndex);

            // coordsToSend will look like "2,3,1|5,8,0|0,1,2"
            // You can get the coords using String.split("|") and then individual numbers in the coord with String.split(",")
            coordsToSend += currentCoordinate[0] + "," + currentCoordinate[1] + "," + currentCoordinate[2] + "|";
        }
        coordsToSend = coordsToSend.substring(0, coordsToSend.length() - 1); //remove final "|"

        return coordsToSend;
    }

    // AI logic
    // Return coord to play
    private int[] makeServerMove() {
      ArrayList<int[]> moves = OthelloPlayer.getMoves(aiPlayer.getBoard(), aiColor);
      if (moves.get(0)[0] == -1) return new int[]{-1, 0}; // No valid moves available
      int bestMove = -1;
      int bestWeight = 0;
      int[] weights = new int[moves.size()];
      for (int i = 0; i < moves.size(); i++) {
          int[] move = moves.get(i);
          Color[][] nextBoard = OthelloPlayer.placeOnBoard(aiPlayer.getBoard(), move, aiColor);

          int weight = OthelloPlayer.count(nextBoard, aiColor);
          if (OthelloPlayer.isCorner(move)) weight += 20;
          if (OthelloPlayer.isEdge(move)) weight += 5;
          if (OthelloPlayer.isNextToCorner(move)) weight -= 15;

          weight -= OthelloPlayer.bestOutcome(nextBoard, Color.PLAYER);

          weights[i] = weight;
          if (weight > bestWeight) {
              bestWeight = weight;
              bestMove = i;
          }
      }
      return moves.get(bestMove);
    }

    // Needs to send who wins or loses to the client and then end the connection
    private void endGame() {
        Color winner = OthelloPlayer.winner(aiPlayer.getBoard());

        int[] winnerCoord = null;
        winnerCoord = switch (winner) {
            case Color.AI -> playerLoses;
            case Color.PLAYER -> playerWins;
            case Color.EMPTY -> tie;
        };

        //Send the coordinate as "-1,0" for example
        String stringToWrite = "" + winnerCoord[0] + "," + winnerCoord[1];

        boolean sentWinner = false;

        do {
            try {
                writer.write(stringToWrite);
                writer.newLine();
                writer.flush();
                sentWinner = true;
            }
            catch (IOException ex) {
                sentWinner = false;
            }
        } while (!sentWinner);
    }
}

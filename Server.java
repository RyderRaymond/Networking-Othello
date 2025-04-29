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
    private static final int sleepTimeAfterSendUpdate = 3 * 1000; //5 seconds (5 * 1000 milliseconds)

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

        try {
            do {
                //Get player Move -----------------------------------
                playerCoord = getPlayerMove();

                // Process Player Last Move -------------------------------------------
                // Check if last move was pass and player move is pass then game is over
                if (Arrays.equals(playerCoord, noValidMove) && Arrays.equals(lastMove, noValidMove)) {
                    // end game logic
                    endGame();
                    break;
                }
                else if (Arrays.equals(playerCoord, noValidMove)) {
                    sendCoordOrNotification(noValidMove);
                }
                else {
                    ArrayList<int[]> playerUpdatedCoords = new ArrayList<>();
                    playerUpdatedCoords = OthelloPlayer.place(aiPlayer.getBoard(), playerCoord, Color.PLAYER);
                    aiPlayer.updateBoard(playerUpdatedCoords);
                    sendCoordOrNotification(playerCoord);
                    sendClientUpdatedCoords(playerUpdatedCoords);
                }

                OthelloPlayer.printBoard(aiPlayer.getBoard());

                // Sleep to allow the user to see how their board has updated
                try {
                    Thread.sleep(sleepTimeAfterSendUpdate);
                } catch (InterruptedException ex) {
                    System.out.println("Problem sleeping to let the user look at what coordinates are updated: " + ex.getMessage() + ".\nContinuing execution.");
                }

                // AI Logic to get the best move and call it on that
//                int[] serverMove = makeBadServerMove(); // Easy AI
                int[] serverMove = makeServerMove(); // Hard AI
                System.out.println("Server move: " + serverMove[0] + "," + serverMove[1]);
                lastMove = serverMove;


                if (Arrays.equals(serverMove, noValidMove) && Arrays.equals(playerCoord, noValidMove)) {
                    endGame();
                    break;
                }
                else if (Arrays.equals(serverMove, noValidMove))
                {
                    sendCoordOrNotification(noValidMove);
                }
                else {
                    ArrayList<int[]> serverUpdatedCoords = OthelloPlayer.place(aiPlayer.getBoard(), serverMove, Color.AI);
                    aiPlayer.updateBoard(serverUpdatedCoords);
                    sendCoordOrNotification(serverMove);
                    sendServerUpdatedCoords(serverUpdatedCoords);
                }
            } while (true); //keep playing until someone loses or wins
        }
        catch (Exception ex)
        {
            System.out.println("There was an error while running the server: " + ex.getMessage());
        }

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
    private int[] getPlayerMove() throws IOException
    {
        int[] playerMove = null;

        String str_player_move = reader.readLine();
        System.out.println("String: " + str_player_move);

        // Split the data sent from the client by ','
        // So if the reader reads "4,6" it parses to an array index 0 = 4, index 1 = 6
        String[] coordNums = str_player_move.split(",");

        playerMove = new int[2];
        playerMove[0] = Integer.parseInt(coordNums[0]);
        playerMove[1] = Integer.parseInt(coordNums[1]);


        System.out.println("Received coordinate from client: (" + playerMove[0] + "," + playerMove[1] + ")" );
        return playerMove;
    }

    // Sends the coordinates that the client's move updated
    // See parseListOfCoordinates to see what the string looks like

    // Basically, every time the client tries to read from the server the coordinates the Player's last move updated,
    // and whenever the client is trying to read the server's updated coordinates, it needs to read a single coordinate as one line,
    // and then determine if that is a valid coordinate or if it is a notification that someone has won.
    // If someone won, display who won
    // If no one won and this is a real coordinate, display the coordinates changed
    private void sendClientUpdatedCoords(ArrayList<int[]> coordsChanged) throws IOException {
        String coordsToSend = parseListOfCoordinates(coordsChanged);

        writer.write(coordsToSend);
        writer.newLine();
        writer.flush();
    }

    private void sendCoordOrNotification(int[] coord) throws IOException
    {
        String coordToSend = "" + coord[0] + "," + coord[1];
        writer.write(coordToSend);
        writer.newLine();
        writer.flush();
    }

    // When the client reads the server's updated coordinates,
    private void sendServerUpdatedCoords(ArrayList<int[]> coordsChanged) throws IOException
    {
        String coordsToSend = parseListOfCoordinates(coordsChanged);

        writer.write(coordsToSend);
        writer.newLine();
        writer.flush();
    }

    // Takes in the ArrayList of changed coordinates on the board and creates a string that will be sent to the client
    // The string will look like "2,3,1|5,8,0|" for coordinate (2, 3) being changed to Color.AI and (5, 8) being set to Empty
    private String parseListOfCoordinates(ArrayList<int[]> coordsChanged) {
        String coordsToSend = "";

        for (int[] currentCoordinate : coordsChanged) {
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
      int bestMove = 0;
      int bestWeight = 0;
      int[] weights = new int[moves.size()];
      for (int i = 0; i < moves.size(); i++) {
          int[] move = moves.get(i);
          Color[][] nextBoard = OthelloPlayer.placeOnBoard(OthelloPlayer.deepCopyBoard(aiPlayer.getBoard()), move, aiColor);

          int weight = OthelloPlayer.count(nextBoard, aiColor);
          if (OthelloPlayer.isCorner(move)) weight += 30;
          if (OthelloPlayer.isEdge(move)) weight += 5;
          if (OthelloPlayer.isNextToCorner(move)) weight -= 15;
          //System.out.println("before bestOutcome: ");
          //OthelloPlayer.printBoard(aiPlayer.getBoard());
          weight -= OthelloPlayer.bestOutcome(nextBoard, Color.PLAYER);
          //System.out.println("after bestOutcome: ");
          //OthelloPlayer.printBoard(aiPlayer.getBoard());
          weights[i] = weight;
          if (weight > bestWeight) {
              bestWeight = weight;
              bestMove = i;
          }
      }
      return moves.get(bestMove);
    }

    private int[] makeBadServerMove() { // This is the bad AI
      ArrayList<int[]> moves = OthelloPlayer.getMoves(aiPlayer.getBoard(), aiColor);
      if (moves.get(0)[0] == -1) return new int[]{-1, 0}; // No valid moves available
      int bestMove = 0;
      int bestWeight = 0;
      int[] weights = new int[moves.size()];
      for (int i = 0; i < moves.size(); i++) {
          int[] move = moves.get(i);
          Color[][] nextBoard = OthelloPlayer.placeOnBoard(OthelloPlayer.deepCopyBoard(aiPlayer.getBoard()), move, aiColor);

          int weight = 0 - OthelloPlayer.count(nextBoard, aiColor);
          if (OthelloPlayer.isCorner(move)) weight -= 30;
          if (OthelloPlayer.isEdge(move)) weight -= 5;
          if (OthelloPlayer.isNextToCorner(move)) weight += 15;
          //System.out.println("before bestOutcome: ");
          //OthelloPlayer.printBoard(aiPlayer.getBoard());
          weight += OthelloPlayer.bestOutcome(nextBoard, Color.PLAYER);
          //System.out.println("after bestOutcome: ");
          //OthelloPlayer.printBoard(aiPlayer.getBoard());
          weights[i] = weight;
          if (weight > bestWeight) {
              bestWeight = weight;
              bestMove = i;
          }
      }
      return moves.get(bestMove);
    }

    // Needs to send who wins or loses to the client and then end the connection
    private void endGame() throws IOException
    {
        System.out.println("Ending game");
        Color winner = OthelloPlayer.winner(aiPlayer.getBoard());

        int[] winnerCoord = null;
        winnerCoord = switch (winner) {
            case AI -> playerLoses;
            case PLAYER -> playerWins;
            case EMPTY -> tie;
        };

        //Send the coordinate as "-1,0" for example
        String stringToWrite = winnerCoord[0] + "," + winnerCoord[1];
        System.out.println("Sending: " + stringToWrite);

        writer.write(stringToWrite);
        writer.newLine();
        writer.flush();
    }
}

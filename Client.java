import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private OthelloPlayer othelloPlayer;
    private UserInterface ui;

    private static final int[] noValidMove = new int[] {-1, 0};
    private static final int[] playerLoses = new int[] {-1, -1};
    private static final int[] playerWins = new int[] {-1, -2};
    private static final int[] tie = new int[] {-1, -3};

    public Client(Socket socket, BufferedWriter writer, BufferedReader reader)
    {
        this.socket = socket;
        this.writer = writer;
        this.reader = reader;
        othelloPlayer = new OthelloPlayer(Color.PLAYER);
        ui = new UserInterface(this);
    }

    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);
        try {
            String host = "";
            int port = -1;

            System.out.print("Enter the host to connect to: ");

            host = keyboard.nextLine();

            System.out.print("Enter the port the server is listening on: ");
            do {
                String maybePort = keyboard.nextLine();
                try {
                    port = Integer.parseInt(maybePort);
                }
                catch (Exception e) {
                    System.out.print("Invalid port.\nEnter a port: ");
                }
            } while (port < 0);

            Socket socket = new Socket(host, port);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Client player =new Client(socket, writer, reader);
            player.play();

        }
        catch (IOException ex) {
            System.out.println("Error connecting to host: " + ex.getMessage());
        }
    }

    public void play() {
        try {
            int[] lastMove = null;

            while (true) {

                Color color = Color.PLAYER; // Define the color variable
                ArrayList<int[]> moves = OthelloPlayer.getMoves(othelloPlayer.getBoard(), color);
//                System.out.println("Valid Moves");
//                for (int[] move : moves) {
//                    System.out.println(move[0] + ", " + move[1]);
//                }
                OthelloPlayer.printBoard(othelloPlayer.getBoard());
                if (OthelloPlayer.getMoves(othelloPlayer.getBoard(), color).get(0)[0] == -1) {
                    ui.changeServerMessage("No valid moves available. Waiting for opponent...");
                    sendPlayerMove(new int[]{-1, 0});
                }

                int[] notifOrClientCoord = receiveCoordinateOrNotification();

                if (checkGameOver(notifOrClientCoord)) {
                    break;
                }
                else if (Arrays.equals(notifOrClientCoord, noValidMove)) {

                }
                else {
                    ArrayList<int[]> clientUpdatedCoords = receiveClientUpdatedCoords();
                    othelloPlayer.updateBoard(clientUpdatedCoords);

                    int[][] updatedCoords = new int[clientUpdatedCoords.size()][clientUpdatedCoords.get(0).length];
                    for (int i = 0; i < clientUpdatedCoords.size(); i++) {
                        updatedCoords[i] = clientUpdatedCoords.get(i).clone();
                    }
                    ui.receiveServerMessage(updatedCoords);
                }


                int[] notifOrServerMove = receiveServerMove();

                if (checkGameOver(notifOrServerMove)) {
                    break;
                }
                else if (Arrays.equals(notifOrServerMove, noValidMove)) {
                    ui.receiveServerMessage(new int[][] { {-1, 0, 0}});
                }
                else {
                    ArrayList<int[]> serverUpdatedCoords = receiveServerUpdatedCoords();
                    othelloPlayer.updateBoard(serverUpdatedCoords);

                    int[][] serverUpdatedCoordsArray = new int[serverUpdatedCoords.size()][serverUpdatedCoords.get(0).length];
                    for (int i = 0; i < serverUpdatedCoords.size(); i++)
                    {
                        serverUpdatedCoordsArray[i] = serverUpdatedCoords.get(i).clone();
                    }

                    ui.receiveServerMessage(serverUpdatedCoordsArray);
                }


//            othelloPlayer.updateBoard(serverUpdatedCoords);
//            ui.receiveServerMessage((int[][]) serverUpdatedCoords.toArray());
            }
        }
        catch (IOException ex)
        {
            System.out.println("An error occurred during the connection: " + ex.getMessage());
        }
        finally {
            try {
                socket.close();
                writer.close();
                reader.close();
            }
            catch (IOException ex)
            {
                System.out.println("Error closing sockets: " + ex.getMessage());
            }
        }
    }

    private int[] receiveCoordinateOrNotification() throws IOException
    {
        String coordStr = reader.readLine();
        String[] split = coordStr.split(",");
        int[] finalCoord = new int[] {
                Integer.parseInt(split[0]),
                Integer.parseInt(split[1])
        };

        return finalCoord;
    }

    protected void sendPlayerMove(int[] playerMove)
    {
        try {
            String moveToSend = "" + playerMove[0] + "," + playerMove[1];
            System.out.println("Sending move: " + moveToSend);
            writer.write(moveToSend);
            writer.newLine();
            writer.flush();
        } catch (Exception e) {}
    }

    private ArrayList<int[]> receiveClientUpdatedCoords() throws IOException
    {
        String updatedCoords = reader.readLine();
        System.out.println("Updated coords from client move: " + updatedCoords);
        String[] coords = updatedCoords.split("\\|");
        for (String coord : coords) {
            System.out.println(coord);
        }

        ArrayList<int[]> coordinateList = new ArrayList<>();

        for (String coordstr : coords) {
            String[] nums = coordstr.split(",");

            int[] coord = new int[]{
                Integer.parseInt(nums[0]),
            Integer.parseInt(nums[1]),
            Integer.parseInt(nums[2]) };
            coordinateList.add(coord);
        }
        for (int[] item : coordinateList) {
            System.out.println(item[0] + " " + item[1] + " " + item[2]);
        }

        return coordinateList;
    }

    private int[] receiveServerMove() throws IOException
    {
        String move = reader.readLine();
        System.out.println("Server move: " + move);

        String[] serverCoordinate = move.split(",");
        int[] coord = new int[] {
                Integer.parseInt(serverCoordinate[0]),
                Integer.parseInt(serverCoordinate[1]),
        };

        return coord;
    }

    private ArrayList<int[]> receiveServerUpdatedCoords() throws IOException
    {
        String updatedCoords = reader.readLine();
        System.out.println("Updated coords from server move: " + updatedCoords);

        String[] coords = updatedCoords.split("\\|");
        for (String coord : coords) {
            System.out.println(coord);
        }

        ArrayList<int[]> coordinateList = new ArrayList<>();

        for (String coordstr : coords) {
            String[] nums = coordstr.split(",");

            int[] coord = new int[]{
                    Integer.parseInt(nums[0]),
                    Integer.parseInt(nums[1]),
                    Integer.parseInt(nums[2]),
            };
            coordinateList.add(coord);
        }

        return coordinateList;
    }

    private boolean checkGameOver(int[] coords)
    {
        if(coords[0] == -1){
          if(coords[1] == -1){
            ui.changeServerMessage("You lost!");
            endGame();
            return true;
          }
          else if(coords[1] == -2){
            ui.changeServerMessage("You won!");
            endGame();
            return true;
          }
          else if(coords[1] == -3){
            ui.changeServerMessage("Draw!");
            endGame();
            return true;
          }
        }
        return false;
    }

    private void endGame()
    {
        try {
            socket.close();
            writer.close();
            reader.close();
        }
        catch (IOException ex)
        {
            System.out.println("Error closing sockets: " + ex.getMessage());
        }
    }
}

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client {
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private OthelloPlayer othelloPlayer;
    private UserInterface ui;

    public Client(Socket socket, BufferedWriter writer, BufferedReader reader)
    {
        this.socket = socket;
        this.writer = writer;
        this.reader = reader;
        othelloPlayer = new OthelloPlayer(Color.PLAYER);
        ui = new UserInterface(this);
    }

    public static void main(String[] args) {
        try {
            String host = "localhost";
            int port = 9999;

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
            while (true) {

                int[] lastMove = null;

                ArrayList<int[]> clientUpdatedCoords = receiveClientUpdatedCoords();
                OthelloPlayer.updateBoard(othelloPlayer.getBoard(), clientUpdatedCoords);

                int[][] updatedCoords = new int[clientUpdatedCoords.size()][clientUpdatedCoords.get(0).length];

                for (int i = 0; i < clientUpdatedCoords.size(); i++)
                {
                    updatedCoords[i] = clientUpdatedCoords.get(i).clone();
                }

                ui.receiveServerMessage(updatedCoords);

                int[] serverMove = receiveServerMove();

                ArrayList<int[]> serverUpdatedCoords = receiveServerUpdatedcoords();

//            othelloPlayer.updateBoard(serverUpdatedCoords);
//            ui.receiveServerMessage((int[][]) serverUpdatedCoords.toArray());


                //While the game is running
                //Update board
                //Have user input move
                //Send move to server
                //if valid, accept, wait 5 seconds, and send server's move

                //if no more valid moves on either side
                //procedure to end game
                //print to client who won
                //close connection
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
        String lastMove = reader.readLine();
        System.out.println("Read last move: " + lastMove);

        String updatedCoords = reader.readLine();
        System.out.println("Updated coords: " + updatedCoords);
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

    private ArrayList<int[]> receiveServerUpdatedcoords() throws IOException
    {
        String updatedCoords = reader.readLine();
        System.out.println("Updated coords: " + updatedCoords);

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
}

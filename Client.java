import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Client {

    public static void main(String[] args) {
        try {
            String host = "localhost";
            int port = 9999;

            Socket socket = new Socket(host, port);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            ClientPlayer player =new ClientPlayer(socket, writer, reader);
            player.play();


            reader.readLine();


        }
        catch (IOException ex) {
            System.out.println("Error connecting to host: " + ex.getMessage());
        }
    }



}

class ClientPlayer {
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private OthelloPlayer othelloPlayer;
    private UserInterface ui;

    public ClientPlayer(Socket socket, BufferedWriter writer, BufferedReader reader)
    {
        this.socket = socket;
        this.writer = writer;
        this.reader = reader;
        othelloPlayer = new OthelloPlayer(Color.PLAYER);
        ui = new UserInterface(this);
    }

    public void play() {
        try {
            int[] lastMove = null;

            sendPlayerMove(new int[]{3, 5});
            int[][] clientUpdatedCoords = receiveClientUpdatedCoords();
            ui.receiveServerMessage(clientUpdatedCoords);

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
        catch (IOException ex)
        {
            System.out.println("An error occurred during the connection: " + ex.getMessage());
        }
    }

    {
        try {
            String moveToSend = "" + playerMove[0] + "," + playerMove[1];
            System.out.println("Sending move: " + moveToSend);
            writer.write(moveToSend);
            writer.newLine();
            writer.flush();
        } catch (Exception e) {}
    }

    private int[][] receiveClientUpdatedCoords() throws IOException
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
            Integer.parseInt(nums[2]),
};
            coordinateList.add(coord);
        }
        for (int[] item : coordinateList) {
            System.out.println(item[0] + " " + item[1] + " " + item[2]);
        }

        System.out.println(coordinateList.size());
        int[][] result = new int[coordinateList.size()][coordinateList.get(0).length];

        for (int i = 0; i < coordinateList.size(); i++) {
            System.out.println(i);
            System.out.println(coordinateList.get(i).length);
            result[i] = coordinateList.get(i);
        }

        othelloPlayer.updateBoard(coordinateList);

        OthelloPlayer.printBoard(othelloPlayer.getBoard());

        return result;
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

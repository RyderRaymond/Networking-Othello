import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        try{
            String host = "localhost";
            int port = 9999;

            Socket socket = new Socket(host, port);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            UserInterface ui = new UserInterface();

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

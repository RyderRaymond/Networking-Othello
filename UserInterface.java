/*
 * Class UserInterface
 * 
 * This class allows the player to interact with the othello game by displaying the board
 * and allowing for an input of coordinates for moves.
 */

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class UserInterface extends JFrame implements KeyListener {
    private int boardState[][]; // 2d array to hold the board state that will be displayed to the player
    private int cursor[]; // what you are selecting
    private boolean submitted; // blocker for submitting
    private boolean yMove; //blocker for turn
    private JLabel playerSelection; // displays coordinate selection
    private JLabel board; // place to output the board to
    private JLabel instructions; // tells controls
    private JLabel serverMessage; // displays win/loss/waiting
    private Checker chk; // checker
    private Client client; // the client for sending things


    // constructor
    public UserInterface(Client cliPlay) {
        client = cliPlay;

        boardState = new int[][] {
            {0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0},
            {0,0,0,1,2,0,0,0},
            {0,0,0,2,1,0,0,0},
            {0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0}};
        cursor = new int[] {0,0};
        chk = new Checker();
        submitted = false;
        yMove = true;

        // set up gui
        setTitle("Othello");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // prepare labels and frame
        instructions = new JLabel("Select with arrow keys. Confirm with Enter. You are \u25cb. ESC to exit.");
        playerSelection = new JLabel("Selected: {" + cursor[0] + "," + cursor[1] + "}");
        board = new JLabel("");
        serverMessage = new JLabel("Make Your Move...");

        // add labels
        add(instructions);
        instructions.setBounds(20, 20, 400, 15);

        add(playerSelection);
        playerSelection.setBounds(20, 50, 400, 15);

        add(board);
        board.setBounds(120, 70, 280, 200);

        add(serverMessage);
        serverMessage.setBounds(20, 250, 400, 15);

        // call board renderer
        renderBoard();

        // add key listener and display window
        addKeyListener(this);
        setLayout(null);
        setFocusable(true);
        setVisible(true);
    }

    /*
     * method to receive a list of coordinates or a message from the server/checker
     */
    public void receiveServerMessage(int[][] msg) {
        if (msg[0][0] >= -1){
            // remove blockers on sending
            if (!yMove) {
                if (submitted) {
                    submitted = false;
                    changeServerMessage("Awaiting Server move...");
                } else {
                    yMove = true;
                    changeServerMessage("Make Your Move...");
                }
            }
            if (msg[0][0] >= 0)
            // update board if there was a move made
            updateBoardState(msg);
        }
    }

    /*
     * method to submit the selection
     */
    public void submitCoordinates() {
        // do not submit if already submitted
        if (!submitted && yMove) {
            // call checker
            if (chk.checkValidity(cursor)) {
                // put blockers to prevent out-of-turn submission
                submitted = true;
                yMove = false;
                // notify 
                changeServerMessage("Submitting...");
                // send coordinates
                client.sendPlayerMove(cursor);

            } else {
                changeServerMessage("Invalid Move...Try Again");
            }
        }
    }

    /*
     * method to update the board state based on a list of coordinates
     */
    private void updateBoardState(int[][] uCoords) {
        for(int x,y,i = 0; i < uCoords.length; i++) {
            x = uCoords[i][0];
            y = uCoords[i][1];
            boardState[x][y] = uCoords[i][2];
        }
        chk.updateBoard(uCoords);
        renderBoard();
    }

    /*
     * render the board
     */
    private void renderBoard() {
        String tBoardOut = "<html><pre>";

        // generate render
        for (int y = 0; y < boardState.length; y++) {
            for (int x = 0; x < boardState[y].length ; x++) {
                // get token to use
                String t = "";
                switch (boardState[y][x]) {
                    case 0:
                        t = "\u25fd";
                        break;
                    case 1:
                        t = "\u25cb";
                        break;
                    case 2:
                        t = "\u25cf";
                        break;
                }

                if (x == cursor[0] && y == cursor[1]) // add selection indicator
                    tBoardOut += "{" + t + "}";
                else
                    tBoardOut += " " + t + " ";
            }
            tBoardOut += "<br>";
        }
        tBoardOut += "</pre></html>";

        // output
        board.setText(tBoardOut);
        playerSelection.setText("Selected: {" + cursor[0] + "," + cursor[1] + "}");
    }

    public void changeServerMessage(String txt) {
        serverMessage.setText(txt);
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            // up
            case 38:
            case 87:
                cursor[1] -= 1;
                if (cursor[1] == -1) // wrap upwards
                    cursor[1] = 7;
                break;
            // down
            case 40:
            case 83:
                cursor[1] = (cursor[1] + 1) % 8;
                break;
            // left
            case 37:
            case 65:
                cursor[0] -= 1;
                if (cursor[0] == -1) // wrap left
                    cursor[0] = 7;
                break;
            // right
            case 39:
            case 68:
                cursor[0] = (cursor[0] + 1) % 8;
                break;
            // enter
            case 10:
                submitCoordinates();
                break;

            // esc - THIS TERMINATES THE PROGRAM
            case 27:
                System.exit(0);
                break;

            default:
                break;
        }

        // render board
        renderBoard();
    }
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
}

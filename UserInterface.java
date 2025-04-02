/*
 * Class UserInterface
 * 
 * This class allows the player to interact with the othello game by displaying the board
 * and allowing for an input of coordinates for moves.
 */
public class UserInterface {
    private int boardState[][]; // 2d array to hold the board state that will be displayed to the player

    // constructor
    public UserInterface() {
    }

    /*
     * method to receive a list of coordinates or a message from the server/checker
     */
    public void receiveServerMessage(int[][] msg) {

    }

    /*
     * method to update the board based on a list of coordinates
     */
    private void updateBoardState(int[][] uCoords) {
        for(int x,y,i = 0; i < uCoords.length; i++) {
            x = uCoords[i][0];
            y = uCoords[i][1];
            boardState[x][y] = uCoords[i][3];
        }
    }
}

import java.util.ArrayList;
public class Checker { // clientside class that checks the player's moves
  public Color playerColor; // color of the player
  public Color[][] board; // board state
  public ArrayList<int[]> moves; // list of legal moves

  public static final Color EMPTY = Color.EMPTY; // Define EMPTY as a constant
  public static final Color PLAYER = Color.PLAYER; // Define PLAYER as a constant
  public static final Color AI = Color.AI; // Define AI as a constant

  Checker() {
    board = new Color[][] {
      {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
      {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
      {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
      {EMPTY, EMPTY, EMPTY, PLAYER, AI, EMPTY, EMPTY, EMPTY},
      {EMPTY, EMPTY, EMPTY, AI, PLAYER, EMPTY, EMPTY, EMPTY},
      {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
      {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
      {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY}};
    playerColor = PLAYER;
  }
  Checker(Color playerColor, Color[][] board) {
    this.playerColor = playerColor;
    this.board = board; //comment
  }

  public boolean checkValidity(int[] aCoord) {
    int[] coord = {aCoord[1], aCoord[0]};
    if (board[coord[0]][coord[1]] != EMPTY) return false; // Tile is already occupied
    Color opponentColor = AI;
    int[] placedTile = coord;
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        if (x == 0 && y == 0) continue; // Skip the current tile
        int i = placedTile[0] + x;
        int j = placedTile[1] + y;
        // Check if the move is within bounds and if there are opponent pieces in the direction of the move
        while (i >= 0 && i < 8 && j >= 0 && j < 8 && board[i][j] == opponentColor) {
          if (i+x < 0 || i+x >= 8 || j+y < 0 || j+y >= 8) break; // Out of bounds
          if (board[i+x][j+y] == playerColor) return true; // Valid move
          i += x;
          j += y;
        }
      }
    }
    return false; // Invalid move
  }

  public void validMoves() { //updates the list of legal moves
    ArrayList<int[]> validMoves = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if (board[i][j] == EMPTY) { // Check if the tile is empty
          int[] coord = {i, j};
          if (checkValidity(coord)) { // Check if the move is valid
            validMoves.add(coord);
          }
        }
      }
    }
    if(validMoves.isEmpty()) {
      validMoves.add(new int[]{-1, 0}); // No valid moves available
    }
    moves = validMoves; // Initialize moves array with the size of validMoves
  }

  public void setBoard(Color[][] board) { // updates the board state
    this.board = board;
  }

  public Color[][] getBoard() { // returns the board state
    return board;
  }

  public void updateBoard(int[][] changes) { // given the array of changed tiles, update the board
    for (int[] change: changes){
      int x = change[0];
      int y = change[1];
      Color color = Color.values()[change[2]];
      board[x][y] = color;
    }
  }
}

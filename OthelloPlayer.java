import java.util.ArrayList;

public class OthelloPlayer {
  public static final Color EMPTY = Color.EMPTY; // Define EMPTY as a constant
  public static final Color PLAYER = Color.PLAYER; // Define PLAYER as a constant
  public static final Color AI = Color.AI; // Define AI as a constant
  public Color color;
  public Color[][] board = new Color[][] {
    {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
    {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
    {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
    {EMPTY, EMPTY, EMPTY, PLAYER, AI, EMPTY, EMPTY, EMPTY},
    {EMPTY, EMPTY, EMPTY, AI, PLAYER, EMPTY, EMPTY, EMPTY},
    {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
    {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
    {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY}};
  // 1=white, 2=black, 0=empty
  public int numWhite = 2;
  public int numBlack = 2;

  OthelloPlayer(Color color) {
    this.color = color;
  }

  public Color getColor() {
    return color;
  }

  public Color[][] getBoard() {
    return board;
  }

  public int getNumWhite() {
    return numWhite;
  }

  public int getNumBlack() {
    return numBlack;
  }

  public static int[][] getMoves(Color[][] board, Color color) { // Returns all legal moves for the given color
    return new int[][]{{0, 0}}; // Placeholder for actual move logic
    // return {{-1, 0}}; // Placeholder for no moves available
  }

  public static int bestOutcome(Color[][] board, Color color) { // Returns the best outcome for the given color
    // Outcomes should equal the number of tiles flipped, plus ten if the corner is captured
    // call getMoves and calculate the outcome for each move
    // return the best outcome
    return 0; // Placeholder for actual outcome logic
  }

  public ArrayList<ArrayList<Integer>> place(int[] coord) { // Places a piece on the board at the given coordinates, assumes valid move
    ArrayList<ArrayList<Integer>> changes = new ArrayList<>();
    int[] placedTile = coord;
    ArrayList<Integer> change = new ArrayList<>();
    change.add(placedTile[0]);
    change.add(placedTile[1]);
    change.add(color.ordinal());
    changes.add(change);

    // Check all 8 directions for pieces to flip
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        if (x == 0 && y == 0) continue; // Skip the current tile
        int i = placedTile[0] + x;
        int j = placedTile[1] + y;
        ArrayList<Integer> flip = new ArrayList<>();
        while (i >= 0 && i < 8 && j >= 0 && j < 8) {
          if (board[i][j] == EMPTY) break; // No pieces to flip
          if (board[i][j] == color) {
            // Flip the pieces in the direction of the move
            for (int k = 1; k <= Math.abs(i - placedTile[0]); k++) { // here, i-placedTile[0] is the number of pieces to flip
              flip.add(placedTile[0] + k * x);
              flip.add(placedTile[1] + k * y);
              flip.add(color.ordinal());
              board[placedTile[0] + k * x][placedTile[1] + k * y] = color; // Flip the piece
            }
            changes.add(flip);
            break;
          }
          i += x;
          j += y;
        }
      }
    }

    return changes;
    // return a list of coordinates of the pieces changed {x, y, color}
  }

  public static boolean isValidMove(Color[][] aBoard, int[] coord, Color aColor) { // Checks if a move on a given board is valid
    if (aBoard[coord[0]][coord[1]] != EMPTY) return false; // Tile is already occupied
    Color opponentColor = (aColor == PLAYER) ? AI : PLAYER; // Determine the opponent's color
    int[] placedTile = coord;
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        if (x == 0 && y == 0) continue; // Skip the current tile
        int i = placedTile[0] + x;
        int j = placedTile[1] + y;
        // Check if the move is within bounds and if there are opponent pieces in the direction of the move
        while (i >= 0 && i < 8 && j >= 0 && j < 8 && aBoard[i][j] == opponentColor) {
          if (aBoard[i][j] == EMPTY) break; // No pieces to flip
          if (aBoard[i][j] == aColor) return true; // Valid move
          i += x;
          j += y;
        }
      }
    }
    return false; // Invalid move
  }

  public static boolean isCorner(int[] coord) { // Checks if the move is in a corner
    return false; // Placeholder for actual corner check logic
  }

  public static boolean isEdge(int[] coord) { // Checks if the move is on an edge
    return false; // Placeholder for actual edge check logic
  }

  public static boolean isNextToCorner(int[] coord) { // Checks if the move is next to a corner
    return false; // Placeholder for actual next to corner check logic
  }

  public static Color winner(Color[][] board) { // Checks if there is a winner
    return Color.EMPTY; // Placeholder for actual winner check logic
  }


}

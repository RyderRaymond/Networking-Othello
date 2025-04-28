import java.util.ArrayList;

public class OthelloPlayer {
  public static final Color EMPTY = Color.EMPTY;
  public static final Color PLAYER = Color.PLAYER;
  public static final Color AI = Color.AI;
  public Color color;
  public Color[][] myBoard = new Color[][] {
    {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
    {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
    {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
    {EMPTY, EMPTY, EMPTY, PLAYER, AI, EMPTY, EMPTY, EMPTY},
    {EMPTY, EMPTY, EMPTY, AI, PLAYER, EMPTY, EMPTY, EMPTY},
    {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
    {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
    {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY}};

  OthelloPlayer(Color color) {
    this.color = color;
  }

  public Color getColor() {
    return color;
  }

  public Color[][] getBoard() {
    return myBoard;
  } //comment

  public static Color[][] deepCopyBoard(Color[][] board) {
    Color[][] copy = new Color[board.length][board[0].length];
    for (int i = 0; i < board.length; i++) {
        System.arraycopy(board[i], 0, copy[i], 0, board[i].length);
    }
    return copy;
  }

  public static int count(Color[][] board, Color color) { // Counts the number of pieces of the given color on the board
    int count = 0;
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if (board[i][j] == color) {
          count++;
        }
      }
    }
    return count;
  }

  public static ArrayList<int[]> getMoves(Color[][] board, Color color) { // Returns all legal moves for the given color
    ArrayList<int[]> validMoves = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if (board[i][j] == EMPTY) { // Check if the tile is empty
          int[] coord = {i, j};
          if (isValidMove(board, coord, color)) { // Check if the move is valid
            int[] move = {j, i}; // Store the move in (x, y) format
            validMoves.add(move); // Add the move to the list
          }
        }
      }
    }
    if(validMoves.isEmpty()) {
      validMoves.add(new int[]{-1, 0}); // No valid moves available
    }
    return validMoves;
  }

  public static int bestOutcome(Color[][] aBoard, Color color) { // Returns the best outcome for the given color
    Color[][] board = deepCopyBoard(aBoard);
    ArrayList<int[]> moves = getMoves(board, color);
    int bestOutcome = 0;
    if (moves.get(0)[0] == -1) return 0; // No valid moves available
    for (int[] move : moves) {
      int outcome = 0;
      if(isCorner(move)) outcome += 20; // Corner captured
      if(isEdge(move)) outcome += 5; // Edge captured
      if(isNextToCorner(move)) outcome -= 15; // Next to corner
      Color[][] newBoard = deepCopyBoard(board);
      ArrayList<int[]> changes = place(newBoard, move, color); // get changed tiles
      newBoard = updateBoard(newBoard, changes); // update the board
      outcome += count(newBoard, color) - count(newBoard, color == PLAYER ? AI : PLAYER);
      if (outcome > bestOutcome) bestOutcome = outcome; // Update best outcome
      // Compare outcomes and return the best one
    }
    return bestOutcome;
  }

  public static ArrayList<int[]> place(Color[][] aBoard, int[] coord, Color color) { // returns the array of changed tiles
    Color[][] board = deepCopyBoard(aBoard);
    ArrayList<int[]> changes = new ArrayList<>();
    int[] placedTile = new int[2];
    placedTile[0] = coord[1];
    placedTile[1] = coord[0];
    int[] change = {placedTile[0], placedTile[1], color.ordinal()};
    changes.add(change);

    // Check all 8 directions for pieces to flip
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        if (x == 0 && y == 0) continue; // Skip the current tile
        int i = placedTile[0] + x;
        int j = placedTile[1] + y;
        int distance = 1;
        while (i >= 0 && i < 8 && j >= 0 && j < 8) {
          if (board[i][j] == EMPTY) break; // No pieces to flip
          if (board[i][j] == color) {
            // Flip the pieces in the direction of the move
            for (int k = 1; k < distance; k++) { // here, i-placedTile[0] is the number of pieces to flip
              int[] flip = new int[3];
              flip[0] = placedTile[0] + k * x;
              flip[1] = placedTile[1] + k * y;
              flip[2] = color.ordinal();
              changes.add(flip);
            }
            break;
          }
          i += x;
          j += y;
          distance++;
        }
      }
    }
    /*System.out.println("Changes: ");
    for (int[] change1 : changes) {
      System.out.println("x: " + change1[0] + ", y: " + change1[1] + ", color: " + Color.values()[change1[2]]);}
    */
    return changes;
    // return a list of coordinates of the pieces changed {x, y, color}
    // must call updateBoard(changes) to update the board
  }

  public static Color[][] placeOnBoard(Color[][] aBoard, int[] coord, Color color) { // returns a new board after the move
    Color[][] board = deepCopyBoard(aBoard);
    int[] placedTile = new int[2];
    placedTile[0] = coord[1];
    placedTile[1] = coord[0];
    Color[][] newBoard = board;
    newBoard[placedTile[0]][placedTile[1]] = color;

    // Check all 8 directions for pieces to flip
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        if (x == 0 && y == 0) continue; // Skip the current tile
        int i = placedTile[0] + x;
        int j = placedTile[1] + y;
        int distance = 1;
        while (i >= 0 && i < 8 && j >= 0 && j < 8) {
          if (board[i][j] == EMPTY) break; // No pieces to flip
          if (board[i][j] == color) {
            // Flip the pieces in the direction of the move
            for (int k = 1; k < distance; k++) { // here, i-placedTile[0] is the number of pieces to flip
              newBoard[placedTile[0] + k * x][placedTile[1] + k * y] = color;
            }
            break;
          }
          i += x;
          j += y;
          distance++;
        }
      }
    }

    return newBoard;
  }

  public void updateBoard(ArrayList<int[]> changes) { // given the array of changed tiles, update the board
    for (int[] change: changes){
      int x = change[0];
      int y = change[1];
      Color color = Color.values()[change[2]];
      myBoard[x][y] = color;
    }
  }

  public static Color[][] updateBoard(Color[][] aBoard, ArrayList<int[]> changes) { // given the array of changed tiles, update the board
    Color[][] board = deepCopyBoard(aBoard);
    for (int[] change: changes){
      int x = change[0];
      int y = change[1];
      Color color = Color.values()[change[2]];
      board[x][y] = color;
    }
    return board;
  }

  public static boolean isValidMove(Color[][] aBoard, int[] coord, Color aColor) { // Checks if a move on a given board is valid
    Color[][] board = deepCopyBoard(aBoard);
    if (board[coord[0]][coord[1]] != EMPTY) return false; // Tile is already occupied
    Color opponentColor = (aColor == PLAYER) ? AI : PLAYER; // Determine the opponent's color
    int[] placedTile = coord;
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        if (x == 0 && y == 0) continue; // Skip the current tile
        int i = placedTile[0] + x;
        int j = placedTile[1] + y;
        // Check if the move is within bounds and if there are opponent pieces in the direction of the move
        while (i >= 0 && i < 8 && j >= 0 && j < 8 && board[i][j] == opponentColor) {
          if (i+x < 0 || i+x >= 8 || j+y < 0 || j+y >= 8) break; // Out of bounds
          if (board[i+x][j+y] == aColor) return true; // Valid move
          i += x;
          j += y;
        }
      }
    }
    return false; // Invalid move
  }

  public static boolean isCorner(int[] coord) { // Checks if the move is in a corner
    if (coord[0] == 0 && coord[1] == 0) return true; // Top left corner
    if (coord[0] == 0 && coord[1] == 7) return true; // Top right corner
    if (coord[0] == 7 && coord[1] == 0) return true; // Bottom left corner
    if (coord[0] == 7 && coord[1] == 7) return true; // Bottom right corner
    return false;
  }

  public static boolean isEdge(int[] coord) { // Checks if the move is on an edge
    if (coord[0] == 0) return true; // Top edge
    if (coord[0] == 7) return true; // Bottom edge
    if (coord[1] == 0) return true; // Left edge
    if (coord[1] == 7) return true; // Right edge
    return false;
  }

  public static boolean isNextToCorner(int[] coord) { // Checks if the move is next to a corner
    if (coord[0] == 0){
      if (coord[1] == 1 || coord[1] == 6) return true;
    } else if (coord[0] == 7){
      if (coord[1] == 1 || coord[1] == 6) return true;
    } else if (coord[0] == 1){
      if (coord[1] == 0 || coord[0] == 1 || coord[1] == 6 || coord[1] == 7) return true;
    } else if (coord[0] == 6){
      if (coord[1] == 0 || coord[0] == 1 || coord[1] == 6 || coord[1] == 7) return true;
    }
    return false;
  }

  public static Color winner(Color[][] board) { // Checks who the winner is
    int totalPlayer = 0;
    int totalAI = 0;
    for (int i = 0; i < board.length; i++){
        for (int j = 0; j < board[i].length; j++){
            Color current = board[i][j];
            if (current == Color.EMPTY){
                continue;
                }
            else if (current == Color.PLAYER){
                totalPlayer++;
                }
            else if (current == Color.AI){
                totalAI++;
                }
            }
        }
        if (totalPlayer > totalAI){
            return Color.PLAYER; //Player wins
            }
        else if(totalAI > totalPlayer){
            return Color.AI; //AI wins
            }
        else{
            return Color.EMPTY; //tie between both players
            }
  }

  // Helper method to print the board
  public static void printBoard(Color[][] board) {
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[i].length; j++) {
        if (board[i][j] == Color.EMPTY) {
          System.out.print(".\t");
        } else if (board[i][j] == Color.PLAYER) {
          System.out.print("P\t");
        } else if (board[i][j] == Color.AI) {
          System.out.print("A\t");
        }
      }
      System.out.println();
    }
  }

  public static void printChanges(ArrayList<int[]> changes) {
    for (int[] change : changes) {
      System.out.print("x: " + change[0] + ", y: " + change[1] + ", color: " + Color.values()[change[2]] + "\n");
    }
    System.out.println();
  }

  public static void main(String[] args) {
    // Initialize the board
    Color[][] board = new Color[][] {
        {Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY},
        {Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY},
        {Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.PLAYER, Color.EMPTY, Color.EMPTY, Color.EMPTY},
        {Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.PLAYER, Color.PLAYER, Color.EMPTY, Color.EMPTY, Color.EMPTY},
        {Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.AI, Color.PLAYER, Color.EMPTY, Color.EMPTY, Color.EMPTY},
        {Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY},
        {Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY},
        {Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY, Color.EMPTY}
    };

    // Print the initial board
    System.out.println("Initial Board:");
    printBoard(board);
    ArrayList<int[]> moves = getMoves(board, AI);
    System.out.println("Valid Moves:");
    for (int[] move : moves) {
      System.out.print("(" + move[0] + ", " + move[1] + ") ");
    }
    System.out.println();

    /*for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        boolean isValid = OthelloPlayer.isValidMove(board, new int[]{i, j}, Color.PLAYER);
        if (isValid) {
          System.out.print("X\t"); // Valid move
        } else {
          System.out.print(".\t"); // Invalid move
        }
      }
      System.out.println();
    }
    */
    // Make a move
    int[] move = {3, 2}; // Example move
    ArrayList<int[]> changes = OthelloPlayer.place(board, move, Color.AI); // Get changed tiles
    System.out.println("Changes:");
    OthelloPlayer.printChanges(changes); // Print the changes
    Color[][] updatedBoard = OthelloPlayer.updateBoard(board, changes); // Update the board using changes
    //Color[][] updatedBoard = OthelloPlayer.placeOnBoard(board, move, Color.AI); // Update the board using the move

    // Print the updated board
    //System.out.println("\nUpdated Board:");
    printBoard(updatedBoard);
  }

}

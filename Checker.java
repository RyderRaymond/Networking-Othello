public class Checker { // clientside class that checks the player's moves
  public Color playerColor; // color of the player
  public Color[][] board; // board state
  public int[][] moves; // list of legal moves

  public Checker(Color playerColor, Color[][] board) {
    this.playerColor = playerColor;
    this.board = board;
  }

  public boolean checkValidity(int[] coord) {
    return false; // placeholder for validation logic
  }

  public void validMoves() { //updates the list of legal moves
  }

  public void setBoard(Color[][] board) { // updates the board state
    this.board = board;
  }

  public Color[][] getBoard() { // returns the board state
    return board;
  }

  public void updateBoard(int[][] changes) { // given the array of changed tiles, update the board

  }
}

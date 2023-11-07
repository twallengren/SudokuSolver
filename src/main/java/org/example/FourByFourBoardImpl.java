package org.example;

import java.util.Arrays;

public class FourByFourBoardImpl extends AbstractBoard {

  FourByFourBoardImpl() {
    super(4);
  }

  @Override
  public boolean isValidPermutation(int[] permutation, int rowNumber) {
    for (int index = 0; index < permutation.length; index++) {
      int value = permutation[index];
      if (index == value) {
        return false;
      }
      if (rowNumber % 2 == 0 && index < 2 && value < 2) {
        return false;
      }
      if (rowNumber % 2 == 0 && index > 1 && value > 1) {
        return false;
      }
    }
    Square[] squaresInRow = squares[rowNumber];
    for (int index = 0; index < size; index++) {
      Square squareToMove = squaresInRow[index];
      int newLocation = permutation[index];
      Square[] squaresInColumn = getSquaresInColumn(newLocation);
      if (Arrays.stream(squaresInColumn).anyMatch(sq -> sq == squareToMove)) {
        return false;
      }
    }
    return true;
  }
}

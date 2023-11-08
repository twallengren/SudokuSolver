package org.example;

public class FourByFourSudokuBoard extends AbstractBoard {

  FourByFourSudokuBoard() {
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
    return super.isValidPermutation(permutation, rowNumber);
  }
}

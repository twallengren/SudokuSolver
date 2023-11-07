package org.example;

import java.util.ArrayList;
import java.util.List;

public class AbstractBoard implements Board {

  int size;
  Square[][] squares;

  AbstractBoard(int size) {
    this.size = size;
    squares = getInitialSquares(size);
  }

  private static Square[][] getInitialSquares(int size) {
    Square[][] squares = new Square[size][size];
    for (int row = 0; row < size; row++) {
      for (int column = 0; column < size; column++) {
        squares[row][column] = new Square(Constants.ROW_NAME_MAP.get(row) + (column + 1));
      }
    }
    return squares;
  }

  @Override
  public void applyPermutationToRow(int rowNumber, Permutation permutation) {
    Square[] rowA = squares[rowNumber];
    Square[] permutedSquares = new Square[size];
    int[] indices = permutation.elements();
    for (int index = 0; index < size; index++) {
      int newLocation = indices[index];
      permutedSquares[newLocation] = rowA[index];
    }
    squares[rowNumber + 1] = permutedSquares;
  }

  @Override
  public List<Permutation> computePossiblePermutationsToNextRow(int rowNumber) {
    int[] array = new int[size];
    for (int index = 0; index < size; index++) {
      array[index] = index;
    }
    return Permutation.generatePermutations(
        size, array, new ArrayList<>(), (p) -> isValidPermutation(p, rowNumber));
  }

  @Override
  public boolean isValidPermutation(int[] permutation, int rowNumber) {
    return false;
  }

  @Override
  public void print() {
    for (int i = 0; i < size; i++) {
      if (size == 9 && i % 3 == 0 && i != 0) {
        System.out.println("------+-------+------");
      } else if (size == 4 && i % 2 == 0 && i != 0) {
        System.out.println("------+------");
      }
      for (int j = 0; j < size; j++) {
        if (size == 9 && j % 3 == 0 && j != 0) {
          System.out.print("| ");
        } else if (size == 4 && j % 2 == 0 && j != 0) {
          System.out.print("| ");
        }
        System.out.print(squares[i][j].name() + " ");
      }
      System.out.println();
    }
    System.out.println();
  }

  Square[] getSquaresInColumn(int colNumber) {
    Square[] column = new Square[size];
    for (int index = 0; index < size; index++) {
      column[index] = squares[index][colNumber];
    }
    return column;
  }
}

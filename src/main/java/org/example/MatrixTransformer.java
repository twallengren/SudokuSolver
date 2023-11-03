package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatrixTransformer {

  static String[][] computeMatrixTranspose(String[][] matrix) {
    String[][] transpose = new String[matrix.length][matrix.length];
    for (int row = 0; row < matrix.length; row++) {
      for (int col = 0; col < matrix.length; col++) {
        transpose[col][row] = matrix[row][col];
      }
    }
    return transpose;
  }

  static String[][] computeTransformationMatrix(
      String name, GridSquare[] squaresA, GridSquare[] squaresB) {

    // Initialize matrix with unknowns
    String[][] matrix = new String[SudokuBoard.SIZE][SudokuBoard.SIZE];
    for (int i = 0; i < SudokuBoard.SIZE; i++) {
      for (int j = 0; j < SudokuBoard.SIZE; j++) {
        matrix[i][j] = name + (i + 1) + (j + 1); // Adjusting index by +1 to match grid 1-9 notation
      }
    }

    for (int i = 0; i < SudokuBoard.SIZE; i++) {
      Optional<Integer> fromValue = squaresA[i].getValue();
      if (fromValue.isEmpty()) {
        // cannot say anything about possible transformations if we don't know the value
        continue;
      }
      int toIndex = indexOf(squaresB, fromValue.get());
      if (toIndex != -1) {
        // full mapping
        for (int j = 0; j < SudokuBoard.SIZE; j++) {
          // row should be zeros
          if (j == toIndex) {
            matrix[i][j] = "1";
          } else {
            matrix[i][j] = "0";
          }
          // corresponding column should be zeros
          if (j != i) {
            matrix[j][toIndex] = "0";
          }
        }
      }

      // If the value is missing in B
      if (toIndex == -1) {
        // iterate through grid squares and check for invalid values
        for (int j = 0; j < SudokuBoard.SIZE; j++) {
          if (squaresB[j].isInvalidValue(fromValue.get())) {
            matrix[i][j] = "0";
          }
        }
      }
    }

    // if the value in any particular row or column of the transformation matrix is the only
    // non-zero value in that row or column, then it is a 1. If we update to a 1, then we must
    // also update the row & column it is in to ensure the rest of the values are 0.
    for (int i = 0; i < SudokuBoard.SIZE; i++) {
      for (int j = 0; j < SudokuBoard.SIZE; j++) {
        // Check if the current element is the only non-zero element in its row and column
        if (isOnlyNonZeroInRow(matrix, i, j) || isOnlyNonZeroInColumn(matrix, i, j)) {
          matrix[i][j] = "1";
          // Set all other elements in the row and column to "0"
          for (int k = 0; k < SudokuBoard.SIZE; k++) {
            if (k != j) matrix[i][k] = "0";
            if (k != i) matrix[k][j] = "0";
          }
        }
      }
    }

    return matrix;
  }

  static String[][] computeMatrixProduct(String[][] matrixA, String[][] matrixB) {
    if (matrixA.length != matrixB.length || matrixA[0].length != matrixB[0].length) {
      throw new IllegalArgumentException("Matrices must be of the same size");
    }

    int n = matrixA.length;
    String[][] result = new String[n][n];

    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        List<String> products = new ArrayList<>();
        for (int k = 0; k < n; k++) {
          products.add(multiply(matrixA[i][k], matrixB[k][j]));
        }
        result[i][j] = simplifySum(products);
      }
    }

    return result;
  }

  private static String multiply(String a, String b) {
    try {
      int intA = Integer.parseInt(a);
      int intB = Integer.parseInt(b);
      return String.valueOf(intA * intB);
    } catch (NumberFormatException e) {
      // One or both are not integers
      if ("0".equals(a) || "0".equals(b)) return "0";
      if ("1".equals(a)) return b;
      if ("1".equals(b)) return a;
      return a + "*" + b;
    }
  }

  private static String simplifySum(List<String> products) {
    int sum = 0;
    List<String> nonIntegers = new ArrayList<>();
    for (String prod : products) {
      try {
        sum += Integer.parseInt(prod);
      } catch (NumberFormatException e) {
        if (!"0".equals(prod)) {
          nonIntegers.add(prod);
        }
      }
    }

    if (sum != 0 || nonIntegers.isEmpty()) {
      nonIntegers.add(0, String.valueOf(sum));
    }

    return String.join("+", nonIntegers);
  }

  private static int indexOf(GridSquare[] squares, int value) {
    for (int i = 0; i < squares.length; i++) {
      if (squares[i].getValue().isPresent() && (squares[i].getValue().get() == value)) {
        return i;
      }
    }
    return -1; // value not found
  }

  // Helper method to check if the current element is the only non-zero element in its row and
  // column
  private static boolean isOnlyNonZeroInRow(String[][] matrix, int row, int col) {
    for (int i = 0; i < matrix.length; i++) {
      if (i != col && !matrix[row][i].equals("0")) return false;
    }
    return true;
  }

  // Helper method to check if the current element is the only non-zero element in its row and
  // column
  private static boolean isOnlyNonZeroInColumn(String[][] matrix, int row, int col) {
    for (int j = 0; j < matrix.length; j++) {
      if (j != row && !matrix[j][col].equals("0")) return false;
    }
    return true;
  }
}

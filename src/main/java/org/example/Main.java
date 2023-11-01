package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args) {
    int[] easyRow1 = new int[] {3, 0, 6, 2, 0, 0, 5, 7, 0};
    int[] easyRow2 = new int[] {1, 5, 7, 0, 0, 0, 2, 0, 0};
    int[] easyRow3 = new int[] {2, 8, 0, 0, 3, 0, 6, 0, 0};
    int[] easyRow4 = new int[] {0, 1, 0, 0, 0, 6, 8, 3, 0};
    int[] easyRow5 = new int[] {0, 6, 3, 1, 0, 0, 9, 0, 0};
    int[] easyRow6 = new int[] {0, 0, 0, 3, 4, 7, 1, 5, 0};
    int[] easyRow7 = new int[] {6, 7, 0, 0, 0, 0, 0, 0, 0};
    int[] easyRow8 = new int[] {0, 0, 1, 6, 0, 0, 7, 2, 0};
    int[] easyRow9 = new int[] {5, 0, 9, 8, 7, 0, 3, 6, 0};

    int[][] easyPuzzle =
        new int[][] {
          easyRow1, easyRow2, easyRow3, easyRow4, easyRow5, easyRow6, easyRow7, easyRow8, easyRow9
        };

    solveAndPrintPuzzle("EASY PUZZLE", easyPuzzle);

    int[] masterRow1 = new int[] {9, 0, 0, 0, 5, 0, 0, 6, 0};
    int[] masterRow2 = new int[] {0, 5, 3, 7, 0, 0, 0, 8, 0};
    int[] masterRow3 = new int[] {4, 0, 0, 0, 0, 0, 0, 0, 3};
    int[] masterRow4 = new int[] {0, 9, 0, 0, 0, 0, 0, 0, 0};
    int[] masterRow5 = new int[] {0, 8, 5, 0, 6, 0, 0, 0, 1};
    int[] masterRow6 = new int[] {0, 0, 0, 4, 0, 0, 6, 0, 0};
    int[] masterRow7 = new int[] {0, 6, 1, 0, 4, 0, 0, 0, 8};
    int[] masterRow8 = new int[] {0, 0, 0, 0, 0, 2, 0, 7, 0};
    int[] masterRow9 = new int[] {3, 0, 0, 0, 0, 0, 0, 0, 0};

    int[][] masterPuzzle =
        new int[][] {
          masterRow1,
          masterRow2,
          masterRow3,
          masterRow4,
          masterRow5,
          masterRow6,
          masterRow7,
          masterRow8,
          masterRow9
        };

    solveAndPrintPuzzle("MASTER PUZZLE", masterPuzzle);
  }

  private static void solveAndPrintPuzzle(String puzzleName, int[][] rowForm) {
    System.out.println("BEFORE SOLVING " + puzzleName);
    printSudokuBoard(rowForm);
    System.out.println("");
    solvePuzzle(rowForm);
    System.out.println("AFTER SOLVING " + puzzleName);
    printSudokuBoard(rowForm);
    System.out.println("");
  }

  private static void solvePuzzle(int[][] rowForm) {
    int rows = rowForm.length;
    int cols = rowForm[0].length;
    int[][] colForm = new int[cols][rows];

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        colForm[j][i] = rowForm[i][j];
      }
    }

    GridSquare[][] grid = new GridSquare[9][9];

    // initial population
    for (int rowNum = 1; rowNum <= 9; rowNum++) {
      for (int colNum = 1; colNum <= 9; colNum++) {
        int rowIndex = rowNum - 1;
        int colIndex = colNum - 1;
        GridSquare gridSquare;
        if (rowForm[rowIndex][colIndex] != 0) {
          gridSquare = new GridSquare(rowForm[rowIndex][colIndex]);
        } else {
          gridSquare = new GridSquare(null);
        }
        gridSquare.setRowFormCoordinate(rowIndex, colIndex);
        gridSquare.setColFormCoordinate(colIndex, rowIndex);
        grid[rowIndex][colIndex] = gridSquare;
      }
    }

    int[][] gridForm = new int[9][9];
    int gridSize = 3;

    for (int gridRow = 0; gridRow < gridSize; gridRow++) {
      for (int gridCol = 0; gridCol < gridSize; gridCol++) {
        int[] gridVals = new int[9];
        int index = 0;

        for (int row = gridRow * gridSize; row < (gridRow + 1) * gridSize; row++) {
          for (int col = gridCol * gridSize; col < (gridCol + 1) * gridSize; col++) {
            grid[row][col].setGridFormCoordinate(gridRow * gridSize + gridCol, index);
            gridVals[index++] = rowForm[row][col];
          }
        }

        gridForm[gridRow * gridSize + gridCol] = gridVals;
      }
    }

    // make connections with neighbors
    for (int rowNum = 1; rowNum <= 9; rowNum++) {
      for (int colNum = 1; colNum <= 9; colNum++) {
        int rowIndex = rowNum - 1;
        int colIndex = colNum - 1;
        GridSquare gridSquare = grid[rowIndex][colIndex];
        if (colIndex == 0) {
          gridSquare.setRightNeighbor(grid[rowIndex][colIndex + 1]);
        } else if (colIndex == 8) {
          gridSquare.setLeftNeighbor(grid[rowIndex][colIndex - 1]);
        } else {
          gridSquare.setRightNeighbor(grid[rowIndex][colIndex + 1]);
          gridSquare.setLeftNeighbor(grid[rowIndex][colIndex - 1]);
        }
        if (rowIndex == 0) {
          gridSquare.setBelowNeighbor(grid[rowIndex + 1][colIndex]);
        } else if (rowIndex == 8) {
          gridSquare.setAboveNeighbor(grid[rowIndex - 1][colIndex]);
        } else {
          gridSquare.setBelowNeighbor(grid[rowIndex + 1][colIndex]);
          gridSquare.setAboveNeighbor(grid[rowIndex - 1][colIndex]);
        }
      }
    }

    spotInvalidValues(rowForm, colForm, gridForm, grid, true);

    if (Arrays.stream(rowForm).noneMatch(Main::hasZeroValue)) {
      return;
    }

    // start looking at group transformations between 3x3 grids
    System.out.println("A -> B");
    String[][] Tab = computeTransformationMatrix("AB", 0, 3, gridForm, grid);
    printTransformation(Tab, "5");
    System.out.println();

    // start looking at group transformations between 3x3 grids
    System.out.println("B -> A");
    String[][] Tba = computeTransformationMatrix("BA", 3, 0, gridForm, grid);
    printTransformation(Tba, "5");
    System.out.println();

    System.out.println("A*B");
    String[][] product = computeProduct(Tab, Tba);
    printTransformation(product, "100");
    System.out.println();

    // collect values from the product that can be definitively 0 or 1.
    Map<String, String> valMap = new HashMap<>();
    for (int i = 0 ; i < product.length ; i++) {
      for (int j = 0 ; j < product.length ; j++) {
        String value = product[i][j];
        if (!"1".equals(value) && !"0".equals(value) && !value.contains("*")) {
          if (i == j) {
            valMap.put(value, "1");
          } else {
            valMap.put(value, "0");
          }
        }
      }
    }
    System.out.println(valMap);

    for (int i = 0 ; i < product.length ; i++) {
      for (int j = 0 ; j < product.length ; j++) {
        String value = Tab[i][j];
        if (valMap.containsKey(value)) {
          Tab[i][j] = valMap.get(value);
        }
      }
    }

    for (int i = 0; i < product.length; i++) {
      for (int j = 0; j < product.length; j++) {
        // Check if the current element is the only non-zero element in its row and column
        if (isOnlyNonZeroInRow(Tab, i, j) || isOnlyNonZeroInColumn(Tab, i, j)) {
          Tab[i][j] = "1";
          // Set all other elements in the row and column to "0"
          for (int k = 0; k < product.length; k++) {
            if (k != j) Tab[i][k] = "0";
            if (k != i) Tab[k][j] = "0";
          }
        }
      }
    }

    for (int i = 0 ; i < product.length ; i++) {
      for (int j = 0 ; j < product.length ; j++) {
        String value = Tba[i][j];
        if (valMap.containsKey(value)) {
          Tba[i][j] = valMap.get(value);
        }
      }
    }

    for (int i = 0; i < product.length; i++) {
      for (int j = 0; j < product.length; j++) {
        // Check if the current element is the only non-zero element in its row and column
        if (isOnlyNonZeroInRow(Tba, i, j) || isOnlyNonZeroInColumn(Tba, i, j)) {
          Tba[i][j] = "1";
          // Set all other elements in the row and column to "0"
          for (int k = 0; k < product.length; k++) {
            if (k != j) Tba[i][k] = "0";
            if (k != i) Tba[k][j] = "0";
          }
        }
      }
    }

    // start looking at group transformations between 3x3 grids
    System.out.println("A -> B");
    printTransformation(Tab, "5");
    System.out.println();

    // start looking at group transformations between 3x3 grids
    System.out.println("B -> A");
    printTransformation(Tba, "5");
    System.out.println();

    System.out.println("A*B");
    product = computeProduct(Tab, Tba);
    printTransformation(product, "100");
    System.out.println();
  }

  private static String[][] computeTransformationMatrix(
      String name, int gridNumA, int gridNumB, int[][] gridForm, GridSquare[][] grid) {
    int size = 9;
    String[][] matrix = new String[size][size];

    // Initialize matrix with unknowns
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        matrix[i][j] = name + (i + 1) + (j + 1); // Adjusting index by +1 to match grid 1-9 notation
      }
    }

    int[] subGridA = gridForm[gridNumA];
    int[] subGridB = gridForm[gridNumB];

    for (int i = 0; i < size; i++) {
      int fromValue = subGridA[i];
      if (fromValue == 0) {
        // cannot say anything about possible transformations
        continue;
      }
      int toIndex = indexOf(subGridB, fromValue);
      if (toIndex != -1) {
        // full mapping
        for (int j = 0; j < size; j++) {
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
        // need to convert (gridNumB, j) in gridForm -> (row, col)
        Set<GridSquare> squaresToCheck = Arrays.stream(grid)
                .flatMap(Arrays::stream) // Flatten the 2D array to a Stream<GridSquare>
                .filter(gs -> gs.gridFormCoordX == gridNumB) // Filter by gridCoordX
                .collect(Collectors.toSet()); // Collect into a Set

        for (GridSquare square : squaresToCheck) {
          if (square.isInvalidValue(fromValue)) {
            int gridCoordY = square.gridFormCoordY;
            matrix[i][gridCoordY] = "0";
          }
        }
      }
    }

    // if the value in any particular row or column of the transformation matrix is the only non-zero value in that
    // row or column, then it is a 1. If we update to a 1, then we must also update the row & column it is in
    // to ensure the rest of the values are 0.
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        // Check if the current element is the only non-zero element in its row and column
        if (isOnlyNonZeroInRow(matrix, i, j) || isOnlyNonZeroInColumn(matrix, i, j)) {
          matrix[i][j] = "1";
          // Set all other elements in the row and column to "0"
          for (int k = 0; k < size; k++) {
            if (k != j) matrix[i][k] = "0";
            if (k != i) matrix[k][j] = "0";
          }
        }
      }
    }

    return matrix;
  }

  // Helper method to check if the current element is the only non-zero element in its row and column
  private static boolean isOnlyNonZeroInRow(String[][] matrix, int row, int col) {
    for (int i = 0; i < matrix.length; i++) {
      if (i != col && !matrix[row][i].equals("0")) return false;
    }
    return true;
  }

  // Helper method to check if the current element is the only non-zero element in its row and column
  private static boolean isOnlyNonZeroInColumn(String[][] matrix, int row, int col) {
    for (int j = 0; j < matrix.length; j++) {
      if (j != row && !matrix[j][col].equals("0")) return false;
    }
    return true;
  }

  private static String[][] computeProduct(String[][] matrixA, String[][] matrixB) {
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

  // Helper function to find index of an element in an array
  private static int indexOf(int[] array, int value) {
    for (int i = 0; i < array.length; i++) {
      if (array[i] == value) {
        return i;
      }
    }
    return -1; // value not found
  }

  private static boolean hasZeroValue(int[] arr) {
    return Arrays.stream(arr).anyMatch(value -> value == 0);
  }

  private static void spotInvalidValues(
      int[][] rowForm, int[][] colForm, int[][] gridForm, GridSquare[][] grid, boolean simplifyPuzzle) {

    // set invalid values based on rows
    for (int rowNum = 1; rowNum <= 9; rowNum++) {
      int rowIndex = rowNum - 1;
      int[] row = rowForm[rowIndex];
      for (int colNum = 1; colNum <= 9; colNum++) {
        int colIndex = colNum - 1;
        GridSquare gridSquare = grid[rowIndex][colIndex];
        gridSquare.addInvalidValues(row);
      }
    }

    // set invalid values based on columns
    for (int colNum = 1; colNum <= 9; colNum++) {
      int colIndex = colNum - 1;
      int[] col = colForm[colIndex];
      for (int rowNum = 1; rowNum <= 9; rowNum++) {
        int rowIndex = rowNum - 1;
        GridSquare gridSquare = grid[rowIndex][colIndex];
        gridSquare.addInvalidValues(col);
      }
    }

    // set invalid values based on local 3x3 grids
    for (int rowNum = 1; rowNum <= 9; rowNum++) {
      for (int colNum = 1; colNum <= 9; colNum++) {
        int rowIndex = rowNum - 1;
        int colIndex = colNum - 1;
        GridSquare gridSquare = grid[rowIndex][colIndex];
        if (rowIndex < 3) {
          if (colIndex < 3) {
            gridSquare.addInvalidValues(gridForm[0]);
          } else if (colIndex < 6) {
            gridSquare.addInvalidValues(gridForm[1]);
          } else {
            gridSquare.addInvalidValues(gridForm[2]);
          }
        } else if (rowIndex < 6) {
          if (colIndex < 3) {
            gridSquare.addInvalidValues(gridForm[3]);
          } else if (colIndex < 6) {
            gridSquare.addInvalidValues(gridForm[4]);
          } else {
            gridSquare.addInvalidValues(gridForm[5]);
          }
        } else {
          if (colIndex < 3) {
            gridSquare.addInvalidValues(gridForm[6]);
          } else if (colIndex < 6) {
            gridSquare.addInvalidValues(gridForm[7]);
          } else {
            gridSquare.addInvalidValues(gridForm[8]);
          }
        }
      }
    }

    if (!simplifyPuzzle) {
      return;
    }

    // iterate through all grid squares and update maps with determined values
    boolean updateOccurred = false;
    for (GridSquare[] gridSquares : grid) {
      for (GridSquare gridSquare : gridSquares) {
        if (gridSquare.getValue().isPresent()
            && rowForm[gridSquare.rowFormCoordX][gridSquare.rowFormCoordY] == 0) {
          int value = gridSquare.getValue().get();
          rowForm[gridSquare.rowFormCoordX][gridSquare.rowFormCoordY] = value;
          colForm[gridSquare.colFormCoordX][gridSquare.colFormCoordY] = value;
          gridForm[gridSquare.gridFormCoordX][gridSquare.gridFormCoordY] = value;
          updateOccurred = true;
        }
      }
    }

    if (updateOccurred) {
      spotInvalidValues(rowForm, colForm, gridForm, grid, simplifyPuzzle);
    }
  }

  public static void printSudokuBoard(int[][] board) {
    for (int i = 0; i < board.length; i++) {
      if (i % 3 == 0 && i != 0) {
        System.out.println("------+-------+------");
      }
      for (int j = 0; j < board[0].length; j++) {
        if (j % 3 == 0 && j != 0) {
          System.out.print("| ");
        }
        System.out.print(board[i][j] + " ");
      }
      System.out.println();
    }
  }

  public static void printTransformation(String[][] board, String padding) {
    for (int i = 0; i < board.length; i++) {
      if (i % 3 == 0 && i != 0) {
        System.out.println("------------------+-------------------+-----------------");
      }
      for (int j = 0; j < board[0].length; j++) {
        if (j % 3 == 0 && j != 0) {
          System.out.print("| ");
        }
        System.out.printf("%-"+ padding +"s ", board[i][j]);  // Ensures each element takes at least 2 spaces
      }
      System.out.println();
    }
  }
}

package org.example;

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

    int[][] easyPuzzle = new int[][] {easyRow1, easyRow2, easyRow3, easyRow4, easyRow5, easyRow6, easyRow7, easyRow8, easyRow9};

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

    int[][] masterPuzzle = new int[][] {masterRow1, masterRow2, masterRow3, masterRow4, masterRow5, masterRow6, masterRow7, masterRow8, masterRow9};

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

    simplifyPuzzle(rowForm, colForm, gridForm, grid);

    // start looking at group transformations between 3x3 grids
  }

  private static void simplifyPuzzle(int[][] rowForm, int[][] colForm, int[][] gridForm, GridSquare[][] grid) {

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

    // iterate through all grid squares and update maps with determined values
    boolean updateOccurred = false;
    for (GridSquare[] gridSquares : grid) {
      for (GridSquare gridSquare : gridSquares) {
        if (gridSquare.getValue().isPresent() && rowForm[gridSquare.rowFormCoordX][gridSquare.rowFormCoordY] == 0) {
          int value = gridSquare.getValue().get();
          rowForm[gridSquare.rowFormCoordX][gridSquare.rowFormCoordY] = value;
          colForm[gridSquare.colFormCoordX][gridSquare.colFormCoordY] = value;
          gridForm[gridSquare.gridFormCoordX][gridSquare.gridFormCoordY] = value;
          updateOccurred = true;
        }
      }
    }

    if (updateOccurred) {
      simplifyPuzzle(rowForm, colForm, gridForm, grid);
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
}

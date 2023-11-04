package org.example;

import java.util.*;

public class Main {
  public static void main(String[] args) {

    int[][] easyPuzzle =
        new int[][] {
          {0, 0, 9, 0, 7, 0, 3, 0, 0},
          {0, 5, 0, 0, 0, 0, 0, 0, 6},
          {0, 0, 0, 2, 0, 6, 8, 0, 1},
          {4, 3, 0, 0, 0, 0, 0, 0, 2},
          {0, 0, 6, 0, 0, 0, 4, 0, 9},
          {0, 0, 0, 7, 0, 0, 0, 0, 0},
          {0, 9, 0, 0, 0, 3, 0, 5, 0},
          {0, 0, 0, 0, 0, 0, 0, 0, 0},
          {8, 6, 0, 4, 0, 0, 0, 0, 3}
        };

    SudokuBoard easyBoard = new SudokuBoard(easyPuzzle);
    easyBoard.print();

    //    solveAndPrintPuzzle("EASY PUZZLE", easyPuzzle);
    //
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

    SudokuBoard masterBoard = easyBoard;
    masterBoard.print();

    List<String[][]> transformations = new ArrayList<>();
    int[] order = new int[] {0,1,2,4,0};
    Map<Integer, String> nameMap =
        Map.of(
            0, "A",
            1, "B",
            2, "C",
            3, "D",
            4, "E",
            5, "F",
            6, "G",
            7, "H",
            8, "I");
    for (int index = 0; index < order.length - 1; index++) {
      GridSquare[] squareA = masterBoard.getAllSquaresInGrid(order[index]);
      GridSquare[] squareB = masterBoard.getAllSquaresInGrid(order[index + 1]);
      String name = nameMap.get(order[index]) + nameMap.get(order[index + 1]);
      String[][] t01 = BoardReducer.symmetricReduce(name, squareA, squareB);
      transformations.add(t01);
    }

    String[][] bigTransform = transformations.get(0);
    for (int index = 1; index < transformations.size(); index++) {
      bigTransform =
          MatrixTransformer.computeMatrixProduct(bigTransform, transformations.get(index));
    }

    BoardReducer.printTransformation(bigTransform, "100");
    //    String[][] inv = MatrixTransformer.computeMatrixTranspose(transform);
    //    BoardReducer.printTransformation(inv, "10");
    //
    //    String[][] id = MatrixTransformer.computeMatrixProduct(bigTransform, inv);
    //    BoardReducer.printTransformation(id, "100");
    for (int row = 0; row < bigTransform.length; row++) {
      for (int col = 0; col < bigTransform[row].length; col++) {
        List<String> factors = getHighestLevelFactors(bigTransform[row][col]);
        if (factors != null) {
          System.out.println("Row " + row + " column " + col);
          System.out.println(getHighestLevelFactors(bigTransform[row][col]));
        }
      }
    }
    System.out.println("OMG");

    //    int indA = 0;
    //    int indB = 3;
    //    GridSquare[] squaresA = masterBoard.getAllSquaresInGrid(indA);
    //    GridSquare[] squaresB = masterBoard.getAllSquaresInGrid(indB);
    //    String[][] t01 = BoardReducer.symmetricReduce("AB", squaresA, squaresB);
    //
    //    indA = 3;
    //    indB = 6;
    //    squaresA = masterBoard.getAllSquaresInGrid(indA);
    //    squaresB = masterBoard.getAllSquaresInGrid(indB);
    //    String[][] t12 = BoardReducer.symmetricReduce("BC", squaresA, squaresB);
    //
    //    String[][] prod = MatrixTransformer.computeMatrixProduct(t01, t12);
    //    System.out.println("PATH:");
    //    BoardReducer.printTransformation(prod, "50");
    //
    //    indA = 0;
    //    indB = 6;
    //    squaresA = masterBoard.getAllSquaresInGrid(indA);
    //    squaresB = masterBoard.getAllSquaresInGrid(indB);
    //    String[][] t02 = BoardReducer.symmetricReduce("AC", squaresA, squaresB);
    //
    //    String[][] t02inv = MatrixTransformer.computeMatrixTranspose(t02);
    //    prod = MatrixTransformer.computeMatrixProduct(prod, t02inv);
    //    System.out.println("PROD (should be identity):");
    //    BoardReducer.printTransformation(prod, "50");

    //
    //    solveAndPrintPuzzle("MASTER PUZZLE", masterPuzzle);
  }

  public static List<String> getHighestLevelFactors(String expression) {

    try {
      Integer.parseInt(expression);
    } catch (NumberFormatException e) {
      List<String> factors = new ArrayList<>();
      int depth = 0; // Current depth of parentheses
      int start = 0; // Start index of a factor
      boolean isProductAtHighestLevel =
          true; // Flag to check if at the highest level it's only product

      for (int i = 0; i < expression.length(); i++) {
        char ch = expression.charAt(i);
        if (ch == '(') {
          // Increase the depth level when '(' is encountered
          depth++;
          if (depth == 1) {
            // Potential start of a high level factor
            start = i;
          }
        } else if (ch == ')') {
          // Decrease the depth level when ')' is encountered
          depth--;
          if (depth == 0) {
            // End of a high level factor
            factors.add(expression.substring(start, i + 1));
          }
        } else if ((ch == '+' || ch == '-') && depth == 0) {
          // If we encounter '+' or '-' at the highest level, it's not purely a product
          isProductAtHighestLevel = false;
          break;
        }
      }

      if (!isProductAtHighestLevel) {
        return null;
      }

      // If there's only one factor and it's surrounded by parentheses, we need to go deeper
      if (factors.size() == 1) {
        String innerExpression = factors.get(0);
        // Remove the outermost parentheses
        innerExpression = innerExpression.substring(1, innerExpression.length() - 1);
        return getHighestLevelFactors(innerExpression); // Recursively process the inner expression
      }

      return factors;
    }

    return null;
  }
}

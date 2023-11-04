package org.example;

import java.util.*;

public class BoardReducer {

  static String[][] symmetricReduce(String name, GridSquare[] squaresA, GridSquare[] squaresB) {
    // these matrices should be inverses & transpositions of each other
    String[][] transformationAB =
        MatrixTransformer.computeTransformationMatrix(name, squaresA, squaresB);
    String[][] transformationBA =
        MatrixTransformer.computeTransformationMatrix(name, squaresB, squaresA);

    System.out.println("BEFORE REDUCE:");
    printTransformation(transformationAB, "10");
    for (int i = 0; i < SudokuBoard.SIZE; i++) {
      for (int j = 0; j < SudokuBoard.SIZE; j++) {
        String elementBA = transformationBA[i][j];
        if ("1".equals(elementBA) || "0".equals(elementBA)) {
          transformationAB[j][i] = elementBA;
        }
      }
    }
    System.out.println("AFTER REDUCE:");
    printTransformation(transformationAB, "10");

    return transformationAB;
  }

  static void basicReduce(SudokuBoard sudokuBoard) {
    boolean updateOccurred = false;
    for (int i = 0; i < SudokuBoard.SIZE; i++) {
      boolean squareInRowUpdated = setInvalidValues(sudokuBoard.getAllSquaresInRow(i));
      boolean squareInColUpdated = setInvalidValues(sudokuBoard.getAllSquaresInColumn(i));
      boolean squareInGridUpdated = setInvalidValues(sudokuBoard.getAllSquaresInGrid(i));
      if (!updateOccurred && (squareInRowUpdated || squareInColUpdated || squareInGridUpdated)) {
        updateOccurred = true;
      }
    }
    if (updateOccurred) {
      basicReduce(sudokuBoard);
    }
    for (int i = 0; i < SudokuBoard.SIZE; i++) {
      boolean squareInRowUpdated = findUniqueValidValues(sudokuBoard.getAllSquaresInRow(i));
      boolean squareInColUpdated = findUniqueValidValues(sudokuBoard.getAllSquaresInColumn(i));
      boolean squareInGridUpdated = findUniqueValidValues(sudokuBoard.getAllSquaresInGrid(i));
      if (!updateOccurred && (squareInRowUpdated || squareInColUpdated || squareInGridUpdated)) {
        updateOccurred = true;
      }
    }
    if (updateOccurred) {
      basicReduce(sudokuBoard);
    }
  }

  public static boolean findUniqueValidValues(GridSquare[] squares) {
    int[] validValueFrequency =
        new int[10]; // Array from 0-9 for simplicity, 0 index will be unused
    List<Integer> uniqueValidValues = new ArrayList<>();

    // Count frequency of valid values for all squares
    for (GridSquare square : squares) {
      for (int value = 1; value <= 9; value++) {
        if (!square.isInvalidValue(value)) {
          validValueFrequency[value]++;
        }
      }
    }

    // Find unique valid values for each square
    boolean updateOccurred = false;
    for (GridSquare square : squares) {
      for (int value = 1; value <= 9; value++) {
        if (!square.isInvalidValue(value)
            && validValueFrequency[value] == 1
            && square.getValue().isEmpty()) {
          square.setValue(value);
          // Optional: Break if you want to only find one unique value per square
          // break;
          updateOccurred = true;
        }
      }
    }
    return updateOccurred;
  }

  private static boolean setInvalidValues(GridSquare[] squares) {
    boolean updateOccurred = false;
    Set<Integer> invalidValues = new HashSet<>();
    for (GridSquare square : squares) {
      square.getValue().ifPresent(invalidValues::add);
    }
    for (GridSquare square : squares) {
      boolean squareDidUpdate = square.addInvalidValuesAndUpdate(invalidValues);
      if (!updateOccurred && squareDidUpdate) {
        updateOccurred = true;
      }
    }
    return updateOccurred;
  }

  static void printTransformation(String[][] board, String padding) {
    for (String[] strings : board) {
      for (int j = 0; j < board[0].length; j++) {
        if (j % 3 == 0 && j != 0) {
          System.out.print("| ");
        }
        String string = strings[j];
        System.out.printf(
            "%-" + padding + "s ",
            shortenString(
                strings[j],
                Integer.parseInt(padding))); // Ensures each element takes at least 2 spaces
      }
      System.out.println();
    }
    System.out.println();
  }

  public static String shortenString(String string, int numberOfCharacters) {
    // Return the original string if it's short enough or if the requested length is too small to
    // add "..."
    if (string.length() <= numberOfCharacters || numberOfCharacters < 4) {
      return string;
    }

    // Calculate how many characters should be taken from the start and end parts of the string
    int charsEachSide = (numberOfCharacters - 3) / 2;
    int charsFromEnd = numberOfCharacters - 3 - charsEachSide;

    // Extract the beginning and ending parts of the string
    String start = string.substring(0, charsEachSide);
    String end = string.substring(string.length() - charsFromEnd);

    // Return the new shortened string
    return start + "..." + end;
  }
}

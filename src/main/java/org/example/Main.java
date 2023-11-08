package org.example;

import java.util.*;

public class Main {
  public static void main(String[] args) {

    // 4x4 study
    // 1. Generate all possible 4x4 boards via permutations
    // 2. Figure out which boards are isomorphic to others via symmetries
    Board initBoard = new FourByFourSudokuBoard();

    List<Permutation> rowZeroPermutations = initBoard.computePossiblePermutationsToNextRow(0);
    for (Permutation rowZeroPermutation : rowZeroPermutations) {
      Board board = new FourByFourSudokuBoard();
      board.applyPermutationToRow(0, rowZeroPermutation);
      List<Permutation> rowOnePermutations = board.computePossiblePermutationsToNextRow(1);
      for (Permutation rowOnePermutation : rowOnePermutations) {
        board = new FourByFourSudokuBoard();
        board.applyPermutationToRow(0, rowZeroPermutation);
        board.applyPermutationToRow(1, rowOnePermutation);
        List<Permutation> rowTwoPermutations = board.computePossiblePermutationsToNextRow(2);
        for (Permutation rowTwoPermutation : rowTwoPermutations) {
          board = new FourByFourSudokuBoard();
          board.applyPermutationToRow(0, rowZeroPermutation);
          board.applyPermutationToRow(1, rowOnePermutation);
          board.applyPermutationToRow(2, rowTwoPermutation);
          board.print();
        }
      }
    }
  }
}

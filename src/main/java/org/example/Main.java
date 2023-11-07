package org.example;

import java.util.*;

public class Main {
  public static void main(String[] args) {

    // 4x4 study
    // 1. Generate all possible 4x4 boards via permutations
    // 2. Figure out which boards are isomorphic to others via symmetries
    Board board = new FourByFourBoardImpl();

    board.print();
    List<Permutation> permutations = board.computePossiblePermutationsToNextRow(0);
    board.applyPermutationToRow(0, permutations.get(0));
    board.print();
    permutations = board.computePossiblePermutationsToNextRow(1);
    board.applyPermutationToRow(1, permutations.get(0));
    board.print();
  }
}

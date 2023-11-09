package org.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

public class Main {

  static final Random RANDOM = new Random();

  public static void main(String[] args) throws IOException {

    Board initBoard = new FiveByFiveLatinSquare();
    List<Permutation> permutations = initBoard.computePossiblePermutationsToNextRow(0);

    PermutationSerializer.serializeToFile(permutations, "src/main/resources/org/example/fivebyfivepermutations.txt");
//
//    int possibleBoards = 0;
//    List<Permutation> rowZeroPermutations = initBoard.computePossiblePermutationsToNextRow(0);
//    Set<Board> boards = new HashSet<>();
//    for (Permutation rowZeroPermutation : rowZeroPermutations) {
//      Board board = new FiveByFiveLatinSquare();
//      board.applyPermutationToRow(0, rowZeroPermutation);
//      List<Permutation> rowOnePermutations = board.computePossiblePermutationsToNextRow(1);
//      for (Permutation rowOnePermutation : rowOnePermutations) {
//        board = new FiveByFiveLatinSquare();
//        board.applyPermutationToRow(0, rowZeroPermutation);
//        board.applyPermutationToRow(1, rowOnePermutation);
//        List<Permutation> rowTwoPermutations = board.computePossiblePermutationsToNextRow(2);
//        for (Permutation rowTwoPermutation : rowTwoPermutations) {
//          board = new FiveByFiveLatinSquare();
//          board.applyPermutationToRow(0, rowZeroPermutation);
//          board.applyPermutationToRow(1, rowOnePermutation);
//          board.applyPermutationToRow(2, rowTwoPermutation);
//          List<Permutation> rowThreePermutations = board.computePossiblePermutationsToNextRow(3);
//          for (Permutation rowThreePermutation : rowThreePermutations) {
//            possibleBoards++;
//            board = new FiveByFiveLatinSquare();
//            board.applyPermutationToRow(0, rowZeroPermutation);
//            board.applyPermutationToRow(1, rowOnePermutation);
//            board.applyPermutationToRow(2, rowTwoPermutation);
//            board.applyPermutationToRow(3, rowThreePermutation);
//
//            for (int i = 0; i < 10; i++) {
//              performTransformations(board);
//            }
//
//            boards.add(board);
//          }
//        }
//      }
//    }
//    for (Board board1 : boards) {
//      for (Board board2 : boards) {
//        if (board1 == board2) {
//          continue;
//        }
//        Set<PermutationChain> permutations1 = board1.getEquivalentPermutationChains();
//        Set<PermutationChain> permutations2 = board2.getEquivalentPermutationChains();
//
//        int size1 = permutations1.size();
//        int size2 = permutations2.size();
//        if (size1 >= size2) {
//          permutations1.removeAll(permutations2);
//        } else {
//          permutations2.removeAll(permutations1);
//        }
//        if (size1 != permutations1.size() || size2 != permutations2.size()) {
//          System.out.println("OMG");
//        }
//      }
//    }
  }

  private static void performTransformations(Board board) {
    board.rotateCounterclockwise();
    performRowAndColumnSwaps(board);
    board.verticalReflection();
    performRowAndColumnSwaps(board);
    board.horizontalReflection();
    performRowAndColumnSwaps(board);
    performRowAndColumnSwaps(board);
  }

  private static void performRowAndColumnSwaps(Board board) {
    performSwapsRandom(board::swapRows, board::swapColumns);
  }

  static void performSwapsRandom(
      BiConsumer<Integer, Integer> function0, BiConsumer<Integer, Integer> function1) {
    int i = 0;
    while (i < 500) {
      int valA = RANDOM.nextInt(5);
      int valB = RANDOM.nextInt(5);
      if (valA == valB) {
        continue;
      }
      int bool = RANDOM.nextInt(2);
      if (bool == 0) {
        function0.accept(valA, valB);
      } else {
        function1.accept(valA, valB);
      }
      i++;
    }
  }
}

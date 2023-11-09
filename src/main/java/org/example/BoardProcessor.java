package org.example;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

public class BoardProcessor {
  private static final Random RANDOM = new Random();

  Callable<Board> boardCreator;
  Board initialBoard;
  List<Permutation> validPermutations;

  BoardProcessor(
      Board initialBoard, List<Permutation> validPermutations, Callable<Board> boardCreator) {
    this.initialBoard = initialBoard;
    this.validPermutations = validPermutations;
    this.boardCreator = boardCreator;
  }

  Callable<Set<Board>> generateBoardsFromPermutation(Permutation permutation) {
    return () -> {
      Set<Board> boards = new HashSet<>();
      Board board = boardCreator.call();
      board.applyPermutationToRow(0, permutation);
      List<Permutation> appliedPermutations = new ArrayList<>();
      appliedPermutations.add(permutation);
      List<Permutation> permutations =
          board.computePossiblePermutationsToNextRow(validPermutations, 1);
      for (Permutation newPermutation : permutations) {
        applyPermutations(appliedPermutations, newPermutation, boards);
      }

      return boards;
    };
  }

  private void applyPermutations(
      List<Permutation> appliedPermutations, Permutation permutation, Set<Board> boards)
      throws Exception {
    Board board = boardCreator.call();
    for (int index = 0; index < appliedPermutations.size(); index++) {
      board.applyPermutationToRow(index, appliedPermutations.get(index));
    }
    board.applyPermutationToRow(appliedPermutations.size(), permutation);
    List<Permutation> newAppliedPermutations = new ArrayList<>(appliedPermutations);
    newAppliedPermutations.add(permutation);
    if (newAppliedPermutations.size() < board.size() - 1) {
      List<Permutation> nextLevelPermutations =
          board.computePossiblePermutationsToNextRow(
              validPermutations, newAppliedPermutations.size());
      for (Permutation newPermutation : nextLevelPermutations) {
        applyPermutations(newAppliedPermutations, newPermutation, boards);
      }
      return;
    }
    for (int i = 0; i < initialBoard.size() * 2; i++) {
      performTransformations(board);
    }
    boards.add(board);
  }

  private void performTransformations(Board board) {
    board.rotateCounterclockwise();
    performRowAndColumnSwaps(board);
    board.verticalReflection();
    performRowAndColumnSwaps(board);
    board.horizontalReflection();
    performRowAndColumnSwaps(board);
    performRowAndColumnSwaps(board);
  }

  private void performRowAndColumnSwaps(Board board) {
    performSwapsRandom(board::swapRows, board::swapColumns);
  }

  void performSwapsRandom(
      BiConsumer<Integer, Integer> function0, BiConsumer<Integer, Integer> function1) {
    int i = 0;
    while (i < 500) {
      int valA = RANDOM.nextInt(initialBoard.size());
      int valB = RANDOM.nextInt(initialBoard.size());
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

package org.example;

import java.util.*;
import java.util.concurrent.*;

public class Main {

  public static void main(String[] args) throws Exception {

    Board initBoard = new FiveByFiveLatinSquare();
    List<Permutation> permutations =
        PermutationSerializer.deserializeFromFile(
            Constants.RESOURCES_DIRECTORY_PATH + "fivebyfivepermutations.txt");

    System.out.println("test");

    BoardProcessor boardProcessor =
        new BoardProcessor(initBoard, permutations, FiveByFiveLatinSquare::new);
    List<Permutation> rowZeroPermutations =
        initBoard.computePossiblePermutationsToNextRow(permutations, 0);
    Set<Board> boards = new HashSet<>();

    List<Callable<Set<Board>>> callables =
        rowZeroPermutations.stream().map(boardProcessor::generateBoardsFromPermutation).toList();

    ExecutorService executorService = Executors.newFixedThreadPool(8);
    try {
      List<Future<Set<Board>>> results = executorService.invokeAll(callables);
      for (Future<Set<Board>> future : results) {
        Set<Board> currentBoards = future.get();
        boards.addAll(currentBoards);
      }
      // Do something with the results here...
    } catch (InterruptedException e) {
      // Handle the exception appropriately
      Thread.currentThread().interrupt(); // set the interrupt flag
    } finally {
      executorService.shutdown(); // Initiate shutdown
//      try {
//        if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
//          executorService.shutdownNow(); // Cancel currently executing tasks
//          // Wait a while for tasks to respond to being cancelled
//          if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
//            System.err.println("Executor did not terminate");
//          }
//        }
//      } catch (InterruptedException ie) {
//        // (Re-)Cancel if current thread also interrupted
//        executorService.shutdownNow();
//        // Preserve interrupt status
//        Thread.currentThread().interrupt();
//      }
    }


    for (Board board1 : boards) {
      for (Board board2 : boards) {
        if (board1 == board2) {
          continue;
        }
        Set<PermutationChain> permutations1 = board1.getEquivalentPermutationChains();
        Set<PermutationChain> permutations2 = board2.getEquivalentPermutationChains();

        int size1 = permutations1.size();
        int size2 = permutations2.size();
        if (size1 >= size2) {
          permutations1.removeAll(permutations2);
        } else {
          permutations2.removeAll(permutations1);
        }
        if (size1 != permutations1.size() || size2 != permutations2.size()) {
          System.out.println("OMG");
        }
      }
    }
  }
}

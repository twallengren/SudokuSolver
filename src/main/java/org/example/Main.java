package org.example;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

  public static void main(String[] args) throws Exception {

    Board initBoard = new SixBySixLatinSquare();

    List<Permutation> permutations =
        PermutationSerializer.deserializeFromFile(
            Constants.RESOURCES_DIRECTORY_PATH + "sixbysixderangements.txt");

    BoardProcessor boardProcessor =
        new BoardProcessor(initBoard, permutations, SixBySixLatinSquare::new);
    List<Permutation> rowZeroPermutations =
        initBoard.computePossiblePermutationsToNextRow(permutations, 0);
    Set<Board> boards = new HashSet<>();

    List<Callable<Set<Board>>> callables =
        rowZeroPermutations.stream().map(boardProcessor::generateBoardsFromPermutation).toList();

    ExecutorService executorService = Executors.newFixedThreadPool(16);
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

    int index = 1;
    for (Board board : boards) {
      String filename =
          Constants.RESOURCES_DIRECTORY_PATH
              + "sixbysix_latinsquare_"
              + "board"
              + index
              + "_permutationchains.txt";
      PermutationSerializer.serializePermutationChains(
          board.getEquivalentPermutationChains(), filename);
      index++;
    }
  }
}

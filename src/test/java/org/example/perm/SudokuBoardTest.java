package org.example.perm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SudokuBoardTest {

  private static SudokuBoard validFourByFour() {
    return SudokuBoard.fromGrid(
        new int[][] {
          {0, 1, 2, 3},
          {2, 3, 0, 1},
          {1, 0, 3, 2},
          {3, 2, 1, 0}
        });
  }

  @Test
  void acceptsAValidBoard() {
    SudokuBoard board = validFourByFour();
    assertEquals(4, board.order());
    assertEquals(2, board.boxSize());
    assertTrue(board.square().isValid());
    assertTrue(board.boxesAreValid());
    assertTrue(board.isValid());
  }

  @Test
  void rejectsOrdersThatAreNotPerfectSquares() {
    LatinSquare orderFive =
        LatinSquare.fromGrid(
            new int[][] {
              {0, 1, 2, 3, 4},
              {1, 2, 3, 4, 0},
              {2, 3, 4, 0, 1},
              {3, 4, 0, 1, 2},
              {4, 0, 1, 2, 3}
            });
    assertThrows(IllegalArgumentException.class, () -> SudokuBoard.of(orderFive));
  }

  @Test
  void latinButNotSudoku() {
    // Valid as a Latin square, but the top-left box holds 0, 1, 1, 0.
    SudokuBoard board =
        SudokuBoard.fromGrid(
            new int[][] {
              {0, 1, 2, 3},
              {1, 0, 3, 2},
              {2, 3, 0, 1},
              {3, 2, 1, 0}
            });
    assertTrue(board.square().isValid(), "it is a Latin square");
    assertFalse(board.boxesAreValid(), "but the boxes repeat symbols");
    assertFalse(board.isValid());
  }

  @Test
  void exposesThePermutationSequenceView() {
    SudokuBoard board = validFourByFour();
    PermutationSequence sequence = board.toSequence();
    assertEquals(3, sequence.length());
    assertTrue(sequence.isLatin());
    assertEquals(board.square(), LatinSquare.fromSequence(board.square().row(0), sequence));
  }

  @Test
  void printsWithBoxSeparators() {
    String rendered = validFourByFour().toString();
    assertTrue(rendered.contains("|"), "boxes should be separated vertically");
    assertTrue(rendered.contains("-+-"), "boxes should be separated horizontally");
  }

  @Test
  void equalityIsByContent() {
    assertEquals(validFourByFour(), validFourByFour());
    assertEquals(validFourByFour().hashCode(), validFourByFour().hashCode());
  }
}

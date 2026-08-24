package org.example.perm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LatinSquareTest {

  /** The cyclic square of order n: each row shifts the previous one by one. */
  private static LatinSquare cyclic(int n) {
    int[] shift = new int[n];
    for (int i = 0; i < n; i++) {
      shift[i] = (i + 1) % n;
    }
    Permutation step = Permutation.of(shift);
    Permutation[] steps = new Permutation[n - 1];
    java.util.Arrays.fill(steps, step);
    return LatinSquare.fromSequence(PermutationSequence.of(steps));
  }

  @Test
  void buildsSquareFromPermutationSequence() {
    LatinSquare square = cyclic(4);
    assertEquals(4, square.order());
    assertArrayEquals(new int[] {0, 1, 2, 3}, square.row(0));
    assertArrayEquals(new int[] {3, 0, 1, 2}, square.row(1));
    assertTrue(square.isValid());
  }

  @Test
  void gridAndSequenceViewsRoundTrip() {
    LatinSquare square = cyclic(5);
    PermutationSequence recovered = square.toSequence();
    assertEquals(square, LatinSquare.fromSequence(square.row(0), recovered));
  }

  @Test
  void roundTripsFromAnArbitraryGrid() {
    int[][] grid = {
      {0, 1, 2, 3},
      {1, 0, 3, 2},
      {2, 3, 0, 1},
      {3, 2, 1, 0}
    };
    LatinSquare square = LatinSquare.fromGrid(grid);
    assertTrue(square.isValid());
    assertEquals(square, LatinSquare.fromSequence(square.row(0), square.toSequence()));
  }

  @Test
  void sequenceHasOneFewerStepThanOrder() {
    LatinSquare square = cyclic(6);
    assertEquals(5, square.toSequence().length());
  }

  @Test
  void latinPropertyMatchesTheAllGapsAreDerangementsCriterion() {
    // The central claim of the permutation-sequence view.
    LatinSquare square = cyclic(5);
    assertTrue(square.isValid());
    assertTrue(square.toSequence().isLatin());
  }

  @Test
  void derangementStepsAloneDoNotGuaranteeALatinSquare() {
    // Both steps are derangements, but composing them fixes points, so columns repeat.
    Permutation step = Permutation.of(1, 0, 3, 2);
    PermutationSequence sequence = PermutationSequence.of(step, step, step);
    assertTrue(sequence.allStepsAreDerangements());
    assertFalse(sequence.isLatin(), "step twice returns to the start, repeating a row");
    assertFalse(LatinSquare.fromSequence(sequence).isValid());
    assertFalse(sequence.nonDerangementGaps().isEmpty());
  }

  @Test
  void detectsInvalidGrids() {
    int[][] repeatedColumn = {
      {0, 1, 2},
      {1, 2, 0},
      {2, 1, 0}
    };
    assertFalse(LatinSquare.fromGrid(repeatedColumn).isValid());
  }

  @Test
  void rejectsMalformedGrids() {
    assertThrows(
        IllegalArgumentException.class, () -> LatinSquare.fromGrid(new int[][] {{0, 1}, {1}}));
    assertThrows(
        IllegalArgumentException.class, () -> LatinSquare.fromGrid(new int[][] {{0, 5}, {1, 0}}));
    assertThrows(IllegalArgumentException.class, () -> LatinSquare.fromGrid(new int[0][0]));
  }

  @Test
  void rejectsSequenceOfTheWrongLength() {
    PermutationSequence tooShort = PermutationSequence.of(Permutation.of(1, 0, 2, 3));
    assertThrows(IllegalArgumentException.class, () -> LatinSquare.fromSequence(tooShort));
  }

  @Test
  void rejectsBaseRowThatIsNotAPermutation() {
    PermutationSequence sequence =
        PermutationSequence.of(Permutation.of(1, 0, 2), Permutation.of(1, 0, 2));
    assertThrows(
        IllegalArgumentException.class,
        () -> LatinSquare.fromSequence(new int[] {0, 0, 1}, sequence));
  }

  @Test
  void rowPermutationCarriesOneRowToAnother() {
    LatinSquare square = cyclic(5);
    Permutation zeroToThree = square.rowPermutation(0, 3);
    assertArrayEquals(square.row(3), zeroToThree.apply(square.row(0)));
    assertEquals(zeroToThree, square.toSequence().prefix(3));
  }

  @Test
  void everyGapOfAValidSquareIsADerangement() {
    LatinSquare square = cyclic(6);
    for (int from = 0; from < 6; from++) {
      for (int to = from + 1; to < 6; to++) {
        assertTrue(
            square.rowPermutation(from, to).isDerangement(),
            "rows " + from + " and " + to + " must differ in every column");
      }
    }
  }

  @Test
  void recognisesReducedSquares() {
    int[][] reduced = {
      {0, 1, 2},
      {1, 2, 0},
      {2, 0, 1}
    };
    assertTrue(LatinSquare.fromGrid(reduced).isReduced());
    assertFalse(cyclic(4).isReduced(), "the cyclic square's first column counts down");
  }

  @Test
  void isImmutable() {
    int[][] grid = {
      {0, 1},
      {1, 0}
    };
    LatinSquare square = LatinSquare.fromGrid(grid);
    grid[0][0] = 1;
    assertEquals(0, square.at(0, 0), "mutating the source grid must not affect the square");

    square.toGrid()[0][0] = 1;
    square.row(0)[0] = 1;
    assertEquals(0, square.at(0, 0), "mutating an exported copy must not affect the square");
  }

  @Test
  void equalityIsByContent() {
    assertEquals(cyclic(4), cyclic(4));
    assertEquals(cyclic(4).hashCode(), cyclic(4).hashCode());
  }
}

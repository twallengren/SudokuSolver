package org.example.explore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import org.example.perm.LatinSquare;
import org.example.perm.PermutationSequence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LatinSquaresTest {

  /** Known totals L(n) for n = 1..5 (OEIS A002860). */
  @ParameterizedTest(name = "L({0}) = {1}")
  @CsvSource({"1, 1", "2, 2", "3, 12", "4, 576", "5, 161280"})
  void countsAllLatinSquares(int n, long expected) {
    assertEquals(expected, LatinSquares.countAll(n));
  }

  /** Known reduced counts R(n) for n = 1..5 (OEIS A000315). */
  @ParameterizedTest(name = "R({0}) = {1}")
  @CsvSource({"1, 1", "2, 1", "3, 1", "4, 4", "5, 56"})
  void countsReducedLatinSquares(int n, long expected) {
    assertEquals(expected, LatinSquares.countReduced(n));
    assertEquals(expected, LatinSquares.reduced(n).size());
  }

  @Test
  void totalIsFactorialTimesTheFixedFirstRowCount() {
    for (int n = 1; n <= 5; n++) {
      assertEquals(
          LatinSquares.countAll(n),
          Permutations.factorial(n) * LatinSquares.countWithNaturalFirstRow(n),
          "mismatch at n=" + n);
    }
  }

  @Test
  void classicIdentityRelatingTotalAndReducedCounts() {
    // L(n) = n! * (n-1)! * R(n)
    for (int n = 2; n <= 5; n++) {
      assertEquals(
          LatinSquares.countAll(n),
          Permutations.factorial(n) * Permutations.factorial(n - 1) * LatinSquares.countReduced(n),
          "mismatch at n=" + n);
    }
  }

  @Test
  void everyEnumeratedSquareIsValidAndDistinct() {
    List<LatinSquare> squares = LatinSquares.withNaturalFirstRow(5);
    assertEquals(1344, squares.size());
    assertTrue(squares.stream().allMatch(LatinSquare::isValid));
    assertEquals(squares.size(), new HashSet<>(squares).size(), "squares must be distinct");
  }

  @Test
  void everyEnumeratedSquareStartsWithTheNaturalRow() {
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(4)) {
      org.junit.jupiter.api.Assertions.assertArrayEquals(new int[] {0, 1, 2, 3}, square.row(0));
    }
  }

  @Test
  void everySequenceIsAChainOfDerangementsAndRebuildsItsSquare() {
    List<PermutationSequence> sequences = LatinSquares.sequences(5);
    assertEquals(1344, sequences.size());
    for (PermutationSequence sequence : sequences) {
      assertEquals(4, sequence.length());
      assertTrue(sequence.allStepsAreDerangements(), "each step must be a derangement");
      assertTrue(sequence.isLatin(), "every gap must be a derangement");
      assertTrue(LatinSquare.fromSequence(sequence).isValid());
    }
  }

  @Test
  void sequencesAreDistinctAndMatchTheSquares() {
    List<LatinSquare> squares = LatinSquares.withNaturalFirstRow(4);
    List<PermutationSequence> sequences = LatinSquares.sequences(4);
    assertEquals(squares.size(), sequences.size());
    assertEquals(sequences.size(), new HashSet<>(sequences).size());
    for (int i = 0; i < squares.size(); i++) {
      assertEquals(squares.get(i).toSequence(), sequences.get(i));
    }
  }

  @Test
  void reducedSquaresAreReduced() {
    for (LatinSquare square : LatinSquares.reduced(5)) {
      assertTrue(square.isReduced());
      assertTrue(square.isValid());
    }
  }

  @Test
  void orderOneIsTheTrivialSquare() {
    List<LatinSquare> squares = LatinSquares.withNaturalFirstRow(1);
    assertEquals(1, squares.size());
    assertEquals(0, squares.get(0).at(0, 0));
  }

  @Test
  void rejectsInvalidOrders() {
    assertThrows(IllegalArgumentException.class, () -> LatinSquares.withNaturalFirstRow(0));
    assertThrows(IllegalArgumentException.class, () -> LatinSquares.sequences(1));
  }
}

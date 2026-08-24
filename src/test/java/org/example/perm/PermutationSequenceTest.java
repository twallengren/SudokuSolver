package org.example.perm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PermutationSequenceTest {

  @Test
  void reportsLengthAndOrder() {
    PermutationSequence sequence =
        PermutationSequence.of(Permutation.of(1, 2, 0), Permutation.of(2, 0, 1));
    assertEquals(2, sequence.length());
    assertEquals(3, sequence.order());
  }

  @Test
  void rejectsMixedSizesAndEmptySequences() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PermutationSequence.of(Permutation.of(1, 0), Permutation.of(1, 2, 0)));
    assertThrows(IllegalArgumentException.class, PermutationSequence::of);
  }

  @Test
  void betweenComposesTheStepsInRange() {
    Permutation a = Permutation.of(1, 2, 0);
    Permutation b = Permutation.of(2, 0, 1);
    PermutationSequence sequence = PermutationSequence.of(a, b);

    assertEquals(a, sequence.between(0, 1));
    assertEquals(b, sequence.between(1, 2));
    assertEquals(a.andThen(b), sequence.between(0, 2));
    assertTrue(sequence.between(1, 1).isIdentity(), "an empty gap is the identity");
  }

  @Test
  void prefixIsTheGapFromRowZero() {
    PermutationSequence sequence =
        PermutationSequence.of(Permutation.of(1, 2, 0), Permutation.of(2, 0, 1));
    assertEquals(sequence.between(0, 2), sequence.prefix(2));
  }

  @Test
  void rejectsOutOfRangeGaps() {
    PermutationSequence sequence = PermutationSequence.of(Permutation.of(1, 0));
    assertThrows(IndexOutOfBoundsException.class, () -> sequence.between(0, 5));
    assertThrows(IndexOutOfBoundsException.class, () -> sequence.between(-1, 1));
    assertThrows(IndexOutOfBoundsException.class, () -> sequence.between(1, 0));
  }

  @Test
  void isLatinAgreesWithTheBuiltSquare() {
    Permutation shift = Permutation.of(1, 2, 3, 0);
    PermutationSequence good = PermutationSequence.of(shift, shift, shift);
    assertTrue(good.isLatin());
    assertTrue(LatinSquare.fromSequence(good).isValid());
    assertTrue(good.nonDerangementGaps().isEmpty());

    Permutation swap = Permutation.of(1, 0, 3, 2);
    PermutationSequence bad = PermutationSequence.of(swap, swap, swap);
    assertFalse(bad.isLatin());
    assertFalse(LatinSquare.fromSequence(bad).isValid());
  }

  @Test
  void identityStepIsNeverLatin() {
    PermutationSequence sequence =
        PermutationSequence.of(Permutation.identity(3), Permutation.of(1, 2, 0));
    assertFalse(sequence.allStepsAreDerangements());
    assertFalse(sequence.isLatin());
  }

  @Test
  void equalityIsByContent() {
    PermutationSequence a = PermutationSequence.of(Permutation.of(1, 2, 0));
    PermutationSequence b = PermutationSequence.of(Permutation.of(1, 2, 0));
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
  }
}

package org.example.perm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SymmetryTest {

  private static LatinSquare cyclic(int n) {
    int[] shift = new int[n];
    for (int i = 0; i < n; i++) {
      shift[i] = (i + 1) % n;
    }
    Permutation[] steps = new Permutation[n - 1];
    Arrays.fill(steps, Permutation.of(shift));
    return LatinSquare.fromSequence(PermutationSequence.of(steps));
  }

  private static LatinSquare sample() {
    return LatinSquare.fromGrid(
        new int[][] {
          {0, 1, 2, 3},
          {1, 0, 3, 2},
          {2, 3, 1, 0},
          {3, 2, 0, 1}
        });
  }

  @Test
  void everyOperationPreservesTheLatinProperty() {
    LatinSquare square = sample();
    assertTrue(square.isValid(), "precondition");

    assertTrue(Symmetry.transpose(square).isValid());
    assertTrue(Symmetry.rotateClockwise(square).isValid());
    assertTrue(Symmetry.rotateCounterclockwise(square).isValid());
    assertTrue(Symmetry.reflectHorizontally(square).isValid());
    assertTrue(Symmetry.reflectVertically(square).isValid());
    assertTrue(Symmetry.swapRows(square, 0, 2).isValid());
    assertTrue(Symmetry.swapColumns(square, 1, 3).isValid());
    assertTrue(Symmetry.relabelSymbols(square, Permutation.of(1, 2, 3, 0)).isValid());
  }

  @Test
  void transposeIsAnInvolution() {
    LatinSquare square = sample();
    assertEquals(square, Symmetry.transpose(Symmetry.transpose(square)));
  }

  @Test
  void reflectionsAreInvolutions() {
    LatinSquare square = sample();
    assertEquals(square, Symmetry.reflectHorizontally(Symmetry.reflectHorizontally(square)));
    assertEquals(square, Symmetry.reflectVertically(Symmetry.reflectVertically(square)));
  }

  @Test
  void fourQuarterTurnsReturnToTheStart() {
    LatinSquare square = sample();
    LatinSquare rotated = square;
    for (int turn = 0; turn < 4; turn++) {
      rotated = Symmetry.rotateClockwise(rotated);
    }
    assertEquals(square, rotated);
  }

  @Test
  void rotationsAreInverseToEachOther() {
    LatinSquare square = sample();
    assertEquals(square, Symmetry.rotateCounterclockwise(Symmetry.rotateClockwise(square)));
  }

  @Test
  void rotatingClockwiseIsTransposeThenHorizontalReflection() {
    LatinSquare square = sample();
    assertEquals(
        Symmetry.rotateClockwise(square),
        Symmetry.reflectHorizontally(Symmetry.transpose(square)));
  }

  @Test
  void swappingTheSameRowsTwiceIsIdentity() {
    LatinSquare square = sample();
    assertEquals(square, Symmetry.swapRows(Symmetry.swapRows(square, 0, 3), 0, 3));
    assertEquals(square, Symmetry.swapColumns(Symmetry.swapColumns(square, 1, 2), 1, 2));
  }

  @Test
  void permuteRowsSendsRowToItsImage() {
    LatinSquare square = sample();
    Permutation p = Permutation.of(1, 2, 3, 0);
    LatinSquare moved = Symmetry.permuteRows(square, p);
    for (int row = 0; row < square.order(); row++) {
      org.junit.jupiter.api.Assertions.assertArrayEquals(
          square.row(row), moved.row(p.imageOf(row)));
    }
  }

  @Test
  void relabellingByIdentityChangesNothing() {
    LatinSquare square = sample();
    assertEquals(square, Symmetry.relabelSymbols(square, Permutation.identity(4)));
  }

  @Test
  void relabellingComposesLikeTheUnderlyingPermutations() {
    LatinSquare square = sample();
    Permutation p = Permutation.of(1, 2, 3, 0);
    Permutation q = Permutation.of(2, 3, 0, 1);
    assertEquals(
        Symmetry.relabelSymbols(Symmetry.relabelSymbols(square, p), q),
        Symmetry.relabelSymbols(square, p.andThen(q)));
  }

  @Test
  void reducedFormHasNaturalFirstRowAndColumn() {
    LatinSquare reduced = Symmetry.toReduced(cyclic(5));
    assertTrue(reduced.isReduced());
    assertTrue(reduced.isValid());
  }

  @Test
  void reducingIsIdempotent() {
    LatinSquare reduced = Symmetry.toReduced(sample());
    assertEquals(reduced, Symmetry.toReduced(reduced));
  }

  @Test
  void relabellingAndRowSwapsShareAReducedForm() {
    LatinSquare square = sample();
    LatinSquare disguised =
        Symmetry.swapRows(Symmetry.relabelSymbols(square, Permutation.of(2, 3, 0, 1)), 1, 3);
    assertNotEquals(square, disguised);
    assertEquals(
        Symmetry.toReduced(square),
        Symmetry.toReduced(disguised),
        "relabelling and reordering rows must not change the reduced representative");
  }

  @Test
  void reducingRejectsNonLatinSquares() {
    LatinSquare invalid =
        LatinSquare.fromGrid(
            new int[][] {
              {0, 1, 2},
              {1, 2, 0},
              {2, 1, 0}
            });
    assertThrows(IllegalArgumentException.class, () -> Symmetry.toReduced(invalid));
  }

  @Test
  void rejectsPermutationsOfTheWrongSize() {
    LatinSquare square = sample();
    Permutation tooSmall = Permutation.of(1, 0);
    assertThrows(IllegalArgumentException.class, () -> Symmetry.permuteRows(square, tooSmall));
    assertThrows(IllegalArgumentException.class, () -> Symmetry.relabelSymbols(square, tooSmall));
    assertThrows(IndexOutOfBoundsException.class, () -> Symmetry.swapRows(square, 0, 9));
  }
}

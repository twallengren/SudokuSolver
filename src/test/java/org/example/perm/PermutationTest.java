package org.example.perm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PermutationTest {

  @Test
  void rejectsMalformedMappings() {
    assertThrows(IllegalArgumentException.class, () -> Permutation.of(0, 0, 2));
    assertThrows(IllegalArgumentException.class, () -> Permutation.of(0, 1, 5));
    assertThrows(IllegalArgumentException.class, () -> Permutation.of(-1, 0));
    assertThrows(IllegalArgumentException.class, Permutation::of);
  }

  @Test
  void isImmutable() {
    int[] source = {1, 0, 2};
    Permutation p = Permutation.of(source);
    source[0] = 99;
    assertEquals(1, p.imageOf(0), "mutating the source array must not affect the permutation");

    p.toArray()[0] = 99;
    assertEquals(1, p.imageOf(0), "mutating the exported array must not affect the permutation");
  }

  @Test
  void applyMovesEntryToItsImageIndex() {
    // 0 -> 2, so the entry at index 0 lands at index 2.
    Permutation p = Permutation.of(2, 0, 1);
    assertArrayEquals(new int[] {20, 30, 10}, p.apply(new int[] {10, 20, 30}));
  }

  @Test
  void applyRejectsMismatchedRowLength() {
    Permutation p = Permutation.of(2, 0, 1);
    assertThrows(IllegalArgumentException.class, () -> p.apply(new int[] {1, 2}));
  }

  @Test
  void andThenMatchesSequentialApplication() {
    Permutation p = Permutation.of(2, 0, 1);
    Permutation q = Permutation.of(1, 2, 0);
    int[] row = {10, 20, 30};

    assertArrayEquals(
        q.apply(p.apply(row)),
        p.andThen(q).apply(row),
        "applying p then q must equal applying p.andThen(q)");
  }

  @Test
  void composeIsAndThenReversed() {
    Permutation p = Permutation.of(2, 0, 1);
    Permutation q = Permutation.of(1, 2, 0);
    assertEquals(p.andThen(q), q.compose(p));
  }

  @Test
  void inverseUndoesThePermutation() {
    Permutation p = Permutation.of(3, 0, 4, 2, 1);
    assertTrue(p.andThen(p.inverse()).isIdentity());
    assertTrue(p.inverse().andThen(p).isIdentity());
    assertEquals(p, p.inverse().inverse());
  }

  @Test
  void identityIsNeutral() {
    Permutation p = Permutation.of(3, 0, 4, 2, 1);
    Permutation e = Permutation.identity(5);
    assertEquals(p, p.andThen(e));
    assertEquals(p, e.andThen(p));
    assertTrue(e.isIdentity());
    assertFalse(e.isDerangement());
  }

  @Test
  void detectsDerangements() {
    assertTrue(Permutation.of(1, 0, 3, 2).isDerangement());
    assertFalse(Permutation.of(1, 0, 2, 3).isDerangement());
    assertEquals(2, Permutation.of(1, 0, 2, 3).fixedPoints());
  }

  @Test
  void decomposesIntoCycles() {
    // 0 -> 4 -> 1 -> 2 -> 3 -> 0 is a single 5-cycle.
    Permutation p = Permutation.of(4, 2, 3, 0, 1);
    assertEquals(1, p.cycles().size());
    assertArrayEquals(new int[] {0, 4, 1, 2, 3}, p.cycles().get(0));
    assertEquals("(0 4 1 2 3)", p.toCycleNotation());
  }

  @Test
  void cycleNotationOmitsFixedPointsAndRendersIdentity() {
    assertEquals("()", Permutation.identity(4).toCycleNotation());
    assertEquals("(0 1)", Permutation.of(1, 0, 2, 3).toCycleNotation());
    assertEquals("(0 1)(2 3)", Permutation.of(1, 0, 3, 2).toCycleNotation());
  }

  @Test
  void computesOrder() {
    assertEquals(1, Permutation.identity(5).order());
    assertEquals(2, Permutation.of(1, 0, 3, 2).order());
    assertEquals(5, Permutation.of(4, 2, 3, 0, 1).order());
    // A 2-cycle and a 3-cycle: order lcm(2,3) = 6.
    assertEquals(6, Permutation.fromCycles(5, new int[] {0, 1}, new int[] {2, 3, 4}).order());
  }

  @Test
  void raisingToItsOrderGivesIdentity() {
    Permutation p = Permutation.fromCycles(5, new int[] {0, 1}, new int[] {2, 3, 4});
    assertTrue(p.power(p.order()).isIdentity());
    assertEquals(p, p.power(1));
    assertEquals(p.inverse(), p.power(-1));
    assertTrue(p.power(0).isIdentity());
  }

  @Test
  void computesSign() {
    assertEquals(1, Permutation.identity(4).sign());
    assertEquals(-1, Permutation.of(1, 0, 2, 3).sign(), "a transposition is odd");
    assertEquals(1, Permutation.of(1, 0, 3, 2).sign(), "two transpositions are even");
    assertEquals(1, Permutation.of(4, 2, 3, 0, 1).sign(), "a 5-cycle is even");
  }

  @Test
  void buildsFromCycles() {
    assertEquals(
        Permutation.of(4, 2, 3, 0, 1), Permutation.fromCycles(5, new int[] {0, 4, 1, 2, 3}));
    assertTrue(Permutation.fromCycles(3).isIdentity());
    assertThrows(
        IllegalArgumentException.class,
        () -> Permutation.fromCycles(3, new int[] {0, 1}, new int[] {1, 2}));
    assertThrows(IllegalArgumentException.class, () -> Permutation.fromCycles(3, new int[] {0, 7}));
  }

  @Test
  void rejectsSizeMismatchOnComposition() {
    Permutation p = Permutation.of(1, 0);
    Permutation q = Permutation.of(2, 0, 1);
    assertThrows(IllegalArgumentException.class, () -> p.andThen(q));
  }

  @Test
  void equalityIsByContent() {
    assertEquals(Permutation.of(1, 0, 2), Permutation.of(1, 0, 2));
    assertEquals(Permutation.of(1, 0, 2).hashCode(), Permutation.of(1, 0, 2).hashCode());
    assertNotEquals(Permutation.of(1, 0, 2), Permutation.of(2, 0, 1));
  }

  @Test
  void oneLineNotationRoundTrips() {
    Permutation p = Permutation.of(4, 2, 3, 0, 1);
    assertEquals("4,2,3,0,1", p.toString());
  }
}

package org.example.perm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Wrap;
import org.junit.jupiter.api.Test;

class DerivativeTest {

  private static LatinSquare cyclic(int n) {
    int[] shift = new int[n];
    for (int i = 0; i < n; i++) {
      shift[i] = (i + 1) % n;
    }
    List<Permutation> steps = new ArrayList<>();
    for (int i = 0; i < n - 1; i++) {
      steps.add(Permutation.of(shift));
    }
    return LatinSquare.fromSequence(PermutationSequence.of(steps));
  }

  /** Every Latin square of order 4, all 576 of them. */
  private static List<LatinSquare> allOrderFour() {
    List<LatinSquare> all = new ArrayList<>();
    for (LatinSquare square : org.example.explore.LatinSquares.withNaturalFirstRow(4)) {
      for (Permutation relabel : org.example.explore.Permutations.all(4)) {
        all.add(Symmetry.relabelSymbols(square, relabel));
      }
    }
    return all;
  }

  @Test
  void afterDifferenceIsTheStepFromAToB() {
    Permutation a = Permutation.of(1, 2, 3, 0);
    Permutation b = Permutation.of(2, 0, 3, 1);
    Permutation q = Derivative.difference(a, b, Quotient.AFTER);
    assertEquals(b, a.andThen(q));
  }

  @Test
  void beforeDifferenceIsTheStepPrecedingA() {
    Permutation a = Permutation.of(1, 2, 3, 0);
    Permutation b = Permutation.of(2, 0, 3, 1);
    Permutation q = Derivative.difference(a, b, Quotient.BEFORE);
    assertEquals(b, q.andThen(a));
  }

  @Test
  void differenceOfEqualTermsIsTheIdentity() {
    Permutation p = Permutation.of(1, 2, 3, 0);
    assertTrue(Derivative.difference(p, p, Quotient.AFTER).isIdentity());
    assertTrue(Derivative.difference(p, p, Quotient.BEFORE).isIdentity());
  }

  @Test
  void cyclicPreservesLengthAndLinearShortens() {
    PermutationSequence sequence =
        PermutationSequence.of(
            Permutation.of(1, 2, 0), Permutation.of(2, 0, 1), Permutation.identity(3));
    assertEquals(3, Derivative.apply(sequence, Wrap.CYCLIC, Quotient.AFTER).length());
    assertEquals(2, Derivative.apply(sequence, Wrap.LINEAR, Quotient.AFTER).length());
  }

  @Test
  void aConstantSequenceCollapsesToTheIdentityInOneStep() {
    Permutation p = Permutation.of(1, 2, 3, 0);
    PermutationSequence constant = PermutationSequence.of(p, p, p, p);
    assertTrue(
        Derivative.isConstantIdentity(Derivative.apply(constant, Wrap.CYCLIC, Quotient.AFTER)));
    assertEquals(1, Derivative.order(constant, Wrap.CYCLIC, Quotient.AFTER).orElseThrow());
  }

  @Test
  void anAlreadyIdentitySequenceHasOrderZero() {
    PermutationSequence identities =
        PermutationSequence.of(Permutation.identity(4), Permutation.identity(4));
    assertEquals(0, Derivative.order(identities, Wrap.CYCLIC, Quotient.AFTER).orElseThrow());
  }

  @Test
  void theCyclicSquareOfOrderFourHasOrderTwo() {
    PermutationSequence rows = cyclic(4).rowsAsSequence();
    assertEquals(2, Derivative.order(rows, Wrap.CYCLIC, Quotient.AFTER).orElseThrow());

    // Step one is constant, step two is the identity.
    PermutationSequence first = Derivative.apply(rows, Wrap.CYCLIC, Quotient.AFTER);
    assertEquals(1, first.steps().stream().distinct().count(), "first derivative is constant");
    assertTrue(Derivative.isConstantIdentity(Derivative.apply(first, Wrap.CYCLIC, Quotient.AFTER)));
  }

  @Test
  void everyLatinSquareOfOrderFourConvergesFromItsRows() {
    List<LatinSquare> squares = allOrderFour();
    assertEquals(576, squares.size());
    for (LatinSquare square : squares) {
      assertTrue(
          Derivative.order(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER).isPresent(),
          "expected convergence for\n" + square);
    }
  }

  @Test
  void theTwoQuotientConventionsGiveTheSameOrder() {
    for (LatinSquare square : allOrderFour()) {
      PermutationSequence rows = square.rowsAsSequence();
      assertEquals(
          Derivative.order(rows, Wrap.CYCLIC, Quotient.AFTER),
          Derivative.order(rows, Wrap.CYCLIC, Quotient.BEFORE),
          "conventions disagree for\n" + square);
    }
  }

  @Test
  void orderFiveDoesNotAlwaysConverge() {
    // The universal convergence seen at order 4 is not a general fact.
    long converging =
        org.example.explore.LatinSquares.withNaturalFirstRow(5).stream()
            .filter(
                s -> Derivative.order(s.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER).isPresent())
            .count();
    assertEquals(144, converging, "144 of the 1344 squares with the natural first row converge");
  }

  @Test
  void trajectoryEndsAtTheConstantIdentityWhenItConverges() {
    List<PermutationSequence> trajectory =
        Derivative.trajectory(cyclic(4).rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER);
    assertEquals(3, trajectory.size(), "D^0, D^1, D^2");
    assertTrue(Derivative.isConstantIdentity(trajectory.get(trajectory.size() - 1)));
  }

  @Test
  void theBoundIsRespected() {
    PermutationSequence rows = cyclic(4).rowsAsSequence();
    assertEquals(OptionalInt.empty(), Derivative.order(rows, Wrap.CYCLIC, Quotient.AFTER, 1));
    assertEquals(2, Derivative.order(rows, Wrap.CYCLIC, Quotient.AFTER, 2).orElseThrow());
    assertThrows(
        IllegalArgumentException.class,
        () -> Derivative.order(rows, Wrap.CYCLIC, Quotient.AFTER, -1));
  }

  @Test
  void linearIterationCannotEmptyASequence() {
    PermutationSequence single = PermutationSequence.of(Permutation.of(1, 0));
    assertThrows(
        IllegalArgumentException.class,
        () -> Derivative.apply(single, Wrap.LINEAR, Quotient.AFTER));
    assertFalse(Derivative.order(single, Wrap.LINEAR, Quotient.AFTER).isPresent());
  }
}

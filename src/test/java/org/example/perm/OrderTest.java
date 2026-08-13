package org.example.perm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.example.explore.LatinSquares;
import org.example.explore.Permutations;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Spectrum;
import org.example.perm.Derivative.Wrap;
import org.junit.jupiter.api.Test;

/**
 * The D-order behaves like polynomial degree: a square has order k exactly when its (k-1)-th
 * derivative is constant, differentiating drops the order by one, and integrating raises it by one.
 */
class OrderTest {

  private static final int LIMIT = 50_000;

  private static Spectrum shape(PermutationSequence s) {
    return Derivative.spectrum(s, Wrap.CYCLIC, Quotient.AFTER, LIMIT).orElseThrow();
  }

  private static boolean isConstant(PermutationSequence s) {
    return s.steps().stream().distinct().count() == 1;
  }

  private static PermutationSequence differentiate(PermutationSequence s, int times) {
    PermutationSequence state = s;
    for (int i = 0; i < times; i++) {
      state = Derivative.apply(state, Wrap.CYCLIC, Quotient.AFTER);
    }
    return state;
  }

  @Test
  void orderKMeansTheKMinusOnethDerivativeIsConstant() {
    for (int n : new int[] {4, 5}) {
      for (LatinSquare square : LatinSquares.withNaturalFirstRow(n)) {
        Spectrum shape = shape(square.rowsAsSequence());
        if (!shape.reachesIdentity()) {
          continue;
        }
        PermutationSequence penultimate = differentiate(square.rowsAsSequence(), shape.tail() - 1);
        assertTrue(isConstant(penultimate), "the (k-1)-th derivative must be constant");
        assertFalse(
            Derivative.isConstantIdentity(penultimate), "and must not already be the identity");
      }
    }
  }

  @Test
  void differentiatingDropsTheOrderByExactlyOne() {
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5)) {
      Spectrum shape = shape(square.rowsAsSequence());
      if (!shape.reachesIdentity() || shape.tail() < 2) {
        continue;
      }
      PermutationSequence derivative =
          Derivative.apply(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER);
      assertEquals(shape.tail() - 1, shape(derivative).tail());
    }
  }

  @Test
  void noLatinSquareHasOrderOne() {
    // Order 1 would mean the sequence itself is constant, i.e. every row identical.
    for (int n : new int[] {3, 4, 5}) {
      for (LatinSquare square : LatinSquares.withNaturalFirstRow(n)) {
        Spectrum shape = shape(square.rowsAsSequence());
        assertTrue(!shape.reachesIdentity() || shape.tail() >= 2);
      }
    }
  }

  @Test
  void everyPowerSquareHasOrderTwo() {
    // The cheap direction, checked up to order 7: (n-1)! squares, one per n-cycle.
    for (int n = 3; n <= 7; n++) {
      int built = 0;
      for (Permutation g : Permutations.all(n)) {
        // It must be a single n-cycle, not merely an element of order n: at n = 6 the permutation
        // (0 1 2)(3 4) has order 6 but fixes a point, so its powers repeat within a column.
        if (g.cycles().size() != 1) {
          continue;
        }
        List<Permutation> rows = new ArrayList<>();
        Permutation power = Permutation.identity(n);
        for (int i = 0; i < n; i++) {
          rows.add(power);
          power = power.andThen(g);
        }
        Spectrum shape = shape(PermutationSequence.of(rows));
        assertTrue(
            shape.reachesIdentity() && shape.tail() == 2, "powers of an n-cycle give order 2");
        built++;
      }
      assertEquals(Permutations.factorial(n - 1), built, "there are (n-1)! n-cycles");
    }
  }

  /**
   * The converse, that nothing else has order 2, is checked exhaustively only to order 5 here;
   * OrderStructure confirms it at order 6 too, but scanning 1,128,960 squares is too slow for a
   * test.
   */
  @Test
  void orderTwoSquaresAreExactlyThePowersOfOnePermutation() {
    for (int n : new int[] {3, 4, 5}) {
      int orderTwo = 0;
      for (LatinSquare square : LatinSquares.withNaturalFirstRow(n)) {
        List<Permutation> rows = square.rowsAsSequence().steps();
        Spectrum shape = shape(square.rowsAsSequence());
        boolean isOrderTwo = shape.reachesIdentity() && shape.tail() == 2;

        Permutation g = rows.get(1);
        Permutation power = Permutation.identity(n);
        boolean isGeometric = true;
        for (Permutation row : rows) {
          if (!power.equals(row)) {
            isGeometric = false;
            break;
          }
          power = power.andThen(g);
        }
        assertEquals(isGeometric, isOrderTwo, "order 2 and geometric must coincide at n=" + n);
        if (isOrderTwo) {
          orderTwo++;
        }
      }
      assertEquals(
          Permutations.factorial(n - 1),
          orderTwo,
          "there should be (n-1)! order-2 squares at n=" + n);
    }
  }

  @Test
  void integrationInvertsDifferentiation() {
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5)) {
      PermutationSequence s = square.rowsAsSequence();
      PermutationSequence derivative = Derivative.apply(s, Wrap.CYCLIC, Quotient.AFTER);
      Optional<PermutationSequence> recovered = Derivative.integrate(derivative, s.step(0));
      assertTrue(recovered.isPresent(), "a derivative always integrates back");
      assertEquals(s, recovered.get());
    }
  }

  @Test
  void integrationExistsExactlyWhenTheTermsMultiplyToTheIdentity() {
    Permutation g = Permutation.of(1, 2, 3, 4, 0);
    List<Permutation> constant = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      constant.add(g);
    }
    // g^5 is the identity, so this closes up.
    assertTrue(
        Derivative.integrate(PermutationSequence.of(constant), Permutation.identity(5))
            .isPresent());

    // Change one term so the product is no longer the identity.
    constant.set(0, Permutation.of(1, 0, 2, 3, 4));
    assertTrue(
        Derivative.integrate(PermutationSequence.of(constant), Permutation.identity(5)).isEmpty());
  }

  @Test
  void integratingRaisesTheOrderByOne() {
    Permutation g = Permutation.of(1, 2, 3, 4, 0);
    List<Permutation> constant = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      constant.add(g);
    }
    PermutationSequence level = PermutationSequence.of(constant);
    assertEquals(1, shape(level).tail(), "a constant sequence has order 1");

    for (int expected = 2; expected <= 4; expected++) {
      level = Derivative.integrate(level, Permutation.identity(5)).orElseThrow();
      assertEquals(expected, shape(level).tail(), "each integration adds one to the order");
    }
  }

  @Test
  void theFirstIntegralOfAConstantIsTheCyclicSquare() {
    Permutation g = Permutation.of(1, 2, 3, 4, 0);
    List<Permutation> constant = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      constant.add(g);
    }
    PermutationSequence integrated =
        Derivative.integrate(PermutationSequence.of(constant), Permutation.identity(5))
            .orElseThrow();
    int[][] grid = new int[5][];
    for (int i = 0; i < 5; i++) {
      grid[i] = integrated.step(i).toArray();
    }
    assertTrue(LatinSquare.fromGrid(grid).isValid(), "and it is a Latin square");
  }
}

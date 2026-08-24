package org.example.explore;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Spectrum;
import org.example.perm.Derivative.Wrap;
import org.example.perm.LatinSquare;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;
import org.junit.jupiter.api.Test;

/** Pins the structural explanation of convergence versus cycling. */
class ConvergenceWhyTest {

  private static final int LIMIT = 5_000;

  private static int[] signVector(PermutationSequence s) {
    int[] v = new int[s.length()];
    for (int i = 0; i < s.length(); i++) {
      v[i] = s.step(i).sign() > 0 ? 0 : 1;
    }
    return v;
  }

  private static Spectrum shape(PermutationSequence s) {
    return Derivative.spectrum(s, Wrap.CYCLIC, Quotient.AFTER, LIMIT).orElseThrow();
  }

  @Test
  void signCommutesWithD() {
    // The sign map is a homomorphism, so the parity vector of D(s) is the binary Ducci step of the
    // parity vector of s. This is what lets classical Ducci theory constrain the permutation orbit.
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5).subList(0, 200)) {
      PermutationSequence s = square.rowsAsSequence();
      assertArrayEquals(
          ConvergenceWhy.ducciStep(signVector(s), 2),
          signVector(Derivative.apply(s, Wrap.CYCLIC, Quotient.AFTER)));
    }
  }

  @Test
  void onlyTwoParityVectorsOfLengthFiveCanDie() {
    int converging = 0;
    for (int mask = 0; mask < 32; mask++) {
      int[] v = new int[5];
      for (int i = 0; i < 5; i++) {
        v[i] = (mask >> i) & 1;
      }
      if (ConvergenceWhy.ducciConverges(v, 2)) {
        converging++;
        assertTrue(mask == 0 || mask == 31, "only all-even and all-odd survive");
      }
    }
    assertEquals(2, converging);
  }

  @Test
  void atOrderFiveConvergenceIsExactlyAllRowsEven() {
    // The sign obstruction is not merely necessary at order 5 — it is the whole story.
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5)) {
      PermutationSequence rows = square.rowsAsSequence();
      boolean allEven = Arrays.stream(signVector(rows)).allMatch(x -> x == 0);
      assertEquals(
          allEven, shape(rows).reachesIdentity(), "parity must decide convergence for\n" + square);
    }
  }

  @Test
  void atOrderFiveTheDifferenceGroupIsAllOrNothing() {
    // Convergent squares: differences generate Z_5. Cycling squares: differences generate all of
    // S_5. Nothing in between occurs.
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5)) {
      PermutationSequence rows = square.rowsAsSequence();
      Set<Permutation> diffGroup =
          ConvergenceWhy.generate(Derivative.apply(rows, Wrap.CYCLIC, Quotient.AFTER).steps(), 200);
      assertEquals(shape(rows).reachesIdentity() ? 5 : 120, diffGroup.size());
    }
  }

  @Test
  void cyclingOrderFiveSquaresHaveSignPeriodFifteenAndFullPeriodThirty() {
    // The classical binary Ducci period at length 5 is 15; the permutation orbit doubles it.
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5)) {
      PermutationSequence rows = square.rowsAsSequence();
      Spectrum spectrum = shape(rows);
      if (spectrum.reachesIdentity()) {
        continue;
      }
      assertEquals(15, ConvergenceWhy.ducciPeriod(signVector(rows), 2));
      assertEquals(30, spectrum.period());
    }
  }

  @Test
  void orderFourDifferencesAlwaysGenerateAnAbelianGroupOfOrderFour() {
    // With the sequence length 4 = 2^2 and the differences confined to an abelian 2-group, the
    // classical linear theory forces convergence — a complete explanation of order 4.
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(4)) {
      for (Permutation relabel : Permutations.all(4)) {
        LatinSquare relabelled = org.example.perm.Symmetry.relabelSymbols(square, relabel);
        Set<Permutation> diffGroup =
            ConvergenceWhy.generate(
                Derivative.apply(relabelled.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER).steps(),
                60);
        assertEquals(4, diffGroup.size());
        assertTrue(ConvergenceWhy.isAbelian(diffGroup));
      }
    }
  }

  @Test
  void componentwiseLinearAlgebraPredictsEveryZSixOrdering() {
    int n = 6;
    int[] shift = new int[n];
    for (int i = 0; i < n; i++) {
      shift[i] = (i + 1) % n;
    }
    Permutation g = Permutation.of(shift);
    List<Permutation> powers = new ArrayList<>();
    Permutation p = Permutation.identity(n);
    for (int i = 0; i < n; i++) {
      powers.add(p);
      p = p.andThen(g);
    }
    int convergent = 0;
    for (Permutation ordering : Permutations.all(n)) {
      int[] e = ordering.toArray();
      List<Permutation> rows = new ArrayList<>();
      int[] mod2 = new int[n];
      int[] mod3 = new int[n];
      for (int i = 0; i < n; i++) {
        rows.add(powers.get(e[i]));
        mod2[i] = e[i] % 2;
        mod3[i] = e[i] % 3;
      }
      boolean predicted =
          ConvergenceWhy.ducciConverges(mod2, 2) && ConvergenceWhy.ducciConverges(mod3, 3);
      boolean actual = shape(PermutationSequence.of(rows)).reachesIdentity();
      assertEquals(predicted, actual, "prediction must match for exponent order " + ordering);
      if (actual) {
        convergent++;
      }
    }
    assertEquals(12, convergent, "exactly 12 of the 720 orderings converge");
  }

  @Test
  void everySequenceOverAPGroupOfMatchingLengthConverges() {
    // Z_3, length 3: all 27.
    List<Permutation> z3 =
        List.copyOf(ConvergenceWhy.generate(List.of(Permutation.of(1, 2, 0)), 10));
    for (Permutation a : z3) {
      for (Permutation b : z3) {
        for (Permutation c : z3) {
          assertTrue(
              Derivative.order(PermutationSequence.of(a, b, c), Wrap.CYCLIC, Quotient.AFTER, LIMIT)
                  .isPresent());
        }
      }
    }
  }

  @Test
  void theSignObstructionKillsASequenceOutright() {
    // Only four length-6 parity vectors can die: 000000, 111111, 010101, 101010. A single odd term
    // among evens gives 100000, which cycles forever mod 2 — so the permutation sequence can never
    // die either, whatever its terms are.
    Permutation r = Permutation.of(1, 2, 0); // even
    Permutation t = Permutation.of(1, 0, 2); // odd
    assertFalse(ConvergenceWhy.ducciConverges(new int[] {1, 0, 0, 0, 0, 0}, 2));
    PermutationSequence s = PermutationSequence.of(t, r, r, r, r, r);
    assertFalse(Derivative.order(s, Wrap.CYCLIC, Quotient.AFTER, LIMIT).isPresent());

    // Whereas the alternating pattern 010101 is one of the four survivors, and the corresponding
    // permutation sequence really does die — the obstruction is exact here, in both directions.
    assertTrue(ConvergenceWhy.ducciConverges(new int[] {0, 1, 0, 1, 0, 1}, 2));
    assertTrue(
        Derivative.order(
                PermutationSequence.of(r, t, r, t, r, t), Wrap.CYCLIC, Quotient.AFTER, LIMIT)
            .isPresent());
  }
}

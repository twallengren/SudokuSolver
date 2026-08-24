package org.example.explore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Spectrum;
import org.example.perm.Derivative.Wrap;
import org.example.perm.LatinSquare;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;
import org.junit.jupiter.api.Test;

/** The structure shared by squares in the same period class. */
class PeriodClassesTest {

  private static final int LIMIT = 50_000;

  @Test
  void parityPeriodDividesFullPeriodOnASample() {
    // The sign map is a homomorphism, so the parity cycle is a quotient of the full cycle.
    int checked = 0;
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(6)) {
      if (++checked > 400) {
        break;
      }
      PermutationSequence rows = square.rowsAsSequence();
      Spectrum spectrum =
          Derivative.spectrum(rows, Wrap.CYCLIC, Quotient.AFTER, LIMIT).orElseThrow();
      int[] signs = new int[6];
      for (int i = 0; i < 6; i++) {
        signs[i] = rows.step(i).sign() > 0 ? 0 : 1;
      }
      int parityPeriod = ConvergenceWhy.ducciPeriod(signs, 2);
      int fullPeriod = spectrum.reachesIdentity() ? 1 : spectrum.period();
      assertEquals(0, fullPeriod % parityPeriod, "parity period must divide the full period");
    }
  }

  @Test
  void zSixPeriodIsExactlyTheLcmOfThePerPrimePeriods() {
    // Within the abelian family the cycle-length classification is complete: the period of every
    // one of the 720 orderings equals lcm(mod-2 period, mod-3 period) of the exponent vector.
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
    Map<Integer, Integer> histogram = new TreeMap<>();
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
      Spectrum spectrum =
          Derivative.spectrum(PermutationSequence.of(rows), Wrap.CYCLIC, Quotient.AFTER, LIMIT)
              .orElseThrow();
      int expected = lcm(ConvergenceWhy.ducciPeriod(mod2, 2), ConvergenceWhy.ducciPeriod(mod3, 3));
      assertEquals(expected, spectrum.period(), "abelian period must be the lcm of components");
      histogram.merge(spectrum.period(), 1, Integer::sum);
    }
    assertEquals(Map.of(1, 36, 3, 252, 6, 432), histogram);
  }

  @Test
  void onceInsideASubgroupTheOrbitStaysThere() {
    // Differences of elements of G lie in G, so the orbit after step one is a Ducci orbit over the
    // difference group — the reason the difference group organises the period classes.
    LatinSquare square = LatinSquares.withNaturalFirstRow(6).get(17);
    PermutationSequence state =
        Derivative.apply(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER);
    var group = ConvergenceWhy.generate(state.steps(), 800);
    for (int step = 0; step < 50; step++) {
      state = Derivative.apply(state, Wrap.CYCLIC, Quotient.AFTER);
      assertTrue(group.containsAll(state.steps()), "orbit must remain inside the difference group");
    }
  }

  private static int lcm(int a, int b) {
    return a / gcd(a, b) * b;
  }

  private static int gcd(int a, int b) {
    return b == 0 ? a : gcd(b, a % b);
  }
}

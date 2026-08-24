package org.example.perm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.example.explore.LatinSquares;
import org.example.explore.Permutations;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Spectrum;
import org.example.perm.Derivative.Wrap;
import org.junit.jupiter.api.Test;

/** Pins the structural findings about the shape of D-orbits. */
class SpectrumTest {

  private static Spectrum spectrumOf(LatinSquare square) {
    return Derivative.spectrum(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, 20_000)
        .orElseThrow();
  }

  private static List<LatinSquare> allOrderFour() {
    List<LatinSquare> all = new ArrayList<>();
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(4)) {
      for (Permutation relabel : Permutations.all(4)) {
        all.add(Symmetry.relabelSymbols(square, relabel));
      }
    }
    return all;
  }

  /** Whether the rows, as permutations, are closed under composition. */
  private static boolean rowsFormAGroup(LatinSquare square) {
    Set<Permutation> rows = new HashSet<>(square.rowsAsSequence().steps());
    for (Permutation a : rows) {
      for (Permutation b : rows) {
        if (!rows.contains(a.andThen(b))) {
          return false;
        }
      }
    }
    return true;
  }

  /** The rows of the cyclic group of order n, in the given order. */
  private static PermutationSequence cyclicGroupRows(List<Integer> order) {
    int n = order.size();
    List<Permutation> rows = new ArrayList<>();
    for (int shift : order) {
      int[] mapping = new int[n];
      for (int j = 0; j < n; j++) {
        mapping[j] = (shift + j) % n;
      }
      rows.add(Permutation.of(mapping));
    }
    return PermutationSequence.of(rows);
  }

  @Test
  void tailEqualsTheOrderWhenTheIdentityIsReached() {
    for (LatinSquare square : allOrderFour()) {
      Spectrum spectrum = spectrumOf(square);
      assertTrue(spectrum.reachesIdentity());
      assertEquals(1, spectrum.period(), "the identity is a fixed point");
      assertEquals(
          Derivative.order(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER).orElseThrow(),
          spectrum.tail(),
          "tail and order must agree");
    }
  }

  @Test
  void relabellingSymbolsPreservesTheSpectrum() {
    LatinSquare square = LatinSquares.withNaturalFirstRow(5).get(7);
    for (Permutation relabel : Permutations.all(5)) {
      assertEquals(
          spectrumOf(square),
          spectrumOf(Symmetry.relabelSymbols(square, relabel)),
          "relabelling must not change the orbit shape");
    }
  }

  @Test
  void permutingColumnsPreservesTheSpectrum() {
    LatinSquare square = LatinSquares.withNaturalFirstRow(5).get(11);
    for (Permutation permutation : Permutations.all(5)) {
      assertEquals(spectrumOf(square), spectrumOf(Symmetry.permuteColumns(square, permutation)));
    }
  }

  @Test
  void reversingTheRowOrderPreservesTheSpectrum() {
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5)) {
      assertEquals(spectrumOf(square), spectrumOf(Symmetry.reflectVertically(square)));
    }
  }

  @Test
  void permutingRowsDoesNotPreserveTheSpectrum() {
    // Row order is what D differences, so reordering rows genuinely changes the orbit.
    boolean foundDifference = false;
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5)) {
      LatinSquare moved = Symmetry.permuteRows(square, Permutation.of(0, 2, 1, 3, 4));
      if (!spectrumOf(square).equals(spectrumOf(moved))) {
        foundDifference = true;
        break;
      }
    }
    assertTrue(foundDifference, "some row permutation must change the spectrum");
  }

  @Test
  void everyOrderFiveCycleHasLengthThirty() {
    Set<Integer> periods = new TreeSet<>();
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5)) {
      Spectrum spectrum = spectrumOf(square);
      if (!spectrum.reachesIdentity()) {
        periods.add(spectrum.period());
      }
    }
    assertEquals(Set.of(30), periods, "non-convergent order-5 orbits all cycle with period 30");
  }

  @Test
  void orderFiveHasExactlyFourSpectra() {
    Set<String> shapes =
        LatinSquares.withNaturalFirstRow(5).stream()
            .map(s -> spectrumOf(s).toString())
            .collect(Collectors.toCollection(TreeSet::new));
    assertEquals(
        Set.of(
            "order 2 (identity)", "order 4 (identity)", "tail 21, cycle 30", "tail 189, cycle 30"),
        shapes);
  }

  @Test
  void atOrderFiveConvergenceIsExactlyGroupStructure() {
    int convergent = 0;
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5)) {
      boolean converges = spectrumOf(square).reachesIdentity();
      assertEquals(
          rowsFormAGroup(square),
          converges,
          "convergence and group structure must coincide for\n" + square);
      if (converges) {
        convergent++;
      }
    }
    // Six regular subgroups isomorphic to Z_5, each with 4! orderings of the remaining rows.
    assertEquals(144, convergent);
  }

  @Test
  void groupRowsConvergeExactlyAtPrimePowerOrders() {
    Random random = new Random(7L);
    for (int n : new int[] {4, 5, 6, 7, 8, 9}) {
      boolean primePower = n != 6;
      List<Integer> order = new ArrayList<>();
      for (int i = 0; i < n; i++) {
        order.add(i);
      }
      int converged = 0;
      int trials = 40;
      for (int trial = 0; trial < trials; trial++) {
        Collections.shuffle(order, random);
        if (Derivative.spectrum(cyclicGroupRows(order), Wrap.CYCLIC, Quotient.AFTER, 50_000)
            .orElseThrow()
            .reachesIdentity()) {
          converged++;
        }
      }
      if (primePower) {
        assertEquals(trials, converged, "every ordering should converge at n=" + n);
      } else {
        assertNotEquals(trials, converged, "some ordering must fail to converge at n=" + n);
      }
    }
  }

  @Test
  void orderFourConvergesWithoutGroupStructure() {
    // Group rows are sufficient for convergence but not necessary: order 4 converges regardless.
    long nonGroups = allOrderFour().stream().filter(s -> !rowsFormAGroup(s)).count();
    assertTrue(nonGroups > 0);
    assertTrue(allOrderFour().stream().allMatch(s -> spectrumOf(s).reachesIdentity()));
  }

  @Test
  void spectrumRejectsNonsense() {
    assertFalse(new Spectrum(3, 1, true).toString().isEmpty());
    assertEquals(5, new Spectrum(3, 2, false).visited());
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> new Spectrum(-1, 1, false));
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> new Spectrum(0, 0, false));
  }
}

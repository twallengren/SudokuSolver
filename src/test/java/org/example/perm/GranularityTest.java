package org.example.perm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.example.explore.LatinSquares;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Spectrum;
import org.example.perm.Derivative.Wrap;
import org.junit.jupiter.api.Test;

/** Whether the tail and the column direction are load-bearing, or could be dropped. */
class GranularityTest {

  private static final int LIMIT = 30_000;

  private static List<LatinSquare> isotopyRepresentatives(int n) {
    Map<LatinSquare, LatinSquare> byCanonical = new LinkedHashMap<>();
    for (LatinSquare square : LatinSquares.reduced(n)) {
      byCanonical.putIfAbsent(Isotopy.canonical(square), square);
    }
    return new ArrayList<>(byCanonical.values());
  }

  private static long separated(int n, Function<Optional<Spectrum>, String> key, boolean paired) {
    return isotopyRepresentatives(n).stream()
        .map(
            square -> {
              String invariant = Classifier.profile(square, true, LIMIT, key).toString();
              return paired
                  ? invariant + " || " + Classifier.profile(square, false, LIMIT, key)
                  : invariant;
            })
        .distinct()
        .count();
  }

  @Test
  void everyOrderFourOrbitHasPeriodOne() {
    // Order 4 always converges, and the identity is a fixed point, so the cycle is always trivial.
    for (LatinSquare square : LatinSquares.reduced(4)) {
      Spectrum spectrum =
          Derivative.spectrum(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, LIMIT)
              .orElseThrow();
      assertEquals(1, spectrum.period());
      assertTrue(spectrum.reachesIdentity());
    }
  }

  @Test
  void atOrderFourTheCycleCarriesNothingAtAll() {
    // Every period is 1, so keying by cycle length alone makes the two classes indistinguishable.
    List<LatinSquare> classes = isotopyRepresentatives(4);
    assertEquals(2, classes.size());
    assertEquals(
        Classifier.profile(classes.get(0), true, LIMIT, Classifier.CYCLE_ONLY),
        Classifier.profile(classes.get(1), true, LIMIT, Classifier.CYCLE_ONLY),
        "cycle-only cannot tell the order-4 classes apart");
    assertNotEquals(
        Classifier.profile(classes.get(0), true, LIMIT, Classifier.FULL_SHAPE),
        Classifier.profile(classes.get(1), true, LIMIT, Classifier.FULL_SHAPE),
        "the full shape can");
  }

  @Test
  void atOrderFiveTheCycleAloneIsEnough() {
    // One class converges under every ordering (period 1), the other never does (period 30).
    assertEquals(2, separated(5, Classifier.CYCLE_ONLY, false));
    assertEquals(2, separated(5, Classifier.FULL_SHAPE, false));
  }

  @Test
  void atOrderSixDroppingTheTailCostsSeparation() {
    assertEquals(21, separated(6, Classifier.FULL_SHAPE, false), "full shape, rows only");
    assertEquals(22, separated(6, Classifier.FULL_SHAPE, true), "full shape, paired");
    assertEquals(20, separated(6, Classifier.CYCLE_ONLY, false), "cycle only, rows only");
    assertEquals(21, separated(6, Classifier.CYCLE_ONLY, true), "cycle only, paired");
  }

  @Test
  void theColumnDirectionStillAddsSomethingWithoutTheTail() {
    // Pairing lifts cycle-only from 20 to 21, so the second direction is not redundant.
    assertTrue(
        separated(6, Classifier.CYCLE_ONLY, true) > separated(6, Classifier.CYCLE_ONLY, false));
  }

  @Test
  void cycleOnlyRowAndColumnProfilesOftenDiffer() {
    long identical =
        isotopyRepresentatives(6).stream()
            .filter(
                s ->
                    Classifier.profile(s, true, LIMIT, Classifier.CYCLE_ONLY)
                        .equals(Classifier.profile(s, false, LIMIT, Classifier.CYCLE_ONLY)))
            .count();
    assertEquals(11, identical, "only half of the order-6 classes agree across the two directions");
  }

  @Test
  void theHybridKeyMatchesTheFullShapeEverywhereTested() {
    // "steps to reach the identity, or else the cycle length" — one number instead of a pair.
    assertEquals(2, separated(4, Classifier.ORDER_OR_CYCLE, false));
    assertEquals(2, separated(5, Classifier.ORDER_OR_CYCLE, false));
    assertEquals(21, separated(6, Classifier.ORDER_OR_CYCLE, false));
    assertEquals(22, separated(6, Classifier.ORDER_OR_CYCLE, true));
  }

  @Test
  void theHybridRecoversWhatCycleOnlyLoses() {
    assertEquals(2, separated(4, Classifier.ORDER_OR_CYCLE, false));
    assertEquals(1, separated(4, Classifier.CYCLE_ONLY, false));
  }

  @Test
  void dHasFixedPointsOtherThanTheIdentity() {
    // D(s) = s means s(i+1) = s(i)^2. For g of order 3 the sequence g^2, g, g^2, ... closes up
    // over six terms, so it is fixed by D without being the all-identity state.
    Permutation g = Permutation.of(1, 2, 0, 5, 3, 4);
    assertEquals(3, g.order());
    Permutation gSquared = g.andThen(g);

    PermutationSequence fixed = PermutationSequence.of(gSquared, g, gSquared, g, gSquared, g);
    assertEquals(
        fixed, Derivative.apply(fixed, Wrap.CYCLIC, Quotient.AFTER), "D must fix this sequence");
    assertFalse(Derivative.isConstantIdentity(fixed), "but it is not the identity state");

    // So a period-1 orbit is not the same thing as a convergent one.
    Spectrum spectrum =
        Derivative.spectrum(fixed, Wrap.CYCLIC, Quotient.AFTER, LIMIT).orElseThrow();
    assertEquals(1, spectrum.period());
    assertFalse(spectrum.reachesIdentity());
  }
}

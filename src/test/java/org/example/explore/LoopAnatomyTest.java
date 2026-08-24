package org.example.explore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Wrap;
import org.example.perm.LatinSquare;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;
import org.junit.jupiter.api.Test;

/** The loops themselves: cycle length is not the whole story, and at order 5 it nearly is. */
class LoopAnatomyTest {

  private static final int LIMIT = 50_000;

  @Test
  void orderFiveHasExactlyOneLoopUpToRotationAndConjugation() {
    // All 1200 cycling squares land on 30 raw attractors, 40 squares each, and every attractor is
    // a rotation/conjugation image of a single loop.
    Map<String, Integer> raw = new LinkedHashMap<>();
    Map<String, List<PermutationSequence>> representative = new LinkedHashMap<>();
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5)) {
      PermutationSequence rows = square.rowsAsSequence();
      var spectrum = Derivative.spectrum(rows, Wrap.CYCLIC, Quotient.AFTER, LIMIT).orElseThrow();
      if (spectrum.reachesIdentity()) {
        continue;
      }
      List<PermutationSequence> cycle = LoopAnatomy.cycleStates(rows);
      String key = LoopAnatomy.rotationKey(cycle);
      raw.merge(key, 1, Integer::sum);
      representative.putIfAbsent(key, cycle);
    }
    assertEquals(30, raw.size(), "30 raw attractors");
    assertTrue(raw.values().stream().allMatch(c -> c == 40), "each catches exactly 40 squares");

    Set<String> conjugacyClasses = new TreeSet<>();
    for (List<PermutationSequence> cycle : representative.values()) {
      conjugacyClasses.add(LoopAnatomy.conjugacyKey(cycle, 5));
    }
    assertEquals(1, conjugacyClasses.size(), "a single loop up to the two proven symmetries");
  }

  @Test
  void theOrderFiveLoopLivesInACopyOfSThree() {
    // The attractor's terms generate a group of order 6 — far smaller than the difference group,
    // which is all of S_5. The orbit compresses as it falls onto its loop.
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5)) {
      PermutationSequence rows = square.rowsAsSequence();
      var spectrum = Derivative.spectrum(rows, Wrap.CYCLIC, Quotient.AFTER, LIMIT).orElseThrow();
      if (spectrum.reachesIdentity()) {
        continue;
      }
      List<PermutationSequence> cycle = LoopAnatomy.cycleStates(rows);
      List<Permutation> terms = new ArrayList<>();
      for (PermutationSequence state : cycle) {
        terms.addAll(state.steps());
      }
      assertEquals(6, ConvergenceWhy.generate(terms, 800).size());
      assertEquals(
          Map.of("1+1+1+1+1", 30, "2+1+1+1", 80, "3+1+1", 40), LoopAnatomy.typeCensus(cycle));
      break; // one is enough here; the class-uniqueness test covers the rest
    }
  }

  @Test
  void orderSixLoopsOfEqualLengthCanBeQualitativelyDifferent() {
    // Period 12 arises from loops whose terms generate groups of many different orders, so cycle
    // length alone is not an informative class label at order 6.
    List<LatinSquare> reservoir = new ArrayList<>(1500);
    Random random = new Random(66L);
    long[] seen = {0};
    LatinSquares.forEachWithNaturalFirstRow(
        6,
        square -> {
          seen[0]++;
          if (reservoir.size() < 1500) {
            reservoir.add(square);
          } else {
            long index = Math.floorMod(random.nextLong(), seen[0]);
            if (index < 1500) {
              reservoir.set((int) index, square);
            }
          }
        });
    Set<Integer> loopGroupOrders = new TreeSet<>();
    for (LatinSquare square : reservoir) {
      PermutationSequence rows = square.rowsAsSequence();
      var spectrum = Derivative.spectrum(rows, Wrap.CYCLIC, Quotient.AFTER, LIMIT).orElseThrow();
      if (spectrum.reachesIdentity() || spectrum.period() != 12) {
        continue;
      }
      List<PermutationSequence> cycle = LoopAnatomy.cycleStates(rows);
      List<Permutation> terms = new ArrayList<>();
      for (PermutationSequence state : cycle) {
        terms.addAll(state.steps());
      }
      loopGroupOrders.add(ConvergenceWhy.generate(terms, 800).size());
      if (loopGroupOrders.size() >= 3) {
        break;
      }
    }
    assertTrue(
        loopGroupOrders.size() >= 2,
        "period-12 loops with different loop groups must exist, found " + loopGroupOrders);
  }
}

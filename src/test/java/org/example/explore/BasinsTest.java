package org.example.explore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Wrap;
import org.example.perm.LatinSquare;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;
import org.junit.jupiter.api.Test;

/**
 * Basins of attraction at size 5: which squares fall into which exact loop, and what basin-mates
 * share. The surprise is uniformity — every visible feature of a starting square is spread evenly
 * across all thirty loops, so none of them predicts the basin.
 */
class BasinsTest {

  /** Basin key -> parity patterns (with multiplicity) and tails of its member squares. */
  private record Census(
      Map<String, List<String>> basinParities, Map<String, Map<Integer, Integer>> basinTails) {}

  private static Census census() {
    Map<String, List<String>> basinParities = new TreeMap<>();
    Map<String, Map<Integer, Integer>> basinTails = new TreeMap<>();
    for (LatinSquare sq : LatinSquares.withNaturalFirstRow(5)) {
      PermutationSequence rows = sq.rowsAsSequence();
      var spec = Derivative.spectrum(rows, Wrap.CYCLIC, Quotient.AFTER, 5000).orElseThrow();
      if (spec.reachesIdentity()) {
        continue;
      }
      String key = LoopAnatomy.rotationKey(LoopAnatomy.cycleStates(rows));
      StringBuilder pv = new StringBuilder();
      for (Permutation p : rows.steps()) {
        pv.append(p.sign() > 0 ? '0' : '1');
      }
      basinParities.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(pv.toString());
      basinTails.computeIfAbsent(key, k -> new TreeMap<>()).merge(spec.tail(), 1, Integer::sum);
    }
    return new Census(basinParities, basinTails);
  }

  @Test
  void onlyFiveParityPatternsAreRealisedByLoopingSquares() {
    // With the first row fixed to the identity, 15 mixed patterns are conceivable; Latin squares of
    // size 5 realise exactly five of them — one odd row (in any of four positions) or four odd
    // rows.
    // Two-odd and three-odd patterns never occur. Unexplained.
    Census census = census();
    Set<String> global = new TreeSet<>();
    census.basinParities().values().forEach(global::addAll);
    assertEquals(Set.of("00001", "00010", "00100", "01000", "01111"), global);
  }

  @Test
  void everyBasinIsAPerfectCrossSection() {
    // Thirty basins of forty squares each — and each basin contains all five realised parity
    // patterns and splits its tails exactly 20/20 between 21 and 189. No visible feature of a
    // starting square predicts which loop it falls into.
    Census census = census();
    assertEquals(30, census.basinParities().size());
    for (List<String> parities : census.basinParities().values()) {
      assertEquals(40, parities.size());
      assertEquals(5, new TreeSet<>(parities).size(), "all five patterns present");
    }
    for (Map<Integer, Integer> tails : census.basinTails().values()) {
      assertEquals(Map.of(21, 20, 189, 20), tails);
    }
  }

  @Test
  void loopStatesAreNotLatinSquares() {
    // The grids inside a loop have a shuffle in every row, but their columns repeat symbols — the
    // machine leaves the Latin world immediately and the loops live outside it.
    for (LatinSquare sq : LatinSquares.withNaturalFirstRow(5)) {
      var spec =
          Derivative.spectrum(sq.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, 5000).orElseThrow();
      if (spec.reachesIdentity()) {
        continue;
      }
      List<PermutationSequence> cycle = LoopAnatomy.cycleStates(sq.rowsAsSequence());
      boolean anyLatin = false;
      for (PermutationSequence state : cycle) {
        int[][] grid = new int[5][];
        for (int i = 0; i < 5; i++) {
          grid[i] = state.step(i).toArray();
        }
        if (LatinSquare.fromGrid(grid).isValid()) {
          anyLatin = true;
        }
      }
      assertTrue(!anyLatin, "no state of this loop should be a Latin square");
      break; // one loop suffices; all thirty are the same loop up to renaming
    }
  }
}

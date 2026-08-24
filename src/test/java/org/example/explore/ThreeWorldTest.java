package org.example.explore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Wrap;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;
import org.junit.jupiter.api.Test;

/**
 * The complete universe at size 3: all 216 grids of three shuffles, every trajectory resolved. The
 * only size where the whole machine is visible at once, and the base case of the ladder.
 */
class ThreeWorldTest {

  @Test
  void theCompleteCensus() {
    List<Permutation> s3 =
        new ArrayList<>(
            ConvergenceWhy.generate(List.of(Permutation.of(1, 2, 0), Permutation.of(1, 0, 2)), 10));
    assertEquals(6, s3.size());

    int die = 0;
    int loop = 0;
    int sameParityDies = 0;
    int mixedLoops = 0;
    Set<String> loops = new LinkedHashSet<>();
    for (Permutation a : s3) {
      for (Permutation b : s3) {
        for (Permutation c : s3) {
          PermutationSequence st = PermutationSequence.of(a, b, c);
          var spec = Derivative.spectrum(st, Wrap.CYCLIC, Quotient.AFTER, 500).orElseThrow();
          int odd = (a.sign() < 0 ? 1 : 0) + (b.sign() < 0 ? 1 : 0) + (c.sign() < 0 ? 1 : 0);
          boolean sameParity = odd == 0 || odd == 3;
          if (spec.reachesIdentity()) {
            die++;
            if (sameParity) {
              sameParityDies++;
            }
          } else {
            loop++;
            if (!sameParity) {
              mixedLoops++;
            }
            loops.add(LoopAnatomy.rotationKey(LoopAnatomy.cycleStates(st)));
          }
        }
      }
    }
    // Dying is EXACTLY "all rows share a parity" at this size: 27 all-even + 27 all-odd.
    assertEquals(54, die);
    assertEquals(54, sameParityDies);
    assertEquals(162, loop);
    assertEquals(162, mixedLoops);
    // Six loops in total: three of length 3 (one per swap) and three of length 6.
    assertEquals(6, loops.size());
  }
}

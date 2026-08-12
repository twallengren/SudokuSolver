package org.example.explore;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Wrap;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;

/**
 * Tests whether the cyclic difference operator converges for <em>every</em> starting sequence, as a
 * function of the sequence length, independently of any Latin square.
 */
public final class ConvergenceSurvey {

  private ConvergenceSurvey() {}

  private static final int SAMPLES = 1000;

  private static final int LIMIT = 400;

  public static void main(String[] args) {
    for (int symbols : new int[] {3, 4, 5}) {
      System.out.println("===== terms drawn from S_" + symbols + " =====");
      List<Permutation> pool = Permutations.all(symbols);
      Random random = new Random(20260812L);
      for (int length = 2; length <= 9; length++) {
        int converged = 0;
        int maxOrder = 0;
        for (int trial = 0; trial < SAMPLES; trial++) {
          List<Permutation> terms = new ArrayList<>(length);
          for (int i = 0; i < length; i++) {
            terms.add(pool.get(random.nextInt(pool.size())));
          }
          OptionalInt order =
              Derivative.order(PermutationSequence.of(terms), Wrap.CYCLIC, Quotient.AFTER, LIMIT);
          if (order.isPresent()) {
            converged++;
            maxOrder = Math.max(maxOrder, order.getAsInt());
          }
        }
        System.out.printf(
            "  length %d%-14s %5.1f%% converge   max order %d%n",
            length,
            isPowerOfTwo(length) ? " (power of two)" : "",
            100.0 * converged / SAMPLES,
            maxOrder);
      }
      System.out.println();
    }
  }

  private static boolean isPowerOfTwo(int value) {
    return (value & (value - 1)) == 0;
  }
}

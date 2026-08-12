package org.example.explore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Spectrum;
import org.example.perm.Derivative.Wrap;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;

/**
 * Takes the rows of the cyclic group of order {@code n} in random orders and asks how often D
 * converges, across a range of {@code n}. Convergence appears to track whether {@code n} is a prime
 * power rather than its size.
 */
public final class PrimePowerProbe {

  private PrimePowerProbe() {}

  private static final int TRIALS = 200;
  private static final int LIMIT = 50_000;

  public static void main(String[] args) {
    System.out.println("  n    prime power   converge   unresolved");
    for (int n = 3; n <= 20; n++) {
      List<Permutation> rows = new ArrayList<>();
      for (int i = 0; i < n; i++) {
        int[] mapping = new int[n];
        for (int j = 0; j < n; j++) {
          mapping[j] = (i + j) % n;
        }
        rows.add(Permutation.of(mapping));
      }
      Random random = new Random(1234L + n);
      int converged = 0;
      int unresolved = 0;
      for (int trial = 0; trial < TRIALS; trial++) {
        List<Permutation> shuffled = new ArrayList<>(rows);
        Collections.shuffle(shuffled, random);
        Optional<Spectrum> spectrum =
            Derivative.spectrum(
                PermutationSequence.of(shuffled), Wrap.CYCLIC, Quotient.AFTER, LIMIT);
        if (spectrum.isEmpty()) {
          unresolved++;
        } else if (spectrum.get().reachesIdentity()) {
          converged++;
        }
      }
      System.out.printf(
          "  %-4d %-13s %4d/%-4d  %d%n",
          n, isPrimePower(n) ? "yes" : "no", converged, TRIALS, unresolved);
    }
  }

  private static boolean isPrimePower(int value) {
    for (int prime = 2; prime <= value; prime++) {
      if (value % prime != 0) {
        continue;
      }
      int remaining = value;
      while (remaining % prime == 0) {
        remaining /= prime;
      }
      return remaining == 1;
    }
    return false;
  }
}

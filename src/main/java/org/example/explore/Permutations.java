package org.example.explore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.example.perm.Permutation;

/**
 * Generators for the permutations of {@code 0..n-1}.
 *
 * <p>These lists used to be precomputed and checked into the repository as text files. They are
 * cheap to produce, so they are generated on demand instead.
 */
public final class Permutations {

  private Permutations() {}

  /** All {@code n!} permutations of {@code 0..n-1}, in lexicographic order. */
  public static List<Permutation> all(int n) {
    List<Permutation> result = new ArrayList<>();
    forEach(n, result::add);
    return result;
  }

  /**
   * All derangements of {@code 0..n-1} — the permutations with no fixed point. These are exactly
   * the candidates for a single row-to-row step of a Latin square.
   */
  public static List<Permutation> derangements(int n) {
    List<Permutation> result = new ArrayList<>();
    forEachDerangement(n, result::add);
    return result;
  }

  /** Visits every permutation of {@code 0..n-1} without materialising the whole list. */
  public static void forEach(int n, Consumer<Permutation> consumer) {
    generate(n, consumer, false);
  }

  /** Visits every derangement of {@code 0..n-1} without materialising the whole list. */
  public static void forEachDerangement(int n, Consumer<Permutation> consumer) {
    generate(n, consumer, true);
  }

  /** The number of derangements of {@code n} points, the subfactorial {@code !n}. */
  public static long countDerangements(int n) {
    if (n < 0) {
      throw new IllegalArgumentException("n must be non-negative, got " + n);
    }
    // !0 = 1, !1 = 0, !n = (n-1) * (!(n-1) + !(n-2))
    long twoBack = 1;
    long oneBack = 0;
    if (n == 0) {
      return twoBack;
    }
    for (int i = 2; i <= n; i++) {
      long current = (i - 1) * (oneBack + twoBack);
      twoBack = oneBack;
      oneBack = current;
    }
    return oneBack;
  }

  /** {@code n!}. */
  public static long factorial(int n) {
    if (n < 0) {
      throw new IllegalArgumentException("n must be non-negative, got " + n);
    }
    long result = 1;
    for (int i = 2; i <= n; i++) {
      result *= i;
    }
    return result;
  }

  private static void generate(int n, Consumer<Permutation> consumer, boolean derangementsOnly) {
    if (n < 1) {
      throw new IllegalArgumentException("Permutation size must be at least 1, got " + n);
    }
    int[] mapping = new int[n];
    boolean[] used = new boolean[n];
    extend(0, mapping, used, consumer, derangementsOnly);
  }

  private static void extend(
      int index,
      int[] mapping,
      boolean[] used,
      Consumer<Permutation> consumer,
      boolean derangementsOnly) {
    int n = mapping.length;
    if (index == n) {
      consumer.accept(Permutation.of(mapping));
      return;
    }
    for (int value = 0; value < n; value++) {
      if (used[value] || (derangementsOnly && value == index)) {
        continue;
      }
      used[value] = true;
      mapping[index] = value;
      extend(index + 1, mapping, used, consumer, derangementsOnly);
      used[value] = false;
    }
  }
}

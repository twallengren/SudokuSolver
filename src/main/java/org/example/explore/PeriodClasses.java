package org.example.explore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Spectrum;
import org.example.perm.Derivative.Wrap;
import org.example.perm.LatinSquare;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;

/**
 * What do order-6 squares with the same cycle length have in common?
 *
 * <p>Two structural handles constrain the answer. First, once the terms of a sequence lie in a
 * subgroup G they stay in G, so the entire orbit after one step is a Ducci orbit over the group the
 * differences generate — the period is a property of that group plus the starting point inside it.
 * Second, the parity vector's period divides the full period (the quotient of a cycle is a cycle).
 *
 * <p>For rows drawn from an abelian group the story should be complete: the dynamics decompose per
 * prime, and the period should be exactly the lcm of the mod-2 and mod-3 component periods.
 */
public final class PeriodClasses {

  private PeriodClasses() {}

  private static final int LIMIT = 50_000;
  private static final int SAMPLE = 6_000;

  public static void main(String[] args) {
    zSixComplete();
    generalFingerprint();
  }

  /** The abelian family, exhaustively: all 720 orderings of the rows of Z_6. */
  private static void zSixComplete() {
    System.out.println("=== Z_6 rows, all 720 orderings: period = lcm of per-prime periods? ===");
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

    Map<Integer, Integer> periodCounts = new TreeMap<>();
    int lcmMatches = 0;
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
      int p2 = ConvergenceWhy.ducciPeriod(mod2, 2);
      int p3 = ConvergenceWhy.ducciPeriod(mod3, 3);
      if (spectrum.period() == lcm(p2, p3)) {
        lcmMatches++;
      }
      periodCounts.merge(spectrum.period(), 1, Integer::sum);
    }
    System.out.println("  period histogram: " + periodCounts);
    System.out.println("  period == lcm(mod-2 period, mod-3 period): " + lcmMatches + " / 720");
    System.out.println();
  }

  /** A sample of general order-6 squares, fingerprinted per period class. */
  private static void generalFingerprint() {
    System.out.println(
        "=== order 6, " + SAMPLE + " sampled squares: fingerprint of each period class ===");
    List<LatinSquare> reservoir = new ArrayList<>(SAMPLE);
    Random random = new Random(66L);
    long[] seen = {0};
    LatinSquares.forEachWithNaturalFirstRow(
        6,
        square -> {
          seen[0]++;
          if (reservoir.size() < SAMPLE) {
            reservoir.add(square);
          } else {
            long index = Math.floorMod(random.nextLong(), seen[0]);
            if (index < SAMPLE) {
              reservoir.set((int) index, square);
            }
          }
        });

    Map<String, ClassStats> classes = new TreeMap<>();
    int divisibilityViolations = 0;
    int unresolved = 0;
    for (LatinSquare square : reservoir) {
      PermutationSequence rows = square.rowsAsSequence();
      var maybe = Derivative.spectrum(rows, Wrap.CYCLIC, Quotient.AFTER, LIMIT);
      if (maybe.isEmpty()) {
        unresolved++;
        continue;
      }
      Spectrum spectrum = maybe.orElseThrow();
      String label =
          spectrum.reachesIdentity()
              ? String.format("order %d (converges)", spectrum.tail())
              : String.format("period %6d", spectrum.period());

      int[] signs = new int[6];
      for (int i = 0; i < 6; i++) {
        signs[i] = rows.step(i).sign() > 0 ? 0 : 1;
      }
      int parityPeriod = ConvergenceWhy.ducciPeriod(signs, 2);
      int fullPeriod = spectrum.reachesIdentity() ? 1 : spectrum.period();
      if (fullPeriod % parityPeriod != 0) {
        divisibilityViolations++;
      }

      Set<Permutation> diffGroup =
          ConvergenceWhy.generate(Derivative.apply(rows, Wrap.CYCLIC, Quotient.AFTER).steps(), 800);
      boolean insideA6 = diffGroup.stream().allMatch(q -> q.sign() > 0);

      classes
          .computeIfAbsent(label, k -> new ClassStats())
          .add(diffGroup.size(), ConvergenceWhy.isAbelian(diffGroup), insideA6, parityPeriod);
    }

    System.out.printf(
        "%-18s %6s   %-28s %-10s %-8s %s%n",
        "class", "count", "diff-group orders", "abelian", "in A6", "parity periods");
    classes.forEach(
        (label, stats) ->
            System.out.printf(
                "%-18s %6d   %-28s %-10s %-8s %s%n",
                label,
                stats.count,
                stats.groupOrders,
                stats.abelianCount + "/" + stats.count,
                stats.insideA6Count + "/" + stats.count,
                stats.parityPeriods));
    System.out.println("  parity-period divisibility violations: " + divisibilityViolations);
    System.out.println("  unresolved within limit: " + unresolved);
  }

  private static final class ClassStats {
    int count;
    int abelianCount;
    int insideA6Count;
    final Map<Integer, Integer> groupOrders = new TreeMap<>();
    final Set<Integer> parityPeriods = new TreeSet<>();

    void add(int groupOrder, boolean abelian, boolean insideA6, int parityPeriod) {
      count++;
      if (abelian) {
        abelianCount++;
      }
      if (insideA6) {
        insideA6Count++;
      }
      groupOrders.merge(groupOrder, 1, Integer::sum);
      parityPeriods.add(parityPeriod);
    }
  }

  private static int lcm(int a, int b) {
    return a / gcd(a, b) * b;
  }

  private static int gcd(int a, int b) {
    return b == 0 ? a : gcd(b, a % b);
  }
}

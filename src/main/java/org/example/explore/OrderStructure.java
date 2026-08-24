package org.example.explore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Spectrum;
import org.example.perm.Derivative.Wrap;
import org.example.perm.LatinSquare;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;

/**
 * What do squares sharing a D-order have in common?
 *
 * <p>Order here is a single number: the steps to reach the identity if the orbit converges,
 * otherwise the cycle length.
 *
 * <p>The organising idea is that D behaves like differentiation. A square has order k exactly when
 * its (k-1)-th derivative is constant — the same statement as "a polynomial has degree d when its
 * d-th derivative is constant". Non-convergent squares are the ones that are not polynomial at all.
 */
public final class OrderStructure {

  private OrderStructure() {}

  private static final int LIMIT = 50_000;

  public static void main(String[] args) {
    partition(4, 0);
    partition(5, 0);
    partition(6, 4000);
    towers(4);
    towers(5);
    derivativeIsConstantCheck(4);
    derivativeIsConstantCheck(5);
    geometricProgressions();
    recursion(5);
    integration(5);
  }

  /** Differentiating once should drop the order by exactly one, as with polynomials. */
  private static void recursion(int n) {
    int checked = 0;
    int failed = 0;
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(n)) {
      PermutationSequence s = square.rowsAsSequence();
      Spectrum spectrum = Derivative.spectrum(s, Wrap.CYCLIC, Quotient.AFTER, LIMIT).orElseThrow();
      if (!spectrum.reachesIdentity() || spectrum.tail() < 2) {
        continue;
      }
      checked++;
      PermutationSequence derivative = Derivative.apply(s, Wrap.CYCLIC, Quotient.AFTER);
      if (dOrder(derivative) != spectrum.tail() - 1) {
        failed++;
      }
    }
    System.out.printf(
        "=== order %d: differentiating drops the order by one in %d of %d convergent squares ===%n%n",
        n, checked - failed, checked);
  }

  /** Building sequences upward by integration instead of searching for them. */
  private static void integration(int n) {
    System.out.println("=== integrating upward from a constant, at order " + n + " ===");
    int[] shift = new int[n];
    for (int i = 0; i < n; i++) {
      shift[i] = (i + 1) % n;
    }
    Permutation g = Permutation.of(shift);

    // Level 1: the constant sequence g. Its product is g^n = identity, so it integrates.
    List<Permutation> constant = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      constant.add(g);
    }
    PermutationSequence level = PermutationSequence.of(constant);
    System.out.println("  start (constant)      " + oneLine(level) + "   order " + dOrder(level));

    for (int step = 1; step <= 3; step++) {
      var integrated = Derivative.integrate(level, Permutation.identity(n));
      if (integrated.isEmpty()) {
        System.out.println(
            "  integrate #"
                + step
                + ": no antiderivative — the terms do not multiply to the identity");
        break;
      }
      level = integrated.get();
      boolean latin = isLatin(level);
      System.out.printf(
          "  integrate #%d          %s   order %d   %s%n",
          step, oneLine(level), dOrder(level), latin ? "IS a Latin square" : "not Latin");
    }
    System.out.println();
  }

  private static boolean isLatin(PermutationSequence s) {
    int n = s.order();
    int[][] grid = new int[n][];
    for (int i = 0; i < n; i++) {
      grid[i] = s.step(i).toArray();
    }
    return LatinSquare.fromGrid(grid).isValid();
  }

  /** The single number: steps to identity, else cycle length. */
  private static int dOrder(PermutationSequence s) {
    Spectrum spectrum = Derivative.spectrum(s, Wrap.CYCLIC, Quotient.AFTER, LIMIT).orElseThrow();
    return spectrum.reachesIdentity() ? spectrum.tail() : spectrum.period();
  }

  private static String label(PermutationSequence s) {
    Spectrum spectrum = Derivative.spectrum(s, Wrap.CYCLIC, Quotient.AFTER, LIMIT).orElseThrow();
    return spectrum.reachesIdentity()
        ? "order " + spectrum.tail()
        : "cycles, length " + spectrum.period();
  }

  private static void partition(int n, int sampleSize) {
    List<LatinSquare> squares = new ArrayList<>();
    if (sampleSize == 0) {
      squares = LatinSquares.withNaturalFirstRow(n);
    } else {
      Random random = new Random(11L);
      List<LatinSquare> reservoir = squares;
      long[] seen = {0};
      LatinSquares.forEachWithNaturalFirstRow(
          n,
          square -> {
            seen[0]++;
            if (reservoir.size() < sampleSize) {
              reservoir.add(square);
            } else {
              long index = Math.floorMod(random.nextLong(), seen[0]);
              if (index < sampleSize) {
                reservoir.set((int) index, square);
              }
            }
          });
    }
    Map<String, Integer> histogram = new TreeMap<>();
    for (LatinSquare square : squares) {
      histogram.merge(label(square.rowsAsSequence()), 1, Integer::sum);
    }
    System.out.println(
        "=== order "
            + n
            + ": "
            + squares.size()
            + (sampleSize == 0 ? " squares (all, natural first row)" : " squares (sample)")
            + " ===");
    histogram.forEach((k, v) -> System.out.printf("  %-22s %d%n", k, v));
    System.out.println();
  }

  /** The derivative tower of one representative per order value. */
  private static void towers(int n) {
    System.out.println("=== order " + n + ": one square of each D-order, differentiated ===");
    Map<String, LatinSquare> seen = new LinkedHashMap<>();
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(n)) {
      seen.putIfAbsent(label(square.rowsAsSequence()), square);
    }
    seen.forEach(
        (key, square) -> {
          System.out.println("--- " + key + " ---");
          PermutationSequence state = square.rowsAsSequence();
          for (int step = 0; step <= 6; step++) {
            boolean constant = state.steps().stream().distinct().count() == 1;
            boolean identity = Derivative.isConstantIdentity(state);
            System.out.println(
                "  D^"
                    + step
                    + "  "
                    + oneLine(state)
                    + (identity ? "   <- ALL IDENTITY" : constant ? "   <- CONSTANT" : ""));
            if (identity) {
              break;
            }
            state = Derivative.apply(state, Wrap.CYCLIC, Quotient.AFTER);
          }
          System.out.println();
        });
  }

  /** Order k should mean: the (k-1)-th derivative is constant and not the identity. */
  private static void derivativeIsConstantCheck(int n) {
    int checked = 0;
    int failed = 0;
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(n)) {
      Spectrum spectrum =
          Derivative.spectrum(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, LIMIT)
              .orElseThrow();
      if (!spectrum.reachesIdentity()) {
        continue;
      }
      checked++;
      PermutationSequence state = square.rowsAsSequence();
      for (int step = 0; step < spectrum.tail() - 1; step++) {
        state = Derivative.apply(state, Wrap.CYCLIC, Quotient.AFTER);
      }
      boolean constant = state.steps().stream().distinct().count() == 1;
      if (!constant || Derivative.isConstantIdentity(state)) {
        failed++;
      }
    }
    System.out.printf(
        "=== order %d: of %d convergent squares, the (k-1)-th derivative is a non-identity constant "
            + "in %d cases, fails in %d ===%n%n",
        n, checked, checked - failed, failed);
  }

  /** Order-2 squares should be exactly the powers of a single n-cycle. */
  private static void geometricProgressions() {
    System.out.println("=== order-2 squares versus powers of a single permutation ===");
    for (int n = 3; n <= 6; n++) {
      int orderTwo = 0;
      int geometric = 0;
      int both = 0;
      for (LatinSquare square : LatinSquares.withNaturalFirstRow(n)) {
        List<Permutation> rows = square.rowsAsSequence().steps();
        boolean isOrderTwo =
            dOrder(square.rowsAsSequence()) == 2
                && Derivative.spectrum(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, LIMIT)
                    .orElseThrow()
                    .reachesIdentity();
        // Is row i always the i-th power of row 1?
        Permutation g = rows.get(1);
        boolean isGeometric = true;
        Permutation power = Permutation.identity(n);
        for (int i = 0; i < n; i++) {
          if (!power.equals(rows.get(i))) {
            isGeometric = false;
            break;
          }
          power = power.andThen(g);
        }
        if (isOrderTwo) {
          orderTwo++;
        }
        if (isGeometric) {
          geometric++;
        }
        if (isOrderTwo && isGeometric) {
          both++;
        }
      }
      System.out.printf(
          "  n=%d   order-2: %-4d   powers of one permutation: %-4d   both: %-4d   (n-1)! = %d%n",
          n, orderTwo, geometric, both, factorial(n - 1));
    }
    System.out.println();
  }

  private static long factorial(int n) {
    long result = 1;
    for (int i = 2; i <= n; i++) {
      result *= i;
    }
    return result;
  }

  private static String oneLine(PermutationSequence s) {
    StringBuilder sb = new StringBuilder();
    for (Permutation p : s.steps()) {
      sb.append(p).append("  ");
    }
    return sb.toString().stripTrailing();
  }
}

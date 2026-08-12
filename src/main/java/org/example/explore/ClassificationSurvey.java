package org.example.explore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.UnaryOperator;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Spectrum;
import org.example.perm.Derivative.Wrap;
import org.example.perm.LatinSquare;
import org.example.perm.Permutation;
import org.example.perm.Symmetry;

/**
 * Asks whether the shape of the D-orbit can classify Latin squares: how the spectra are
 * distributed, whether they survive the symmetry operations, and what distinguishes the squares
 * that converge.
 */
public final class ClassificationSurvey {

  private ClassificationSurvey() {}

  private static final int LIMIT = 20_000;

  public static void main(String[] args) {
    for (int n : new int[] {4, 5}) {
      List<LatinSquare> squares = squaresFor(n);
      System.out.println("===== order " + n + ": " + squares.size() + " squares =====");
      distribution(squares);
      invariance(n, squares);
      convergentStructure(squares);
      System.out.println();
    }
    cyclicProbe();
    scrambledGroupProbe();
    sampleOrderSix();
  }

  /** Rows of a cyclic group in a scrambled order: does group structure alone force convergence? */
  private static void scrambledGroupProbe() {
    System.out.println("===== Z_n rows in random orders (does group => convergence?) =====");
    Random random = new Random(4242L);
    for (int n = 4; n <= 9; n++) {
      List<Permutation> rows = new ArrayList<>();
      for (int i = 0; i < n; i++) {
        int[] mapping = new int[n];
        for (int j = 0; j < n; j++) {
          mapping[j] = (i + j) % n;
        }
        rows.add(Permutation.of(mapping));
      }
      int trials = 300;
      int converged = 0;
      Set<String> shapes = new java.util.TreeSet<>();
      for (int trial = 0; trial < trials; trial++) {
        List<Permutation> shuffled = new ArrayList<>(rows);
        java.util.Collections.shuffle(shuffled, random);
        Optional<Spectrum> spectrum =
            Derivative.spectrum(
                org.example.perm.PermutationSequence.of(shuffled),
                Wrap.CYCLIC,
                Quotient.AFTER,
                LIMIT);
        if (spectrum.isPresent()) {
          shapes.add(spectrum.get().toString());
          if (spectrum.get().reachesIdentity()) {
            converged++;
          }
        }
      }
      System.out.printf("  n=%-3d %3d/%d converge   shapes: %s%n", n, converged, trials, shapes);
    }
  }

  /** A random sample of order-6 squares, where exhaustive enumeration is out of reach. */
  private static void sampleOrderSix() {
    System.out.println(
        "===== order 6: reservoir sample of squares with the natural first row =====");
    int wanted = 1000;
    List<LatinSquare> reservoir = new ArrayList<>(wanted);
    Random random = new Random(99L);
    int[] seen = {0};
    LatinSquares.forEachWithNaturalFirstRow(
        6,
        square -> {
          seen[0]++;
          if (reservoir.size() < wanted) {
            reservoir.add(square);
          } else {
            int index = random.nextInt(seen[0]);
            if (index < wanted) {
              reservoir.set(index, square);
            }
          }
        });
    System.out.println("  sampled " + reservoir.size() + " of " + seen[0]);

    Map<String, Integer> histogram = new TreeMap<>();
    int unresolved = 0;
    int convergentGroups = 0;
    int convergentNonGroups = 0;
    for (LatinSquare square : reservoir) {
      Optional<Spectrum> spectrum = spectrumOf(square);
      if (spectrum.isEmpty()) {
        unresolved++;
        continue;
      }
      histogram.merge(spectrum.get().toString(), 1, Integer::sum);
      if (spectrum.get().reachesIdentity()) {
        if (rowsFormAGroup(square)) {
          convergentGroups++;
        } else {
          convergentNonGroups++;
        }
      }
    }
    histogram.forEach((shape, count) -> System.out.printf("    %-24s %d%n", shape, count));
    if (unresolved > 0) {
      System.out.println("    unresolved within the limit: " + unresolved);
    }
    System.out.printf(
        "  convergent with group rows: %d, convergent without: %d%n",
        convergentGroups, convergentNonGroups);
  }

  /**
   * The Cayley table of the cyclic group of each order, whose rows always form a group, probed well
   * past the orders that can be enumerated exhaustively.
   */
  private static void cyclicProbe() {
    System.out.println("===== Cayley table of Z_n (rows always form a group) =====");
    for (int n = 2; n <= 16; n++) {
      int[] shift = new int[n];
      for (int i = 0; i < n; i++) {
        shift[i] = (i + 1) % n;
      }
      List<Permutation> steps = new ArrayList<>();
      for (int i = 0; i < n - 1; i++) {
        steps.add(Permutation.of(shift));
      }
      LatinSquare square = LatinSquare.fromSequence(org.example.perm.PermutationSequence.of(steps));
      System.out.printf(
          "  n=%-3d %s%n",
          n, spectrumOf(square).map(Spectrum::toString).orElse("unresolved within the limit"));
    }
  }

  private static List<LatinSquare> squaresFor(int n) {
    List<LatinSquare> base = LatinSquares.withNaturalFirstRow(n);
    if (n > 4) {
      return base;
    }
    List<LatinSquare> all = new ArrayList<>();
    for (LatinSquare square : base) {
      for (Permutation relabel : Permutations.all(n)) {
        all.add(Symmetry.relabelSymbols(square, relabel));
      }
    }
    return all;
  }

  private static Optional<Spectrum> spectrumOf(LatinSquare square) {
    return Derivative.spectrum(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, LIMIT);
  }

  private static void distribution(List<LatinSquare> squares) {
    Map<String, Integer> histogram = new TreeMap<>();
    int unresolved = 0;
    for (LatinSquare square : squares) {
      Optional<Spectrum> spectrum = spectrumOf(square);
      if (spectrum.isEmpty()) {
        unresolved++;
      } else {
        histogram.merge(spectrum.get().toString(), 1, Integer::sum);
      }
    }
    System.out.println("  spectra:");
    histogram.forEach((shape, count) -> System.out.printf("    %-24s %d%n", shape, count));
    if (unresolved > 0) {
      System.out.println("    unresolved within the limit: " + unresolved);
    }
    System.out.println("    distinct spectra: " + histogram.size());
  }

  /** Does the spectrum survive the operations that preserve the Latin property? */
  private static void invariance(int n, List<LatinSquare> squares) {
    Random random = new Random(20260812L);
    List<Permutation> pool = Permutations.all(n);
    Map<String, UnaryOperator<LatinSquare>> operations = new LinkedHashMap<>();
    operations.put("transpose", Symmetry::transpose);
    operations.put("reflect vertically", Symmetry::reflectVertically);
    operations.put(
        "relabel symbols", s -> Symmetry.relabelSymbols(s, pool.get(random.nextInt(pool.size()))));
    operations.put(
        "permute rows", s -> Symmetry.permuteRows(s, pool.get(random.nextInt(pool.size()))));
    operations.put(
        "permute columns", s -> Symmetry.permuteColumns(s, pool.get(random.nextInt(pool.size()))));

    System.out.println("  spectrum preserved by:");
    operations.forEach(
        (name, operation) -> {
          int preserved = 0;
          int comparable = 0;
          for (LatinSquare square : squares) {
            Optional<Spectrum> before = spectrumOf(square);
            Optional<Spectrum> after = spectrumOf(operation.apply(square));
            if (before.isPresent() && after.isPresent()) {
              comparable++;
              if (before.equals(after)) {
                preserved++;
              }
            }
          }
          System.out.printf(
              "    %-20s %5d / %-5d  (%.0f%%)%n",
              name, preserved, comparable, 100.0 * preserved / Math.max(1, comparable));
        });
  }

  /** Do the squares that converge have rows closed under composition? */
  private static void convergentStructure(List<LatinSquare> squares) {
    int convergent = 0;
    int convergentClosed = 0;
    int divergentClosed = 0;
    for (LatinSquare square : squares) {
      Optional<Spectrum> spectrum = spectrumOf(square);
      boolean converges = spectrum.isPresent() && spectrum.get().reachesIdentity();
      boolean closed = rowsFormAGroup(square);
      if (converges) {
        convergent++;
        if (closed) {
          convergentClosed++;
        }
      } else if (closed) {
        divergentClosed++;
      }
    }
    System.out.printf(
        "  convergent: %d, of which rows form a group: %d%n", convergent, convergentClosed);
    System.out.printf("  non-convergent whose rows form a group: %d%n", divergentClosed);
  }

  /** Whether the set of rows, viewed as permutations, is closed under composition. */
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
}

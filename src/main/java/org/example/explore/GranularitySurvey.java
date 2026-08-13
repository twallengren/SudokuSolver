package org.example.explore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.example.perm.Classifier;
import org.example.perm.Derivative;
import org.example.perm.Isotopy;
import org.example.perm.LatinSquare;

/**
 * Asks how much of the invariant is actually load-bearing.
 *
 * <p>Two reductions are on trial. Dropping the tail and keeping only the cycle length, on the
 * grounds that the cycle is the attractor and the tail only says how far the start sat from it. And
 * dropping the column direction, keeping only the rows.
 *
 * <p>Each combination is scored by how many of the order's isotopy classes it separates.
 */
public final class GranularitySurvey {

  private GranularitySurvey() {}

  private static final int LIMIT = 30_000;

  public static void main(String[] args) {
    for (int n : new int[] {4, 5, 6}) {
      List<LatinSquare> classes = isotopyRepresentatives(n);
      System.out.println("===== order " + n + ": " + classes.size() + " isotopy classes =====");

      score("full shape, rows only    ", classes, Classifier.FULL_SHAPE, false);
      score("full shape, rows+columns ", classes, Classifier.FULL_SHAPE, true);
      score("cycle only, rows only    ", classes, Classifier.CYCLE_ONLY, false);
      score("cycle only, rows+columns ", classes, Classifier.CYCLE_ONLY, true);

      redundancy(classes);
      System.out.println();
    }
  }

  private static void score(
      String label,
      List<LatinSquare> classes,
      Function<Optional<Derivative.Spectrum>, String> key,
      boolean paired) {
    Map<String, List<Integer>> byInvariant = new LinkedHashMap<>();
    for (int index = 0; index < classes.size(); index++) {
      LatinSquare square = classes.get(index);
      String invariant = Classifier.profile(square, true, LIMIT, key).toString();
      if (paired) {
        invariant += " || " + Classifier.profile(square, false, LIMIT, key);
      }
      byInvariant.computeIfAbsent(invariant, k -> new ArrayList<>()).add(index + 1);
    }
    List<List<Integer>> collisions =
        byInvariant.values().stream().filter(group -> group.size() > 1).toList();
    System.out.printf(
        "  %s separates %2d / %2d%s%n",
        label,
        byInvariant.size(),
        classes.size(),
        collisions.isEmpty() ? "" : "   collisions: " + collisions);
  }

  /** Under cycle-only keying, does the column direction ever say anything the rows do not? */
  private static void redundancy(List<LatinSquare> classes) {
    int identical = 0;
    for (LatinSquare square : classes) {
      if (Classifier.profile(square, true, LIMIT, Classifier.CYCLE_ONLY)
          .equals(Classifier.profile(square, false, LIMIT, Classifier.CYCLE_ONLY))) {
        identical++;
      }
    }
    System.out.printf(
        "  cycle-only row and column profiles identical for %d of %d classes%n",
        identical, classes.size());
  }

  private static List<LatinSquare> isotopyRepresentatives(int n) {
    Map<LatinSquare, LatinSquare> byCanonical = new LinkedHashMap<>();
    for (LatinSquare square : LatinSquares.reduced(n)) {
      byCanonical.putIfAbsent(Isotopy.canonical(square), square);
    }
    return new ArrayList<>(byCanonical.values());
  }
}

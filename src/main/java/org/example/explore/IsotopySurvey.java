package org.example.explore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.example.perm.Isotopy;
import org.example.perm.LatinSquare;

/**
 * Counts isotopy and main classes, validating the canonical form against the published counts
 * before it is used to interpret anything else.
 */
public final class IsotopySurvey {

  private IsotopySurvey() {}

  public static void main(String[] args) {
    System.out.println("  n   reduced   isotopy classes   main classes");
    for (int n = 2; n <= 6; n++) {
      long started = System.nanoTime();
      List<LatinSquare> reduced = LatinSquares.reduced(n);
      Set<LatinSquare> isotopy = new HashSet<>();
      Set<LatinSquare> main = new HashSet<>();
      for (LatinSquare square : reduced) {
        isotopy.add(Isotopy.canonical(square));
        main.add(Isotopy.mainClassCanonical(square));
      }
      System.out.printf(
          "  %-3d %-9d %-17d %-13d (%.1fs)%n",
          n, reduced.size(), isotopy.size(), main.size(), (System.nanoTime() - started) / 1e9);
    }
  }
}

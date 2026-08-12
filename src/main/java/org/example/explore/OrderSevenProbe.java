package org.example.explore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Spectrum;
import org.example.perm.Derivative.Wrap;
import org.example.perm.Isotopy;
import org.example.perm.LatinSquare;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;

/**
 * Measures what an order-7 run would actually cost before one is attempted.
 *
 * <p>The three unknowns are how long the reduced enumeration takes, how long the D-orbits are at
 * order 7, and how long a single paired profile takes. Orbit length is the one that decides
 * everything: order 5 orbits are short and order 6 orbits run to thousands of states, and 7 being
 * prime could fall either way.
 */
public final class OrderSevenProbe {

  private OrderSevenProbe() {}

  private static final int LIMIT = 200_000;

  public static void main(String[] args) {
    long started = System.nanoTime();
    int wanted = 200;
    List<LatinSquare> reservoir = new ArrayList<>(wanted);
    Random random = new Random(7777L);
    long[] seen = {0};
    LatinSquares.forEachReduced(
        7,
        square -> {
          seen[0]++;
          if (reservoir.size() < wanted) {
            reservoir.add(square);
          } else {
            long index = Math.floorMod(random.nextLong(), seen[0]);
            if (index < wanted) {
              reservoir.set((int) index, square);
            }
          }
        });
    double enumerationSeconds = (System.nanoTime() - started) / 1e9;
    System.out.printf(
        "reduced squares of order 7: %d  (%.1fs, expected 16942080)%n",
        seen[0], enumerationSeconds);

    // Orbit lengths.
    started = System.nanoTime();
    Map<Integer, Integer> periods = new TreeMap<>();
    int maxTail = 0;
    int maxVisited = 0;
    int unresolved = 0;
    for (LatinSquare square : reservoir) {
      Optional<Spectrum> spectrum =
          Derivative.spectrum(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, LIMIT);
      if (spectrum.isEmpty()) {
        unresolved++;
        continue;
      }
      periods.merge(spectrum.get().period(), 1, Integer::sum);
      maxTail = Math.max(maxTail, spectrum.get().tail());
      maxVisited = Math.max(maxVisited, spectrum.get().visited());
    }
    System.out.printf(
        "%d spectra in %.1fs   max tail %d   max states %d   unresolved %d%n",
        reservoir.size(), (System.nanoTime() - started) / 1e9, maxTail, maxVisited, unresolved);
    System.out.println("periods: " + periods);

    // One paired profile, naive and dihedral-reduced.
    started = System.nanoTime();
    profile(reservoir.get(0), true);
    profile(reservoir.get(0), false);
    double naiveSeconds = (System.nanoTime() - started) / 1e9;
    System.out.printf("one paired profile, naive (2 x 5040 spectra): %.1fs%n", naiveSeconds);

    started = System.nanoTime();
    org.example.perm.Classifier.profile(reservoir.get(0), LIMIT);
    double reducedSeconds = (System.nanoTime() - started) / 1e9;
    System.out.printf(
        "one paired profile, dihedral-reduced (2 x 360): %.1fs  (%.1fx faster)%n",
        reducedSeconds, naiveSeconds / reducedSeconds);
    System.out.printf("=> 564 classes would cost about %.0f minutes%n", 564 * reducedSeconds / 60);

    // Canonicalisation cost, which decides how classes can be found at all.
    started = System.nanoTime();
    for (int i = 0; i < 20; i++) {
      Isotopy.canonical(reservoir.get(i));
    }
    double canonicalSeconds = (System.nanoTime() - started) / 1e9 / 20;
    System.out.printf("one canonical form: %.3fs%n", canonicalSeconds);
    System.out.printf(
        "=> canonicalising all 16942080 reduced squares would cost about %.0f hours%n",
        16942080 * canonicalSeconds / 3600);
  }

  private static Map<String, Integer> profile(LatinSquare square, boolean alongRows) {
    List<Permutation> terms =
        (alongRows ? square.rowsAsSequence() : square.columnsAsSequence()).steps();
    Map<String, Integer> profile = new TreeMap<>();
    for (Permutation ordering : Permutations.all(square.order())) {
      List<Permutation> reordered = new ArrayList<>(terms.size());
      for (int i = 0; i < terms.size(); i++) {
        reordered.add(terms.get(ordering.imageOf(i)));
      }
      profile.merge(
          Derivative.spectrum(PermutationSequence.of(reordered), Wrap.CYCLIC, Quotient.AFTER, LIMIT)
              .map(Spectrum::toString)
              .orElse("unresolved"),
          1,
          Integer::sum);
    }
    return profile;
  }
}

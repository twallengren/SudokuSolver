package org.example.explore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.example.perm.Classifier;
import org.example.perm.Isotopy;
import org.example.perm.LatinSquare;

/**
 * Looks for a counterexample at order 7: two squares that are not isotopic but share the paired
 * D-profile.
 *
 * <p>Sampling suits this because the goal is falsification rather than coverage. Canonical forms
 * are cheap, so many genuine isotopy classes can be identified quickly, and the budget is then
 * spent only on profiling distinct classes. A collision found this way is conclusive; the absence
 * of one across a partial sample is not, and is reported as such.
 */
public final class OrderSevenSample {

  private OrderSevenSample() {}

  private static final int LIMIT = 200_000;

  public static void main(String[] args) {
    int samples = args.length > 0 ? Integer.parseInt(args[0]) : 4000;
    int maxClasses = args.length > 1 ? Integer.parseInt(args[1]) : 120;

    long started = System.nanoTime();
    List<LatinSquare> reservoir = reservoir(7, samples);
    System.out.printf(
        "sampled %d reduced squares of order 7 in %.1fs%n",
        reservoir.size(), (System.nanoTime() - started) / 1e9);

    started = System.nanoTime();
    Map<LatinSquare, LatinSquare> byCanonical = new LinkedHashMap<>();
    for (LatinSquare square : reservoir) {
      byCanonical.putIfAbsent(Isotopy.canonical(square), square);
    }
    List<LatinSquare> representatives = new ArrayList<>(byCanonical.values());
    System.out.printf(
        "found %d distinct isotopy classes in %.1fs (of 564 total)%n",
        representatives.size(), (System.nanoTime() - started) / 1e9);

    int toProfile = Math.min(maxClasses, representatives.size());
    System.out.printf("profiling %d of them%n%n", toProfile);

    Map<String, List<Integer>> byProfile = new LinkedHashMap<>();
    int collisions = 0;
    for (int index = 0; index < toProfile; index++) {
      long each = System.nanoTime();
      Classifier.Profile profile = Classifier.profile(representatives.get(index), LIMIT);
      String key = profile.rows() + " || " + profile.columns();
      List<Integer> sharing = byProfile.computeIfAbsent(key, k -> new ArrayList<>());
      sharing.add(index);
      System.out.printf(
          "  class %-4d rows %-3d columns %-3d shapes   %.1fs%s%n",
          index,
          profile.rows().size(),
          profile.columns().size(),
          (System.nanoTime() - each) / 1e9,
          sharing.size() > 1
              ? "   <<< COLLISION with " + sharing.subList(0, sharing.size() - 1)
              : "");
      if (sharing.size() > 1) {
        collisions++;
        System.out.println("  --- colliding representatives ---");
        for (int other : sharing) {
          System.out.println(representatives.get(other));
          System.out.println();
        }
      }
    }

    System.out.printf(
        "%nprofiled %d classes, %d distinct profiles, %d collisions%n",
        toProfile, byProfile.size(), collisions);
    if (collisions == 0) {
      System.out.println(
          "no counterexample in this sample; this does not establish completeness, since "
              + (564 - toProfile)
              + " of the 564 classes were not profiled");
    }
  }

  private static List<LatinSquare> reservoir(int n, int wanted) {
    List<LatinSquare> reservoir = new ArrayList<>(wanted);
    Random random = new Random(20260812L);
    long[] seen = {0};
    LatinSquares.forEachReduced(
        n,
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
    return reservoir;
  }
}

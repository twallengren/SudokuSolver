package org.example.perm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The D-orbit invariant of a Latin square: the multiset of orbit shapes over every ordering of the
 * rows, paired with the same along the columns.
 *
 * <p>Both halves are isotopy invariants, and pairing them is what separates conjugate classes,
 * since transposing exchanges the two directions.
 *
 * <p>Only one ordering in each dihedral orbit is actually evaluated. D commutes with rotating a
 * cyclic sequence, and reversing a sequence inverts each difference and reverses it, so both leave
 * the orbit shape alone. Orderings therefore come in groups of up to {@code 2n} sharing a spectrum,
 * and skipping the duplicates is exact rather than approximate — worth a factor of 14 at order 7.
 */
public final class Classifier {

  private Classifier() {}

  /** The paired invariant: row profile and column profile. */
  public record Profile(Map<String, Integer> rows, Map<String, Integer> columns) {

    /** Whether two squares share the invariant, and so may be isotopic. */
    public boolean matches(Profile other) {
      return rows.equals(other.rows) && columns.equals(other.columns);
    }
  }

  /** The paired invariant of a square. */
  public static Profile profile(LatinSquare square, int limit) {
    return new Profile(profile(square, true, limit), profile(square, false, limit));
  }

  /** One direction of the invariant. */
  public static Map<String, Integer> profile(LatinSquare square, boolean alongRows, int limit) {
    List<Permutation> terms =
        (alongRows ? square.rowsAsSequence() : square.columnsAsSequence()).steps();
    int n = terms.size();
    Map<String, Integer> profile = new TreeMap<>();
    for (int[] ordering : dihedralRepresentatives(n)) {
      List<Permutation> reordered = new ArrayList<>(n);
      for (int index : ordering) {
        reordered.add(terms.get(index));
      }
      String shape =
          Derivative.spectrum(
                  PermutationSequence.of(reordered),
                  Derivative.Wrap.CYCLIC,
                  Derivative.Quotient.AFTER,
                  limit)
              .map(Derivative.Spectrum::toString)
              .orElse("unresolved");
      profile.merge(shape, dihedralOrbitSize(ordering), Integer::sum);
    }
    return profile;
  }

  /**
   * One ordering from each orbit of the dihedral action — rotation and reversal — on the {@code n!}
   * orderings.
   */
  static List<int[]> dihedralRepresentatives(int n) {
    List<int[]> representatives = new ArrayList<>();
    int[] ordering = new int[n];
    boolean[] used = new boolean[n];
    collect(0, ordering, used, representatives);
    return representatives;
  }

  private static void collect(int index, int[] ordering, boolean[] used, List<int[]> out) {
    int n = ordering.length;
    if (index == n) {
      if (isDihedralRepresentative(ordering)) {
        out.add(ordering.clone());
      }
      return;
    }
    for (int value = 0; value < n; value++) {
      if (used[value]) {
        continue;
      }
      used[value] = true;
      ordering[index] = value;
      collect(index + 1, ordering, used, out);
      used[value] = false;
    }
  }

  /** Whether this ordering is the lexicographically least in its dihedral orbit. */
  private static boolean isDihedralRepresentative(int[] ordering) {
    for (int[] variant : dihedralOrbit(ordering)) {
      if (compare(variant, ordering) < 0) {
        return false;
      }
    }
    return true;
  }

  /** How many of the {@code n!} orderings share this one's spectrum. */
  private static int dihedralOrbitSize(int[] ordering) {
    List<int[]> distinct = new ArrayList<>();
    for (int[] variant : dihedralOrbit(ordering)) {
      boolean seen = false;
      for (int[] existing : distinct) {
        if (compare(existing, variant) == 0) {
          seen = true;
          break;
        }
      }
      if (!seen) {
        distinct.add(variant);
      }
    }
    return distinct.size();
  }

  /** The rotations of an ordering and of its reversal. */
  private static List<int[]> dihedralOrbit(int[] ordering) {
    int n = ordering.length;
    List<int[]> orbit = new ArrayList<>(2 * n);
    for (int shift = 0; shift < n; shift++) {
      int[] rotated = new int[n];
      int[] reflected = new int[n];
      for (int i = 0; i < n; i++) {
        rotated[i] = ordering[(shift + i) % n];
        reflected[i] = ordering[Math.floorMod(shift - i, n)];
      }
      orbit.add(rotated);
      orbit.add(reflected);
    }
    return orbit;
  }

  private static int compare(int[] a, int[] b) {
    return java.util.Arrays.compare(a, b);
  }
}

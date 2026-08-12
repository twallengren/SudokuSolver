package org.example.explore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
 * Tests whether D sees isotopy classes.
 *
 * <p>A single square's spectrum depends on the order of its rows, so it cannot be an isotopy
 * invariant on its own. The multiset of spectra over <em>all</em> row orderings is one by
 * construction: reordering rows only permutes the multiset, while permuting columns and relabelling
 * symbols leave each spectrum alone. The question is whether that profile is fine enough to
 * separate the classes.
 */
public final class SpectrumProfileSurvey {

  private SpectrumProfileSurvey() {}

  private static final int LIMIT = 30_000;

  public static void main(String[] args) {
    for (int n : new int[] {4, 5, 6}) {
      System.out.println("===== order " + n + " =====");
      List<LatinSquare> representatives = isotopyRepresentatives(n);
      System.out.println("  isotopy classes: " + representatives.size());

      Map<String, List<Integer>> profileToClasses = new LinkedHashMap<>();
      for (int index = 0; index < representatives.size(); index++) {
        Map<String, Integer> profile = spectrumProfile(representatives.get(index));
        int converging =
            profile.entrySet().stream()
                .filter(e -> e.getKey().contains("identity"))
                .mapToInt(Map.Entry::getValue)
                .sum();
        int orderings = profile.values().stream().mapToInt(Integer::intValue).sum();
        System.out.printf(
            "  class %-3d converge %4d/%-4d  distinct spectra %-3d  %s%n",
            index + 1, converging, orderings, profile.size(), summarise(profile));
        profileToClasses
            .computeIfAbsent(profile.toString(), key -> new ArrayList<>())
            .add(index + 1);
      }

      System.out.println("  distinct profiles: " + profileToClasses.size());
      profileToClasses.values().stream()
          .filter(classes -> classes.size() > 1)
          .forEach(classes -> System.out.println("    classes sharing a profile: " + classes));
      System.out.println();
    }
  }

  /** One square per isotopy class, found among the reduced squares. */
  private static List<LatinSquare> isotopyRepresentatives(int n) {
    Map<LatinSquare, LatinSquare> byCanonical = new LinkedHashMap<>();
    for (LatinSquare square : LatinSquares.reduced(n)) {
      byCanonical.putIfAbsent(Isotopy.canonical(square), square);
    }
    return new ArrayList<>(byCanonical.values());
  }

  /** The multiset of spectra over every ordering of the rows. */
  private static Map<String, Integer> spectrumProfile(LatinSquare square) {
    List<Permutation> rows = square.rowsAsSequence().steps();
    Map<String, Integer> profile = new TreeMap<>();
    for (Permutation ordering : Permutations.all(square.order())) {
      List<Permutation> reordered = new ArrayList<>(rows.size());
      for (int i = 0; i < rows.size(); i++) {
        reordered.add(rows.get(ordering.imageOf(i)));
      }
      Optional<Spectrum> spectrum =
          Derivative.spectrum(
              PermutationSequence.of(reordered), Wrap.CYCLIC, Quotient.AFTER, LIMIT);
      profile.merge(spectrum.map(Spectrum::toString).orElse("unresolved"), 1, Integer::sum);
    }
    return profile;
  }

  private static String summarise(Map<String, Integer> profile) {
    if (profile.size() > 4) {
      return profile.entrySet().stream()
              .limit(3)
              .map(e -> e.getKey() + " x" + e.getValue())
              .reduce((a, b) -> a + ", " + b)
              .orElse("")
          + ", ...";
    }
    return profile.entrySet().stream()
        .map(e -> e.getKey() + " x" + e.getValue())
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }
}

package org.example.perm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.example.explore.LatinSquares;
import org.example.explore.Permutations;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Spectrum;
import org.example.perm.Derivative.Wrap;
import org.junit.jupiter.api.Test;

class IsotopyTest {

  private static List<LatinSquare> isotopyRepresentatives(int n) {
    Map<LatinSquare, LatinSquare> byCanonical = new LinkedHashMap<>();
    for (LatinSquare square : LatinSquares.reduced(n)) {
      byCanonical.putIfAbsent(Isotopy.canonical(square), square);
    }
    return new ArrayList<>(byCanonical.values());
  }

  /** The multiset of D-spectra over every ordering of the rows. */
  private static Map<String, Integer> spectrumProfile(LatinSquare square) {
    List<Permutation> rows = square.rowsAsSequence().steps();
    Map<String, Integer> profile = new TreeMap<>();
    for (Permutation ordering : Permutations.all(square.order())) {
      List<Permutation> reordered = new ArrayList<>();
      for (int i = 0; i < rows.size(); i++) {
        reordered.add(rows.get(ordering.imageOf(i)));
      }
      profile.merge(
          Derivative.spectrum(PermutationSequence.of(reordered), Wrap.CYCLIC, Quotient.AFTER, 5_000)
              .map(Spectrum::toString)
              .orElse("unresolved"),
          1,
          Integer::sum);
    }
    return profile;
  }

  @Test
  void reproducesThePublishedClassCounts() {
    // Isotopy classes: 1, 1, 2, 2 for n = 2..5. Main classes agree at these orders.
    assertEquals(1, countClasses(2, false));
    assertEquals(1, countClasses(3, false));
    assertEquals(2, countClasses(4, false));
    assertEquals(2, countClasses(5, false));
    assertEquals(2, countClasses(4, true));
    assertEquals(2, countClasses(5, true));
  }

  private static int countClasses(int n, boolean mainClass) {
    Set<LatinSquare> classes = new HashSet<>();
    for (LatinSquare square : LatinSquares.reduced(n)) {
      classes.add(mainClass ? Isotopy.mainClassCanonical(square) : Isotopy.canonical(square));
    }
    return classes.size();
  }

  @Test
  void canonicalFormIsIdempotent() {
    for (LatinSquare square : LatinSquares.reduced(5)) {
      LatinSquare canonical = Isotopy.canonical(square);
      assertEquals(canonical, Isotopy.canonical(canonical));
      assertTrue(canonical.isValid());
    }
  }

  @Test
  void canonicalFormSurvivesEveryIsotopy() {
    LatinSquare square = LatinSquares.reduced(5).get(3);
    LatinSquare canonical = Isotopy.canonical(square);
    for (Permutation p : Permutations.all(5)) {
      assertEquals(canonical, Isotopy.canonical(Symmetry.permuteRows(square, p)));
      assertEquals(canonical, Isotopy.canonical(Symmetry.permuteColumns(square, p)));
      assertEquals(canonical, Isotopy.canonical(Symmetry.relabelSymbols(square, p)));
    }
  }

  @Test
  void thereAreSixConjugatesAndTransposeIsOneOfThem() {
    LatinSquare square = LatinSquares.reduced(5).get(2);
    List<LatinSquare> conjugates = Isotopy.conjugates(square);
    assertEquals(6, conjugates.size());
    assertTrue(conjugates.stream().allMatch(LatinSquare::isValid));
    assertTrue(conjugates.contains(square));
    assertTrue(conjugates.contains(Symmetry.transpose(square)));
  }

  @Test
  void theSpectrumProfileIsAnIsotopyInvariant() {
    LatinSquare square = LatinSquares.reduced(5).get(1);
    Map<String, Integer> profile = spectrumProfile(square);
    for (Permutation p : List.of(Permutation.of(1, 0, 3, 2, 4), Permutation.of(2, 3, 4, 0, 1))) {
      assertEquals(profile, spectrumProfile(Symmetry.permuteRows(square, p)));
      assertEquals(profile, spectrumProfile(Symmetry.permuteColumns(square, p)));
      assertEquals(profile, spectrumProfile(Symmetry.relabelSymbols(square, p)));
    }
  }

  @Test
  void atOrderFiveTheProfileSeparatesTheTwoClassesCompletely() {
    List<LatinSquare> representatives = isotopyRepresentatives(5);
    assertEquals(2, representatives.size());
    Map<String, Integer> first = spectrumProfile(representatives.get(0));
    Map<String, Integer> second = spectrumProfile(representatives.get(1));
    assertNotEquals(first, second);

    // One class converges under every row ordering; the other under none.
    Map<String, Integer> converging =
        first.keySet().stream().anyMatch(k -> k.contains("identity")) ? first : second;
    Map<String, Integer> cycling = converging == first ? second : first;
    assertTrue(converging.keySet().stream().allMatch(k -> k.contains("identity")));
    assertTrue(cycling.keySet().stream().noneMatch(k -> k.contains("identity")));
    assertEquals(120, converging.values().stream().mapToInt(Integer::intValue).sum());
  }

  @Test
  void atOrderFourTheProfileSeparatesTheTwoClasses() {
    List<LatinSquare> representatives = isotopyRepresentatives(4);
    assertEquals(2, representatives.size());
    assertNotEquals(
        spectrumProfile(representatives.get(0)), spectrumProfile(representatives.get(1)));
  }
}

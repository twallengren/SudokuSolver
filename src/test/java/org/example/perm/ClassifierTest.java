package org.example.perm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.example.explore.LatinSquares;
import org.example.explore.Permutations;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Spectrum;
import org.example.perm.Derivative.Wrap;
import org.junit.jupiter.api.Test;

class ClassifierTest {

  private static final int LIMIT = 20_000;

  /** The unoptimised profile: every one of the n! orderings evaluated separately. */
  private static Map<String, Integer> naiveProfile(LatinSquare square, boolean alongRows) {
    List<Permutation> terms =
        (alongRows ? square.rowsAsSequence() : square.columnsAsSequence()).steps();
    Map<String, Integer> profile = new TreeMap<>();
    for (Permutation ordering : Permutations.all(square.order())) {
      List<Permutation> reordered = new ArrayList<>();
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

  @Test
  void dihedralReductionReproducesTheNaiveProfileExactly() {
    for (int n : new int[] {4, 5}) {
      for (LatinSquare square : LatinSquares.reduced(n)) {
        assertEquals(
            naiveProfile(square, true),
            Classifier.profile(square, true, LIMIT),
            "row profiles must agree at order " + n);
        assertEquals(
            naiveProfile(square, false),
            Classifier.profile(square, false, LIMIT),
            "column profiles must agree at order " + n);
      }
    }
  }

  @Test
  void weightsAccountForEveryOrdering() {
    for (int n : new int[] {3, 4, 5, 6}) {
      LatinSquare square = LatinSquares.reduced(n).get(0);
      long total =
          Classifier.profile(square, true, LIMIT).values().stream()
              .mapToLong(Integer::longValue)
              .sum();
      assertEquals(Permutations.factorial(n), total, "weights must sum to n! at order " + n);
    }
  }

  @Test
  void representativesAreFewerThanAllOrderings() {
    // Up to 2n orderings share a spectrum, so the saving approaches a factor of 2n.
    assertEquals(3, Classifier.dihedralRepresentatives(4).size());
    assertEquals(12, Classifier.dihedralRepresentatives(5).size());
    assertEquals(60, Classifier.dihedralRepresentatives(6).size());
    assertEquals(360, Classifier.dihedralRepresentatives(7).size());
  }

  @Test
  void pairedProfileSeparatesTheOrderFiveClasses() {
    List<LatinSquare> reduced = LatinSquares.reduced(5);
    LatinSquare cyclic =
        reduced.stream()
            .filter(s -> !Isotopy.canonical(s).equals(Isotopy.canonical(reduced.get(0))))
            .findFirst()
            .orElseThrow();
    assertFalse(
        Classifier.profile(reduced.get(0), LIMIT).matches(Classifier.profile(cyclic, LIMIT)));
  }

  @Test
  void isotopicSquaresShareTheProfile() {
    LatinSquare square = LatinSquares.reduced(5).get(1);
    Classifier.Profile expected = Classifier.profile(square, LIMIT);
    for (Permutation p : List.of(Permutation.of(1, 0, 3, 2, 4), Permutation.of(2, 3, 4, 0, 1))) {
      assertTrue(expected.matches(Classifier.profile(Symmetry.permuteRows(square, p), LIMIT)));
      assertTrue(expected.matches(Classifier.profile(Symmetry.permuteColumns(square, p), LIMIT)));
      assertTrue(expected.matches(Classifier.profile(Symmetry.relabelSymbols(square, p), LIMIT)));
    }
  }

  @Test
  void theMachineDecidesIsotopyExactly() {
    List<LatinSquare> reduced = LatinSquares.reduced(5);
    LatinSquare first = reduced.get(0);
    // Every isotopy of a square is in its class.
    for (Permutation p : Permutations.all(5)) {
      assertTrue(Isotopy.sameIsotopyClass(first, Symmetry.permuteRows(first, p)));
      assertTrue(Isotopy.sameIsotopyClass(first, Symmetry.permuteColumns(first, p)));
      assertTrue(Isotopy.sameIsotopyClass(first, Symmetry.relabelSymbols(first, p)));
      assertTrue(Isotopy.sameMainClass(first, Symmetry.transpose(first)));
    }
    // And the two order-5 classes are distinguished.
    LatinSquare other =
        reduced.stream().filter(s -> !Isotopy.sameIsotopyClass(first, s)).findFirst().orElseThrow();
    assertFalse(Isotopy.sameIsotopyClass(first, other));
  }

  @Test
  void squaresOfDifferentOrdersAreNeverRelated() {
    assertFalse(
        Isotopy.sameIsotopyClass(LatinSquares.reduced(4).get(0), LatinSquares.reduced(5).get(0)));
    assertFalse(
        Isotopy.sameMainClass(LatinSquares.reduced(4).get(0), LatinSquares.reduced(5).get(0)));
  }
}

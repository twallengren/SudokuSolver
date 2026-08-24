package org.example.explore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Spectrum;
import org.example.perm.Derivative.Wrap;
import org.example.perm.Isotopy;
import org.example.perm.LatinSquare;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;
import org.example.perm.Symmetry;

/**
 * Differentiating in both directions.
 *
 * <p>Differencing down the rows and differencing across the columns have complementary blind spots.
 * With the {@code AFTER} quotient a step is {@code b . a^-1}, so re-indexing every term on the
 * right cancels: permuting columns leaves the row differences untouched, and permuting rows leaves
 * the column differences untouched. Each direction is invariant under exactly the operation that
 * destroys the other, which is what makes pairing them worth trying.
 */
public final class DirectionalSurvey {

  private DirectionalSurvey() {}

  private static final int LIMIT = 30_000;

  public static void main(String[] args) {
    complementarity();
    separation(args.length > 0 ? Integer.parseInt(args[0]) : 6);
  }

  /**
   * Confirms the complementary invariance against every permutation in S_5. Testing a single cyclic
   * rotation would prove nothing: rotating a cyclic difference sequence only rotates its orbit, so
   * every combination would look invariant.
   */
  private static void complementarity() {
    System.out.println("===== complementary invariance: 100 squares x all 120 permutations =====");
    int rowUnderColumn = 0;
    int rowUnderRow = 0;
    int columnUnderRow = 0;
    int columnUnderColumn = 0;
    int total = 0;
    List<LatinSquare> squares = LatinSquares.withNaturalFirstRow(5).subList(0, 100);
    for (LatinSquare square : squares) {
      Spectrum rows = rowSpectrum(square);
      Spectrum columns = columnSpectrum(square);
      for (Permutation p : Permutations.all(5)) {
        total++;
        LatinSquare rowsMoved = Symmetry.permuteRows(square, p);
        LatinSquare columnsMoved = Symmetry.permuteColumns(square, p);
        if (rows.equals(rowSpectrum(columnsMoved))) {
          rowUnderColumn++;
        }
        if (rows.equals(rowSpectrum(rowsMoved))) {
          rowUnderRow++;
        }
        if (columns.equals(columnSpectrum(rowsMoved))) {
          columnUnderRow++;
        }
        if (columns.equals(columnSpectrum(columnsMoved))) {
          columnUnderColumn++;
        }
      }
    }
    System.out.printf("  row spectrum    under column permutation: %d/%d%n", rowUnderColumn, total);
    System.out.printf("  row spectrum    under row permutation:    %d/%d%n", rowUnderRow, total);
    System.out.printf("  column spectrum under row permutation:    %d/%d%n", columnUnderRow, total);
    System.out.printf(
        "  column spectrum under column permutation: %d/%d%n", columnUnderColumn, total);
    System.out.println();
  }

  /** Does pairing the two directional profiles separate the isotopy classes? */
  private static void separation(int n) {
    System.out.println("===== order " + n + ": paired directional profiles =====");
    Map<LatinSquare, LatinSquare> byCanonical = new LinkedHashMap<>();
    for (LatinSquare square : LatinSquares.reduced(n)) {
      byCanonical.putIfAbsent(Isotopy.canonical(square), square);
    }
    List<LatinSquare> representatives = new ArrayList<>(byCanonical.values());
    System.out.println("  isotopy classes: " + representatives.size());

    Map<String, List<Integer>> rowOnly = new LinkedHashMap<>();
    Map<String, List<Integer>> paired = new LinkedHashMap<>();
    for (int index = 0; index < representatives.size(); index++) {
      LatinSquare square = representatives.get(index);
      Map<String, Integer> rowProfile = profile(square, true);
      Map<String, Integer> columnProfile = profile(square, false);
      String rows = rowProfile.toString();
      String columns = columnProfile.toString();
      rowOnly.computeIfAbsent(rows, key -> new ArrayList<>()).add(index + 1);
      paired.computeIfAbsent(rows + " || " + columns, key -> new ArrayList<>()).add(index + 1);
      System.out.printf(
          "  class %-3d rows: %-3d distinct   columns: %-3d distinct%s%n",
          index + 1,
          rowProfile.size(),
          columnProfile.size(),
          rows.equals(columns) ? "   (directions agree)" : "");
    }
    System.out.println("  distinct row-only profiles: " + rowOnly.size());
    rowOnly.values().stream()
        .filter(classes -> classes.size() > 1)
        .forEach(classes -> System.out.println("    row-only collision: " + classes));
    System.out.println("  distinct paired profiles:   " + paired.size());
    paired.values().stream()
        .filter(classes -> classes.size() > 1)
        .forEach(classes -> System.out.println("    paired collision: " + classes));
  }

  private static Spectrum rowSpectrum(LatinSquare square) {
    return Derivative.spectrum(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, LIMIT)
        .orElseThrow();
  }

  private static Spectrum columnSpectrum(LatinSquare square) {
    return Derivative.spectrum(square.columnsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, LIMIT)
        .orElseThrow();
  }

  /** The multiset of spectra over every ordering, along rows or along columns. */
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

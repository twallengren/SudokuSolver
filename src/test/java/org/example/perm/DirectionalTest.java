package org.example.perm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.example.explore.LatinSquares;
import org.example.explore.Permutations;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Spectrum;
import org.example.perm.Derivative.Wrap;
import org.junit.jupiter.api.Test;

/**
 * The two directions in which a square can be differentiated have complementary blind spots. With
 * the {@code AFTER} quotient a step is {@code b . a^-1}, so re-indexing every term on the right
 * cancels out of the difference.
 */
class DirectionalTest {

  private static Spectrum rowSpectrum(LatinSquare square) {
    return Derivative.spectrum(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, 5_000)
        .orElseThrow();
  }

  private static Spectrum columnSpectrum(LatinSquare square) {
    return Derivative.spectrum(square.columnsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, 5_000)
        .orElseThrow();
  }

  @Test
  void columnsAreTheTransposedRows() {
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5).subList(0, 20)) {
      assertEquals(square.columnsAsSequence(), Symmetry.transpose(square).rowsAsSequence());
      assertEquals(square.rowsAsSequence(), Symmetry.transpose(square).columnsAsSequence());
    }
  }

  @Test
  void rowSpectrumSurvivesEveryColumnPermutation() {
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5).subList(0, 25)) {
      Spectrum expected = rowSpectrum(square);
      for (Permutation p : Permutations.all(5)) {
        assertEquals(expected, rowSpectrum(Symmetry.permuteColumns(square, p)));
      }
    }
  }

  @Test
  void columnSpectrumSurvivesEveryRowPermutation() {
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5).subList(0, 25)) {
      Spectrum expected = columnSpectrum(square);
      for (Permutation p : Permutations.all(5)) {
        assertEquals(expected, columnSpectrum(Symmetry.permuteRows(square, p)));
      }
    }
  }

  @Test
  void eachDirectionIsBrokenByItsOwnAxis() {
    // Not every permutation breaks it — a cyclic rotation only rotates the difference sequence, so
    // testing a single rotation would wrongly suggest full invariance.
    boolean rowsBroken = false;
    boolean columnsBroken = false;
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5).subList(0, 25)) {
      for (Permutation p : Permutations.all(5)) {
        if (!rowSpectrum(square).equals(rowSpectrum(Symmetry.permuteRows(square, p)))) {
          rowsBroken = true;
        }
        if (!columnSpectrum(square).equals(columnSpectrum(Symmetry.permuteColumns(square, p)))) {
          columnsBroken = true;
        }
      }
    }
    assertTrue(rowsBroken, "some row permutation must change the row spectrum");
    assertTrue(columnsBroken, "some column permutation must change the column spectrum");
  }

  @Test
  void cyclicRotationsPreserveBothDirections() {
    // The reason the previous test needs the full symmetric group rather than one sample.
    Permutation rotation = Permutation.of(1, 2, 3, 4, 0);
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5).subList(0, 25)) {
      assertEquals(rowSpectrum(square), rowSpectrum(Symmetry.permuteRows(square, rotation)));
      assertEquals(
          columnSpectrum(square), columnSpectrum(Symmetry.permuteColumns(square, rotation)));
    }
  }

  @Test
  void bothDirectionsSurviveRelabelling() {
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5).subList(0, 15)) {
      Spectrum rows = rowSpectrum(square);
      Spectrum columns = columnSpectrum(square);
      for (Permutation p : Permutations.all(5)) {
        assertEquals(rows, rowSpectrum(Symmetry.relabelSymbols(square, p)));
        assertEquals(columns, columnSpectrum(Symmetry.relabelSymbols(square, p)));
      }
    }
  }

  @Test
  void transposeExchangesTheTwoDirections() {
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5).subList(0, 20)) {
      assertEquals(rowSpectrum(square), columnSpectrum(Symmetry.transpose(square)));
      assertEquals(columnSpectrum(square), rowSpectrum(Symmetry.transpose(square)));
    }
  }

  @Test
  void rejectsColumnsThatAreNotPermutations() {
    List<LatinSquare> squares = LatinSquares.withNaturalFirstRow(4);
    assertTrue(squares.stream().allMatch(s -> s.columnsAsSequence().length() == 4));
  }
}

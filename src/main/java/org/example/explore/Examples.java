package org.example.explore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.example.perm.Classifier;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Wrap;
import org.example.perm.LatinSquare;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;
import org.example.perm.Symmetry;

/** Generates the worked examples used in the write-up, so every one of them is real output. */
public final class Examples {

  private Examples() {}

  private static final int LIMIT = 20_000;

  public static void main(String[] args) {
    squareToSquare();
    convergentAndNot();
    complementarity();
    orderFourProfiles();
  }

  /** Does D map a Latin square to another Latin square? */
  private static void squareToSquare() {
    System.out.println("=== A. D applied to a 4x4, shown as grids ===");
    LatinSquare square = cyclic(4);
    PermutationSequence state = square.rowsAsSequence();
    for (int step = 0; step <= 2; step++) {
      System.out.println("D^" + step + ":");
      System.out.println(asGrid(state));
      System.out.println(
          "  rows all permutations: yes   valid Latin square: "
              + (asSquare(state).isValid() ? "YES" : "NO"));
      state = Derivative.apply(state, Wrap.CYCLIC, Quotient.AFTER);
    }
    System.out.println();
  }

  /** One order-5 square that converges and one that does not. */
  private static void convergentAndNot() {
    System.out.println("=== B. Order 5: convergent vs cycling ===");
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5)) {
      var spectrum =
          Derivative.spectrum(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, LIMIT)
              .orElseThrow();
      if (spectrum.reachesIdentity() && spectrum.tail() == 4) {
        System.out.println(
            "converges, " + spectrum + ", rows form a group: " + rowsFormAGroup(square));
        System.out.println(square);
        System.out.println();
        break;
      }
    }
    for (LatinSquare square : LatinSquares.withNaturalFirstRow(5)) {
      var spectrum =
          Derivative.spectrum(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, LIMIT)
              .orElseThrow();
      if (!spectrum.reachesIdentity() && spectrum.tail() == 21) {
        System.out.println(
            "never reaches identity, "
                + spectrum
                + ", rows form a group: "
                + rowsFormAGroup(square));
        System.out.println(square);
        System.out.println();
        break;
      }
    }
  }

  /** What the row-versus-column check actually looks like on one square. */
  private static void complementarity() {
    System.out.println(
        "=== C. Row and column differencing under one row swap and one column swap ===");
    LatinSquare square = LatinSquares.withNaturalFirstRow(5).get(3);
    Permutation swap = Permutation.of(0, 2, 1, 3, 4); // swaps rows/columns 1 and 2
    System.out.println("start:");
    System.out.println(square);
    report("  original          ", square);
    report("  rows 1,2 swapped  ", Symmetry.permuteRows(square, swap));
    report("  cols 1,2 swapped  ", Symmetry.permuteColumns(square, swap));
    report("  symbols relabelled", Symmetry.relabelSymbols(square, Permutation.of(1, 2, 3, 4, 0)));
    System.out.println();
  }

  private static void report(String label, LatinSquare square) {
    System.out.printf(
        "%s  row spectrum: %-22s column spectrum: %s%n",
        label,
        Derivative.spectrum(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, LIMIT)
            .orElseThrow(),
        Derivative.spectrum(square.columnsAsSequence(), Wrap.CYCLIC, Quotient.AFTER, LIMIT)
            .orElseThrow());
  }

  /** At order 4 a whole profile is small enough to print in full. */
  private static void orderFourProfiles() {
    System.out.println("=== D. Complete profiles for the two order-4 isotopy classes ===");
    for (LatinSquare square : LatinSquares.reduced(4)) {
      Map<String, Integer> rows = Classifier.profile(square, true, LIMIT);
      Map<String, Integer> columns = Classifier.profile(square, false, LIMIT);
      System.out.println(square);
      System.out.println("  row profile:    " + rows);
      System.out.println("  column profile: " + columns);
      System.out.println();
    }
  }

  private static boolean rowsFormAGroup(LatinSquare square) {
    var rows = new java.util.HashSet<>(square.rowsAsSequence().steps());
    for (Permutation a : rows) {
      for (Permutation b : rows) {
        if (!rows.contains(a.andThen(b))) {
          return false;
        }
      }
    }
    return true;
  }

  private static String asGrid(PermutationSequence sequence) {
    StringBuilder sb = new StringBuilder();
    for (Permutation p : sequence.steps()) {
      sb.append("  ");
      int[] values = p.toArray();
      for (int i = 0; i < values.length; i++) {
        sb.append(values[i]).append(i < values.length - 1 ? " " : "");
      }
      sb.append(System.lineSeparator());
    }
    return sb.toString().stripTrailing();
  }

  private static LatinSquare asSquare(PermutationSequence sequence) {
    int n = sequence.order();
    int[][] grid = new int[n][];
    for (int i = 0; i < n; i++) {
      grid[i] = sequence.step(i).toArray();
    }
    return LatinSquare.fromGrid(grid);
  }

  private static LatinSquare cyclic(int n) {
    int[] shift = new int[n];
    for (int i = 0; i < n; i++) {
      shift[i] = (i + 1) % n;
    }
    List<Permutation> steps = new ArrayList<>();
    for (int i = 0; i < n - 1; i++) {
      steps.add(Permutation.of(shift));
    }
    return LatinSquare.fromSequence(PermutationSequence.of(steps));
  }
}

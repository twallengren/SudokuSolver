package org.example.perm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Canonical forms under isotopy — independently permuting rows, columns and symbols.
 *
 * <p>Two Latin squares are isotopic when some such triple carries one to the other. The canonical
 * form is the lexicographically least grid in the whole isotopy class, so two squares are isotopic
 * exactly when their canonical forms are equal.
 *
 * <p>The search is small because the least grid must start with the natural first row: choosing
 * which row leads and how the columns are permuted forces the symbol permutation, and the remaining
 * rows are then simply sorted. That leaves {@code n * n!} candidates rather than {@code (n!)^3}.
 */
public final class Isotopy {

  private Isotopy() {}

  /** The lexicographically least grid isotopic to {@code square}. */
  public static LatinSquare canonical(LatinSquare square) {
    int n = square.order();
    List<Permutation> columnPermutations = allPermutations(n);
    int[] best = null;
    int[] symbolMap = new int[n];
    for (int leadRow = 0; leadRow < n; leadRow++) {
      for (Permutation columns : columnPermutations) {
        // The lead row must become 0, 1, ..., n-1, which pins the symbol permutation.
        for (int column = 0; column < n; column++) {
          symbolMap[square.at(leadRow, column)] = columns.imageOf(column);
        }
        List<int[]> others = new ArrayList<>(n - 1);
        for (int row = 0; row < n; row++) {
          if (row == leadRow) {
            continue;
          }
          int[] mapped = new int[n];
          for (int column = 0; column < n; column++) {
            mapped[columns.imageOf(column)] = symbolMap[square.at(row, column)];
          }
          others.add(mapped);
        }
        others.sort(Isotopy::compare);

        int[] candidate = new int[n * n];
        for (int column = 0; column < n; column++) {
          candidate[column] = column;
        }
        int at = n;
        for (int[] row : others) {
          System.arraycopy(row, 0, candidate, at, n);
          at += n;
        }
        if (best == null || compare(candidate, best) < 0) {
          best = candidate;
        }
      }
    }
    int[][] grid = new int[n][n];
    for (int row = 0; row < n; row++) {
      System.arraycopy(best, row * n, grid[row], 0, n);
    }
    return LatinSquare.fromGrid(grid);
  }

  /**
   * The canonical form under the wider main-class equivalence, which also allows the roles of row,
   * column and symbol to be exchanged.
   */
  public static LatinSquare mainClassCanonical(LatinSquare square) {
    LatinSquare best = null;
    for (LatinSquare conjugate : conjugates(square)) {
      LatinSquare candidate = canonical(conjugate);
      if (best == null || compare(flatten(candidate), flatten(best)) < 0) {
        best = candidate;
      }
    }
    return best;
  }

  /**
   * The six conjugates of a square, one for each way of reading the triples {@code (row, column,
   * symbol)} as a Latin square.
   */
  public static List<LatinSquare> conjugates(LatinSquare square) {
    int n = square.order();
    int[][] order = {{0, 1, 2}, {0, 2, 1}, {1, 0, 2}, {1, 2, 0}, {2, 0, 1}, {2, 1, 0}};
    List<LatinSquare> result = new ArrayList<>(6);
    for (int[] roles : order) {
      int[][] grid = new int[n][n];
      for (int row = 0; row < n; row++) {
        for (int column = 0; column < n; column++) {
          int[] triple = {row, column, square.at(row, column)};
          grid[triple[roles[0]]][triple[roles[1]]] = triple[roles[2]];
        }
      }
      result.add(LatinSquare.fromGrid(grid));
    }
    return result;
  }

  private static int[] flatten(LatinSquare square) {
    int n = square.order();
    int[] flat = new int[n * n];
    for (int row = 0; row < n; row++) {
      System.arraycopy(square.row(row), 0, flat, row * n, n);
    }
    return flat;
  }

  private static int compare(int[] a, int[] b) {
    return Arrays.compare(a, b);
  }

  private static List<Permutation> allPermutations(int n) {
    List<Permutation> result = new ArrayList<>();
    int[] mapping = new int[n];
    boolean[] used = new boolean[n];
    extend(0, mapping, used, result);
    return result;
  }

  private static void extend(int index, int[] mapping, boolean[] used, List<Permutation> out) {
    int n = mapping.length;
    if (index == n) {
      out.add(Permutation.of(mapping));
      return;
    }
    for (int value = 0; value < n; value++) {
      if (used[value]) {
        continue;
      }
      used[value] = true;
      mapping[index] = value;
      extend(index + 1, mapping, used, out);
      used[value] = false;
    }
  }
}

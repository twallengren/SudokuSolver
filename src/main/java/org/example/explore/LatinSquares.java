package org.example.explore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.example.perm.LatinSquare;
import org.example.perm.PermutationSequence;

/**
 * Backtracking enumeration of Latin squares.
 *
 * <p>Squares are built one row at a time, tracking which symbols each column has already used. The
 * search is deterministic and single-threaded, which makes counts reproducible; the old
 * implementation spread the same work over a thread pool and wrote its results into the source
 * tree.
 *
 * <p>Counts grow very fast — there are 812,851,200 Latin squares of order 6 — so prefer the {@code
 * count} and {@code forEach} methods over the list-returning ones beyond order 5.
 */
public final class LatinSquares {

  private LatinSquares() {}

  /**
   * Visits every Latin square of order {@code n} whose first row is {@code 0, 1, ..., n-1}. There
   * are {@code L(n) / n!} of them.
   */
  public static void forEachWithNaturalFirstRow(int n, Consumer<LatinSquare> consumer) {
    if (n < 1) {
      throw new IllegalArgumentException("Order must be at least 1, got " + n);
    }
    int[][] grid = new int[n][n];
    int[] columnMasks = new int[n];
    for (int column = 0; column < n; column++) {
      grid[0][column] = column;
      columnMasks[column] = 1 << column;
    }
    if (n == 1) {
      consumer.accept(LatinSquare.fromGrid(grid));
      return;
    }
    fillRow(grid, columnMasks, 1, 0, 0, consumer);
  }

  private static void fillRow(
      int[][] grid,
      int[] columnMasks,
      int row,
      int column,
      int rowMask,
      Consumer<LatinSquare> consumer) {
    int n = grid.length;
    if (column == n) {
      if (row == n - 1) {
        consumer.accept(LatinSquare.fromGrid(grid));
        return;
      }
      fillRow(grid, columnMasks, row + 1, 0, 0, consumer);
      return;
    }
    for (int symbol = 0; symbol < n; symbol++) {
      int bit = 1 << symbol;
      if ((rowMask & bit) != 0 || (columnMasks[column] & bit) != 0) {
        continue;
      }
      grid[row][column] = symbol;
      columnMasks[column] |= bit;
      fillRow(grid, columnMasks, row, column + 1, rowMask | bit, consumer);
      columnMasks[column] &= ~bit;
    }
  }

  /** Every Latin square of order {@code n} whose first row is {@code 0, 1, ..., n-1}. */
  public static List<LatinSquare> withNaturalFirstRow(int n) {
    List<LatinSquare> squares = new ArrayList<>();
    forEachWithNaturalFirstRow(n, squares::add);
    return squares;
  }

  /** Every reduced Latin square of order {@code n} — first row and first column both natural. */
  public static List<LatinSquare> reduced(int n) {
    List<LatinSquare> squares = new ArrayList<>();
    forEachReduced(n, squares::add);
    return squares;
  }

  /**
   * Visits every reduced Latin square of order {@code n}, constraining the first column during the
   * search rather than filtering afterwards.
   *
   * <p>The difference matters past order 6: there are 16,942,080 reduced squares of order 7 but
   * 12,198,297,600 with merely the natural first row, so filtering would walk a thousand times more
   * ground than it keeps.
   */
  public static void forEachReduced(int n, Consumer<LatinSquare> consumer) {
    if (n < 1) {
      throw new IllegalArgumentException("Order must be at least 1, got " + n);
    }
    int[][] grid = new int[n][n];
    int[] columnMasks = new int[n];
    for (int column = 0; column < n; column++) {
      grid[0][column] = column;
      columnMasks[column] = 1 << column;
    }
    if (n == 1) {
      consumer.accept(LatinSquare.fromGrid(grid));
      return;
    }
    // The first column is forced to 0, 1, ..., n-1 as well.
    for (int row = 1; row < n; row++) {
      grid[row][0] = row;
      columnMasks[0] |= 1 << row;
    }
    fillReduced(grid, columnMasks, 1, 1, 1 << 1, consumer);
  }

  private static void fillReduced(
      int[][] grid,
      int[] columnMasks,
      int row,
      int column,
      int rowMask,
      Consumer<LatinSquare> consumer) {
    int n = grid.length;
    if (column == n) {
      if (row == n - 1) {
        consumer.accept(LatinSquare.fromGrid(grid));
        return;
      }
      fillReduced(grid, columnMasks, row + 1, 1, 1 << (row + 1), consumer);
      return;
    }
    for (int symbol = 0; symbol < n; symbol++) {
      int bit = 1 << symbol;
      if ((rowMask & bit) != 0 || (columnMasks[column] & bit) != 0) {
        continue;
      }
      grid[row][column] = symbol;
      columnMasks[column] |= bit;
      fillReduced(grid, columnMasks, row, column + 1, rowMask | bit, consumer);
      columnMasks[column] &= ~bit;
    }
  }

  /**
   * The permutation sequences of every Latin square of order {@code n} with the natural first row —
   * the chains of derangements this playground is built to study.
   */
  public static List<PermutationSequence> sequences(int n) {
    if (n < 2) {
      throw new IllegalArgumentException("A sequence needs at least two rows, so order >= 2");
    }
    List<PermutationSequence> sequences = new ArrayList<>();
    forEachWithNaturalFirstRow(n, square -> sequences.add(square.toSequence()));
    return sequences;
  }

  /** The number of Latin squares of order {@code n} with the natural first row. */
  public static long countWithNaturalFirstRow(int n) {
    long[] count = {0};
    forEachWithNaturalFirstRow(n, square -> count[0]++);
    return count[0];
  }

  /**
   * The total number of Latin squares of order {@code n}. Permuting symbols acts freely on the
   * first row, so this is {@code n!} times the count with the first row fixed.
   */
  public static long countAll(int n) {
    return Permutations.factorial(n) * countWithNaturalFirstRow(n);
  }

  /** The number of reduced Latin squares of order {@code n}. */
  public static long countReduced(int n) {
    long[] count = {0};
    forEachReduced(n, square -> count[0]++);
    return count[0];
  }
}

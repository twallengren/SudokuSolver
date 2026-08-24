package org.example.perm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An {@code n x n} grid of symbols {@code 0..n-1}, viewable either as a grid or as a base row
 * followed by a sequence of permutations.
 *
 * <p>The two views are interchangeable: {@link #fromSequence} builds a square by applying each step
 * to the previous row, and {@link #toSequence} recovers those steps from the grid. Instances are
 * immutable.
 *
 * <p>A grid is accepted even when it is not Latin, so that near-misses can be inspected; use {@link
 * #isValid()} to test the Latin property.
 */
public final class LatinSquare {

  private final int[][] grid;

  private LatinSquare(int[][] grid) {
    this.grid = grid;
  }

  /** Wraps a square grid whose entries all lie in {@code 0..n-1}. */
  public static LatinSquare fromGrid(int[][] grid) {
    Objects.requireNonNull(grid, "grid");
    int n = grid.length;
    if (n == 0) {
      throw new IllegalArgumentException("Grid must be non-empty");
    }
    int[][] copy = new int[n][];
    for (int row = 0; row < n; row++) {
      if (grid[row] == null || grid[row].length != n) {
        throw new IllegalArgumentException("Grid must be square; row " + row + " has wrong length");
      }
      for (int value : grid[row]) {
        if (value < 0 || value >= n) {
          throw new IllegalArgumentException(
              "Symbol " + value + " out of range 0.." + (n - 1) + " in row " + row);
        }
      }
      copy[row] = grid[row].clone();
    }
    return new LatinSquare(copy);
  }

  /**
   * Builds a square from a base row and a sequence of steps: row {@code k+1} is {@code
   * step(k).apply(row k)}.
   *
   * @param baseRow the first row; must be a permutation of {@code 0..n-1}
   * @param sequence exactly {@code n - 1} steps
   */
  public static LatinSquare fromSequence(int[] baseRow, PermutationSequence sequence) {
    Objects.requireNonNull(baseRow, "baseRow");
    Objects.requireNonNull(sequence, "sequence");
    int n = baseRow.length;
    if (sequence.order() != n) {
      throw new IllegalArgumentException(
          "Steps act on " + sequence.order() + " points but the base row has length " + n);
    }
    if (sequence.length() != n - 1) {
      throw new IllegalArgumentException(
          "A square of order " + n + " needs " + (n - 1) + " steps, got " + sequence.length());
    }
    // Validates that the base row really is a permutation of 0..n-1.
    Permutation.of(baseRow);

    int[][] rows = new int[n][];
    rows[0] = baseRow.clone();
    for (int k = 0; k < sequence.length(); k++) {
      rows[k + 1] = sequence.step(k).apply(rows[k]);
    }
    return new LatinSquare(rows);
  }

  /** Builds a square from the natural base row {@code 0, 1, ..., n-1} and a sequence of steps. */
  public static LatinSquare fromSequence(PermutationSequence sequence) {
    return fromSequence(naturalRow(sequence.order()), sequence);
  }

  private static int[] naturalRow(int n) {
    int[] row = new int[n];
    for (int i = 0; i < n; i++) {
      row[i] = i;
    }
    return row;
  }

  /** The side length of the square. */
  public int order() {
    return grid.length;
  }

  /** The symbol at the given position. */
  public int at(int row, int column) {
    return grid[row][column];
  }

  /** A copy of the given row. */
  public int[] row(int index) {
    return grid[index].clone();
  }

  /** A copy of the given column. */
  public int[] column(int index) {
    int[] column = new int[grid.length];
    for (int row = 0; row < grid.length; row++) {
      column[row] = grid[row][index];
    }
    return column;
  }

  /** A copy of the underlying grid. */
  public int[][] toGrid() {
    int[][] copy = new int[grid.length][];
    for (int row = 0; row < grid.length; row++) {
      copy[row] = grid[row].clone();
    }
    return copy;
  }

  /** Whether every row and every column contains each symbol exactly once. */
  public boolean isValid() {
    for (int i = 0; i < grid.length; i++) {
      if (!containsEachSymbolOnce(grid[i]) || !containsEachSymbolOnce(column(i))) {
        return false;
      }
    }
    return true;
  }

  private boolean containsEachSymbolOnce(int[] values) {
    boolean[] seen = new boolean[grid.length];
    for (int value : values) {
      if (seen[value]) {
        return false;
      }
      seen[value] = true;
    }
    return true;
  }

  /** Whether the first row and the first column are both in natural order. */
  public boolean isReduced() {
    for (int i = 0; i < grid.length; i++) {
      if (grid[0][i] != i || grid[i][0] != i) {
        return false;
      }
    }
    return true;
  }

  /**
   * The permutation carrying row {@code from} to row {@code to}: it sends index {@code i} to the
   * index at which row {@code to} holds the symbol that row {@code from} holds at {@code i}.
   *
   * @throws IllegalStateException if the two rows do not hold the same set of symbols
   */
  public Permutation rowPermutation(int from, int to) {
    int n = grid.length;
    int[] positionInTarget = new int[n];
    Arrays.fill(positionInTarget, -1);
    for (int index = 0; index < n; index++) {
      positionInTarget[grid[to][index]] = index;
    }
    int[] mapping = new int[n];
    for (int index = 0; index < n; index++) {
      int target = positionInTarget[grid[from][index]];
      if (target < 0) {
        throw new IllegalStateException(
            "Rows " + from + " and " + to + " do not hold the same symbols");
      }
      mapping[index] = target;
    }
    return Permutation.of(mapping);
  }

  /**
   * Recovers the sequence of row-to-row steps. Feeding the result back to {@link
   * #fromSequence(int[], PermutationSequence)} together with {@link #row(int) row(0)} reproduces
   * this square.
   *
   * @throws IllegalStateException if the rows are not all permutations of the same symbols
   */
  public PermutationSequence toSequence() {
    List<Permutation> steps = new ArrayList<>();
    for (int row = 0; row < grid.length - 1; row++) {
      steps.add(rowPermutation(row, row + 1));
    }
    return PermutationSequence.of(steps);
  }

  /**
   * The rows themselves as a sequence of permutations, each mapping a column index to the symbol it
   * holds. Unlike {@link #toSequence()} this keeps all {@code n} rows rather than the {@code n - 1}
   * steps between them, which is the natural starting point for iterating a difference operator.
   *
   * @throws IllegalArgumentException if some row is not a permutation of {@code 0..n-1}
   */
  public PermutationSequence rowsAsSequence() {
    List<Permutation> rows = new ArrayList<>();
    for (int[] row : grid) {
      rows.add(Permutation.of(row));
    }
    return PermutationSequence.of(rows);
  }

  /**
   * The columns as a sequence of permutations, each mapping a row index to the symbol it holds.
   * This is the transpose's {@link #rowsAsSequence()}, and differencing along it is the other
   * direction in which a square can be differentiated.
   *
   * @throws IllegalArgumentException if some column is not a permutation of {@code 0..n-1}
   */
  public PermutationSequence columnsAsSequence() {
    List<Permutation> columns = new ArrayList<>();
    for (int index = 0; index < grid.length; index++) {
      columns.add(Permutation.of(column(index)));
    }
    return PermutationSequence.of(columns);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LatinSquare other)) {
      return false;
    }
    return Arrays.deepEquals(grid, other.grid);
  }

  @Override
  public int hashCode() {
    return Arrays.deepHashCode(grid);
  }

  /** The grid, one row per line, symbols separated by spaces. */
  @Override
  public String toString() {
    int width = String.valueOf(grid.length - 1).length();
    StringBuilder sb = new StringBuilder();
    for (int row = 0; row < grid.length; row++) {
      if (row > 0) {
        sb.append(System.lineSeparator());
      }
      for (int column = 0; column < grid.length; column++) {
        if (column > 0) {
          sb.append(' ');
        }
        sb.append(String.format("%" + width + "d", grid[row][column]));
      }
    }
    return sb.toString();
  }
}

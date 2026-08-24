package org.example.perm;

import java.util.Objects;

/**
 * A Latin square carrying the extra Sudoku constraint: each of the {@code m x m} boxes must also
 * contain every symbol exactly once, where {@code m * m} is the order.
 *
 * <p>A Sudoku board is exactly a Latin square whose order is a perfect square and whose boxes
 * happen to be clean, so this type wraps a {@link LatinSquare} rather than replacing it, and the
 * permutation-sequence view carries over unchanged.
 */
public final class SudokuBoard {

  private final LatinSquare square;
  private final int boxSize;

  private SudokuBoard(LatinSquare square, int boxSize) {
    this.square = square;
    this.boxSize = boxSize;
  }

  /**
   * Wraps a square whose order is a perfect square. The box constraint is not enforced here; use
   * {@link #isValid()} to test it.
   */
  public static SudokuBoard of(LatinSquare square) {
    Objects.requireNonNull(square, "square");
    int order = square.order();
    int boxSize = (int) Math.round(Math.sqrt(order));
    if (boxSize * boxSize != order) {
      throw new IllegalArgumentException(
          "A Sudoku board needs an order that is a perfect square, got " + order);
    }
    return new SudokuBoard(square, boxSize);
  }

  /** Wraps a grid whose order is a perfect square. */
  public static SudokuBoard fromGrid(int[][] grid) {
    return of(LatinSquare.fromGrid(grid));
  }

  /** The underlying Latin square. */
  public LatinSquare square() {
    return square;
  }

  /** The side length of the board. */
  public int order() {
    return square.order();
  }

  /** The side length of one box; the square root of the order. */
  public int boxSize() {
    return boxSize;
  }

  /** Whether the board is Latin <em>and</em> every box contains each symbol exactly once. */
  public boolean isValid() {
    return square.isValid() && boxesAreValid();
  }

  /** Whether every box contains each symbol exactly once, ignoring rows and columns. */
  public boolean boxesAreValid() {
    for (int boxRow = 0; boxRow < boxSize; boxRow++) {
      for (int boxColumn = 0; boxColumn < boxSize; boxColumn++) {
        boolean[] seen = new boolean[order()];
        for (int row = 0; row < boxSize; row++) {
          for (int column = 0; column < boxSize; column++) {
            int symbol = square.at(boxRow * boxSize + row, boxColumn * boxSize + column);
            if (seen[symbol]) {
              return false;
            }
            seen[symbol] = true;
          }
        }
      }
    }
    return true;
  }

  /** The sequence of row-to-row permutations of the underlying square. */
  public PermutationSequence toSequence() {
    return square.toSequence();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SudokuBoard other)) {
      return false;
    }
    return square.equals(other.square);
  }

  @Override
  public int hashCode() {
    return square.hashCode();
  }

  /** The grid with box separators drawn in. */
  @Override
  public String toString() {
    int n = order();
    int width = String.valueOf(n - 1).length();
    StringBuilder sb = new StringBuilder();
    for (int row = 0; row < n; row++) {
      if (row > 0) {
        sb.append(System.lineSeparator());
      }
      if (row > 0 && row % boxSize == 0) {
        sb.append(horizontalRule(n, width)).append(System.lineSeparator());
      }
      for (int column = 0; column < n; column++) {
        if (column > 0) {
          sb.append(column % boxSize == 0 ? " | " : " ");
        }
        sb.append(String.format("%" + width + "d", square.at(row, column)));
      }
    }
    return sb.toString();
  }

  private String horizontalRule(int n, int width) {
    StringBuilder rule = new StringBuilder();
    for (int column = 0; column < n; column++) {
      if (column > 0) {
        rule.append(column % boxSize == 0 ? "-+-" : "-");
      }
      rule.append("-".repeat(width));
    }
    return rule.toString();
  }
}

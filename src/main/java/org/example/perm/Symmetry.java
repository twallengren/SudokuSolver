package org.example.perm;

/**
 * Transformations that carry Latin squares to Latin squares.
 *
 * <p>Every operation returns a new square, leaving the input untouched. Rearranging rows, columns,
 * or symbol labels preserves the Latin property, so these are the moves that generate a square's
 * equivalence class — and each one acts on the underlying {@linkplain LatinSquare#toSequence()
 * permutation sequence} in its own way, which is much of what there is to explore here.
 */
public final class Symmetry {

  private Symmetry() {}

  /** Reflects across the main diagonal, swapping the roles of rows and columns. */
  public static LatinSquare transpose(LatinSquare square) {
    int n = square.order();
    int[][] result = new int[n][n];
    for (int row = 0; row < n; row++) {
      for (int column = 0; column < n; column++) {
        result[column][row] = square.at(row, column);
      }
    }
    return LatinSquare.fromGrid(result);
  }

  /** Rotates the square a quarter turn clockwise. */
  public static LatinSquare rotateClockwise(LatinSquare square) {
    int n = square.order();
    int[][] result = new int[n][n];
    for (int row = 0; row < n; row++) {
      for (int column = 0; column < n; column++) {
        result[column][n - 1 - row] = square.at(row, column);
      }
    }
    return LatinSquare.fromGrid(result);
  }

  /** Rotates the square a quarter turn counterclockwise. */
  public static LatinSquare rotateCounterclockwise(LatinSquare square) {
    int n = square.order();
    int[][] result = new int[n][n];
    for (int row = 0; row < n; row++) {
      for (int column = 0; column < n; column++) {
        result[n - 1 - column][row] = square.at(row, column);
      }
    }
    return LatinSquare.fromGrid(result);
  }

  /** Mirrors left to right, reversing the order of the columns. */
  public static LatinSquare reflectHorizontally(LatinSquare square) {
    int n = square.order();
    int[][] result = new int[n][n];
    for (int row = 0; row < n; row++) {
      for (int column = 0; column < n; column++) {
        result[row][n - 1 - column] = square.at(row, column);
      }
    }
    return LatinSquare.fromGrid(result);
  }

  /** Mirrors top to bottom, reversing the order of the rows. */
  public static LatinSquare reflectVertically(LatinSquare square) {
    int n = square.order();
    int[][] result = new int[n][n];
    for (int row = 0; row < n; row++) {
      result[n - 1 - row] = square.row(row);
    }
    return LatinSquare.fromGrid(result);
  }

  /** Moves row {@code i} to row {@code permutation.imageOf(i)}. */
  public static LatinSquare permuteRows(LatinSquare square, Permutation permutation) {
    requireMatchingOrder(square, permutation);
    int n = square.order();
    int[][] result = new int[n][];
    for (int row = 0; row < n; row++) {
      result[permutation.imageOf(row)] = square.row(row);
    }
    return LatinSquare.fromGrid(result);
  }

  /** Moves column {@code j} to column {@code permutation.imageOf(j)}. */
  public static LatinSquare permuteColumns(LatinSquare square, Permutation permutation) {
    requireMatchingOrder(square, permutation);
    int n = square.order();
    int[][] result = new int[n][n];
    for (int row = 0; row < n; row++) {
      for (int column = 0; column < n; column++) {
        result[row][permutation.imageOf(column)] = square.at(row, column);
      }
    }
    return LatinSquare.fromGrid(result);
  }

  /** Replaces every symbol {@code s} with {@code permutation.imageOf(s)}. */
  public static LatinSquare relabelSymbols(LatinSquare square, Permutation permutation) {
    requireMatchingOrder(square, permutation);
    int n = square.order();
    int[][] result = new int[n][n];
    for (int row = 0; row < n; row++) {
      for (int column = 0; column < n; column++) {
        result[row][column] = permutation.imageOf(square.at(row, column));
      }
    }
    return LatinSquare.fromGrid(result);
  }

  /** Exchanges two rows. */
  public static LatinSquare swapRows(LatinSquare square, int a, int b) {
    return permuteRows(square, transposition(square.order(), a, b));
  }

  /** Exchanges two columns. */
  public static LatinSquare swapColumns(LatinSquare square, int a, int b) {
    return permuteColumns(square, transposition(square.order(), a, b));
  }

  /**
   * Brings a square to reduced form, with the first row and first column both in natural order, by
   * relabelling symbols and then reordering rows. Every Latin square has exactly one reduced form
   * under these two moves, so this is a convenient representative for comparing squares.
   *
   * @throws IllegalArgumentException if the square is not Latin
   */
  public static LatinSquare toReduced(LatinSquare square) {
    if (!square.isValid()) {
      throw new IllegalArgumentException("Only a valid Latin square can be reduced");
    }
    int n = square.order();

    // Relabel so that the first row reads 0, 1, ..., n-1.
    int[] relabel = new int[n];
    int[] firstRow = square.row(0);
    for (int index = 0; index < n; index++) {
      relabel[firstRow[index]] = index;
    }
    LatinSquare relabelled = relabelSymbols(square, Permutation.of(relabel));

    // Reorder rows so that the first column reads 0, 1, ..., n-1.
    int[] rowOrder = new int[n];
    for (int row = 0; row < n; row++) {
      rowOrder[row] = relabelled.at(row, 0);
    }
    return permuteRows(relabelled, Permutation.of(rowOrder));
  }

  private static Permutation transposition(int n, int a, int b) {
    if (a < 0 || a >= n || b < 0 || b >= n) {
      throw new IndexOutOfBoundsException(
          "Indices " + a + " and " + b + " must be within 0.." + (n - 1));
    }
    int[] mapping = Permutation.identity(n).toArray();
    mapping[a] = b;
    mapping[b] = a;
    return Permutation.of(mapping);
  }

  private static void requireMatchingOrder(LatinSquare square, Permutation permutation) {
    if (permutation.size() != square.order()) {
      throw new IllegalArgumentException(
          "Permutation of size "
              + permutation.size()
              + " cannot act on a square of order "
              + square.order());
    }
  }
}

package org.example;

import java.util.Set;

/** Represents 3x3 subgrid in sudoku puzzle */
public class SubGrid {

  private static final Set<Integer> VALID_POSITIONS = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

  private GridSquare[] gridSquares;

  SubGrid(GridSquare[] gridSquares) {
    this.gridSquares = gridSquares;
  }

  static class Builder {

    private final GridSquare[] gridSquares = new GridSquare[9];

    Builder addGridSquareAtPosition(GridSquare gridSquare, int position) {
      if (!VALID_POSITIONS.contains(position)) {
        throw new IllegalArgumentException("Position must be in " + VALID_POSITIONS);
      }
      if (gridSquares[position - 1] != null) {
        throw new IllegalArgumentException("Grid square already exists at position " + position);
      }
      gridSquares[position - 1] = gridSquare;
      return this;
    }

    SubGrid build() {
      return new SubGrid(gridSquares);
    }
  }
}

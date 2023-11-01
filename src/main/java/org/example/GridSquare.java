package org.example;

import java.util.*;

public class GridSquare {

  private static final Set<Integer> VALID_INTEGERS = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

  private Integer value;
  private GridSquare left;
  private GridSquare right;
  private GridSquare above;
  private GridSquare below;
  private final Set<Integer> invalidValues;
  int rowFormCoordX = -1;
  int rowFormCoordY = -1;
  int colFormCoordX = -1;
  int colFormCoordY = -1;
  int gridFormCoordX = -1;
  int gridFormCoordY = -1;

  GridSquare(Integer value) {

    if (value != null && !VALID_INTEGERS.contains(value)) {
      throw new IllegalArgumentException("Value must be in " + VALID_INTEGERS);
    }

    this.value = value;
    this.left = null;
    this.right = null;
    this.above = null;
    this.below = null;

    Set<Integer> invalidSet;
    if (value != null) {
      invalidSet = new HashSet<>(VALID_INTEGERS);
      invalidSet.remove(value);
    } else {
      invalidSet = new HashSet<>();
    }
    this.invalidValues = invalidSet;
  }

  void setRowFormCoordinate(int x, int y) {
    if (rowFormCoordX == -1) {
      rowFormCoordX = x;
    }
    if (rowFormCoordY == -1) {
      rowFormCoordY = y;
    }
  }

  void setColFormCoordinate(int x, int y) {
    if (colFormCoordX == -1) {
      colFormCoordX = x;
    }
    if (colFormCoordY == -1) {
      colFormCoordY = y;
    }
  }

  void setGridFormCoordinate(int x, int y) {
    if (gridFormCoordX == -1) {
      gridFormCoordX = x;
    }
    if (gridFormCoordY == -1) {
      gridFormCoordY = y;
    }
  }

  void setLeftNeighbor(GridSquare leftNeighbor) {
    if (left != null) {
      return;
    }
    this.left = leftNeighbor;
  }

  void setRightNeighbor(GridSquare rightNeighbor) {
    if (right != null) {
      return;
    }
    this.right = rightNeighbor;
  }

  void setAboveNeighbor(GridSquare aboveNeighbor) {
    if (above != null) {
      return;
    }
    this.above = aboveNeighbor;
  }

  void setBelowNeighbor(GridSquare belowNeighbor) {
    if (below != null) {
      return;
    }
    this.below = belowNeighbor;
  }

  void addInvalidValues(int[] invalidValues) {
    if (value != null) {
      return;
    }
    for (int invalidValue : invalidValues) {
      if (invalidValue == 0) {
        continue;
      }
      this.invalidValues.add(invalidValue);
    }
    if (this.invalidValues.size() > 8) {
      System.out.println("Oh no");
    }
    if (this.invalidValues.size() == 8) {
      Set<Integer> validValues = new HashSet<>(VALID_INTEGERS);
      validValues.removeAll(this.invalidValues);
      validValues.stream().findFirst().ifPresent(validValue -> value = validValue);
    }
  }

  Optional<Integer> getValue() {
    return Optional.ofNullable(value);
  }

  Optional<GridSquare> getLeftNeighbor() {
    return Optional.ofNullable(left);
  }

  Optional<GridSquare> getRightNeighbor() {
    return Optional.ofNullable(right);
  }

  Optional<GridSquare> getAboveNeighbor() {
    return Optional.ofNullable(above);
  }

  Optional<GridSquare> getBelowNeighbor() {
    return Optional.ofNullable(below);
  }

  Set<Integer> getInvalidValues() {
    return invalidValues;
  }
}

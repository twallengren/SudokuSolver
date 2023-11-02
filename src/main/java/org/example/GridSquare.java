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
  int rowFormX = -1;
  int rowFormY = -1;
  int colFormX = -1;
  int colFormY = -1;
  int gridFormX = -1;
  int gridFormY = -1;

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

  void setCoordinates(int rowX, int rowY) {
    setRowFormCoordinate(rowX, rowY);
    //noinspection SuspiciousNameCombination
    setColFormCoordinate(rowY, rowX);
    setGridFormCoordinate(rowY / 3, (rowX % 3) * 3 + (rowY % 3));
  }

  void setRowFormCoordinate(int x, int y) {
    if (rowFormX == -1) {
      rowFormX = x;
    }
    if (rowFormY == -1) {
      rowFormY = y;
    }
  }

  void setColFormCoordinate(int x, int y) {
    if (colFormX == -1) {
      colFormX = x;
    }
    if (colFormY == -1) {
      colFormY = y;
    }
  }

  void setGridFormCoordinate(int x, int y) {
    if (gridFormX == -1) {
      gridFormX = x;
    }
    if (gridFormY == -1) {
      gridFormY = y;
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

  void addInvalidValues(Set<Integer> invalidValues) {
    for (int invalidValue : invalidValues) {
      if (invalidValue == 0) {
        continue;
      }
      if (value != null && value == invalidValue) {
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

  boolean addInvalidValuesAndUpdate(Set<Integer> invalidValues) {
    for (int invalidValue : invalidValues) {
      if (invalidValue == 0) {
        continue;
      }
      if (value != null && value == invalidValue) {
        continue;
      }
      this.invalidValues.add(invalidValue);
    }
    if (this.invalidValues.size() > 8) {
      System.out.println("Oh no");
    }
    if (this.invalidValues.size() == 8 && value == null) {
      Set<Integer> validValues = new HashSet<>(VALID_INTEGERS);
      validValues.removeAll(this.invalidValues);
      validValues.stream().findFirst().ifPresent(validValue -> value = validValue);
      return true;
    }
    return false;
  }

  Optional<Integer> getValue() {
    return Optional.ofNullable(value);
  }

  boolean isInvalidValue(int value) {
    return invalidValues.contains(value);
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

  public void setValue(Integer value) {
    this.value = value;
  }
}

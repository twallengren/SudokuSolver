package org.example;

import java.util.*;

public class GridSquare {

  private static final Set<Integer> VALID_INTEGERS = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

  private Integer value;
  private GridSquare left;
  private GridSquare right;
  private GridSquare above;
  private GridSquare below;
  private Set<Integer> invalidValues;

  GridSquare(
      Integer value,
      GridSquare left,
      GridSquare right,
      GridSquare above,
      GridSquare below,
      Set<Integer> invalidValues) {
    this.value = value;
    this.left = left;
    this.right = right;
    this.above = above;
    this.below = below;
    this.invalidValues = invalidValues;
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

  static class Builder {

    private Integer value;
    private GridSquare left;
    private GridSquare right;
    private GridSquare above;
    private GridSquare below;
    private final Set<Integer> invalidValues = new HashSet<>();

    Builder withValue(int value) {
      if (!VALID_INTEGERS.contains(value)) {
        throw new IllegalArgumentException("Value must be in " + VALID_INTEGERS);
      }
      this.value = value;
      invalidValues.addAll(VALID_INTEGERS);
      invalidValues.remove(value);
      return this;
    }

    Builder withLeftNeighbor(GridSquare left) {
      this.left = left;
      left.getValue().ifPresent(invalidValues::add);
      return this;
    }

    Builder withRightNeighbor(GridSquare right) {
      this.right = right;
      right.getValue().ifPresent(invalidValues::add);
      return this;
    }

    Builder withAboveNeighbor(GridSquare above) {
      this.above = above;
      above.getValue().ifPresent(invalidValues::add);
      return this;
    }

    Builder withBelowNeighbor(GridSquare below) {
      this.below = below;
      below.getValue().ifPresent(invalidValues::add);
      return this;
    }

    GridSquare build() {
      return new GridSquare(value, left, right, above, below, invalidValues);
    }
  }
}

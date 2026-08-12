package org.example.perm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An ordered sequence of permutations, read as the steps that carry each row of a square to the
 * next.
 *
 * <p>This is the view the playground is built around. A square of order {@code n} is a base row plus
 * {@code n - 1} steps. The square is Latin exactly when every <em>gap</em> composition {@link
 * #between(int, int)} is a derangement: two rows may share no symbol in any column, and the
 * permutation carrying one row to the other is precisely what would have to fix a point for that to
 * happen.
 *
 * <p>Instances are immutable.
 */
public final class PermutationSequence {

  private final List<Permutation> steps;
  private final int order;

  private PermutationSequence(List<Permutation> steps, int order) {
    this.steps = steps;
    this.order = order;
  }

  /** A sequence of steps, all acting on the same number of points. */
  public static PermutationSequence of(List<Permutation> steps) {
    Objects.requireNonNull(steps, "steps");
    if (steps.isEmpty()) {
      throw new IllegalArgumentException("A permutation sequence needs at least one step");
    }
    int order = steps.get(0).size();
    for (Permutation step : steps) {
      Objects.requireNonNull(step, "step");
      if (step.size() != order) {
        throw new IllegalArgumentException(
            "All steps must act on the same number of points; found " + order + " and " + step.size());
      }
    }
    return new PermutationSequence(List.copyOf(steps), order);
  }

  /** A sequence of steps, all acting on the same number of points. */
  public static PermutationSequence of(Permutation... steps) {
    return of(Arrays.asList(steps));
  }

  /** The number of points each step acts on. */
  public int order() {
    return order;
  }

  /** The number of steps in this sequence. */
  public int length() {
    return steps.size();
  }

  /** The steps, as an unmodifiable list. */
  public List<Permutation> steps() {
    return steps;
  }

  /** The step at {@code index}. */
  public Permutation step(int index) {
    return steps.get(index);
  }

  /**
   * The composition of steps {@code from} (inclusive) through {@code to} (exclusive) — the single
   * permutation carrying row {@code from} to row {@code to}.
   */
  public Permutation between(int from, int to) {
    if (from < 0 || to > steps.size() || from > to) {
      throw new IndexOutOfBoundsException(
          "Invalid gap [" + from + ", " + to + ") for a sequence of " + steps.size() + " steps");
    }
    Permutation composed = Permutation.identity(order);
    for (int i = from; i < to; i++) {
      composed = composed.andThen(steps.get(i));
    }
    return composed;
  }

  /** The permutation carrying row 0 to row {@code k}. */
  public Permutation prefix(int k) {
    return between(0, k);
  }

  /** Whether every individual step is a derangement (adjacent rows share no column). */
  public boolean allStepsAreDerangements() {
    return steps.stream().allMatch(Permutation::isDerangement);
  }

  /**
   * Whether every gap composition is a derangement. This is exactly the condition for a square built
   * from this sequence to be Latin.
   */
  public boolean isLatin() {
    for (int from = 0; from <= steps.size(); from++) {
      for (int to = from + 1; to <= steps.size(); to++) {
        if (!between(from, to).isDerangement()) {
          return false;
        }
      }
    }
    return true;
  }

  /** The gap compositions that are not derangements, as {@code [from, to]} pairs. */
  public List<int[]> nonDerangementGaps() {
    List<int[]> offenders = new ArrayList<>();
    for (int from = 0; from <= steps.size(); from++) {
      for (int to = from + 1; to <= steps.size(); to++) {
        if (!between(from, to).isDerangement()) {
          offenders.add(new int[] {from, to});
        }
      }
    }
    return offenders;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PermutationSequence other)) {
      return false;
    }
    return order == other.order && steps.equals(other.steps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(order, steps);
  }

  /** One step per line, in cycle notation. */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < steps.size(); i++) {
      if (i > 0) {
        sb.append(System.lineSeparator());
      }
      sb.append("row ")
          .append(i)
          .append(" -> ")
          .append(i + 1)
          .append(": ")
          .append(steps.get(i).toCycleNotation())
          .append("  [")
          .append(steps.get(i))
          .append(']');
    }
    return sb.toString();
  }
}

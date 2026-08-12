package org.example.perm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An element of the symmetric group S_n, stored in one-line notation.
 *
 * <p>A permutation is a bijection on {@code {0, 1, ..., n-1}}. The value {@code imageOf(i)} is where
 * index {@code i} is sent. Instances are immutable.
 *
 * <p>The central operation for this project is {@link #apply(int[])}: a permutation acts on a row of
 * a square by moving the entry at index {@code i} to index {@code imageOf(i)}. Applying {@code p}
 * and then {@code q} is the same as applying {@code p.andThen(q)}, which is what lets a Latin square
 * be described as a sequence of permutations.
 */
public final class Permutation {

  private final int[] mapping;

  private Permutation(int[] mapping) {
    this.mapping = mapping;
  }

  /** Creates a permutation from its one-line notation, e.g. {@code of(4, 2, 3, 0, 1)}. */
  public static Permutation of(int... mapping) {
    validate(mapping);
    return new Permutation(mapping.clone());
  }

  /** The identity permutation on {@code n} points. */
  public static Permutation identity(int n) {
    if (n < 1) {
      throw new IllegalArgumentException("Permutation size must be at least 1, got " + n);
    }
    int[] mapping = new int[n];
    for (int i = 0; i < n; i++) {
      mapping[i] = i;
    }
    return new Permutation(mapping);
  }

  /**
   * Builds a permutation on {@code n} points from disjoint cycles, e.g. {@code fromCycles(5, new
   * int[] {0, 4, 1}, new int[] {2, 3})}. Points not mentioned are fixed.
   */
  public static Permutation fromCycles(int n, int[]... cycles) {
    int[] mapping = identity(n).mapping.clone();
    boolean[] seen = new boolean[n];
    for (int[] cycle : cycles) {
      for (int point : cycle) {
        if (point < 0 || point >= n) {
          throw new IllegalArgumentException("Cycle point " + point + " out of range for n=" + n);
        }
        if (seen[point]) {
          throw new IllegalArgumentException("Cycles must be disjoint; " + point + " repeats");
        }
        seen[point] = true;
      }
      for (int i = 0; i < cycle.length; i++) {
        mapping[cycle[i]] = cycle[(i + 1) % cycle.length];
      }
    }
    return new Permutation(mapping);
  }

  private static void validate(int[] mapping) {
    if (mapping == null || mapping.length == 0) {
      throw new IllegalArgumentException("Permutation must be non-empty");
    }
    boolean[] seen = new boolean[mapping.length];
    for (int value : mapping) {
      if (value < 0 || value >= mapping.length) {
        throw new IllegalArgumentException(
            "Value " + value + " out of range for a permutation of size " + mapping.length);
      }
      if (seen[value]) {
        throw new IllegalArgumentException("Value " + value + " appears more than once");
      }
      seen[value] = true;
    }
  }

  /** The number of points this permutation acts on. */
  public int size() {
    return mapping.length;
  }

  /** Where index {@code i} is sent. */
  public int imageOf(int i) {
    return mapping[i];
  }

  /** A copy of the one-line notation. */
  public int[] toArray() {
    return mapping.clone();
  }

  /**
   * Acts on a row: the entry at index {@code i} moves to index {@code imageOf(i)}.
   *
   * @return a new array; the input is not modified
   */
  public int[] apply(int[] row) {
    if (row.length != mapping.length) {
      throw new IllegalArgumentException(
          "Row of length " + row.length + " cannot be permuted by a permutation of size " + size());
    }
    int[] result = new int[row.length];
    for (int i = 0; i < row.length; i++) {
      result[mapping[i]] = row[i];
    }
    return result;
  }

  /** The permutation equivalent to applying {@code this} first and then {@code next}. */
  public Permutation andThen(Permutation next) {
    requireSameSize(next);
    int[] result = new int[mapping.length];
    for (int i = 0; i < mapping.length; i++) {
      result[i] = next.mapping[mapping[i]];
    }
    return new Permutation(result);
  }

  /** Standard function composition {@code this ∘ first}: apply {@code first}, then {@code this}. */
  public Permutation compose(Permutation first) {
    return first.andThen(this);
  }

  /** The inverse permutation, undoing this one. */
  public Permutation inverse() {
    int[] result = new int[mapping.length];
    for (int i = 0; i < mapping.length; i++) {
      result[mapping[i]] = i;
    }
    return new Permutation(result);
  }

  /** Whether this permutation moves nothing. */
  public boolean isIdentity() {
    for (int i = 0; i < mapping.length; i++) {
      if (mapping[i] != i) {
        return false;
      }
    }
    return true;
  }

  /**
   * Whether this permutation has no fixed points. Row-to-row permutations of a Latin square are
   * always derangements, since no symbol may stay in its column.
   */
  public boolean isDerangement() {
    for (int i = 0; i < mapping.length; i++) {
      if (mapping[i] == i) {
        return false;
      }
    }
    return true;
  }

  /** The number of points left in place. */
  public int fixedPoints() {
    int count = 0;
    for (int i = 0; i < mapping.length; i++) {
      if (mapping[i] == i) {
        count++;
      }
    }
    return count;
  }

  /**
   * The complete cycle decomposition, including fixed points as one-element cycles. Cycles are
   * ordered by their smallest element.
   */
  public List<int[]> cycles() {
    List<int[]> cycles = new ArrayList<>();
    boolean[] visited = new boolean[mapping.length];
    for (int start = 0; start < mapping.length; start++) {
      if (visited[start]) {
        continue;
      }
      List<Integer> cycle = new ArrayList<>();
      int node = start;
      while (!visited[node]) {
        visited[node] = true;
        cycle.add(node);
        node = mapping[node];
      }
      cycles.add(cycle.stream().mapToInt(Integer::intValue).toArray());
    }
    return cycles;
  }

  /** The multiplicative order: the least {@code k > 0} with {@code this^k == identity}. */
  public int order() {
    int order = 1;
    for (int[] cycle : cycles()) {
      order = lcm(order, cycle.length);
    }
    return order;
  }

  /** The sign of the permutation: {@code +1} if even, {@code -1} if odd. */
  public int sign() {
    return (mapping.length - cycles().size()) % 2 == 0 ? 1 : -1;
  }

  /** This permutation repeated {@code k} times. Negative {@code k} uses the inverse. */
  public Permutation power(int k) {
    if (k < 0) {
      return inverse().power(-k);
    }
    Permutation result = identity(mapping.length);
    Permutation base = this;
    for (int remaining = k; remaining > 0; remaining >>= 1) {
      if ((remaining & 1) == 1) {
        result = result.andThen(base);
      }
      base = base.andThen(base);
    }
    return result;
  }

  private static int lcm(int a, int b) {
    return a / gcd(a, b) * b;
  }

  private static int gcd(int a, int b) {
    return b == 0 ? a : gcd(b, a % b);
  }

  private void requireSameSize(Permutation other) {
    if (other.mapping.length != mapping.length) {
      throw new IllegalArgumentException(
          "Size mismatch: " + size() + " and " + other.size());
    }
  }

  /**
   * Cycle notation, omitting fixed points, e.g. {@code (0 4 1 2 3)}. The identity renders as {@code
   * ()}.
   */
  public String toCycleNotation() {
    StringBuilder sb = new StringBuilder();
    for (int[] cycle : cycles()) {
      if (cycle.length == 1) {
        continue;
      }
      sb.append('(');
      for (int i = 0; i < cycle.length; i++) {
        if (i > 0) {
          sb.append(' ');
        }
        sb.append(cycle[i]);
      }
      sb.append(')');
    }
    return sb.isEmpty() ? "()" : sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Permutation other)) {
      return false;
    }
    return Arrays.equals(mapping, other.mapping);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(mapping);
  }

  /** One-line notation, e.g. {@code 4,2,3,0,1}. */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < mapping.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(mapping[i]);
    }
    return sb.toString();
  }
}

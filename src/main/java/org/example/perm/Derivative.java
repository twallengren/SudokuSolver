package org.example.perm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * The difference operator <b>D</b> on a sequence of permutations, and the iteration order it
 * induces.
 *
 * <p>D replaces each term of a sequence by the permutation carrying it to the following term, the
 * group-valued analogue of a discrete derivative. Applying D repeatedly may reach the constant
 * identity sequence; the number of applications needed is the <em>order</em> of the starting
 * sequence.
 *
 * <p>Two choices have to be pinned down, and both are offered here because they are not equivalent
 * in a non-abelian group:
 *
 * <ul>
 *   <li>{@link Quotient} — whether the difference of {@code a} and {@code b} is the permutation
 *       applied <em>after</em> {@code a} to reach {@code b}, or the one applied <em>before</em>.
 *   <li>{@link Wrap} — whether the last term is compared with the first, keeping the length fixed,
 *       or the sequence shortens by one each time.
 * </ul>
 *
 * <p>The state space is finite, so iteration is always eventually periodic: D either reaches the
 * constant identity or falls into a cycle that never will. {@link #order} distinguishes the two.
 */
public final class Derivative {

  private Derivative() {}

  /** Which one-sided quotient to use as the difference of two permutations. */
  public enum Quotient {
    /** The {@code q} with {@code a.andThen(q).equals(b)} — the step applied after {@code a}. */
    AFTER,
    /** The {@code q} with {@code q.andThen(a).equals(b)} — the step applied before {@code a}. */
    BEFORE
  }

  /** Whether the sequence is treated as a cycle or as a finite list. */
  public enum Wrap {
    /** Compare the last term with the first; the length is preserved. */
    CYCLIC,
    /** Stop at the last adjacent pair; the length drops by one each time. */
    LINEAR
  }

  /** The difference of two permutations under the given convention. */
  public static Permutation difference(Permutation a, Permutation b, Quotient quotient) {
    return switch (quotient) {
      case AFTER -> a.inverse().andThen(b);
      case BEFORE -> b.andThen(a.inverse());
    };
  }

  /** One application of D. */
  public static PermutationSequence apply(
      PermutationSequence sequence, Wrap wrap, Quotient quotient) {
    List<Permutation> terms = sequence.steps();
    int length = terms.size();
    int resultLength = wrap == Wrap.CYCLIC ? length : length - 1;
    if (resultLength < 1) {
      throw new IllegalArgumentException(
          "D would empty a sequence of length " + length + " under " + wrap);
    }
    List<Permutation> result = new ArrayList<>(resultLength);
    for (int i = 0; i < resultLength; i++) {
      result.add(difference(terms.get(i), terms.get((i + 1) % length), quotient));
    }
    return PermutationSequence.of(result);
  }

  /** Whether every term is the identity — the fixed point D drives towards. */
  public static boolean isConstantIdentity(PermutationSequence sequence) {
    return sequence.steps().stream().allMatch(Permutation::isIdentity);
  }

  /**
   * The successive images of {@code sequence} under D, starting with {@code sequence} itself and
   * stopping once the constant identity is reached or a previously seen state repeats.
   */
  public static List<PermutationSequence> trajectory(
      PermutationSequence sequence, Wrap wrap, Quotient quotient) {
    List<PermutationSequence> states = new ArrayList<>();
    Set<PermutationSequence> seen = new HashSet<>();
    PermutationSequence current = sequence;
    while (seen.add(current)) {
      states.add(current);
      if (isConstantIdentity(current) || !canApply(current, wrap)) {
        break;
      }
      current = apply(current, wrap, quotient);
    }
    return states;
  }

  /** The default ceiling on the number of applications {@link #order} will try. */
  public static final int DEFAULT_LIMIT = 10_000;

  /**
   * The number of applications of D needed to reach the constant identity sequence, or empty if it
   * is not reached within {@link #DEFAULT_LIMIT} applications.
   */
  public static OptionalInt order(PermutationSequence sequence, Wrap wrap, Quotient quotient) {
    return order(sequence, wrap, quotient, DEFAULT_LIMIT);
  }

  /**
   * The number of applications of D needed to reach the constant identity sequence, or empty if the
   * iteration provably cycles first or does not get there within {@code limit} applications.
   *
   * <p>A bound is necessary: the orbit is eventually periodic only because the state space is
   * finite, and for a sequence of length {@code k} over {@code S_n} that space has {@code (n!)^k}
   * elements, so a non-converging orbit can run far longer than is practical to follow.
   */
  public static OptionalInt order(
      PermutationSequence sequence, Wrap wrap, Quotient quotient, int limit) {
    if (limit < 0) {
      throw new IllegalArgumentException("limit must be non-negative, got " + limit);
    }
    Set<PermutationSequence> seen = new HashSet<>();
    PermutationSequence current = sequence;
    for (int applications = 0; applications <= limit; applications++) {
      if (isConstantIdentity(current)) {
        return OptionalInt.of(applications);
      }
      if (!canApply(current, wrap) || !seen.add(current)) {
        return OptionalInt.empty();
      }
      current = apply(current, wrap, quotient);
    }
    return OptionalInt.empty();
  }

  /**
   * The shape of a D-orbit. Because the state space is finite the orbit is eventually periodic, so
   * it consists of a tail of length {@code tail} leading into a cycle of length {@code period}.
   *
   * <p>Reaching the constant identity is the special case {@code period == 1} at that fixed point,
   * and then {@code tail} is exactly the order in the sense of {@link #order}. Everything else ends
   * in a longer cycle, which is what makes {@code period} the natural generalisation of order.
   */
  public record Spectrum(int tail, int period, boolean reachesIdentity) {

    public Spectrum {
      if (tail < 0 || period < 1) {
        throw new IllegalArgumentException("Invalid spectrum: tail=" + tail + " period=" + period);
      }
    }

    /** The number of distinct states in the orbit. */
    public int visited() {
      return tail + period;
    }

    @Override
    public String toString() {
      return reachesIdentity
          ? "order " + tail + " (identity)"
          : "tail " + tail + ", cycle " + period;
    }
  }

  /**
   * The shape of the orbit of {@code sequence} under D, or empty if it is still neither periodic
   * nor exhausted after {@code limit} applications.
   */
  public static Optional<Spectrum> spectrum(
      PermutationSequence sequence, Wrap wrap, Quotient quotient, int limit) {
    if (limit < 0) {
      throw new IllegalArgumentException("limit must be non-negative, got " + limit);
    }
    Map<PermutationSequence, Integer> firstSeenAt = new HashMap<>();
    PermutationSequence current = sequence;
    for (int step = 0; step <= limit; step++) {
      Integer previous = firstSeenAt.putIfAbsent(current, step);
      if (previous != null) {
        int period = step - previous;
        return Optional.of(
            new Spectrum(previous, period, period == 1 && isConstantIdentity(current)));
      }
      if (!canApply(current, wrap)) {
        return Optional.empty();
      }
      current = apply(current, wrap, quotient);
    }
    return Optional.empty();
  }

  /** The shape of the orbit under D, using {@link #DEFAULT_LIMIT}. */
  public static Optional<Spectrum> spectrum(
      PermutationSequence sequence, Wrap wrap, Quotient quotient) {
    return spectrum(sequence, wrap, quotient, DEFAULT_LIMIT);
  }

  private static boolean canApply(PermutationSequence sequence, Wrap wrap) {
    return wrap == Wrap.CYCLIC || sequence.length() >= 2;
  }
}

package org.example.explore;

import java.util.ArrayList;
import java.util.List;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Wrap;
import org.example.perm.LatinSquare;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;

/**
 * The all-identity state is not the only fixed point of D.
 *
 * <p>{@code D(s) = s} means {@code s(i+1) = s(i) . s(i)} all the way round the cycle, so any
 * sequence of repeated squarings that closes up is fixed. Orbits can settle onto one of these
 * instead of onto the identity, which is the difference between "converges" and "reaches a period-1
 * cycle".
 */
public final class FixedPointProbe {

  private FixedPointProbe() {}

  private static final int LIMIT = 30_000;

  public static void main(String[] args) {
    // The order-6 representative of isotopy class 22.
    LatinSquare square =
        LatinSquare.fromGrid(
            new int[][] {
              {0, 1, 2, 3, 4, 5},
              {1, 2, 0, 4, 5, 3},
              {2, 0, 1, 5, 3, 4},
              {3, 5, 4, 1, 0, 2},
              {4, 3, 5, 2, 1, 0},
              {5, 4, 3, 0, 2, 1}
            });
    List<Permutation> rows = square.rowsAsSequence().steps();

    for (Permutation ordering : Permutations.all(6)) {
      List<Permutation> reordered = new ArrayList<>();
      for (int i = 0; i < rows.size(); i++) {
        reordered.add(rows.get(ordering.imageOf(i)));
      }
      PermutationSequence start = PermutationSequence.of(reordered);
      var spectrum = Derivative.spectrum(start, Wrap.CYCLIC, Quotient.AFTER, LIMIT).orElseThrow();
      if (spectrum.period() != 1 || spectrum.reachesIdentity()) {
        continue;
      }

      // Walk to the fixed point and show it.
      PermutationSequence state = start;
      for (int step = 0; step < spectrum.tail(); step++) {
        state = Derivative.apply(state, Wrap.CYCLIC, Quotient.AFTER);
      }
      System.out.println("Reached after " + spectrum.tail() + " steps, and D fixes it:");
      for (Permutation p : state.steps()) {
        System.out.println("  " + p + "   " + p.toCycleNotation());
      }
      System.out.println(
          "  is the all-identity state: "
              + Derivative.isConstantIdentity(state)
              + "    D(state) == state: "
              + Derivative.apply(state, Wrap.CYCLIC, Quotient.AFTER).equals(state));

      System.out.println("  each term is the square of the one before it:");
      List<Permutation> terms = state.steps();
      for (int i = 0; i < terms.size(); i++) {
        Permutation next = terms.get((i + 1) % terms.size());
        System.out.printf(
            "    term %d squared = %s   term %d = %s   %s%n",
            i,
            terms.get(i).andThen(terms.get(i)),
            (i + 1) % terms.size(),
            next,
            terms.get(i).andThen(terms.get(i)).equals(next) ? "match" : "MISMATCH");
      }
      return;
    }
    System.out.println("no non-identity fixed point found");
  }
}

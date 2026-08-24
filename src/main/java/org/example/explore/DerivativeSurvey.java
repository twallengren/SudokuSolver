package org.example.explore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.function.Function;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Wrap;
import org.example.perm.LatinSquare;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;
import org.example.perm.Symmetry;

/**
 * Surveys how the difference operator D behaves under each convention, so the definition that
 * actually converges can be identified empirically rather than guessed.
 */
public final class DerivativeSurvey {

  private DerivativeSurvey() {}

  private record Start(String name, Function<LatinSquare, PermutationSequence> extract) {}

  private static final List<Start> STARTS =
      List.of(
          new Start("rows ", LatinSquare::rowsAsSequence),
          new Start("steps", LatinSquare::toSequence));

  public static void main(String[] args) {
    for (int n : new int[] {4, 5}) {
      List<LatinSquare> squares = squaresFor(n);
      System.out.println("===== order " + n + ": " + squares.size() + " squares =====");
      for (Start start : STARTS) {
        for (Wrap wrap : Wrap.values()) {
          for (Quotient quotient : Quotient.values()) {
            report(squares, start, wrap, quotient);
          }
        }
      }
      System.out.println();
    }
    demonstrate();
  }

  /** All squares for n = 4 (cheap); natural first row only for n = 5. */
  private static List<LatinSquare> squaresFor(int n) {
    List<LatinSquare> base = LatinSquares.withNaturalFirstRow(n);
    if (n > 4) {
      return base;
    }
    List<LatinSquare> all = new ArrayList<>();
    for (LatinSquare square : base) {
      for (Permutation relabel : Permutations.all(n)) {
        all.add(Symmetry.relabelSymbols(square, relabel));
      }
    }
    return all;
  }

  private static void report(List<LatinSquare> squares, Start start, Wrap wrap, Quotient quotient) {
    Map<Integer, Integer> histogram = new TreeMap<>();
    int diverged = 0;
    for (LatinSquare square : squares) {
      OptionalInt order = Derivative.order(start.extract().apply(square), wrap, quotient);
      if (order.isPresent()) {
        histogram.merge(order.getAsInt(), 1, Integer::sum);
      } else {
        diverged++;
      }
    }
    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format("  start=%s wrap=%-6s quotient=%-6s -> ", start.name(), wrap, quotient));
    if (histogram.isEmpty()) {
      sb.append("never reaches the identity");
    } else {
      sb.append("orders ");
      histogram.forEach((order, count) -> sb.append(order).append("x").append(count).append("  "));
      if (diverged > 0) {
        sb.append("| ").append(diverged).append(" never reach it");
      } else {
        sb.append("| all converge");
      }
    }
    System.out.println(sb);
  }

  /** Shows one full trajectory, for the cyclic square of order 4. */
  private static void demonstrate() {
    System.out.println("===== trajectory: cyclic square of order 4, rows, CYCLIC, AFTER =====");
    int[] shift = {1, 2, 3, 0};
    List<Permutation> steps = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      steps.add(Permutation.of(shift));
    }
    LatinSquare square = LatinSquare.fromSequence(PermutationSequence.of(steps));
    System.out.println(square);
    List<PermutationSequence> trajectory =
        Derivative.trajectory(square.rowsAsSequence(), Wrap.CYCLIC, Quotient.AFTER);
    for (int i = 0; i < trajectory.size(); i++) {
      System.out.println(
          "D^"
              + i
              + ": "
              + trajectory.get(i).steps()
              + (Derivative.isConstantIdentity(trajectory.get(i))
                  ? "   <- constant identity"
                  : ""));
    }
  }
}

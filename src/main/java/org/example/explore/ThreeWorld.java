package org.example.explore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.example.perm.Derivative;
import org.example.perm.Derivative.Quotient;
import org.example.perm.Derivative.Wrap;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;

/** The COMPLETE universe of the machine at size 3: all 216 grids. */
public class ThreeWorld {
  public static void main(String[] args) {
    List<Permutation> s3 =
        new ArrayList<>(
            ConvergenceWhy.generate(List.of(Permutation.of(1, 2, 0), Permutation.of(1, 0, 2)), 10));

    Map<String, Integer> fateByParityClass = new TreeMap<>();
    Map<Integer, Integer> orderHistogram = new TreeMap<>(); // convergent: steps to die
    Map<String, Integer> loopSquaresCount = new LinkedHashMap<>(); // loop key -> basin size
    Map<String, List<PermutationSequence>> loopContent = new LinkedHashMap<>();
    Map<String, Integer> penByFate = new TreeMap<>();

    int convergent = 0, looping = 0;
    for (Permutation a : s3)
      for (Permutation b : s3)
        for (Permutation c : s3) {
          PermutationSequence st = PermutationSequence.of(a, b, c);
          var spec = Derivative.spectrum(st, Wrap.CYCLIC, Quotient.AFTER, 500).orElseThrow();

          int odd = (a.sign() < 0 ? 1 : 0) + (b.sign() < 0 ? 1 : 0) + (c.sign() < 0 ? 1 : 0);
          String pclass = odd == 0 ? "all even" : odd == 3 ? "all odd " : "mixed   ";

          var pen =
              ConvergenceWhy.generate(
                  Derivative.apply(st, Wrap.CYCLIC, Quotient.AFTER).steps(), 10);
          String fate = spec.reachesIdentity() ? "dies" : "loops";
          fateByParityClass.merge(pclass + " -> " + fate, 1, Integer::sum);
          penByFate.merge("pen size " + pen.size() + " -> " + fate, 1, Integer::sum);

          if (spec.reachesIdentity()) {
            convergent++;
            orderHistogram.merge(spec.tail(), 1, Integer::sum);
          } else {
            looping++;
            List<PermutationSequence> cyc = LoopAnatomy.cycleStates(st);
            String key = LoopAnatomy.rotationKey(cyc);
            loopSquaresCount.merge(key, 1, Integer::sum);
            loopContent.putIfAbsent(key, cyc);
          }
        }

    System.out.println("total grids: 216   die: " + convergent + "   loop forever: " + looping);
    System.out.println();
    System.out.println("fate by parity class:");
    fateByParityClass.forEach((k, v) -> System.out.println("  " + k + " : " + v));
    System.out.println();
    System.out.println("order (steps to die) among the convergent:");
    orderHistogram.forEach((k, v) -> System.out.println("  order " + k + " : " + v + " grids"));
    System.out.println();
    System.out.println("distinct loops: " + loopSquaresCount.size());
    for (var e : loopSquaresCount.entrySet()) {
      List<PermutationSequence> cyc = loopContent.get(e.getKey());
      var pen = new ArrayList<Permutation>();
      for (PermutationSequence stt : cyc) pen.addAll(stt.steps());
      int penSize = ConvergenceWhy.generate(pen, 10).size();
      StringBuilder sb = new StringBuilder();
      for (PermutationSequence stt : cyc) {
        sb.append("[");
        for (Permutation p : stt.steps()) {
          String cn = p.toCycleNotation();
          sb.append(cn.equals("()") ? "e" : cn.replace(" ", "")).append(" ");
        }
        sb.setLength(sb.length() - 1);
        sb.append("] ");
      }
      System.out.printf(
          "  loop length %d, basin %3d grids, loop-pen size %d:  %s%n",
          cyc.size(), e.getValue(), penSize, sb);
    }
    System.out.println();
    System.out.println("playpen size vs fate:");
    penByFate.forEach((k, v) -> System.out.println("  " + k + " : " + v));
  }
}

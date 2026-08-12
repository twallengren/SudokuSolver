package org.example.explore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.example.perm.Isotopy;
import org.example.perm.LatinSquare;

/**
 * Groups the isotopy classes of a given order into main classes, to see which isotopy classes are
 * conjugates of one another.
 */
public final class MainClassCheck {

  private MainClassCheck() {}

  public static void main(String[] args) {
    int n = args.length > 0 ? Integer.parseInt(args[0]) : 6;
    Map<LatinSquare, LatinSquare> byCanonical = new LinkedHashMap<>();
    for (LatinSquare square : LatinSquares.reduced(n)) {
      byCanonical.putIfAbsent(Isotopy.canonical(square), square);
    }
    List<LatinSquare> representatives = new ArrayList<>(byCanonical.values());
    System.out.println("isotopy classes at order " + n + ": " + representatives.size());

    Map<LatinSquare, List<Integer>> mainClasses = new LinkedHashMap<>();
    for (int index = 0; index < representatives.size(); index++) {
      mainClasses
          .computeIfAbsent(
              Isotopy.mainClassCanonical(representatives.get(index)), key -> new ArrayList<>())
          .add(index + 1);
    }
    System.out.println("main classes: " + mainClasses.size());
    int group = 1;
    for (List<Integer> members : mainClasses.values()) {
      System.out.println("  main class " + group++ + " contains isotopy classes " + members);
    }
  }
}

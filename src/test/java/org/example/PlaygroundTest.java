package org.example;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlaygroundTest {

  private ByteArrayOutputStream buffer;
  private Playground playground;

  @BeforeEach
  void setUp() {
    buffer = new ByteArrayOutputStream();
    playground = new Playground(new PrintStream(buffer, true, StandardCharsets.UTF_8));
  }

  private String run(String... commands) {
    buffer.reset();
    for (String command : commands) {
      playground.execute(command);
    }
    return buffer.toString(StandardCharsets.UTF_8);
  }

  @Test
  void describesAPermutation() {
    String output = run("perm 4,2,3,0,1");
    assertTrue(output.contains("(0 4 1 2 3)"), output);
    assertTrue(output.contains("order:        5"), output);
    assertTrue(output.contains("derangement:  yes"), output);
  }

  @Test
  void composesAndInverts() {
    assertTrue(run("compose 1,2,0 1,2,0").contains("2,0,1"));
    assertTrue(run("inverse 1,2,0").contains("2,0,1"));
    assertTrue(run("power 1,2,0 3").contains("0,1,2"));
  }

  @Test
  void buildsAndInspectsASquare() {
    String output = run("cyclic 4", "sequence");
    assertTrue(output.contains("valid Latin square: yes"), output);
    assertTrue(output.contains("all gaps are derangements:  yes"), output);
  }

  @Test
  void buildsFromAnExplicitSequence() {
    String output = run("square 1,2,3,0 1,2,3,0 1,2,3,0");
    assertTrue(output.contains("valid Latin square: yes"), output);
  }

  @Test
  void reportsAnInvalidSquareRatherThanFailing() {
    String output = run("square 1,0,3,2 1,0,3,2 1,0,3,2", "gaps");
    assertTrue(output.contains("valid Latin square: no"), output);
    assertTrue(output.contains("FIXES"), "the offending gap should be called out: " + output);
  }

  @Test
  void appliesSymmetries() {
    assertTrue(run("cyclic 5", "transpose").contains("valid Latin square: yes"));
    assertTrue(run("cyclic 5", "rotate ccw").contains("valid Latin square: yes"));
    assertTrue(run("cyclic 5", "reflect v").contains("valid Latin square: yes"));
    assertTrue(run("cyclic 5", "swaprows 0 2").contains("valid Latin square: yes"));
    assertTrue(run("cyclic 5", "reduce").contains("reduced: yes"));
  }

  @Test
  void viewsASquareAsASudokuBoard() {
    String output = run("grid 0,1,2,3 2,3,0,1 1,0,3,2 3,2,1,0", "sudoku");
    assertTrue(output.contains("valid Sudoku board: yes"), output);

    String latinOnly = run("grid 0,1,2,3 1,0,3,2 2,3,0,1 3,2,1,0", "sudoku");
    assertTrue(latinOnly.contains("boxes valid: no"), latinOnly);
  }

  @Test
  void countsAndEnumerates() {
    assertTrue(run("count squares 4").contains("L(4) = 576"));
    assertTrue(run("count reduced 5").contains("R(5) = 56"));
    assertTrue(run("count derangements 6").contains("!6 = 265"));
    assertTrue(run("list derangements 4").contains("9 derangement(s)"));
    assertTrue(run("enumerate 4 2").contains("24 square(s)"));
  }

  @Test
  void reportsErrorsWithoutCrashing() {
    assertTrue(run("perm 0,0,1").startsWith("error:"));
    assertTrue(run("show").startsWith("error:"));
    assertTrue(run("perm").startsWith("error:"));
    assertTrue(run("rotate sideways").startsWith("error:"));
    assertTrue(run("count squares 12").startsWith("error:"));
    assertTrue(run("cyclic 5", "sudoku").contains("error:"));
    assertTrue(run("wibble").contains("Unknown command"));
  }

  @Test
  void ignoresBlankLinesAndComments() {
    assertTrue(run("", "   ", "# a comment").isEmpty());
  }

  @Test
  void quitSignalsExit() {
    assertFalse(playground.execute("quit"));
    assertFalse(playground.execute("exit"));
    assertTrue(playground.execute("help"));
  }
}

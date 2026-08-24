package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.example.explore.LatinSquares;
import org.example.explore.Permutations;
import org.example.perm.Isotopy;
import org.example.perm.LatinSquare;
import org.example.perm.Permutation;
import org.example.perm.PermutationSequence;
import org.example.perm.SudokuBoard;
import org.example.perm.Symmetry;

/**
 * An interactive shell for exploring Latin squares and Sudoku boards as sequences of permutations.
 *
 * <p>Run with {@code mvn exec:java}. Commands may also be piped in on standard input, which makes
 * short exploration scripts easy to replay.
 */
public final class Playground {

  private final PrintStream out;
  private LatinSquare current;
  private LatinSquare stashed;

  Playground(PrintStream out) {
    this.out = out;
  }

  public static void main(String[] args) throws IOException {
    PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    Playground playground = new Playground(out);
    boolean interactive = System.console() != null;
    if (interactive) {
      out.println("Latin squares playground. Type 'help' for commands, 'quit' to leave.");
    }
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
      String line;
      while (true) {
        if (interactive) {
          out.print("> ");
          out.flush();
        }
        line = reader.readLine();
        if (line == null || !playground.execute(line)) {
          break;
        }
      }
    }
  }

  /**
   * Runs one command line.
   *
   * @return false when the shell should exit
   */
  boolean execute(String line) {
    String trimmed = line.trim();
    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
      return true;
    }
    String[] tokens = trimmed.split("\\s+");
    String command = tokens[0].toLowerCase();
    String[] rest = new String[tokens.length - 1];
    System.arraycopy(tokens, 1, rest, 0, rest.length);

    try {
      switch (command) {
        case "help", "?" -> printHelp();
        case "quit", "exit" -> {
          return false;
        }
        case "perm" -> describePermutation(requireOne(rest, "perm <permutation>"));
        case "inverse" -> {
          Permutation p = parsePermutation(requireOne(rest, "inverse <permutation>"));
          out.println(format(p.inverse()));
        }
        case "compose" -> compose(rest);
        case "power" -> power(rest);
        case "cyclic" -> setCyclic(rest);
        case "square" -> buildFromSequence(rest);
        case "grid" -> buildFromGrid(rest);
        case "show" -> show();
        case "sequence" -> showSequence();
        case "gaps" -> showGaps();
        case "transpose" -> replace(Symmetry.transpose(require()));
        case "rotate" -> rotate(rest);
        case "reflect" -> reflect(rest);
        case "swaprows" -> replace(Symmetry.swapRows(require(), index(rest, 0), index(rest, 1)));
        case "swapcols" -> replace(Symmetry.swapColumns(require(), index(rest, 0), index(rest, 1)));
        case "relabel" ->
            replace(
                Symmetry.relabelSymbols(
                    require(), parsePermutation(requireOne(rest, "relabel <permutation>"))));
        case "reduce" -> replace(Symmetry.toReduced(require()));
        case "stash" -> {
          stashed = require();
          out.println("stashed the current square");
        }
        case "canonical" -> out.println(Isotopy.canonical(require()));
        case "isotopic" -> compareWithStash();
        case "sudoku" -> showSudoku();
        case "count" -> count(rest);
        case "list" -> list(rest);
        case "enumerate" -> enumerate(rest);
        default -> out.println("Unknown command '" + command + "'. Type 'help' for the list.");
      }
    } catch (RuntimeException e) {
      String message = e.getMessage();
      out.println("error: " + (message == null ? e.getClass().getSimpleName() : message));
    }
    return true;
  }

  private void printHelp() {
    out.println(
        """
        Permutations (one-line notation, e.g. 4,2,3,0,1 or "4 2 3 0 1" without spaces around it)
          perm <p>                 describe a permutation: cycles, order, sign, fixed points
          inverse <p>              the inverse permutation
          compose <p> <p> [...]    compose left to right (apply the first, then the next)
          power <p> <k>            repeat a permutation k times (k may be negative)

        Building a square (the current square is what the other commands act on)
          cyclic <n>               the cyclic square of order n
          square <p1> <p2> [...]   build from a sequence of steps, natural first row
          grid <r1> <r2> [...]     build from explicit rows

        Inspecting the current square
          show                     the grid, with its Latin validity
          sequence                 the row-to-row permutations
          gaps                     every gap composition and whether it is a derangement
          sudoku                   view as a Sudoku board, checking the box constraint

        Transforming the current square
          transpose                reflect across the main diagonal
          rotate [cw|ccw]          quarter turn, clockwise by default
          reflect [h|v]            mirror horizontally or vertically
          swaprows <a> <b>         exchange two rows
          swapcols <a> <b>         exchange two columns
          relabel <p>              rename the symbols
          reduce                   canonical form: first row and column in natural order
          canonical                the least grid isotopic to this one
          stash                    remember the current square for comparison
          isotopic                 is the current square isotopic to the stashed one?

        Counting and enumerating
          count squares <n>        the number of Latin squares of order n
          count reduced <n>        the number of reduced Latin squares of order n
          count derangements <n>   the subfactorial !n
          list derangements <n>    every derangement of n points
          enumerate <n> [limit]    squares of order n with the natural first row

          help                     this message
          quit                     leave the playground
        """);
  }

  private void describePermutation(String token) {
    Permutation p = parsePermutation(token);
    out.println("one-line:     " + p);
    out.println("cycles:       " + p.toCycleNotation());
    out.println("size:         " + p.size());
    out.println("order:        " + p.order());
    out.println("sign:         " + (p.sign() > 0 ? "+1 (even)" : "-1 (odd)"));
    out.println("fixed points: " + p.fixedPoints());
    out.println("derangement:  " + (p.isDerangement() ? "yes" : "no"));
    out.println("inverse:      " + format(p.inverse()));
  }

  private void compose(String[] tokens) {
    if (tokens.length < 2) {
      throw new IllegalArgumentException("usage: compose <permutation> <permutation> [...]");
    }
    Permutation composed = parsePermutation(tokens[0]);
    for (int i = 1; i < tokens.length; i++) {
      composed = composed.andThen(parsePermutation(tokens[i]));
    }
    out.println(format(composed));
  }

  private void power(String[] tokens) {
    if (tokens.length != 2) {
      throw new IllegalArgumentException("usage: power <permutation> <k>");
    }
    Permutation p = parsePermutation(tokens[0]);
    out.println(format(p.power(parseInt(tokens[1], "k"))));
  }

  private void setCyclic(String[] tokens) {
    int n = parseInt(requireOne(tokens, "cyclic <n>"), "n");
    if (n < 2) {
      throw new IllegalArgumentException("Order must be at least 2");
    }
    int[] shift = new int[n];
    for (int i = 0; i < n; i++) {
      shift[i] = (i + 1) % n;
    }
    List<Permutation> steps = new ArrayList<>();
    for (int i = 0; i < n - 1; i++) {
      steps.add(Permutation.of(shift));
    }
    replace(LatinSquare.fromSequence(PermutationSequence.of(steps)));
  }

  private void buildFromSequence(String[] tokens) {
    if (tokens.length == 0) {
      throw new IllegalArgumentException("usage: square <permutation> [<permutation> ...]");
    }
    List<Permutation> steps = new ArrayList<>();
    for (String token : tokens) {
      steps.add(parsePermutation(token));
    }
    replace(LatinSquare.fromSequence(PermutationSequence.of(steps)));
  }

  private void buildFromGrid(String[] tokens) {
    if (tokens.length == 0) {
      throw new IllegalArgumentException("usage: grid <row> [<row> ...]");
    }
    int[][] grid = new int[tokens.length][];
    for (int row = 0; row < tokens.length; row++) {
      grid[row] = parseInts(tokens[row]);
    }
    replace(LatinSquare.fromGrid(grid));
  }

  private void replace(LatinSquare square) {
    current = square;
    show();
  }

  private void show() {
    LatinSquare square = require();
    out.println(square);
    out.println("order " + square.order() + ", valid Latin square: " + yesNo(square.isValid()));
    if (square.isValid()) {
      out.println("reduced: " + yesNo(square.isReduced()));
    }
  }

  private void showSequence() {
    LatinSquare square = require();
    if (square.order() < 2) {
      throw new IllegalArgumentException("A square of order 1 has no steps");
    }
    PermutationSequence sequence = square.toSequence();
    out.println(sequence);
    out.println("all steps are derangements: " + yesNo(sequence.allStepsAreDerangements()));
    out.println("all gaps are derangements:  " + yesNo(sequence.isLatin()));
  }

  private void showGaps() {
    LatinSquare square = require();
    if (square.order() < 2) {
      throw new IllegalArgumentException("A square of order 1 has no gaps");
    }
    PermutationSequence sequence = square.toSequence();
    for (int from = 0; from <= sequence.length(); from++) {
      for (int to = from + 1; to <= sequence.length(); to++) {
        Permutation gap = sequence.between(from, to);
        out.printf(
            "rows %d -> %d: %-24s %s%n",
            from,
            to,
            gap.toCycleNotation(),
            gap.isDerangement() ? "derangement" : "FIXES " + gap.fixedPoints() + " point(s)");
      }
    }
  }

  private void rotate(String[] tokens) {
    String direction = tokens.length == 0 ? "cw" : tokens[0].toLowerCase();
    replace(
        switch (direction) {
          case "cw" -> Symmetry.rotateClockwise(require());
          case "ccw" -> Symmetry.rotateCounterclockwise(require());
          default -> throw new IllegalArgumentException("usage: rotate [cw|ccw]");
        });
  }

  private void reflect(String[] tokens) {
    String axis = tokens.length == 0 ? "h" : tokens[0].toLowerCase();
    replace(
        switch (axis) {
          case "h" -> Symmetry.reflectHorizontally(require());
          case "v" -> Symmetry.reflectVertically(require());
          default -> throw new IllegalArgumentException("usage: reflect [h|v]");
        });
  }

  /** Decides isotopy between the stashed square and the current one. */
  private void compareWithStash() {
    LatinSquare square = require();
    if (stashed == null) {
      throw new IllegalStateException(
          "Nothing stashed. Build a square, run 'stash', then build another.");
    }
    if (stashed.order() != square.order()) {
      out.println("different orders, so neither isotopic nor in the same main class");
      return;
    }
    out.println("same isotopy class: " + yesNo(Isotopy.sameIsotopyClass(stashed, square)));
    out.println("same main class:    " + yesNo(Isotopy.sameMainClass(stashed, square)));
  }

  private void showSudoku() {
    SudokuBoard board = SudokuBoard.of(require());
    out.println(board);
    out.println("boxes valid: " + yesNo(board.boxesAreValid()));
    out.println("valid Sudoku board: " + yesNo(board.isValid()));
  }

  private void count(String[] tokens) {
    if (tokens.length != 2) {
      throw new IllegalArgumentException("usage: count <squares|reduced|derangements> <n>");
    }
    int n = parseInt(tokens[1], "n");
    switch (tokens[0].toLowerCase()) {
      case "squares" -> {
        warnIfSlow(n);
        out.println("L(" + n + ") = " + LatinSquares.countAll(n));
      }
      case "reduced" -> {
        warnIfSlow(n);
        out.println("R(" + n + ") = " + LatinSquares.countReduced(n));
      }
      case "derangements" -> out.println("!" + n + " = " + Permutations.countDerangements(n));
      default ->
          throw new IllegalArgumentException("usage: count <squares|reduced|derangements> <n>");
    }
  }

  private void list(String[] tokens) {
    if (tokens.length != 2 || !tokens[0].equalsIgnoreCase("derangements")) {
      throw new IllegalArgumentException("usage: list derangements <n>");
    }
    List<Permutation> derangements = Permutations.derangements(parseInt(tokens[1], "n"));
    for (Permutation p : derangements) {
      out.println(format(p));
    }
    out.println(derangements.size() + " derangement(s)");
  }

  private void enumerate(String[] tokens) {
    if (tokens.length < 1 || tokens.length > 2) {
      throw new IllegalArgumentException("usage: enumerate <n> [limit]");
    }
    int n = parseInt(tokens[0], "n");
    warnIfSlow(n);
    int limit = tokens.length == 2 ? parseInt(tokens[1], "limit") : 5;
    List<LatinSquare> squares = LatinSquares.withNaturalFirstRow(n);
    int shown = Math.min(limit, squares.size());
    for (int i = 0; i < shown; i++) {
      out.println("--- square " + (i + 1) + " ---");
      out.println(squares.get(i));
    }
    out.println(
        squares.size()
            + " square(s) of order "
            + n
            + " with the natural first row"
            + (shown < squares.size() ? ", showing the first " + shown : ""));
  }

  private void warnIfSlow(int n) {
    if (n > 7) {
      throw new IllegalArgumentException(
          "Order " + n + " is far too large to enumerate; try 7 or below");
    }
  }

  private LatinSquare require() {
    if (current == null) {
      throw new IllegalStateException(
          "No current square. Build one with 'cyclic', 'square' or 'grid'.");
    }
    return current;
  }

  private static String requireOne(String[] tokens, String usage) {
    if (tokens.length != 1) {
      throw new IllegalArgumentException("usage: " + usage);
    }
    return tokens[0];
  }

  private static int index(String[] tokens, int position) {
    if (tokens.length != 2) {
      throw new IllegalArgumentException("usage: <command> <a> <b>");
    }
    return parseInt(tokens[position], "index");
  }

  private static Permutation parsePermutation(String token) {
    return Permutation.of(parseInts(token));
  }

  private static int[] parseInts(String token) {
    String[] parts = token.split(",");
    int[] values = new int[parts.length];
    for (int i = 0; i < parts.length; i++) {
      values[i] = parseInt(parts[i], "entry");
    }
    return values;
  }

  private static int parseInt(String token, String what) {
    try {
      return Integer.parseInt(token.trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("'" + token + "' is not a valid " + what);
    }
  }

  private static String format(Permutation p) {
    return p + "   " + p.toCycleNotation();
  }

  private static String yesNo(boolean value) {
    return value ? "yes" : "no";
  }
}

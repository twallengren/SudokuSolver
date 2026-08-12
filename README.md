# Latin Squares Playground

A small Java playground for investigating Latin squares and Sudoku boards **as sequences of
permutations**.

## The idea

A Latin square of order *n* is an *n × n* grid in which every symbol appears exactly once in each row
and each column. Instead of treating it as a grid of cells, this project treats it as a **first row
plus a sequence of permutations**: each step carries one row to the next.

```
0 1 2 3        row 0 -> 1: (0 1 2 3)
3 0 1 2        row 1 -> 2: (0 1 2 3)
2 3 0 1        row 2 -> 3: (0 1 2 3)
1 2 3 0
```

Written this way, the Latin property becomes a statement about permutations. Two rows may share no
symbol in the same column, so the permutation carrying row *i* to row *j* must have **no fixed
point** — it must be a *derangement*. That has to hold not just for adjacent rows but for every pair,
which gives the criterion the library is built around:

> A sequence of steps yields a Latin square exactly when **every gap composition
> `p_i ∘ p_{i+1} ∘ … ∘ p_{j-1}` is a derangement.**

Adjacent steps being derangements is *not* enough. The playground makes the difference easy to see:

```
> square 1,0,3,2 1,0,3,2 1,0,3,2
order 4, valid Latin square: no
> gaps
rows 0 -> 1: (0 1)(2 3)               derangement
rows 0 -> 2: ()                       FIXES 4 point(s)      <- rows 0 and 2 are identical
```

## Quick start

Requires Java 21 and Maven.

```bash
mvn test          # run the test suite
mvn exec:java     # launch the interactive playground
```

Commands can also be piped in, which makes short experiments repeatable:

```bash
printf 'cyclic 5\nsequence\nreduce\ncount squares 5\nquit\n' | mvn -q exec:java
```

Type `help` inside the playground for the full command list. A tour:

```
perm 4,2,3,0,1           describe a permutation: cycles, order, sign, fixed points
compose 1,2,0 1,2,0      compose left to right
cyclic 4                 build the cyclic square of order 4
square <p1> <p2> ...     build a square from a sequence of steps
grid <r1> <r2> ...       build a square from explicit rows
sequence                 show the row-to-row permutations
gaps                     every gap composition, and whether it is a derangement
transpose | rotate | reflect | swaprows | swapcols | relabel | reduce
sudoku                   view the current square as a Sudoku board
count squares 5          L(5) = 161280
count derangements 6     !6 = 265
```

## Library layout

```
org.example.perm       the core algebra and models
  Permutation          an element of S_n: compose, inverse, power, order, sign, cycles
  PermutationSequence  the sequence-of-permutations view, including the gap criterion
  LatinSquare          converts freely between the grid and sequence views
  Symmetry             transformations preserving the Latin property, plus reduction
  SudokuBoard          the box constraint layered on a LatinSquare

org.example.explore    generation and enumeration
  Permutations         all permutations and all derangements of n points
  LatinSquares         backtracking enumeration and counting

org.example.Playground the interactive shell
```

All model types are immutable; every operation returns a new value.

### The two views round-trip

```java
LatinSquare square = LatinSquare.fromGrid(grid);
PermutationSequence steps = square.toSequence();
assert square.equals(LatinSquare.fromSequence(square.row(0), steps));
```

## Known values

The enumeration is checked against published counts, which double as a correctness anchor:

| n | derangements `!n` | Latin squares `L(n)` | reduced `R(n)` |
|---|---|---|---|
| 4 | 9 | 576 | 4 |
| 5 | 44 | 161280 | 56 |
| 6 | 265 | 812851200 | 9408 |

`L(n) = n! · (n−1)! · R(n)` is asserted directly in the test suite. Counts grow steeply, so the
enumerator is practical to about order 6; `count squares 7` and beyond is a different kind of
project.

## Notes

Derangement lists used to be checked in as text files under `src/main/resources`, and the old entry
point wrote its results back into the source tree. Both are gone: the lists are cheap to generate, so
`Permutations.derangements(n)` produces them on demand, and nothing writes to the repository at
runtime.

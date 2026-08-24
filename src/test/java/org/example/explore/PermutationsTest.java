package org.example.explore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import org.example.perm.Permutation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PermutationsTest {

  @ParameterizedTest(name = "{0}! = {1}")
  @CsvSource({"1, 1", "2, 2", "3, 6", "4, 24", "5, 120", "6, 720"})
  void generatesEveryPermutation(int n, int expected) {
    List<Permutation> all = Permutations.all(n);
    assertEquals(expected, all.size());
    assertEquals(expected, new HashSet<>(all).size(), "permutations must be distinct");
    assertEquals(expected, Permutations.factorial(n));
  }

  /**
   * The counts previously shipped as fourbyfourderangements.txt (9 lines),
   * fivebyfivederangements.txt (44) and sixbysixderangements.txt (265).
   */
  @ParameterizedTest(name = "!{0} = {1}")
  @CsvSource({"1, 0", "2, 1", "3, 2", "4, 9", "5, 44", "6, 265"})
  void generatesEveryDerangement(int n, int expected) {
    List<Permutation> derangements = Permutations.derangements(n);
    assertEquals(expected, derangements.size());
    assertEquals(expected, Permutations.countDerangements(n));
    assertTrue(derangements.stream().allMatch(Permutation::isDerangement));
    assertTrue(derangements.stream().allMatch(p -> p.size() == n));
  }

  @Test
  void derangementsAreExactlyThePermutationsWithoutFixedPoints() {
    for (int n = 1; n <= 6; n++) {
      long filtered = Permutations.all(n).stream().filter(Permutation::isDerangement).count();
      assertEquals(filtered, Permutations.derangements(n).size(), "mismatch at n=" + n);
    }
  }

  @Test
  void subfactorialBaseCases() {
    assertEquals(1, Permutations.countDerangements(0));
    assertEquals(0, Permutations.countDerangements(1));
    assertEquals(1, Permutations.factorial(0));
  }

  @Test
  void generatesInLexicographicOrder() {
    List<Permutation> all = Permutations.all(3);
    assertEquals("0,1,2", all.get(0).toString());
    assertEquals("2,1,0", all.get(5).toString());
  }

  @Test
  void rejectsInvalidSizes() {
    assertThrows(IllegalArgumentException.class, () -> Permutations.all(0));
    assertThrows(IllegalArgumentException.class, () -> Permutations.derangements(0));
    assertThrows(IllegalArgumentException.class, () -> Permutations.countDerangements(-1));
    assertThrows(IllegalArgumentException.class, () -> Permutations.factorial(-1));
  }
}

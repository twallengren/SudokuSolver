package org.example;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public record Permutation(int[] elements) {

  public static List<Permutation> generatePermutations(
      int n, int[] array, List<Permutation> permutations, Predicate<int[]> predicate) {
    if (n == 1) {
      // Check if the current permutation satisfies the predicate
      if (predicate.test(array)) {
        // If it does, add it to the list of valid permutations
        permutations.add(new Permutation(array.clone()));
      }
    } else {
      for (int i = 0; i < n - 1; i++) {
        generatePermutations(n - 1, array, permutations, predicate);
        if (n % 2 == 0) {
          swap(array, i, n - 1);
        } else {
          swap(array, 0, n - 1);
        }
      }
      generatePermutations(n - 1, array, permutations, predicate);
    }
    return permutations;
  }

  // Method to swap elements in an array
  private static void swap(int[] array, int a, int b) {
    int temp = array[a];
    array[a] = array[b];
    array[b] = temp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Permutation that = (Permutation) o;
    return Arrays.equals(elements, that.elements);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(elements);
  }
}

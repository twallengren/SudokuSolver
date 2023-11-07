package org.example;

import java.util.*;

public interface Board {

  void applyPermutationToRow(int rowNumber, Permutation permutation);

  List<Permutation> computePossiblePermutationsToNextRow(int rowNumber);

  boolean isValidPermutation(int[] permutation, int rowNumber);

  void print();
}

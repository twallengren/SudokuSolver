package org.example;

import java.util.List;
import java.util.Set;

public interface Board {

  void applyPermutationToRow(int rowNumber, Permutation permutation);

  List<Permutation> computePossiblePermutationsToNextRow(int rowNumber);

  List<Permutation> computePossiblePermutationsToNextRow(
      List<Permutation> permutations, int rowNumber);

  boolean isValidPermutation(int[] permutation, int rowNumber);

  void print();

  void swapRows(int rowNumberA, int rowNumberB);

  void swapColumns(int colNumberA, int colNumberB);

  void rotateCounterclockwise();

  void transposeBoard();

  void verticalReflection();

  void horizontalReflection();

  void recomputePermutations();

  Set<PermutationChain> getEquivalentPermutationChains();

  int size();
}

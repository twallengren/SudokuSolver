package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PermutationChain {

  int size;
  List<Permutation> permutations;

  PermutationChain(int size) {
    this.size = size;
    permutations = new ArrayList<>();
  }

  PermutationChain(PermutationChain permutationChain) {
    this.size = permutationChain.size;
    this.permutations = new ArrayList<>(permutationChain.permutations);
  }

  void addPermutation(Permutation permutation) {
    if (isFull()) {
      throw new IllegalArgumentException("Permutation chain has reached max size");
    }
    permutations.add(permutation);
  }

  void setPermutation(int index, Permutation permutation) {
    if (index >= permutations.size()) {
      throw new IllegalArgumentException("Invalid index");
    }
    permutations.set(index, permutation);
  }

  boolean isFull() {
    return permutations.size() >= size - 1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PermutationChain that = (PermutationChain) o;
    if (size != that.size) {
      return false;
    }
    for (int index = 0; index < size - 1; index++) {
      if (!Objects.equals(permutations.get(index), that.permutations.get(index))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(size);
    for (Permutation permutation : permutations) {
      result = 31 * result + (permutation != null ? permutation.hashCode() : 0);
    }
    return result;
  }
}

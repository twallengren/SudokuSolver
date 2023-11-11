package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class PermutationSerializer {

  // Method to serialize a list of validPermutations to a text file
  public static void serializeToFile(List<Permutation> permutationGroups, String filename)
      throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
      for (Permutation permutation : permutationGroups) {
        writer.write(permutation.toString());
        writer.newLine(); // Write each permutation on a new line
      }
    }
  }

  // Method to deserialize validPermutations from a text file
  public static List<Permutation> deserializeFromFile(String filename) throws IOException {
    List<Permutation> permutations = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] elements = line.split(",");
        // Convert the string elements back to integers and create a permutation object
        // Assuming you have a constructor Permutation(List<Integer>)
        int[] permutation = Stream.of(elements).mapToInt(Integer::parseInt).toArray();
        // Use the permutation as needed
        permutations.add(new Permutation(permutation));
      }
    }
    return permutations;
  }

  public static void serializePermutationChains(
      Set<PermutationChain> permutationChains, String filename) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
      for (PermutationChain permutationChain : permutationChains) {
        writer.write(permutationChain.toString());
        writer.newLine(); // Write each permutation on a new line
      }
    }
  }
}

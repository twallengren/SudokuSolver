package org.example;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class PermutationSerializer {

    // Method to serialize a list of permutations to a text file
    public static void serializeToFile(List<Permutation> permutationGroups, String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Permutation permutation : permutationGroups) {
                writer.write(permutation.toString());
                writer.newLine(); // Write each permutation on a new line
            }
        }
    }

    // Method to deserialize permutations from a text file
    public static void deserializeFromFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] elements = line.split(",");
                // Convert the string elements back to integers and create a permutation object
                // Assuming you have a constructor Permutation(List<Integer>)
                List<Integer> permutation = Arrays.stream(elements)
                        .map(Integer::valueOf)
                        .collect(Collectors.toList());
                // Use the permutation as needed
            }
        }
    }
}

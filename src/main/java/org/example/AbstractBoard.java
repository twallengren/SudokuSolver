package org.example;

import java.util.*;

public class AbstractBoard implements Board {

  int size;
  Square[][] squares;
  PermutationChain currentPermutationChain;
  Set<PermutationChain> equivalentPermutationChains;

  AbstractBoard(int size) {
    this.size = size;
    squares = getInitialSquares(size);
    currentPermutationChain = new PermutationChain(size);
    equivalentPermutationChains = new HashSet<>();
  }

  private static Square[][] getInitialSquares(int size) {
    Square[][] squares = new Square[size][size];
    for (int row = 0; row < size; row++) {
      for (int column = 0; column < size; column++) {
        squares[row][column] = new Square(Constants.ROW_NAME_MAP.get(row) + (column + 1));
      }
    }
    return squares;
  }

  @Override
  public void applyPermutationToRow(int rowNumber, Permutation permutation) {
    Square[] rowA = squares[rowNumber];
    Square[] permutedSquares = new Square[size];
    int[] indices = permutation.elements();
    for (int index = 0; index < size; index++) {
      int newLocation = indices[index];
      permutedSquares[newLocation] = rowA[index];
    }
    squares[rowNumber + 1] = permutedSquares;
    currentPermutationChain.addPermutation(permutation);
    if (currentPermutationChain.isFull()) {
      PermutationChain permutationChain = new PermutationChain(currentPermutationChain);
      equivalentPermutationChains.add(permutationChain);
    }
  }

  @Override
  public List<Permutation> computePossiblePermutationsToNextRow(int rowNumber) {
    int[] array = new int[size];
    for (int index = 0; index < size; index++) {
      array[index] = index;
    }
    return Permutation.generatePermutations(
        size, array, new ArrayList<>(), (p) -> isValidPermutation(p, rowNumber));
  }

  @Override
  public List<Permutation> computePossiblePermutationsToNextRow(
      List<Permutation> permutations, int rowNumber) {
    return permutations.stream().filter(p -> isValidPermutation(p.elements(), rowNumber)).toList();
  }

  @Override
  public boolean isValidPermutation(int[] permutation, int rowNumber) {
    Square[] squaresInRow = squares[rowNumber];
    for (int index = 0; index < size; index++) {
      Square squareToMove = squaresInRow[index];
      int newLocation = permutation[index];
      Square[] squaresInColumn = getSquaresInColumn(newLocation);
      if (Arrays.stream(squaresInColumn).anyMatch(sq -> sq == squareToMove)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void print() {
    for (int i = 0; i < size; i++) {
      if (size == 9 && i % 3 == 0 && i != 0) {
        System.out.println("------+-------+------");
      } else if (size == 4 && i % 2 == 0 && i != 0) {
        System.out.println("------+------");
      }
      for (int j = 0; j < size; j++) {
        if (size == 9 && j % 3 == 0 && j != 0) {
          System.out.print("| ");
        } else if (size == 4 && j % 2 == 0 && j != 0) {
          System.out.print("| ");
        }
        System.out.print(squares[i][j].name() + " ");
      }
      System.out.println();
    }
    System.out.println();
  }

  @Override
  public void swapRows(int rowNumberA, int rowNumberB) {
    Square[] rowA = squares[rowNumberA];
    Square[] rowB = squares[rowNumberB];
    squares[rowNumberA] = rowB;
    squares[rowNumberB] = rowA;
    recomputePermutations();
  }

  @Override
  public void swapColumns(int colNumberA, int colNumberB) {
    for (int row = 0; row < size; row++) {
      Square squareA = squares[row][colNumberA];
      Square squareB = squares[row][colNumberB];
      squares[row][colNumberA] = squareB;
      squares[row][colNumberB] = squareA;
    }
    recomputePermutations();
  }

  @Override
  public void rotateCounterclockwise() {
    transposeBoard();
    verticalReflection();
  }

  @Override
  public void transposeBoard() {
    for (int i = 0; i < size; i++) {
      for (int j = i; j < size; j++) {
        Square temp = squares[i][j];
        squares[i][j] = squares[j][i];
        squares[j][i] = temp;
      }
    }
    recomputePermutations();
  }

  @Override
  public void verticalReflection() {
    for (int j = 0; j < size; j++) {
      for (int i = 0; i < size / 2; i++) {
        Square temp = squares[i][j];
        squares[i][j] = squares[size - 1 - i][j];
        squares[size - 1 - i][j] = temp;
      }
    }
    recomputePermutations();
  }

  @Override
  public void horizontalReflection() {
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size / 2; j++) {
        Square temp = squares[i][j];
        squares[i][j] = squares[i][size - 1 - j];
        squares[i][size - 1 - j] = temp;
      }
    }
    recomputePermutations();
  }

  @Override
  public void recomputePermutations() {
    for (int row = 0; row < size - 1; row++) {
      int[] elements = new int[size];
      Square[] rowA = squares[row];
      Square[] rowB = squares[row + 1];
      for (int col = 0; col < size; col++) {
        Square squareA = rowA[col];
        for (int index = 0; index < size; index++) {
          Square squareB = rowB[index];
          if (squareA == squareB) {
            elements[col] = index;
            break;
          }
        }
      }
      Permutation permutation = new Permutation(elements);
      currentPermutationChain.setPermutation(row, permutation);
    }
    equivalentPermutationChains.add(new PermutationChain(currentPermutationChain));
  }

  Square[] getSquaresInColumn(int colNumber) {
    Square[] column = new Square[size];
    for (int index = 0; index < size; index++) {
      column[index] = squares[index][colNumber];
    }
    return column;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AbstractBoard that = (AbstractBoard) o;
    return size == that.size
        && equivalentPermutationChains.equals(that.equivalentPermutationChains);
  }

  @Override
  public int hashCode() {
    return Objects.hash(size, equivalentPermutationChains);
  }

  @Override
  public Set<PermutationChain> getEquivalentPermutationChains() {
    return new HashSet<>(equivalentPermutationChains);
  }

  @Override
  public int size() {
    return size;
  }
}

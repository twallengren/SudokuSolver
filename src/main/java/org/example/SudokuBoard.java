package org.example;

import java.util.HashSet;
import java.util.Set;

public class SudokuBoard {
    private static final int SIZE = 9; // Standard Sudoku size
    private final GridSquare[][] board;

    public SudokuBoard(int[][] initialValues) {
        this.board = new GridSquare[SIZE][SIZE];
        initializeBoard(initialValues);
        simplifyBoard();
    }

    private void initializeBoard(int[][] initialValues) {
        if (initialValues.length != SIZE || initialValues[0].length != SIZE) {
            throw new IllegalArgumentException("Initial values must be a 9x9 matrix.");
        }

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int initialValue = initialValues[i][j];
                GridSquare gridSquare = new GridSquare(initialValue == 0 ? null : initialValue);
                gridSquare.setCoordinates(i, j);
                this.board[i][j] = gridSquare;
            }
        }
    }

    public void simplifyBoard() {
        boolean updateOccurred = false;
        for (int i = 0; i < SIZE; i++) {
            boolean squareInRowUpdated = setInvalidValues(getAllSquaresInRow(i));
            boolean squareInColUpdated = setInvalidValues(getAllSquaresInColumn(i));
            boolean squareInGridUpdated = setInvalidValues(getAllSquaresInGrid(i));
            if (!updateOccurred && (squareInRowUpdated || squareInColUpdated || squareInGridUpdated)) {
                updateOccurred = true;
            }
        }
        if (updateOccurred) {
            simplifyBoard();
        }
    }

    private boolean setInvalidValues(GridSquare[] squares) {
        boolean updateOccurred = false;
        Set<Integer> invalidValues = new HashSet<>();
        for (GridSquare square : squares) {
            square.getValue().ifPresent(invalidValues::add);
        }
        for (GridSquare square : squares) {
            boolean squareDidUpdate = square.addInvalidValuesAndUpdate(invalidValues);
            if (!updateOccurred && squareDidUpdate) {
                updateOccurred = true;
            }
        }
        return updateOccurred;
    }

    private GridSquare[] getAllSquaresInRow(int row) {
        // Implementation remains the same
        return board[row];
    }

    private GridSquare[] getAllSquaresInColumn(int col) {
        // Implementation remains the same
        GridSquare[] squares = new GridSquare[SIZE];
        for (int i = 0; i < SIZE; i++) {
            squares[i] = board[i][col];
        }
        return squares;
    }

    private GridSquare[] getAllSquaresInGrid(int grid) {
        // Implementation remains the same
        GridSquare[] squares = new GridSquare[SIZE];
        int startRow = (grid / 3) * 3;
        int startCol = (grid % 3) * 3;
        int index = 0;
        for (int row = startRow; row < startRow + 3; row++) {
            for (int col = startCol; col < startCol + 3; col++) {
                squares[index++] = board[row][col];
            }
        }
        return squares;
    }

    public void print() {
        for (int i = 0; i < board.length; i++) {
            if (i % 3 == 0 && i != 0) {
                System.out.println("------+-------+------");
            }
            for (int j = 0; j < board[0].length; j++) {
                if (j % 3 == 0 && j != 0) {
                    System.out.print("| ");
                }
                System.out.print(board[i][j].getValue().orElse(0) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}

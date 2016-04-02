package org.lisoft.lsml.math.graph;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.lisoft.lsml.math.graph.BackTrackingSolverTest.SudokuPartialCandidate.E;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;
import org.lisoft.lsml.math.graph.BackTrackingSolver;
import org.lisoft.lsml.math.graph.BackTrackingSolver.PartialCandidate;

/**
 * To test the back-tracking algorithm we need a non-trivial problem that preferably has one unique solution. The only
 * thing that came to mind was Suudoku.
 * 
 * @author Emily Björk
 *
 */
public class BackTrackingSolverTest {

    class SudokuPartialCandidate implements PartialCandidate<SudokuPartialCandidate> {
        public final static int E = -1;
        private final int       grid[];
        private final int       penPos;

        public SudokuPartialCandidate(int aGrid[]) {
            this(aGrid, -1);
        }

        private SudokuPartialCandidate(int aGrid[], int aPenPos) {
            if (aGrid.length != 9 * 9)
                throw new IllegalArgumentException();
            grid = aGrid;
            penPos = aPenPos;
        }

        private int nextEmpty(int aPen) {
            int pen = aPen;
            while (pen < 9 * 9 && grid[pen] != E) {
                pen++;
            }
            return pen;
        }

        private int index(int row, int col) {
            return 9 * row + col; // Row major
        }

        @Override
        public boolean reject() {
            // Check all rows && columns
            for (int x0 = 0; x0 < 9; ++x0) {
                boolean rowPresenceMap[] = new boolean[10];
                boolean colPresenceMap[] = new boolean[10];
                for (int x1 = 0; x1 < 9; ++x1) {
                    int rowDigit = grid[index(x0, x1)];
                    int colDigit = grid[index(x1, x0)];
                    if (rowDigit != E) {
                        if (rowPresenceMap[rowDigit])
                            return true;
                        rowPresenceMap[rowDigit] = true;
                    }
                    if (colDigit != E) {
                        if (colPresenceMap[colDigit])
                            return true;
                        colPresenceMap[colDigit] = true;
                    }
                }
            }

            // Check all blocks
            for (int rowBlock = 0; rowBlock < 3; ++rowBlock) {
                for (int colBlock = 0; colBlock < 3; ++colBlock) {

                    boolean presenceMap[] = new boolean[10];
                    for (int row = 0; row < 3; ++row) {
                        for (int col = 0; col < 3; ++col) {
                            int digit = grid[index(rowBlock * 3 + row, colBlock * 3 + col)];
                            if (digit != E) {
                                if (presenceMap[digit])
                                    return true;
                                presenceMap[digit] = true;
                            }
                        }
                    }

                }
            }
            return false;
        }

        @Override
        public boolean accept() {
            for (int v : grid) {
                if (v == E) {
                    return false;
                }
            }
            return !reject();
        }

        @Override
        public Optional<SudokuPartialCandidate> first() {
            if (penPos == grid.length - 1) {
                return Optional.empty();
            }
            int nextPenPos = nextEmpty(penPos + 1);
            if (nextPenPos < grid.length) {
                int gridCopy[] = Arrays.copyOf(grid, grid.length);
                gridCopy[nextPenPos] = 1;

                return Optional.of(new SudokuPartialCandidate(gridCopy, nextPenPos));
            }
            return Optional.empty();
        }

        @Override
        public Optional<SudokuPartialCandidate> next() {
            if (penPos == -1 || grid[penPos] == 9) {
                return Optional.empty();
            }

            int gridCopy[] = Arrays.copyOf(grid, grid.length);
            gridCopy[penPos]++;
            return Optional.of(new SudokuPartialCandidate(gridCopy, penPos));
        }

        int[] getGrid() {
            return grid;
        }
    }

    @Test
    public void testSolveSudoku() {
        // Grid visualisation:
        //
        // 1 E E | E E 2 | E E E
        // E 5 E | E 9 E | 2 E 4
        // E E E | E E 6 | 7 E E
        // ----------------------
        // E 3 4 | E E 1 | E E 5
        // 5 E E | 9 E 8 | E E 7
        // 8 E E | 4 E E | 3 2 E
        // ----------------------
        // E E 9 | 6 E E | E E E
        // 3 E 6 | E 1 E | E 4 E
        // E E E | 7 E E | E E 9

        int grid[] = new int[] { // Comments to enforce line breaks
                1, E, E, E, E, 2, E, E, E, //
                E, 5, E, E, 9, E, 2, E, 4, //
                E, E, E, E, E, 6, 7, E, E, //
                E, 3, 4, E, E, 1, E, E, 5, //
                5, E, E, 9, E, 8, E, E, 7, //
                8, E, E, 4, E, E, 3, 2, E, //
                E, E, 9, 6, E, E, E, E, E, //
                3, E, 6, E, 1, E, E, 4, E, //
                E, E, E, 7, E, E, E, E, 9 //
        };

        BackTrackingSolver<SudokuPartialCandidate> cut = new BackTrackingSolver<>();
        Optional<SudokuPartialCandidate> ans = cut.solveOne(new SudokuPartialCandidate(grid));

        // Unique Solution:
        //
        // 1 4 7 | 3 5 2 | 6 9 8
        // 6 5 8 | 1 9 7 | 2 3 4
        // 9 2 3 | 8 4 6 | 7 5 1
        // ----------------------
        // 7 3 4 | 2 6 1 | 9 8 5
        // 5 6 2 | 9 3 8 | 4 1 7
        // 8 9 1 | 4 7 5 | 3 2 6
        // ----------------------
        // 2 1 9 | 6 8 4 | 5 7 3
        // 3 7 6 | 5 1 9 | 8 4 2
        // 4 8 5 | 7 2 3 | 1 6 9
        int expected[] = new int[] { // Comments to enforce line breaks
                1, 4, 7, 3, 5, 2, 6, 9, 8, //
                6, 5, 8, 1, 9, 7, 2, 3, 4, //
                9, 2, 3, 8, 4, 6, 7, 5, 1, //
                7, 3, 4, 2, 6, 1, 9, 8, 5, //
                5, 6, 2, 9, 3, 8, 4, 1, 7, //
                8, 9, 1, 4, 7, 5, 3, 2, 6, //
                2, 1, 9, 6, 8, 4, 5, 7, 3, //
                3, 7, 6, 5, 1, 9, 8, 4, 2, //
                4, 8, 5, 7, 2, 3, 1, 6, 9,//
        };

        assertTrue(ans.isPresent());
        assertArrayEquals(expected, ans.get().getGrid());
    }

}

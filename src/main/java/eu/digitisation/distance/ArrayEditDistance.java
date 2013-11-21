/*
 * Copyright (C) 2013 Universidad de Alicante
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package eu.digitisation.distance;

/**
 * Provides a basic implementations of some popular edit distance methods
 * applied to arrays of objects (currently, Levenshtein and indel). This
 * implementation can accelerates the computation of distances if, for example,
 * text is handled as a sequence of (Integer) word codes.
 *
 * @version 2011.03.10
 */
public class ArrayEditDistance<Type> {

    /**
     * @return 3-wise minimum.
     */
    private static int min(int x, int y, int z) {
        return Math.min(x, Math.min(y, z));
    }

    /**
     * @param first the first string.
     * @param second the second string.
     * @return the indel distance between first and second.
     */
    public static <Type> int indel(Type[] first, Type[] second) {
        int i, j;
        int[][] A = new int[first.length + 1][second.length + 1];

        // Compute first row
        A[0][0] = 0;
        for (j = 1; j <= second.length; ++j) {
            A[0][j] = A[0][j - 1] + 1;
        }

        // Compute other rows
        for (i = 1; i <= first.length; ++i) {
            A[i][0] = A[i - 1][0] + 1;
            for (j = 1; j <= second.length; ++j) {
                if (first[i - 1].equals(second[j - 1])) {
                    A[i][j] = A[i - 1][j - 1];
                } else {
                    A[i][j] = Math.min(A[i - 1][j] + 1, A[i][j - 1] + 1);
                }
            }
        }
        return A[first.length][second.length];
    }

    /**
     * @param first the first string.
     * @param second the second string.
     * @return the Levenshtein distance between first and second.
     */
    public static <Type> int levenshtein(Type[] first, Type[] second) {
        int i, j;
        int[][] A;

        // intialize
        A = new int[first.length + 1][second.length + 1];

        // Compute first row
        A[0][0] = 0;
        for (j = 1; j <= second.length; ++j) {
            A[0][j] = A[0][j - 1] + 1;
        }

        // Compute other rows
        for (i = 1; i <= first.length; ++i) {
            A[i][0] = A[i - 1][0] + 1;
            for (j = 1; j <= second.length; ++j) {
                if (first[i - 1] == second[j - 1]) {
                    A[i][j] = A[i - 1][j - 1];
                } else {
                    A[i][j] = min(A[i - 1][j] + 1, A[i][j - 1] + 1,
                            A[i - 1][j - 1] + 1);
                }
            }
        }
        return A[first.length][second.length];
    }
}

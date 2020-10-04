/* *****************************************************************************
 *  Name: Spyros Dellas
 *  Date: 28-09-2020
 *  Description: Implementation of the Z algorithm for substring search
 **************************************************************************** */

import edu.princeton.cs.algs4.In;

import java.util.ArrayList;
import java.util.List;

public class Z {

    private static final int NOT_FOUND = -1;

    /**
     * Do not instantiate
     */
    private Z() {
    }

    /**
     * Find all occurrences of the pattern in the text using the Z algorithm
     * <p>
     * The search runs in linear time O(M+N), where M is the length of the pattern
     * and N the length of the text
     */
    public static List<Integer> search(String pattern, String text) {
        char[] joined = join(pattern, text);
        int[] z = computeZ(joined);
        int m = pattern.length();
        List<Integer> result = new ArrayList<>();
        for (int i = m + 1; i < z.length; i++) {
            if (z[i] == m) {
                result.add(i - m - 1);
            }
        }
        return result;
    }

    private static char[] join(String pattern, String text) {
        int m = pattern.length();
        int n = text.length();
        char[] joined = new char[m + n + 1];
        // construct the concatenated string "pattern + null + text"
        // it is assumed that neither the pattern or the text contain the null
        // character
        pattern.getChars(0, m, joined, 0);
        text.getChars(0, n, joined, m + 1);
        return joined;
    }

    /**
     * Apply the Z algorithm to compute z[]
     * <p>
     * The idea is to maintain an interval [L, R] which is the interval with
     * max R such that [L,R] is prefix substring (substring which is also prefix)
     * <p>
     * Steps for maintaining this interval are as follows –
     * <p>
     * 1) If i > R then there is no prefix substring that starts before i and
     * ends after i, so we reset L and R and compute new [L,R] by comparing
     * str[0..] to str[i..] and get Z[i] (= R-L+1).
     * <p>
     * 2) If i <= R then left K = i-L, now Z[i] >= min(Z[K], R-i+1) because
     * str[i..] matches with str[K..] for at least R-i+1 characters (they are in
     * [L,R] interval which we know is a prefix substring).
     * Now two sub cases arise –
     * a) If Z[K] < R-i+1 then there is no prefix substring starting at
     * str[i] (otherwise Z[K] would be larger) so Z[i] = Z[K] and
     * interval [L,R] remains same.
     * b) If Z[K] >= R-i+1 then it is possible to extend the [L,R] interval
     * thus we will set L as i and start matching from str[R] onwards and
     * get new R then we will update interval [L,R] and calculate Z[i]=R-L+1
     */
    private static int[] computeZ(char[] input) {
        int size = input.length;
        // For a string T, Z[i] is the length of the longest prefix of T[i..m]
        // that matches a prefix of T. Z[i] = 0 if the prefixes don't match
        int[] z = new int[size];
        int left = 0;
        int right = 0;
        for (int index = 1; index < size; index++) {
            if (index > right) {
                left = index;
                for (int k = 0; index + k < size; k++) {
                    if (input[k] == input[index + k])
                        z[index]++;
                    else
                        break;
                }
                right = index + z[index] - 1;
            }
            else {
                int start = index - left;
                if (index + z[start] - 1 < right) {
                    z[index] = z[start];
                }
                else {
                    for (int k = right + 1; k < size; k++) {
                        if (input[k] == input[k - index]) {
                            right++;
                        }
                        else {
                            break;
                        }
                    }
                    left = index;
                    z[index] = right - left + 1;
                }
            }
        }
        return z;
    }

    public static void main(String[] args) {

        // import the haystack (long string)
        String file = "\\D:\\Algorithms I\\kdtree\\leipzig1M.txt";
        System.out.println("\nImporting search text '" + file + "'...");
        In in = new In(file);
        String haystack = in.readAll();
        in.close();
        System.out.println("Search string size = " + haystack.length());

        // time the search
        int times = 3;
        String pattern = "tests are p";

        System.out.println("\nSearching " + times + " times for the pattern"
                                   + " using Java String.indexOf()");
        List<Integer> indices = new ArrayList<>();
        double start = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            indices.clear();
            int fromIndex = 0;
            while (fromIndex < haystack.length()) {
                int nextIndex = haystack.indexOf(pattern, fromIndex);
                if (nextIndex == -1)
                    break;
                indices.add(nextIndex);
                fromIndex = nextIndex + pattern.length();
            }
        }
        double end = System.currentTimeMillis();
        System.out.println("Time to complete search: " + (end - start) / 1000 + " secs");
        System.out.println("Results = " + indices);

        System.out.println("\nSearching " + times + " times for the pattern"
                                   + " using Z algorithm");
        List<Integer> result = new ArrayList<>();
        start = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            result = Z.search(pattern, haystack);
        }
        end = System.currentTimeMillis();
        System.out.println("Time to complete search: " + (end - start) / 1000 + " secs");
        System.out.println("Results = " + result);
    }
}

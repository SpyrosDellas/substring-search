/* *****************************************************************************
 *  Name: Spyros Dellas
 *  Date: 26-09-2020
 *  Description: Implementation of Boyer-Moore substring search algorithm
 *
 * This implementation includes the bad character rule only
 * It's performance is sublinear on typical inputs, but O(MN) worst case, for
 * example:
 * Text: aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
 * Pattern: baaaaaaaa
 *
 **************************************************************************** */

import edu.princeton.cs.algs4.In;

import java.util.Arrays;

public class BoyerMooreBCR {

    private static final int EXTENDED_ASCII = 256;
    private static final int NOT_FOUND = -1;

    private final char[] pattern;
    private int[] last;
    // indices marking for each position i the length of the longest suffix in
    // the pattern (up to i) that is also a prefix of the pattern
    private int[] z;

    /**
     * Boyer-Moore substring search constructor
     */
    public BoyerMooreBCR(String pat) {
        pattern = pat.toCharArray();
        computeLast();
        computeZ();
    }

    /**
     * Compute last[] which is used for the bad character rule
     * <p>
     * last stores the indices of the last (rightmost) occurrence of each
     * alphabet character in the pattern
     */
    private void computeLast() {
        // initialize right shifts array; -1 corresponds to a character not
        // in the pattern
        last = new int[EXTENDED_ASCII];
        for (int i = 0; i < EXTENDED_ASCII; i++) {
            last[i] = -1;
        }
        // update with the rightmost occurrence of each character
        for (int i = 0; i < pattern.length; i++) {
            last[pattern[i]] = i;
        }
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
    private void computeZ() {
        int size = pattern.length;
        z = new int[size];
        int left = 0;
        int right = 0;
        for (int index = 1; index < size; index++) {
            if (index > right) {
                left = index;
                for (int k = 0; index + k < size; k++) {
                    if (pattern[k] == pattern[index + k])
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
                        if (pattern[k] == pattern[k - index]) {
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
    }

    /**
     * Boyer-Moore search for the pattern in given text
     */
    public int search(String text) {
        int textLength = text.length();
        int patternLength = pattern.length;
        int skip;
        for (int i = 0; i <= textLength - patternLength; i += skip) {
            skip = 0;
            for (int j = patternLength - 1; j >= 0; j--) {
                char c = text.charAt(i + j);
                if (c != pattern[j]) {
                    skip = j - last[c];
                    if (skip < 1)
                        skip = 1;
                    break;
                }
                if (j == 0) return i;  // pattern found
            }
        }

        return NOT_FOUND;
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
        String[] patterns = {
                "ACABACACD",
                "test pattern",
                "It is against House rules to disclose classified information conveyed to the Intelligence Committee."
        };

        System.out.println("\nSearching for " + times + " different patterns"
                                   + " using Java String.indexOf()");
        int[] indices = new int[times];
        double start = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            indices[i] = haystack.indexOf(patterns[i]);
        }
        double end = System.currentTimeMillis();
        System.out.println("Time to complete search: " + (end - start) / 1000 + " secs");
        System.out.println("Results = " + Arrays.toString(indices));

        System.out.println("\nSearching for " + times + " different patterns"
                                   + " using Boyer-Moore algorithm");
        BoyerMooreBCR[] bm = new BoyerMooreBCR[times];
        for (int i = 0; i < times; i++) {
            bm[i] = new BoyerMooreBCR(patterns[i]);
        }
        start = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            indices[i] = bm[i].search(haystack);
        }
        end = System.currentTimeMillis();
        System.out.println("Time to complete search: " + (end - start) / 1000 + " secs");
        System.out.println("Results = " + Arrays.toString(indices));

/*
        // test on pathological input
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 10.0E6; i++) {
            sb.append('A');
        }
        sb.append('B');
        String pathological = sb.toString();
        String pattern = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB";

        times = 10;
        System.out.println("\nSearching " + times + " times for pattern '" + pattern + "' in a text"
                                   + " consisting of 10,000,000 'A's followed by a single 'B' using Java String.indexOf()");
        start = System.currentTimeMillis();
        int index = -1;
        for (int i = 0; i < times; i++) {
            index = pathological.indexOf(pattern);
        }
        end = System.currentTimeMillis();
        System.out.println("Time to complete search: " + (end - start) / 1000 + " secs");
        System.out.println("Result = " + index);

        System.out.println("\nSearching " + times + " times for pattern '" + pattern + "' in a text"
                                   + " consisting of 10,000,000 'A's followed by a single 'B' using Knuth-Morris-Pratt");
        KMP kmp1 = new KMP(pattern);
        start = System.currentTimeMillis();
        index = -1;
        for (int i = 0; i < times; i++) {
            index = kmp1.search(pathological);
        }
        end = System.currentTimeMillis();
        System.out.println("Time to complete search: " + (end - start) / 1000 + " secs");
        System.out.println("Result = " + index);

 */
    }
}

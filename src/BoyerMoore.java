/* *****************************************************************************
 *  Name: Spyros Dellas
 *  Date: 03-10-2020
 *  Description: Implementation of Boyer-Moore substring search algorithm
 *
 * This implementation is the full version of the algorithm. It includes both
 * the 'bad character' and the 'good suffix' rules
 *
 * It's performance is sublinear on typical inputs. Worst case performance is
 * linear in the length of the text
 *
 **************************************************************************** */

import edu.princeton.cs.algs4.In;

import java.util.Arrays;

public class BoyerMoore {

    private static final int EXTENDED_ASCII = 256;
    private static final int NOT_FOUND = -1;

    private final char[] pattern;
    private final int size;
    private int[] lastOccurrence;
    private int[] shift;

    /**
     * Boyer-Moore substring search constructor
     */
    public BoyerMoore(String pat) {
        pattern = pat.toCharArray();
        size = pattern.length;
        computeLastOccurrence();
        int[] z = computeZ();
        computeShift(z);

    }

    /**
     * Compute the array lastOccurrence which is used for the bad character rule
     * <p>
     * lastOccurrence stores the indices of the last (rightmost) occurrence of
     * each alphabet character in the pattern
     */
    private void computeLastOccurrence() {
        // initialize the array; -1 corresponds to a character not in the pattern
        lastOccurrence = new int[EXTENDED_ASCII];
        for (int i = 0; i < EXTENDED_ASCII; i++) {
            lastOccurrence[i] = -1;
        }
        // update with the rightmost occurrence of each character
        for (int i = 0; i < size; i++) {
            lastOccurrence[pattern[i]] = i;
        }
    }

    /**
     * Compute the z array; z[i] is the length of the longest substring in
     * the pattern ending at position i that is also a suffix of the pattern
     * <p>
     * To compute z[], we apply the Z algorithm inversely; instead of starting
     * from the beginning of the string and move forward, we start from the end
     * and move backwards
     * <p>
     * We maintain an interval [L, R] which is the interval with min L such
     * that pattern[L, R] is a suffix substring, as follows:
     * <p>
     * Case 1:
     * If i < L then there is no suffix substring that starts before i and
     * ends after i, so we reset L and R and compute new [L, R] by comparing
     * pattern[..size - 1] to pattern[..i] BACKWARDS and get Z[i] = R - L + 1
     * <p>
     * Case 2:
     * If i >= L then pattern[i..R] matches pattern[size - 1 - R + i..size - 1]
     * for at least i - L + 1 characters; these characters are within
     * pattern[L.. R], which we already know is a suffix substring
     * Now two sub-cases arise:
     * a) If Z[size - 1 - R + 1] < i - L + 1 then Z[i] = Z[size - 1 - R + 1] and
     * [L, R] remains the same
     * b) Otherwise, it might be possible to extend the [L, R] interval, thus we
     * set R = i and start matching from pattern[R] BACKWARDS. We get the new L
     * and then update Z[i]
     */
    private int[] computeZ() {
        int[] z = new int[size];
        int left = size - 1;
        int right = size - 1;
        for (int index = size - 2; index >= 0; index--) {
            if (index < left) {
                right = index;
                int counter = 0;
                for (int j = 0; index - j >= 0; j++) {
                    if (pattern[size - 1 - j] == pattern[index - j])
                        counter++;
                    else
                        break;
                }
                z[index] = counter;
                left = right - counter + 1;
            }
            else {
                int start = size - 1 - right + index;
                if (z[start] < index - left + 1) {
                    z[index] = z[start];
                }
                else {
                    right = index;
                    for (int j = 1; left - j >= 0; j++) {
                        if (pattern[left - j] == pattern[start - j]) {
                            left--;
                        }
                        else {
                            break;
                        }
                    }
                    z[index] = right - left + 1;
                }
            }
        }
        return z;
    }

    /**
     * Compute the array shift[] which is used for the good suffix rule
     * <p>
     * shift[i] is the number of characters we can shift the pattern to the
     * right if a mismatch occurs at index i, i.e. pattern[i + 1..size - 1]
     * matches the text
     */
    private void computeShift(int[] z) {
        shift = new int[size];
        // trivial case: if we have a mismatch already at the last character,
        // we simply move the pattern 1 position to the right
        shift[size - 1] = 1;
        // Check for re-occurrence of a good suffix
        for (int index = size - 2; index >= 0; index--) {
            int goodSuffixLength = z[index];
            // No good suffix of the pattern ends at index
            if (goodSuffixLength == 0)
                continue;
            // A good suffix of the pattern ends at index, i.e.
            // pattern[size - z[index]..size - 1] is a good suffix
            int mismatchAt = size - 1 - goodSuffixLength;
            // if this is the rightmost re-occurrence of this good suffix, we
            // record the shift
            if (shift[mismatchAt] == 0)
                shift[mismatchAt] = size - 1 - index;
        }
        // Handle the case where a good suffix doesn't re-occur elsewhere
        // in the pattern;
        // Step 1: Store the lengths of the longest prefixes of the pattern that
        // are also suffixes in z and set the next entry of z to zero (as a sentinel)
        int counter = 0;
        for (int i = 0; i < size; i++) {
            if (z[i] == i + 1)
                z[counter++] = z[i];
        }
        z[counter] = 0;
        // Step 2: Update shift
        int lengthOfLongestPrefixSuffix = 0;
        int zPosition = 0;
        for (int index = size - 2; index >= 0; index--) {
            while (z[zPosition] > 0 && size - z[zPosition] > index) {
                lengthOfLongestPrefixSuffix = z[zPosition];
                zPosition++;
            }
            if (shift[index] != 0)
                continue;
            shift[index] = size - lengthOfLongestPrefixSuffix;
        }
    }

    /**
     * Boyer-Moore search for the pattern in given text
     */
    public int search(String text) {
        int textLength = text.length();
        int skip = 0;
        for (int startIndex = 0; startIndex <= textLength - size; startIndex += skip) {
            for (int offset = size - 1; offset >= 0; offset--) {
                char c = text.charAt(startIndex + offset);
                if (c != pattern[offset]) {
                    skip = offset - lastOccurrence[c];
                    if (shift[offset] > skip)
                        skip = shift[offset];
                    break;
                }
                else if (offset == 0) {
                    return startIndex;  // pattern found
                }
            }
        }
        return NOT_FOUND;
    }

    public static void main(String[] args) {

        /*
        String pattern1 = "abbabab";
        String pattern2 = "abababa";
        String pattern3 = "testedtest";
        BoyerMoore bm1 = new BoyerMoore(pattern1);
        BoyerMoore bm2 = new BoyerMoore(pattern2);
        BoyerMoore bm3 = new BoyerMoore(pattern3);
         */

        // import the text
        String file = "\\D:\\Algorithms I\\kdtree\\leipzig1M.txt";
        System.out.println("\nImporting search text '" + file + "'...");
        In in = new In(file);
        String text = in.readAll();
        in.close();
        System.out.println("Search string size = " + text.length());

        // time the search
        String[] patterns = {
                "ACABACACD", "test pattern", "variations",
                "this is just a long random pattern to match",

                "It is against House rules to disclose classified information conveyed to the Intelligence Committee."
        };
        int numOfPatterns = patterns.length;

        System.out.println("\nSearching for " + numOfPatterns + " different patterns"
                                   + " using Java String.indexOf()");
        int[] indicesJava = new int[numOfPatterns];
        double start = System.currentTimeMillis();
        for (int i = 0; i < numOfPatterns; i++) {
            indicesJava[i] = text.indexOf(patterns[i]);
        }
        double end = System.currentTimeMillis();
        System.out.println("Time to complete search: " + (end - start) / 1000 + " secs");
        System.out.println("Results = " + Arrays.toString(indicesJava));

        System.out.println("\nSearching for " + numOfPatterns + " different patterns"
                                   + " using Boyer-Moore algorithm");
        int[] indicesBM = new int[numOfPatterns];
        start = System.currentTimeMillis();
        for (int i = 0; i < numOfPatterns; i++) {
            BoyerMoore bm = new BoyerMoore(patterns[i]);
            indicesBM[i] = bm.search(text);
        }
        end = System.currentTimeMillis();
        System.out.println("Time to complete search: " + (end - start) / 1000 + " secs");
        System.out.println("Results = " + Arrays.toString(indicesBM));


        // test on pathological input
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 10.0E6; i++) {
            sb.append('A');
        }
        sb.append('B');
        String pathological = sb.toString();
        String pattern = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB";

        int times = 10;
        System.out.println(
                "\nSearching " + times + " times for pattern '" + pattern + "' in a text"
                        + " consisting of 10,000,000 'A's followed by a single 'B'");
        System.out.println("A. Using using Java String.indexOf()");
        start = System.currentTimeMillis();
        int index = -1;
        for (int i = 0; i < times; i++) {
            index = pathological.indexOf(pattern);
        }
        end = System.currentTimeMillis();
        System.out.println("Time to complete search: " + (end - start) / 1000 + " secs");
        System.out.println("Result = " + index);

        System.out.println("B. Using using Boyer-Moore");
        start = System.currentTimeMillis();
        index = -1;
        for (int i = 0; i < times; i++) {
            BoyerMoore bm = new BoyerMoore(pattern);
            index = bm.search(pathological);
        }
        end = System.currentTimeMillis();
        System.out.println("Time to complete search: " + (end - start) / 1000 + " secs");
        System.out.println("Result = " + index);

    }
}

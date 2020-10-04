/* *****************************************************************************
 *  Name: Spyros Dellas
 *  Date: 23-09-2020
 *  Description: Implementation of Knuth-Morris-Pratt substring search algorithm
 *
 * This version requires O(RN) space for the DFA, where R is the radix of the
 * alphabet and N is the length of the pattern to search for
 **************************************************************************** */

import edu.princeton.cs.algs4.In;

import java.util.Arrays;

public class KMPDfa {

    private static final int EXTENDED_ASCII = 256;
    private static final int NOT_FOUND = -1;

    private final int[][] dfa;
    private final int length;

    public KMPDfa(String pattern) {
        if (pattern == null)
            throw new IllegalArgumentException("Pattern cannot be null");
        length = pattern.length();
        dfa = new int[EXTENDED_ASCII][length];
        if (length == 0)
            return;
        // define starting state
        dfa[pattern.charAt(0)][0] = 1;
        // define all other states
        int simulatedState = 0;
        for (int state = 1; state < length; state++) {
            // state transitions for character mismatches
            for (int i = 0; i < EXTENDED_ASCII; i++) {
                dfa[i][state] = dfa[i][simulatedState];
            }
            // state transition for character match
            char c = pattern.charAt(state);
            dfa[c][state] = state + 1;
            // update simulated state by simulating pattern[1..state-1] on the
            // dfa constructed thus far and taking the transition c
            simulatedState = dfa[c][simulatedState];
        }
    }

    public int search(String haystack) {
        if (haystack == null)
            throw new IllegalArgumentException("Cannot search a null string");
        if (length == 0)
            return 0;
        int state = 0;
        int index;
        for (index = 0; index < haystack.length(); index++) {
            state = dfa[haystack.charAt(index)][state];
            if (state == length) {
                return index - length + 1;
            }
        }
        return NOT_FOUND;
    }

    public static void main(String[] args) {
        // import the haystack (long string)
        System.out.println("\nImporting search string...");
        String file = "\\D:\\Algorithms I\\kdtree\\leipzig1M.txt";
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
                                   + " using Knuth-Morris-Pratt algorithm");
        KMPDfa[] kmp = new KMPDfa[times];
        for (int i = 0; i < times; i++) {
            kmp[i] = new KMPDfa(patterns[i]);
        }
        start = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            indices[i] = kmp[i].search(haystack);
        }
        end = System.currentTimeMillis();
        System.out.println("Time to complete search: " + (end - start) / 1000 + " secs");
        System.out.println("Results = " + Arrays.toString(indices));
    }
}

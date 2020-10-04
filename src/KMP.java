/* *****************************************************************************
 *  Name: Spyros Dellas
 *  Date: 24-09-2020
 *  Description: Implementation of Knuth-Morris-Pratt substring search algorithm
 *
 * This is the full version based on a NFA, suitable for any alphabet size.
 * Requires O(N) space for the DFA, where N is the length of the pattern to
 * search for.
 * The running time is O(N+M), where M is the text length
 *
 **************************************************************************** */

import edu.princeton.cs.algs4.In;

import java.util.Arrays;

public class KMP {

    private static final int NOT_FOUND = -1;

    private final char[] pattern;
    private final int[] nfa;
    private final int length;

    public KMP(String pat) {
        if (pat == null)
            throw new IllegalArgumentException("Pattern cannot be null");
        pattern = pat.toCharArray();
        length = pat.length();
        // nfa represents the mismatch transitions only, i.e. if we had a mismatch
        // in the current character, what would the state of the nfa
        // be if we had restarted from the second character in the pattern and run
        // the nfa up to and including the current character
        nfa = new int[length];
        if (length == 0)
            return;
        // define state transitions
        int simulatedState = 0;
        for (int state = 1; state < length; state++) {

            // character match; increment simulated state by 1
            if (pattern[state] == pattern[simulatedState]) {
                simulatedState++;
                nfa[state] = simulatedState;
            }
            // character mismatch; what to do?
            // - First get the simulated state if we had restarted the nfa from
            // the second character in the pattern up to the previous character
            // (which is a match)
            // - Then check if the current character is a match with the character
            // at the simulated state
            // - If not, repeat step one until we have a match or we fall back to
            // state 0
            else {
                while (simulatedState > 0 && pattern[state] != pattern[simulatedState]) {
                    simulatedState = nfa[simulatedState - 1];
                }
                nfa[state] = simulatedState;
            }
        }
    }

    public int search(String haystack) {
        if (haystack == null)
            throw new IllegalArgumentException("Cannot search a null string");
        if (length == 0)
            return 0;
        int state = 0;
        int index = 0;
        while (index < haystack.length()) {
            // character match
            if (haystack.charAt(index) == pattern[state]) {
                state++;   // increment state
                index++;   // move to next character
                // check for pattern match
                if (state == length) {
                    return index - length;
                }
            }
            // character mismatch
            else {
                // fall back to previous state
                if (state > 0) {
                    state = nfa[state - 1];
                }
                // mismatch at state 0, move on to next character
                else {
                    index++;
                }
            }
        }
        return NOT_FOUND;
    }

    @Override
    public String toString() {
        return Arrays.toString(nfa);
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
                                   + " using Knuth-Morris-Pratt algorithm");
        KMP[] kmp = new KMP[times];
        for (int i = 0; i < times; i++) {
            kmp[i] = new KMP(patterns[i]);
        }
        start = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            indices[i] = kmp[i].search(haystack);
        }
        end = System.currentTimeMillis();
        System.out.println("Time to complete search: " + (end - start) / 1000 + " secs");
        System.out.println("Results = " + Arrays.toString(indices));


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
    }
}

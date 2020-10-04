/* *****************************************************************************
 *  Name: Spyros Dellas
 *  Date: 29-09-2020
 *  Description: Implementation of Rabin-Karp substring search algorithm
 **************************************************************************** */

import edu.princeton.cs.algs4.In;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RabinKarp {

    private static final int R = 65536;  // the radix of the Unicode alphabet

    // a large prime that won't cause overflow
    private final long largePrime = Long.parseLong("10963707205259");
    private final Map<Long, String> patternHashes;
    private int hashLength;
    private long exponent;

    public RabinKarp(String pattern) {
        this(Collections.singleton(pattern));
    }

    public RabinKarp(Set<String> patterns) {
        patternHashes = new HashMap<>();
        hashLength = -1;
        for (String pattern : patterns) {
            int length = pattern.length();
            if (hashLength == -1) {
                hashLength = length;
            }
            else if (length != hashLength) {
                throw new IllegalArgumentException("All search patterns must have the same length");
            }
            long hash = hash(pattern, hashLength);
            if (patternHashes.containsKey(hash)) {
                throw new RuntimeException("Hash collision detected for search terms: "
                                                   + pattern + ", " + patternHashes.get(hash));
            }
            patternHashes.put(hash, pattern);
        }
        calculateExponent();
    }

    private void calculateExponent() {
        exponent = 1;
        for (int i = 0; i < hashLength - 1; i++) {
            exponent = (exponent * R) % largePrime;
        }
    }

    /**
     * Compute hash for text[0..m-1]
     */
    private long hash(String text, int length) {
        long hashValue = 0;
        for (int i = 0; i < length; i++) {
            hashValue = (hashValue * R) % largePrime + text.charAt(i);
        }
        return hashValue;
    }

    public Map<String, List<Integer>> search(String text) {
        Map<String, List<Integer>> result = new HashMap<>();
        for (String pattern : patternHashes.values()) {
            result.put(pattern, new ArrayList<Integer>());
        }
        long textHash = hash(text, hashLength);
        // check for match at the beginning of the text
        checkAndUpdateResult(textHash, 0, result);
        int offset = 0;
        for (int textIndex = hashLength; textIndex < text.length(); textIndex++) {
            // Remove leading digit, add trailing digit
            textHash = (textHash + largePrime
                    - (exponent * text.charAt(textIndex - hashLength)) % largePrime) % largePrime;
            textHash = (textHash * R + text.charAt(textIndex)) % largePrime;
            offset++;
            checkAndUpdateResult(textHash, offset, result);
        }
        return result;
    }

    private void checkAndUpdateResult(long textHash, int index, Map<String, List<Integer>> result) {
        if (patternHashes.containsKey(textHash))
            result.get(patternHashes.get(textHash)).add(index);
    }

    public static void main(String[] args) {

        // import the haystack (long string)
        String file = "\\D:\\Algorithms I\\kdtree\\leipzig1M.txt";
        System.out.println("\nImporting search text '" + file + "'...");
        In in = new In(file);
        String haystack = in.readAll();
        in.close();
        System.out.println("Search text size = " + haystack.length());

        // Search for a set of n-letter words
        Set<String> patterns = new LinkedHashSet<>();
        int length = 15;
        String phrase = "Compare Rabin-Karp with String.indexOf() by searching "
                + "for all n-letter substrings from this long search phrase";
        for (int startFrom = 0; startFrom <= phrase.length() - length; startFrom++) {
            patterns.add(phrase.substring(startFrom, startFrom + length));
        }

        System.out.println("\nSearching text for all occurrences of following "
                                   + length + "-letter words (total " + patterns.size()
                                   + " words): \n" + patterns);

        System.out.println("\nMethod A - Java String.indexOf()");
        double start = System.currentTimeMillis();
        Map<String, List<Integer>> resultJava = new HashMap<>();
        for (String pattern : patterns) {
            resultJava.put(pattern, new ArrayList<Integer>());
        }
        for (String pattern : patterns) {
            int fromIndex = 0;
            while (fromIndex < haystack.length()) {
                int nextIndex = haystack.indexOf(pattern, fromIndex);
                if (nextIndex == -1)
                    break;
                resultJava.get(pattern).add(nextIndex);
                fromIndex = nextIndex + pattern.length();
            }
        }
        double end = System.currentTimeMillis();
        System.out.println("Time to complete search: " + (end - start) / 1000 + " secs");
        // System.out.println("Result:");
        for (String word : patterns) {
            // System.out.println(word + ": " + resultJava.get(word));
        }

        System.out.println("\nMethod B - Rabin-Karp algorithm");
        start = System.currentTimeMillis();
        RabinKarp rk = new RabinKarp(patterns);
        Map<String, List<Integer>> resultRK = rk.search(haystack);
        end = System.currentTimeMillis();
        System.out.println("Time to complete search: " + (end - start) / 1000 + " secs");
        // System.out.println("Result:");
        for (String word : patterns) {
            // System.out.println(word + ": " + resultRK.get(word));
        }
        // check Rabin-Karp and Java search results are the same
        for (String word : patterns) {
            List<Integer> rJ = resultJava.get(word);
            List<Integer> rRK = resultRK.get(word);
            if (rJ.size() != rRK.size())
                throw new AssertionError("Results are not the same!");
            for (int i = 0; i < rJ.size(); i++) {
                if (!rJ.get(i).equals(rRK.get(i)))
                    throw new AssertionError("Results are not the same!");
            }
        }
        System.out.println("\nRabin-Karp and Java search results are the same!");
    }
}

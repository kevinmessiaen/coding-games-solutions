import java.util.*;
import java.util.Map.Entry;

/*
 * This my solution for the CodingGames puzzle "The Resistance": https://www.codingame.com/training/expert/the-resistance
 * My CodingGames profile is available here: https://www.codingame.com/profile/0a609e6f0803ffabfc8e9519df40a8ac6614143
 *
 * The solution is optimized using memoization and dynamic programming
 */
public class Solution {

    public static void main(String args[]) {
        // Read the problem using standard input as per coding game rules
        Scanner in = new Scanner(System.in);
        String morseSequence = in.next();
        int numberOfWords = in.nextInt();

        DictionaryTree dictionaryTree = new DictionaryTree();

        for (int i = 0; i < numberOfWords; i++) {
            dictionaryTree.addWord(in.next());
        }

        // start generating the solution by starting with the whole sequence
        Map<String, Long> pathCountForSequences = new HashMap<>();
        pathCountForSequences.put(morseSequence, 1L);

        long existingCombinationsCount = 0;
        Map<String, MatchState> routesMatchedBySequences = new HashMap<>();

        while(pathCountForSequences.size() > 0) {
            Map<String, Long> currentSequences = pathCountForSequences;
            pathCountForSequences = new HashMap<>();

            for (Entry<String, Long> entry: currentSequences.entrySet()) {
                String currentMorseSequence = entry.getKey();
                long pathesToThisSequence = entry.getValue();

                existingCombinationsCount += findCombinations(dictionaryTree, pathCountForSequences,
                        existingCombinationsCount, routesMatchedBySequences, currentMorseSequence, pathesToThisSequence);
            }
        }

        System.out.println(existingCombinationsCount);
    }

    private static long findCombinations(DictionaryTree dictionaryTree, Map<String, Long> pathCountForSequences,
                                         long existingCombinationsCount, Map<String, MatchState> routesMatchedBySequences,
                                         String currentMorseSequence, long pathesToThisSequence) {
        MatchState matchState = routesMatchedBySequences.computeIfAbsent(currentMorseSequence, sequence -> {
            MatchState state = new MatchState();
            dictionaryTree.pos(sequence, state);
            return state;
        });

        increasePathCountForSequences(pathCountForSequences, pathesToThisSequence, matchState);

        return matchState.existingCombinationsCount * pathesToThisSequence;
    }

    private static void increasePathCountForSequences(Map<String, Long> pathCountForSequences,
                                                      long pathesToCurrentSequence,
                                                      MatchState matchState) {
        matchState.ends.forEach((sequence, combinations) ->
                pathCountForSequences.compute(sequence, (k, v) ->
                        Objects.requireNonNullElse(v, 0L) + combinations * pathesToCurrentSequence));
    }
}

/*
 * The DictionaryTree is a binary tree that map each morse signals (./-) into an tree where leaves might be existing words
 */
class DictionaryTree {

    // Sequential translation of the alphabet to morse
    private static final String[] MORSE_ALPHABET = {
            ".-", "-...", "-.-.", "-..",
            ".", "..-.", "--.", "....",
            "..", ".---", "-.-", ".-..",
            "--", "-.", "---", ".--.",
            "--.-", ".-.", "...", "-",
            "..-", "...-", ".--", "-..-",
            "-.--", "--.."
    };

    // A map that contains next leaves for each signals (./-)
    private final Map<Character, DictionaryTree> children = new HashMap<>();
    // True if current leave is an existing word
    private boolean isWord;

    protected void addWord(String word) {
        if (word.length() == 0) {
            isWord = true;
        } else {
            children.compute(word.charAt(0), (k, child) -> {
                child = Objects.requireNonNullElseGet(child, DictionaryTree::new);
                child.addWord(word.substring(1));
                return child;
            });
        }
    }

    protected void pos(String word, MatchState state) {
        if (this.isWord) {
            if (word.length() == 0) {
                state.existingCombinationsCount++;
            } else {
                state.ends.compute(word, (w, i) -> Objects.requireNonNullElse(i, 0L) + 1);
            }
        }

        if (word.length() > 0) {
            for (Entry<Character, DictionaryTree> entry: children.entrySet()) {
                String letter = MORSE_ALPHABET[entry.getKey() - 'A'];
                if (word.startsWith(letter)) {
                    entry.getValue().pos(word.substring(letter.length()), state);
                }
            }
        }
    }

}

// Hold the state of existing next words for a given part of morse code
class MatchState {
    protected long existingCombinationsCount;
    protected Map<String, Long> ends = new HashMap<>();
}

package asia.fourtitude.interviewq.jumble.core;

import java.io.*;
import java.util.*;

public class JumbleEngine {

    private final Set<String> wordSet;
    private final List<String> wordList;
    private final Map<Integer, List<String>> wordsByLength;
    private final Random random;

    public JumbleEngine() {
        this.wordSet = new HashSet<>();
        this.wordList = new ArrayList<>();
        this.wordsByLength = new HashMap<>();
        this.random = new Random();
        loadWords();
    }

    private void loadWords() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase();
                if (!word.isEmpty()) {
                    wordSet.add(word);
                    wordList.add(word);
                    wordsByLength.computeIfAbsent(word.length(), k -> new ArrayList<>()).add(word);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load words.txt", e);
        }
    }

    /**
     * From the input `word`, produces/generates a copy which has the same
     * letters, but in different ordering.
     *
     * Example: from "elephant" to "lehnaetp".
     *
     * Evaluation/Grading:
     * a) pass unit test: JumbleEngineTest#scramble()
     * b) scrambled letters/output must not be the same as input
     *
     * @param word  The input word to scramble the letters.
     * @return  The scrambled output/letters.
     */
    public String scramble(String word) {
        List<Character> chars = new ArrayList<>();
        for (char c : word.toCharArray()) {
            chars.add(c);
        }
        String result;
        int attempts = 0;
        do {
            Collections.shuffle(chars, random);
            StringBuilder sb = new StringBuilder(chars.size());
            for (char c : chars) {
                sb.append(c);
            }
            result = sb.toString();
            attempts++;
        } while (result.equals(word) && attempts < 1000);
        return result;
    }

    /**
     * Retrieves the palindrome words from the internal
     * word list/dictionary ("src/main/resources/words.txt").
     *
     * Word of single letter is not considered as valid palindrome word.
     *
     * Examples: "eye", "deed", "level".
     *
     * Evaluation/Grading:
     * a) able to access/use resource from classpath
     * b) using inbuilt Collections
     * c) using "try-with-resources" functionality/statement
     * d) pass unit test: JumbleEngineTest#palindrome()
     *
     * @return  The list of palindrome words found in system/engine.
     * @see https://www.google.com/search?q=palindrome+meaning
     */
    public Collection<String> retrievePalindromeWords() {
        List<String> palindromes = new ArrayList<>();
        for (String word : wordList) {
            if (word.length() > 1) {
                String reversed = new StringBuilder(word).reverse().toString();
                if (word.equals(reversed)) {
                    palindromes.add(word);
                }
            }
        }
        return palindromes;
    }

    /**
     * Picks one word randomly from internal word list.
     *
     * Evaluation/Grading:
     * a) pass unit test: JumbleEngineTest#randomWord()
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param length  The word picked, must of length.
     *                When length is null, then return random word of any length.
     * @return  One of the word (randomly) from word list.
     *          Or null if none matching.
     */
    public String pickOneRandomWord(Integer length) {
        if (length == null) {
            if (wordList.isEmpty()) {
                return null;
            }
            return wordList.get(random.nextInt(wordList.size()));
        }
        List<String> words = wordsByLength.get(length);
        if (words == null || words.isEmpty()) {
            return null;
        }
        return words.get(random.nextInt(words.size()));
    }

    /**
     * Checks if the `word` exists in internal word list.
     * Matching is case insensitive.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param word  The input word to check.
     * @return  true if `word` exists in internal word list.
     */
    public boolean exists(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        return wordSet.contains(word.trim().toLowerCase());
    }

    /**
     * Finds all the words from internal word list which begins with the
     * input `prefix`.
     * Matching is case insensitive.
     *
     * Invalid `prefix` (null, empty string, blank string, non letter) will
     * return empty list.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param prefix  The prefix to match.
     * @return  The list of words matching the prefix.
     */
    public Collection<String> wordsMatchingPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String trimmed = prefix.trim();
        for (char c : trimmed.toCharArray()) {
            if (!Character.isLetter(c)) {
                return Collections.emptyList();
            }
        }
        String lowerPrefix = trimmed.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String word : wordList) {
            if (word.startsWith(lowerPrefix)) {
                result.add(word);
            }
        }
        return result;
    }

    /**
     * Finds all the words from internal word list that is matching
     * the searching criteria.
     *
     * `startChar` and `endChar` must be 'a' to 'z' only. And case insensitive.
     * `length`, if have value, must be positive integer (>= 1).
     *
     * Words are filtered using `startChar` and `endChar` first.
     * Then apply `length` on the result, to produce the final output.
     *
     * Must have at least one valid value out of 3 inputs
     * (`startChar`, `endChar`, `length`) to proceed with searching.
     * Otherwise, return empty list.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param startChar  The first character of the word to search for.
     * @param endChar    The last character of the word to match with.
     * @param length     The length of the word to match.
     * @return  The list of words matching the searching criteria.
     */
    public Collection<String> searchWords(Character startChar, Character endChar, Integer length) {
        boolean validStart = startChar != null && Character.isLetter(startChar);
        boolean validEnd = endChar != null && Character.isLetter(endChar);
        boolean validLength = length != null && length >= 1;

        if (!validStart && !validEnd && !validLength) {
            return Collections.emptyList();
        }

        char lowerStart = validStart ? Character.toLowerCase(startChar) : 0;
        char lowerEnd = validEnd ? Character.toLowerCase(endChar) : 0;

        List<String> result = new ArrayList<>();
        for (String word : wordList) {
            if (validStart && word.charAt(0) != lowerStart) {
                continue;
            }
            if (validEnd && word.charAt(word.length() - 1) != lowerEnd) {
                continue;
            }
            if (validLength && word.length() != length) {
                continue;
            }
            result.add(word);
        }
        return result;
    }

    /**
     * Generates all possible combinations of smaller/sub words using the
     * letters from input word.
     *
     * The `minLength` set the minimum length of sub word that is considered
     * as acceptable word.
     *
     * If length of input `word` is less than `minLength`, then return empty list.
     *
     * The sub words must exist in internal word list.
     *
     * Example: From "yellow" and `minLength` = 3, the output sub words:
     *     low, lowly, lye, ole, owe, owl, well, welly, woe, yell, yeow, yew, yowl
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param word       The input word to use as base/seed.
     * @param minLength  The minimum length (inclusive) of sub words.
     *                   When zero, return empty list.
     *                   Default is 3.
     * @return  The list of sub words constructed from input `word`.
     */
    public Collection<String> generateSubWords(String word, Integer minLength) {
        if (word == null || word.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String trimmed = word.trim();
        for (char c : trimmed.toCharArray()) {
            if (!Character.isLetter(c)) {
                return Collections.emptyList();
            }
        }

        if (minLength == null) {
            minLength = 3;
        }
        if (minLength <= 0) {
            return Collections.emptyList();
        }

        String lowerWord = trimmed.toLowerCase();
        if (lowerWord.length() < minLength) {
            return Collections.emptyList();
        }

        Map<Character, Integer> freqMap = new HashMap<>();
        for (char c : lowerWord.toCharArray()) {
            freqMap.merge(c, 1, Integer::sum);
        }

        List<String> result = new ArrayList<>();
        for (String candidate : wordList) {
            if (candidate.length() < minLength || candidate.length() > lowerWord.length()) {
                continue;
            }
            if (candidate.equals(lowerWord)) {
                continue;
            }
            if (canFormFrom(candidate, freqMap)) {
                result.add(candidate);
            }
        }
        return result;
    }

    private boolean canFormFrom(String candidate, Map<Character, Integer> freqMap) {
        Map<Character, Integer> candidateFreq = new HashMap<>();
        for (char c : candidate.toCharArray()) {
            candidateFreq.merge(c, 1, Integer::sum);
        }
        for (Map.Entry<Character, Integer> entry : candidateFreq.entrySet()) {
            Integer available = freqMap.get(entry.getKey());
            if (available == null || available < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a game state with word to guess, scrambled letters, and
     * possible combinations of words.
     *
     * Word is of length 6 characters.
     * The minimum length of sub words is of length 3 characters.
     *
     * @param length     The length of selected word.
     *                   Expects >= 3.
     * @param minLength  The minimum length (inclusive) of sub words.
     *                   Expects positive integer.
     *                   Default is 3.
     * @return  The game state.
     */
    public GameState createGameState(Integer length, Integer minLength) {
        Objects.requireNonNull(length, "length must not be null");
        if (minLength == null) {
            minLength = 3;
        } else if (minLength <= 0) {
            throw new IllegalArgumentException("Invalid minLength=[" + minLength + "], expect positive integer");
        }
        if (length < 3) {
            throw new IllegalArgumentException("Invalid length=[" + length + "], expect greater than or equals 3");
        }
        if (minLength > length) {
            throw new IllegalArgumentException("Expect minLength=[" + minLength + "] greater than length=[" + length + "]");
        }
        String original = this.pickOneRandomWord(length);
        if (original == null) {
            throw new IllegalArgumentException("Cannot find valid word to create game state");
        }
        String scramble = this.scramble(original);
        Map<String, Boolean> subWords = new TreeMap<>();
        for (String subWord : this.generateSubWords(original, minLength)) {
            subWords.put(subWord, Boolean.FALSE);
        }
        return new GameState(original, scramble, subWords);
    }

}

package no.bibsys.aws.tools;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

public class StringUtils {
    
    private static final String UNDERSCORE_NOT_ALLOWED_IN_AWS = "_";
    private static final String DASH_ALLOWED_IN_AWS = "-";
    private static final String AWS_ALLOWED_CHARS = "[^-a-z0-9]";
    private static final String AWS_NOT_ALLOWED_CHARS1 = AWS_ALLOWED_CHARS;
    private static final String SPLITTING_DELIMITER = "-";
    private static final String ERROR_MESSAGE = "maxLength should be greater than 0";
    
    /**
     * Lowercases the input string, replaces underscores with dashes.
     *
     * @param input The string to be normalized
     * @return The normalized String
     */
    public String normalizeString(String input) {
        String res =
                input.toLowerCase(Locale.getDefault()).replaceAll(UNDERSCORE_NOT_ALLOWED_IN_AWS, DASH_ALLOWED_IN_AWS)
                     .replaceAll(AWS_NOT_ALLOWED_CHARS1, "");
        
        return res;
    }
    
    /**
     * Lowercases the input string, replaces underscores with dashes, and truncates each word (string between two
     * dashes) to {@code maxWordLength}.
     *
     * @param input The string to be normalized
     * @param maxWorldLength max number of characters between two dashes
     * @return a normalized String
     */
    public String shortNormalizedString(String input, int maxWorldLength) {
        String[] words = normalizeString(input).split(SPLITTING_DELIMITER);
        int maxnumberOfWords = Math.min(maxWorldLength, words.length);
        List<String> wordList =
                Arrays.stream(words).map(word -> shorten(word, maxWorldLength)).collect(Collectors.toList())
                      .subList(0, maxnumberOfWords);
        
        return String.join("-", wordList);
    }
    
    private String shorten(String word, int maxLength) {
        int maxIndex = Math.min(word.length(), maxLength);
        return word.substring(0, maxIndex);
    }
    
    public String randomString(int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException(ERROR_MESSAGE);
        }
        Long now = System.currentTimeMillis();
        Random random = new Random();
        Integer randInt = random.nextInt();
        String seedString = now.toString() + randInt.toString();
        String randomString = DigestUtils.sha1Hex(seedString);
        int actualLength = Math.min(randomString.length(), maxLength);
        return randomString.substring(0, actualLength);
    }
}

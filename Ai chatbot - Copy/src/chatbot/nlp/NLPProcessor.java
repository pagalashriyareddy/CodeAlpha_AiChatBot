package chatbot.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NLPProcessor {
    // Basic stop words to ignore during processing
    private static final List<String> STOP_WORDS = Arrays.asList(
            "is", "am", "are", "a", "an", "the", "in", "on", "at", "to", "for", "of", "with"
    );

    /**
     * Tokenizes and cleans the input string.
     */
    public static List<String> tokenize(String input) {
        String cleanInput = input.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
        String[] tokens = cleanInput.split("\\s+");
        List<String> filteredTokens = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isEmpty() && !STOP_WORDS.contains(token)) {
                filteredTokens.add(token);
            }
        }
        return filteredTokens;
    }

    /**
     * Calculates Jaccard similarity between two lists of tokens.
     */
    public static double calculateSimilarity(List<String> inputTokens, List<String> patternTokens) {
        if (inputTokens.isEmpty() && patternTokens.isEmpty()) return 1.0;
        if (inputTokens.isEmpty() || patternTokens.isEmpty()) return 0.0;
        
        List<String> intersection = new ArrayList<>(inputTokens);
        intersection.retainAll(patternTokens);
        
        List<String> union = new ArrayList<>(inputTokens);
        for (String token : patternTokens) {
            if (!union.contains(token)) {
                union.add(token);
            }
        }
        
        return (double) intersection.size() / union.size();
    }
}

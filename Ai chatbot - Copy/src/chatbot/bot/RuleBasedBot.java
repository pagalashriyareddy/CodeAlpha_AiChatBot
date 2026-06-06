package chatbot.bot;

import chatbot.nlp.NLPProcessor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RuleBasedBot {
    private Map<String, String> knowledgeBase;

    public RuleBasedBot() {
        knowledgeBase = new HashMap<>();
        initializeKnowledgeBase();
    }

    private void initializeKnowledgeBase() {
        knowledgeBase.put("hello hi hey greetings", "Hello! How can I assist you today?");
        knowledgeBase.put("how are you", "I'm just a computer program, but I'm doing great! How about you?");
        knowledgeBase.put("what is your name who are you", "I am an AI Chatbot, created to help answer your questions.");
        knowledgeBase.put("what is java", "Java is a high-level, class-based, object-oriented programming language designed to have as few implementation dependencies as possible.");
        knowledgeBase.put("what is artificial intelligence ai", "Artificial Intelligence (AI) is the simulation of human intelligence processes by machines, especially computer systems.");
        knowledgeBase.put("how to learn programming code", "The best way to learn programming is by practicing! Start with a language like Python or Java, and build small projects.");
        knowledgeBase.put("what is python", "Python is an interpreted, high-level, general-purpose programming language known for its readability.");
        knowledgeBase.put("bye goodbye see you", "Goodbye! Have a great day!");
        knowledgeBase.put("help what can you do", "I can answer frequently asked questions about programming, AI, or just have a simple chat.");
        knowledgeBase.put("weather", "I am unable to check the weather right now, but I hope it's nice outside!");
        knowledgeBase.put("exam test preparation study", "For exam preparation, make sure to review your notes, practice coding problems, and get plenty of rest!");
    }

    public String getResponse(String input) {
        List<String> inputTokens = NLPProcessor.tokenize(input);
        
        String bestMatch = null;
        double maxSimilarity = 0.0;

        for (Map.Entry<String, String> entry : knowledgeBase.entrySet()) {
            List<String> patternTokens = NLPProcessor.tokenize(entry.getKey());
            
            // First check for direct similarity
            double similarity = NLPProcessor.calculateSimilarity(inputTokens, patternTokens);
            
            // Also check if any input token exactly matches a key token for simple fallback
            boolean hasKeyword = false;
            for (String t : inputTokens) {
                if (patternTokens.contains(t)) {
                    hasKeyword = true;
                    break;
                }
            }
            
            // Boost similarity if there's a keyword match to ensure it passes the threshold
            if (hasKeyword && similarity < 0.2) {
                similarity = 0.2;
            }
            
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestMatch = entry.getValue();
            }
        }

        if (maxSimilarity > 0.15) {
            return bestMatch;
        } else {
            // Fallback to Wikipedia search
            String wikiAnswer = searchWikipedia(input);
            if (wikiAnswer != null) {
                return wikiAnswer;
            }
            return "I'm sorry, I didn't quite understand that. Could you please rephrase or ask something else?";
        }
    }

    private String searchWikipedia(String query) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String searchUrl = "https://en.wikipedia.org/w/api.php?action=opensearch&search=" 
                             + URLEncoder.encode(query, StandardCharsets.UTF_8) 
                             + "&limit=1&namespace=0&format=json";
            
            HttpRequest searchReq = HttpRequest.newBuilder().uri(URI.create(searchUrl)).header("User-Agent", "JavaChatbot/1.0 (test@example.com)").build();
            HttpResponse<String> searchRes = client.send(searchReq, HttpResponse.BodyHandlers.ofString());
            String searchBody = searchRes.body();
            
            int firstArray = searchBody.indexOf(",[\"");
            if (firstArray == -1) return null;
            
            int startTitle = firstArray + 3;
            int endTitle = searchBody.indexOf("\"]", startTitle);
            if (endTitle == -1) return null;
            
            String title = searchBody.substring(startTitle, endTitle);
            
            String summaryUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/" 
                              + URLEncoder.encode(title.replace(" ", "_"), StandardCharsets.UTF_8);
            
            HttpRequest summaryReq = HttpRequest.newBuilder().uri(URI.create(summaryUrl)).header("User-Agent", "JavaChatbot/1.0 (test@example.com)").build();
            HttpResponse<String> summaryRes = client.send(summaryReq, HttpResponse.BodyHandlers.ofString());
            String summaryBody = summaryRes.body();
            
            int extractIndex = summaryBody.indexOf("\"extract\":\"");
            if (extractIndex != -1) {
                int startExtract = extractIndex + 11;
                int endExtract = summaryBody.indexOf("\",\"extract_html\"", startExtract);
                if (endExtract == -1) endExtract = summaryBody.lastIndexOf("\"");
                
                if (endExtract != -1 && endExtract > startExtract) {
                    String extract = summaryBody.substring(startExtract, endExtract);
                    extract = extract.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
                    return "According to Wikipedia:\n" + extract;
                }
            }
        } catch (Exception e) {
            // Request failed, ignore and fall through
        }
        return null;
    }
}

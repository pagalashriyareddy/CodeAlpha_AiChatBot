package chatbot.bot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class GenerativeAIBot {
    // =========================================================================
    // TODO: PASTE YOUR GOOGLE GEMINI API KEY HERE!
    // Get a free key at: https://aistudio.google.com/app/apikey
    // =========================================================================
    private static final String API_KEY = "AIzaSyC6DTfEidskLwTQLKO_VK1O5Upl5RnBl8E";

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
            + API_KEY;

    private List<String> conversationHistory = new ArrayList<>();

    public String getResponse(String input) {
        if (API_KEY.equals("YOUR_API_KEY_HERE")) {
            return "Please enter your free Google Gemini API Key in the GenerativeAIBot.java file to start chatting!";
        }

        try {
            HttpClient client = HttpClient.newHttpClient();

            // Escape user input for JSON
            String safeInput = input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");

            String userMessageJson = "{\"role\": \"user\", \"parts\": [{\"text\": \"" + safeInput + "\"}]}";
            conversationHistory.add(userMessageJson);

            StringBuilder contentsBuilder = new StringBuilder("[");
            for (int i = 0; i < conversationHistory.size(); i++) {
                contentsBuilder.append(conversationHistory.get(i));
                if (i < conversationHistory.size() - 1) {
                    contentsBuilder.append(",");
                }
            }
            contentsBuilder.append("]");

            String jsonPayload = "{\"contents\":" + contentsBuilder.toString() + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            int maxRetries = 3;
            int attempt = 0;
            HttpResponse<String> response = null;

            while (attempt < maxRetries) {
                try {
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200 || response.statusCode() < 500) {
                        break;
                    }
                    // Server error, retry
                    attempt++;
                    if (attempt < maxRetries) {
                        Thread.sleep(1500 * attempt);
                    }
                } catch (Exception e) {
                    attempt++;
                    if (attempt == maxRetries) {
                        throw e;
                    }
                    Thread.sleep(1500 * attempt);
                }
            }

            if (response != null) {
                String responseBody = response.body();
                if (response.statusCode() == 200) {
                    String aiText = extractTextFromJson(responseBody);
                    // Add AI response to history
                    String safeOutput = aiText.replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "");
                    String modelMessageJson = "{\"role\": \"model\", \"parts\": [{\"text\": \"" + safeOutput + "\"}]}";
                    conversationHistory.add(modelMessageJson);
                    return aiText;
                } else {
                    // Revert history if API failed to prevent context desync
                    if (!conversationHistory.isEmpty()) {
                        conversationHistory.remove(conversationHistory.size() - 1);
                    }
                    return "API Error: " + response.statusCode() + " - " + responseBody;
                }
            } else {
                if (!conversationHistory.isEmpty()) {
                    conversationHistory.remove(conversationHistory.size() - 1);
                }
                return "Failed to connect to AI after multiple retries.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (!conversationHistory.isEmpty()) {
                conversationHistory.remove(conversationHistory.size() - 1);
            }
            return "Error connecting to AI: " + e.getMessage();
        }
    }

    // A simple method to extract the "text" field from the JSON response without
    // external libraries
    private String extractTextFromJson(String json) {
        String key = "\"text\":";
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1)
            return "Sorry, I couldn't understand the AI's response.";

        int startQuote = json.indexOf("\"", keyIndex + key.length());
        if (startQuote == -1)
            return "Sorry, I couldn't understand the AI's response.";

        int start = startQuote + 1;
        int end = start;

        while (end < json.length()) {
            end = json.indexOf("\"", end);
            if (end == -1)
                break;

            int backslashCount = 0;
            int temp = end - 1;
            while (temp >= start && json.charAt(temp) == '\\') {
                backslashCount++;
                temp--;
            }
            if (backslashCount % 2 == 0) {
                break; // Unescaped quote
            }
            end++;
        }

        if (end != -1) {
            String text = json.substring(start, end);
            text = text.replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\r", "")
                    .replace("\\t", "\t");
            return text;
        }
        return "Sorry, I couldn't understand the AI's response.";
    }
}

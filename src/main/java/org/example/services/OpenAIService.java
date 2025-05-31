package org.example.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenAIService {
    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAIService() {
        this.apiKey = System.getenv("OPEN_AI_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Missing OpenAI API key (set 'api_key' as environment variable)");
        }
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    public String generateDocumentationForFile(String fileName, String fileContent) throws IOException, InterruptedException {
        String prompt = "Generate detailed documentation for the following source code file: " + fileName + "\n\n" + fileContent;

        ObjectNode requestBody = objectMapper.createObjectNode()
                .put("model", "gpt-3.5-turbo-instruct")
                .put("prompt", prompt)
                .put("max_tokens", 1000);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("OpenAI API request failed with status code: " + response.statusCode() +
                    "\nResponse: " + response.body());
        }

        ObjectNode responseJson = objectMapper.readValue(response.body(), ObjectNode.class);
        return responseJson.path("choices").get(0).path("text").asText().trim();
    }

//    public String generateDocumentation(String repoContent) throws IOException, InterruptedException {
//        String prompt = "Generate clear and concise technical documentation for the following GitHub repository content:\n\n" + repoContent;
//
//        // Build chat-based request (for GPT-4o or GPT-3.5-turbo)
//        ObjectNode requestBody = objectMapper.createObjectNode();
//        requestBody.put("model", "gpt-3.5-turbo-0125"); // You can change this to "gpt-3.5-turbo"
//        ArrayNode messages = requestBody.putArray("messages");
//
//        ObjectNode userMessage = objectMapper.createObjectNode();
//        userMessage.put("role", "user");
//        userMessage.put("content", prompt);
//        messages.add(userMessage);
//
//        requestBody.put("temperature", 0.7);
//        requestBody.put("max_tokens", 1000);
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
//                .header("Content-Type", "application/json")
//                .header("Authorization", "Bearer " + apiKey)
//                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
//                .build();
//
//        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//        if (response.statusCode() != 200) {
//            throw new IOException("OpenAI API request failed with status code: " + response.statusCode() + "\nResponse: " + response.body());
//        }
//
//        JsonNode responseJson = objectMapper.readTree(response.body());
//        String generatedText = responseJson
//                .path("choices").get(0)
//                .path("message").path("content").asText();
//
//        return "# Repository Documentation\n\n" + generatedText;
//    }
}

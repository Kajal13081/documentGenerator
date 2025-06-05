package org.example.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.backend.Settings;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenAIService {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Settings appSettings;

    public OpenAIService(Settings appSettings) {
        this.appSettings = appSettings;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    public String generateDocumentationForFile(String fileName, String fileContent, String customPrompt) throws IOException, InterruptedException {
        String prompt;
        if (customPrompt != null && !customPrompt.isBlank()) {
            prompt = customPrompt + "\n\nFilename: " + fileName + "\n\nCode:\n" + fileContent;
        } else {
            prompt = "Generate detailed documentation for the following source code file: " + fileName + "\n\n" + fileContent;
        }

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", appSettings.getModel()); // Use Groq-supported LLaMA 3 model

        ArrayNode messages = requestBody.putArray("messages");

        // You can optionally set a system message to guide behavior
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a documentation generator for source code. Your job is to generate rich, complete, Markdown-based documentation for the source code.");
        messages.add(systemMessage);

        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        requestBody.put("temperature", appSettings.getTemperature());
        requestBody.put("max_tokens", appSettings.getMaxTokens());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + appSettings.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("LLaMA API request failed with status code: " + response.statusCode() +
                    "\nResponse: " + response.body());
        }

        JsonNode responseJson = objectMapper.readTree(response.body());
        return responseJson.path("choices").get(0).path("message").path("content").asText().trim();
    }
}

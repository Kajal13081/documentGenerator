package org.example.backend;

import org.example.services.GitHubService;
import org.example.services.OpenAIService;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

public class DocumentationGenerator {
    private final String repoUrl;
    private final String outputPath;
    private final GitHubService gitHubService;
    private final OpenAIService openAIService;
    private final String customPrompt;

    public DocumentationGenerator(String repoUrl, String outputPath, String customPrompt, Settings appSettings) {
        this.repoUrl = repoUrl;
        this.outputPath = outputPath;
        this.gitHubService = new GitHubService();
        this.openAIService = new OpenAIService(appSettings);
        this.customPrompt = customPrompt;
    }

    public String generate(Consumer<String> logger) throws IOException, InterruptedException {
        logger.accept("Fetching repository files...");
        Map<String, String> files = gitHubService.getRepositoryFilesContent(repoUrl);

        if (files.isEmpty()) {
            logger.accept("No source files found in the repository.");
            return "";
        }

        StringBuilder finalDoc = new StringBuilder();
        finalDoc.append("# Documentation for Repository: ").append(repoUrl).append("\n\n");

        int fileCount = 1;
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String fileName = entry.getKey();
            String fileContent = entry.getValue();

            logger.accept("[" + fileCount + "/" + files.size() + "] Generating documentation for: " + fileName);
            String doc = openAIService.generateDocumentationForFile(fileName, fileContent, customPrompt);

            finalDoc.append("## File: ").append(fileName).append("\n\n");
            finalDoc.append(doc).append("\n\n---\n\n");

            Thread.sleep(5000);

            fileCount++;
        }

        //Path outputFile = Paths.get(outputPath, "DOCUMENTATION.md");
        //Files.writeString(outputFile, finalDoc.toString());
        //logger.accept("Documentation saved to " + outputFile.toAbsolutePath());

        return finalDoc.toString();
    }
}

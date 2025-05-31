package org.example.services;

import org.kohsuke.github.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GitHubService {
    private final GitHub github;

    // Allowed source code file extensions
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".java", ".cpp", ".cs", ".vb", ".py"
    );

    /**
     * Creates a GitHub client, connect anonymously by default.
     * You can modify to use authentication token if needed.
     */
    public GitHubService() {
        try {
            this.github = new GitHubBuilder()
                    .withOAuthToken(System.getenv("GITHUB_TOKEN")) // or hardcoded for test
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to GitHub", e);
        }
    }

    /**
     * Fetches the content of all allowed files from the given GitHub repository URL.
     *
     * @param repoUrl GitHub repo URL (e.g. https://github.com/owner/repo)
     * @return Map where key = file path, value = file content as string
     * @throws IOException on IO or GitHub API errors
     */
    public Map<String, String> getRepositoryFilesContent(String repoUrl) throws IOException {
        if (repoUrl == null || !repoUrl.contains("github.com")) {
            throw new IllegalArgumentException("Invalid GitHub repo URL");
        }

        String[] parts = repoUrl.split("/");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid GitHub repo URL format");
        }

        String owner = parts[3];
        String repo = parts[4];

        GHRepository repository = github.getRepository(owner + "/" + repo);
        Map<String, String> filesContent = new HashMap<>();

        fetchContentRecursive(repository, "", filesContent);

        return filesContent;
    }

    /**
     * Recursively fetch files with allowed extensions and add to map.
     */
    private void fetchContentRecursive(GHRepository repo, String path, Map<String, String> filesContent) throws IOException {
        for (GHContent item : repo.getDirectoryContent(path)) {
            if (item.isFile() && hasAllowedExtension(item.getName())) {
                filesContent.put(item.getPath(), getFileContent(item));
            } else if (item.isDirectory()) {
                fetchContentRecursive(repo, item.getPath(), filesContent);
            }
        }
    }

    /**
     * Checks if filename ends with any of the allowed extensions.
     */
    private boolean hasAllowedExtension(String fileName) {
        return ALLOWED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    /**
     * Reads file content as UTF-8 string.
     */
    private String getFileContent(GHContent file) throws IOException {
        try (InputStream inputStream = file.read()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

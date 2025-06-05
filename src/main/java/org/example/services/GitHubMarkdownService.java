package org.example.services;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class GitHubMarkdownService {

    public static void exportMarkdownToRepo(
            String repoUrl,
            String branch,
            String token,
            String fileName,
            String markdownContent,
            String commitMessage,
            File localRepoDir) throws IOException, GitAPIException {

        UsernamePasswordCredentialsProvider credentials =
                new UsernamePasswordCredentialsProvider(token, "");

        Git git;
        if (localRepoDir.exists() && new File(localRepoDir, ".git").exists()) {
            git = Git.open(localRepoDir);
            git.pull()
                    .setCredentialsProvider(credentials)
                    .call();
        } else {
            git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(localRepoDir)
                    .setCredentialsProvider(credentials)
                    .setBranch(branch)
                    .call();
        }

        File mdFile = new File(localRepoDir, fileName);

        if (mdFile.exists() && fileName.toLowerCase().endsWith(".md")) {
            String existingContent = Files.readString(mdFile.toPath());
            String combinedContent = existingContent + "\n\n" + markdownContent;

            try (FileWriter writer = new FileWriter(mdFile)) {
                writer.write(combinedContent);
            }
        } else {
            // Write fresh content if file doesn't exist or is not markdown
            try (FileWriter writer = new FileWriter(mdFile)) {
                writer.write(markdownContent);
            }
        }

        git.add().addFilepattern(fileName).call();
        git.commit().setMessage(commitMessage).call();

        git.push()
                .setCredentialsProvider(credentials)
                .call();

        git.close();
    }
}

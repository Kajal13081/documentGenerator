package org.example.documentGenerator.ui;

import org.example.backend.DocumentationGenerator;
import org.example.backend.ExportOptions;
import org.example.backend.Exporter;
import org.example.backend.Settings;
import org.example.services.GitHubMarkdownService;
import org.example.services.GitHubService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.List;
import java.util.Map;

public class DocumentationGeneratorUI extends JFrame {
    private JTextArea repoUrlField;
    private JTextField outputPathField;
    private JButton browseButton;
    private JButton getGitHubContentButton;
    private JButton generateLLMDocButton;
    private JButton saveButton;
    private JTextArea responseArea;
    private JProgressBar progressBar;
    private GitHubService gitHubService;
    private DocumentationGenerator documentationGenerator;
    private JTextArea customPromptArea;
    private JComboBox<String> exportFormatComboBox;
    private Settings appSettings;
    private JButton exportToGitHubButton;

    private static final Map<String, Exporter> exporters = Map.of(
            "Markdown (.md)", new ExportOptions.MdExporter(),
            "Text (.txt)", new ExportOptions.TxtExporter(),
            "PDF (.pdf)", new ExportOptions.PdfExporter(),
            "Word (.docx)", new ExportOptions.DocExporter(),
            "HTML (.html)", new ExportOptions.HtmlExporter(),
            "JSON (.json)", new ExportOptions.JsonExporter()
    );

    public DocumentationGeneratorUI() {
        setTitle("Documentation Generator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        add(createInputPanel(), BorderLayout.NORTH);
        add(createResponsePanel(), BorderLayout.CENTER);
        add(createOutputPanel(), BorderLayout.SOUTH);

        gitHubService = new GitHubService();
        appSettings = new Settings("", 0.7, 1000,"llama3-70b-8192");
        documentationGenerator = new DocumentationGenerator("", "","", appSettings);

        createMenuBar();

    }

    private void openSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(this, appSettings);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            appSettings = dialog.getSettings();

            boolean apiKeySet = appSettings.getApiKey() != null && !appSettings.getApiKey().isBlank();
            generateLLMDocButton.setEnabled(apiKeySet);
        }
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("AI Services");

        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(e -> openSettingsDialog());

        menu.add(settingsItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);
    }


    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Repository URL label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Repository URL:"), gbc);

        // Repository URL text field
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        repoUrlField = new JTextArea(1, 40);
        repoUrlField.setLineWrap(true);
        repoUrlField.setWrapStyleWord(true);
        panel.add(repoUrlField, gbc);

        // Button panel for parallel buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // Get GitHub Content button
        getGitHubContentButton = new JButton("Get GitHub Content");
        getGitHubContentButton.addActionListener(this::getGitHubContentClicked);
        buttonPanel.add(getGitHubContentButton);

        // Generate LLM Document button
        generateLLMDocButton = new JButton("Generate LLM Document");
        generateLLMDocButton.addActionListener(this::generateLLMDocClicked);
        generateLLMDocButton.setEnabled(false);
        buttonPanel.add(generateLLMDocButton);

        // use cases button
        JButton seeUseCasesButton = new JButton("See More Use Cases");
        seeUseCasesButton.addActionListener(e -> {
            UseCasesDialog dialog = new UseCasesDialog(this, customPromptArea);
            dialog.setVisible(true);
        });
        buttonPanel.add(seeUseCasesButton);

        // Add button panel to main panel
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(buttonPanel, gbc);

        // Custom prompt label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Custom Prompt:"), gbc);

        // Custom prompt text area inside scroll pane
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;  // Allow vertical resizing
        gbc.weighty = 0.3;  // Give some vertical weight so it grows nicely

        customPromptArea = new JTextArea(5, 40);
        customPromptArea.setLineWrap(true);
        customPromptArea.setWrapStyleWord(true);
        JScrollPane promptScrollPane = new JScrollPane(customPromptArea);
        panel.add(promptScrollPane, gbc);

        return panel;
    }

    private JPanel createResponsePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        progressBar = new JProgressBar();
        panel.add(progressBar, BorderLayout.NORTH);

        responseArea = new JTextArea(20, 60);
        responseArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(responseArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Output path label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Output Path:"), gbc);

        // Output path text field
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        outputPathField = new JTextField(30);
        panel.add(outputPathField, gbc);

        // Browse button
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        browseButton = new JButton("Browse");
        browseButton.addActionListener(this::browseButtonClicked);
        panel.add(browseButton, gbc);

        // Export format label
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Export Format:"), gbc);

        // Export format combo box
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        exportFormatComboBox = new JComboBox<>(new String[] {"Markdown (.md)", "Text (.txt)", "PDF (.pdf)", "Word (.docx)", "HTML (.html)", "JSON (.json)"});
        panel.add(exportFormatComboBox, gbc);

        // Save button
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0; // allow horizontal stretch
        saveButton = new JButton("Export");
        saveButton.addActionListener(this::saveButtonClicked);
        panel.add(saveButton, gbc);

        // Export to GitHub button
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        exportToGitHubButton = new JButton("Export to GitHub Repo");
        exportToGitHubButton.addActionListener(e -> exportToGitHub());
        panel.add(exportToGitHubButton, gbc);


        return panel;
    }

    private void exportToGitHub() {
        if (responseArea.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "No documentation content to export.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        GitHubExportDialog dialog = new GitHubExportDialog(this);
        dialog.setVisible(true);

        if (!dialog.isSaved()) {
            return;
        }

        String repoUrl = dialog.getRepoUrl();
        String branch = dialog.getBranch();
        String token = dialog.getToken();
        String commitMsg = dialog.getCommitMessage();
        String readmeFileName = dialog.getReadmeFileName();

        String markdownContent = responseArea.getText();
        File localRepoDir = new File(System.getProperty("user.home"), ".docgen_repo_cache");

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to push changes to the remote repository?",
                "Confirm Push",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                GitHubMarkdownService.exportMarkdownToRepo(
                        repoUrl, branch, token, readmeFileName, markdownContent, commitMsg, localRepoDir);
                return null;
            }

            @Override
            protected void done() {
                exportToGitHubButton.setEnabled(true);
                try {
                    get();
                    JOptionPane.showMessageDialog(DocumentationGeneratorUI.this, "Documentation exported successfully to GitHub repo!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DocumentationGeneratorUI.this, "Error exporting to GitHub repo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void browseButtonClicked(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            outputPathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void getGitHubContentClicked(ActionEvent e) {
        String repoUrl = repoUrlField.getText();

        if (repoUrl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Repository URL.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        disableInputs();
        resetProgress();

        new SwingWorker<String, String>() {
            @Override
            protected String doInBackground() throws Exception {
                // Use the method returning Map<String, String>
                Map<String, String> filesContent = gitHubService.getRepositoryFilesContent(repoUrl);

                // Format map content into a single String for display
                StringBuilder combinedContent = new StringBuilder();
                for (Map.Entry<String, String> entry : filesContent.entrySet()) {
                    combinedContent.append("File: ").append(entry.getKey()).append("\n");
                    combinedContent.append(entry.getValue()).append("\n\n");
                }
                return combinedContent.toString();
            }

            @Override
            protected void done() {
                try {
                    String content = get();
                    responseArea.setText(content);
                    generateLLMDocButton.setEnabled(true);
                    JOptionPane.showMessageDialog(DocumentationGeneratorUI.this, "GitHub content retrieved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DocumentationGeneratorUI.this, "Error retrieving GitHub content: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    enableInputs();
                    progressBar.setValue(100);
                }
            }
        }.execute();
    }

    private void generateLLMDocClicked(ActionEvent e) {
        String repoUrl = repoUrlField.getText();
        String outputPath = outputPathField.getText();
        String repoContent = responseArea.getText();

        if (repoContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please retrieve GitHub content first.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (appSettings.getApiKey() == null || appSettings.getApiKey().isBlank()) {
            JOptionPane.showMessageDialog(this, "API key is missing. Please configure it in Settings.", "Missing API Key", JOptionPane.WARNING_MESSAGE);
            return;
        }

        disableInputs();
        resetProgress();

        new SwingWorker<String, String>() {
            @Override
            protected String doInBackground() throws Exception {
                String customPrompt = customPromptArea.getText().trim();
                // Pass customPrompt only if not empty; else pass null or empty string as per your constructor's expectation
                String promptToUse = customPrompt.isEmpty() ? null : customPrompt;
                documentationGenerator = new DocumentationGenerator(repoUrl, outputPath, promptToUse, appSettings);
                return documentationGenerator.generate(this::publish);
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    responseArea.append(message + "\n");
                    responseArea.setCaretPosition(responseArea.getDocument().getLength());
                }
            }

            @Override
            protected void done() {
                try {
                    String finalDocumentation = get();
                    responseArea.setText(finalDocumentation);
                    saveButton.setEnabled(true);
                    JOptionPane.showMessageDialog(DocumentationGeneratorUI.this, "LLM documentation generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DocumentationGeneratorUI.this, "Error generating LLM documentation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    enableInputs();
                    progressBar.setValue(100);
                }
            }
        }.execute();
    }

    private void disableInputs() {
        repoUrlField.setEnabled(false);
        getGitHubContentButton.setEnabled(false);
        generateLLMDocButton.setEnabled(false);
        outputPathField.setEnabled(false);
    }

    private void enableInputs() {
        repoUrlField.setEnabled(true);
        getGitHubContentButton.setEnabled(true);
        outputPathField.setEnabled(true);
        browseButton.setEnabled(true);
        // generateLLMDocButton is enabled only after GitHub content is retrieved
    }

    private void saveButtonClicked(ActionEvent e) {
        String outputPath = outputPathField.getText().trim();
        if (outputPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an output path or file name.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String docContent = responseArea.getText();
        if (docContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No documentation content to export.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedFormat = (String) exportFormatComboBox.getSelectedItem();
        Exporter exporter = exporters.get(selectedFormat);

        if (exporter == null) {
            JOptionPane.showMessageDialog(this, "Unsupported export format: " + selectedFormat, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File output = new File(outputPath);

        if (output.isDirectory()) {
            output = new File(output, "documentation." + selectedFormat);
        } else {
            if (!output.getName().endsWith("." + selectedFormat)) {
                output = new File(output.getAbsolutePath());
            }
        }

        try {
            exporter.export(output.getAbsolutePath(), docContent);
            JOptionPane.showMessageDialog(this, "Exported successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error exporting documentation: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetProgress() {
        progressBar.setValue(0);
        responseArea.setText("");
    }
}
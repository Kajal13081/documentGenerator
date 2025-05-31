package org.example.documentGenerator.ui;

import org.example.backend.DocumentationGenerator;
import org.example.services.GitHubService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

public class DocumentationGeneratorUI extends JFrame {
    private JTextField repoUrlField;
    private JTextField outputPathField;
    private JButton browseButton;
    private JButton getGitHubContentButton;
    private JButton generateLLMDocButton;
    private JButton saveButton;
    private JTextArea responseArea;
    private JProgressBar progressBar;
    private GitHubService gitHubService;
    private DocumentationGenerator documentationGenerator;

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
        documentationGenerator = new DocumentationGenerator("", "");

        loadConfig();
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
        repoUrlField = new JTextField(40);
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

        // Add button panel to main panel
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(buttonPanel, gbc);

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
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Output Path:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        outputPathField = new JTextField(30);
        panel.add(outputPathField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        browseButton = new JButton("Browse");
        browseButton.addActionListener(this::browseButtonClicked);
        panel.add(browseButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        saveButton = new JButton("Save Documentation");
        saveButton.addActionListener(this::saveButtonClicked);
        saveButton.setEnabled(false);
        panel.add(saveButton, gbc);

        return panel;
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

        if (outputPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an output path.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        disableInputs();
        resetProgress();

        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                documentationGenerator = new DocumentationGenerator(repoUrl, outputPath);
                documentationGenerator.generate(this::publish);
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    responseArea.append(message + "\n");
                    responseArea.setCaretPosition(responseArea.getDocument().getLength());
                }
            }

            @Override
            protected void done() {
                try {
                    get();
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
        browseButton.setEnabled(false);
        saveButton.setEnabled(false);
    }

    private void enableInputs() {
        repoUrlField.setEnabled(true);
        getGitHubContentButton.setEnabled(true);
        outputPathField.setEnabled(true);
        browseButton.setEnabled(true);
        // generateLLMDocButton is enabled only after GitHub content is retrieved
    }

    private void saveButtonClicked(ActionEvent e) {
        String outputPath = outputPathField.getText();
        if (outputPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an output path.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File outputFile = new File(outputPath, "DOCUMENTATION.md");
        try (PrintWriter out = new PrintWriter(outputFile)) {
            out.println(responseArea.getText());
            JOptionPane.showMessageDialog(this, "Documentation saved to " + outputFile.getAbsolutePath(), "Save Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Error saving documentation: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetProgress() {
        progressBar.setValue(0);
        responseArea.setText("");
        saveButton.setEnabled(false);
    }

    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            Properties props = new Properties();
            props.load(input);
            repoUrlField.setText(props.getProperty("default.repo.url", ""));
            outputPathField.setText(props.getProperty("default.output.path", ""));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load configuration: " + e.getMessage(), "Configuration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
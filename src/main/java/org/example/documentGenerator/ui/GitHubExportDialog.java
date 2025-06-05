package org.example.documentGenerator.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GitHubExportDialog extends JDialog {

    private JTextField repoUrlField;
    private JTextField branchField;
    private JTextField tokenField;
    private JTextField commitMsgField;
    private JTextField readmeFile;
    private boolean saved = false;

    public GitHubExportDialog(JFrame parent) {
        super(parent, "Export Markdown to GitHub", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        repoUrlField = new JTextField(30);
        branchField = new JTextField("main", 10);
        tokenField = new JTextField(30);
        commitMsgField = new JTextField("Update documentation", 30);
        readmeFile = new JTextField(30);

        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("GitHub Repo URL:"), gbc);
        gbc.gridx = 1;
        add(repoUrlField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Branch:"), gbc);
        gbc.gridx = 1;
        add(branchField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("GitHub Token:"), gbc);
        gbc.gridx = 1;
        add(tokenField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Commit Message:"), gbc);
        gbc.gridx = 1;
        add(commitMsgField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Readme File Name:"), gbc);
        gbc.gridx = 1;
        add(readmeFile, gbc);

        JButton exportBtn = new JButton("Export");
        exportBtn.addActionListener(this::onExportClicked);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(exportBtn, gbc);

        pack();
        setLocationRelativeTo(parent);
    }

    private void onExportClicked(ActionEvent e) {
        if (repoUrlField.getText().isBlank() || tokenField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Please provide Repo URL and Token", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        saved = true;
        setVisible(false);
    }

    public boolean isSaved() {
        return saved;
    }

    public String getRepoUrl() {
        return repoUrlField.getText().trim();
    }

    public String getBranch() {
        return branchField.getText().trim();
    }

    public String getToken() {
        return tokenField.getText().trim();
    }

    public String getCommitMessage() {
        return commitMsgField.getText().trim();
    }

    public String getReadmeFileName() {
        return readmeFile.getText().trim();
    }
}


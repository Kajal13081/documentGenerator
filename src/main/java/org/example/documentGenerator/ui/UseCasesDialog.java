package org.example.documentGenerator.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UseCasesDialog extends JDialog {
    private JList<String> useCaseList;
    private JButton copyButton;
    private JButton closeButton;
    private JTextArea customPromptArea;

    private static final String[] useCases = {
            "Generate Full Project Documentation",
            "Summarize README and Key Files",
            "Create API Reference Documentation",
            "Generate Inline Code Comments/Annotations",
            "Produce Usage Examples for Functions/Classes",
            "Identify TODOs and Future Improvements",
            "Changelog Generation from Commit History",
            "Explain Complex Code Logic in Simple Terms",
            "Generate Testing Instructions and Scenarios",
            "Suggest Code Refactoring and Improvements",
            "Document Configuration and Setup Steps",
            "Generate Architecture Overview Diagrams (Text Descriptions)"
    };

    public UseCasesDialog(Frame owner, JTextArea customPromptArea) {
        super(owner, "More Use Cases", true);
        this.customPromptArea = customPromptArea;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        useCaseList = new JList<>(useCases);
        useCaseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(useCaseList);

        copyButton = new JButton("Copy to Prompt");
        copyButton.setEnabled(false);
        copyButton.addActionListener(e -> copySelectedUseCase());

        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        useCaseList.addListSelectionListener(e -> {
            copyButton.setEnabled(!useCaseList.isSelectionEmpty());
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(copyButton);
        buttonPanel.add(closeButton);

        add(new JLabel("Select a use case to copy to your prompt:"), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 300);
        setLocationRelativeTo(getOwner());
    }

    private void copySelectedUseCase() {
        String selected = useCaseList.getSelectedValue();
        if (selected != null) {
            customPromptArea.setText(selected);
            JOptionPane.showMessageDialog(this, "Use case copied to prompt area!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}


package org.example.documentGenerator.ui;

import org.example.backend.Settings;
import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {
    private JTextField apiKeyField;
    private JTextField temperatureField;
    private JTextField maxTokensField;
    private JTextField modelField;
    private JComboBox<String> modelComboBox;
    private Settings settings;

    private boolean saved = false;

    public SettingsDialog(Frame owner, Settings currentSettings) {
        super(owner, "Settings", true);
        this.settings = currentSettings;

        String[] supportedModels = {
                "llama3-7b-8192",
                "llama3-13b-8192",
                "llama3-70b-8192",
                "llama3-7b-16384",
                "llama3-13b-16384",
                "llama3-70b-16384"
        };

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // API Key
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("API Key:"), gbc);
        apiKeyField = new JTextField(currentSettings.getApiKey());
        gbc.gridx = 1; gbc.gridy = 0;
        add(apiKeyField, gbc);

        // Temperature
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Temperature (0.0 - 1.0):"), gbc);
        temperatureField = new JTextField(String.valueOf(currentSettings.getTemperature()));
        gbc.gridx = 1; gbc.gridy = 1;
        add(temperatureField, gbc);

        // Max Tokens
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Max Tokens:"), gbc);
        maxTokensField = new JTextField(String.valueOf(currentSettings.getMaxTokens()));
        gbc.gridx = 1; gbc.gridy = 2;
        add(maxTokensField, gbc);

        // Model
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Model:"), gbc);
        modelComboBox = new JComboBox<>(supportedModels);
        modelComboBox.setSelectedItem(settings.getModel());
        gbc.gridx = 1; gbc.gridy = 3;
        add(modelComboBox, gbc);

        // Buttons Panel
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        buttons.add(saveBtn);
        buttons.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        add(buttons, gbc);

        saveBtn.addActionListener(e -> onSave());
        cancelBtn.addActionListener(e -> onCancel());

        pack();
        setLocationRelativeTo(owner);
    }

    private void onSave() {
        try {
            String apiKey = apiKeyField.getText().trim();
            if (apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(this, "API Key cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double temperature = Double.parseDouble(temperatureField.getText().trim());
            if (temperature < 0 || temperature > 1) {
                JOptionPane.showMessageDialog(this, "Temperature must be between 0.0 and 1.0.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int maxTokens = Integer.parseInt(maxTokensField.getText().trim());
            if (maxTokens <= 0) {
                JOptionPane.showMessageDialog(this, "Max Tokens must be positive.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String model = (String) modelComboBox.getSelectedItem();
            if (model.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Model cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update settings object
            settings.setApiKey(apiKey);
            settings.setTemperature(temperature);
            settings.setMaxTokens(maxTokens);
            settings.setModel((String) modelComboBox.getSelectedItem());

            saved = true;
            setVisible(false);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for Temperature and Max Tokens.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel() {
        saved = false;
        setVisible(false);
    }

    public boolean isSaved() {
        return saved;
    }

    public Settings getSettings() {
        return settings;
    }
}

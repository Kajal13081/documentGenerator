package org.example.backend;

public class Settings {
    private String apiKey;
    private double temperature;
    private int maxTokens;
    private String model;

    public Settings(String apiKey, double temperature, int maxTokens, String model) {
        this.apiKey = apiKey;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public String getModel() {
        return model;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public void setModel(String model) {
        this.model = model;
    }

}



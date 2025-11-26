package com.example.vagmobile.model;

import java.io.Serializable;

public class DocPage implements Serializable {
    private String title;
    private String rawUrl;

    public DocPage(String title, String rawUrl) {
        this.title = title;
        this.rawUrl = rawUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getRawUrl() {
        return rawUrl;
    }

    // Добавьте этот метод для удобства
    public String getPath() {
        if (rawUrl.contains("https://raw.githubusercontent.com/MaryZhminkovskaya/VAG_Mobile/Mary/docs/")) {
            String path = rawUrl.substring("https://raw.githubusercontent.com/MaryZhminkovskaya/VAG_Mobile/Mary/docs/".length());
            if (path.endsWith("README.md")) {
                return path.substring(0, path.length() - 9);
            }
            return path;
        }
        return null;
    }
}
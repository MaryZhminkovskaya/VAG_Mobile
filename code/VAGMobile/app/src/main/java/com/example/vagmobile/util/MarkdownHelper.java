package com.example.vagmobile.util;

import android.util.Log;

public class MarkdownHelper {

    private static final String TAG = "MarkdownHelper";
    private static final String BASE_URL = "https://raw.githubusercontent.com/MaryZhminkovskaya/VAG_Mobile/Mary/docs/";

    public static String processMarkdown(String content) {
        if (content == null) return "";

        Log.d(TAG, "Processing markdown content");

        // Обрабатываем изображения
        content = processImages(content);

        // Обрабатываем ссылки
        content = processLinks(content);

        Log.d(TAG, "Markdown processing completed");
        return content;
    }

    private static String processImages(String content) {
        int originalLength = content.length();

        content = content.replaceAll(
                "!\\[([^\\]]*)\\]\\(\\.\\./images/([^)]+)\\)",
                "![$1](" + BASE_URL + "images/$2)"
        );

        content = content.replaceAll(
                "!\\[([^\\]]*)\\]\\(\\./images/([^)]+)\\)",
                "![$1](" + BASE_URL + "images/$2)"
        );

        content = content.replaceAll(
                "!\\[([^\\]]*)\\]\\(/images/([^)]+)\\)",
                "![$1](" + BASE_URL + "images/$2)"
        );

        if (content.length() != originalLength) {
            Log.d(TAG, "Images processed and converted");
        }

        return content;
    }

    private static String processLinks(String content) {
        int linkCount = 0;

        // Обрабатываем ссылки вида [текст](./path)
        String processed = content.replaceAll(
                "\\]\\(\\./([^)]+)\\)",
                "](" + BASE_URL + "$1README.md)"
        );
        if (!processed.equals(content)) {
            linkCount++;
            content = processed;
        }

        // Обрабатываем ссылки вида [текст](/path/)
        processed = content.replaceAll(
                "\\]\\(/([^)]+)/\\)",
                "](" + BASE_URL + "$1/README.md)"
        );
        if (!processed.equals(content)) {
            linkCount++;
            content = processed;
        }

        // Обрабатываем ссылки вида [текст](/path)
        processed = content.replaceAll(
                "\\]\\(/([^)]+)\\)",
                "](" + BASE_URL + "$1/README.md)"
        );
        if (!processed.equals(content)) {
            linkCount++;
            content = processed;
        }

        Log.d(TAG, "Processed " + linkCount + " links");
        return content;
    }
}
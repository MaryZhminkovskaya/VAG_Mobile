package com.example.vagmobile.util;

import android.util.Log;

public class MarkdownHelper {

    private static final String TAG = "MarkdownHelper";
    private static final String BASE_URL = "https://raw.githubusercontent.com/MaryZhminkovskaya/VAG_Mobile/Mary/docs/";

    public static String processMarkdown(String content) {
        if (content == null) return "";

        Log.d(TAG, "Processing markdown content");

        content = processImages(content);

        content = processLinks(content);

        Log.d(TAG, "Markdown processing completed");
        return content;
    }

    private static String processImages(String content) {
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

        return content;
    }

    private static String processLinks(String content) {
        content = content.replaceAll(
                "\\]\\(\\./([^)]+)\\)",
                "](" + BASE_URL + "$1README.md)"
        );

        content = content.replaceAll(
                "\\]\\(/([^)]+)/\\)",
                "](" + BASE_URL + "$1/README.md)"
        );

        content = content.replaceAll(
                "\\]\\(/([^)]+)\\)",
                "](" + BASE_URL + "$1/README.md)"
        );

        return content;
    }
}
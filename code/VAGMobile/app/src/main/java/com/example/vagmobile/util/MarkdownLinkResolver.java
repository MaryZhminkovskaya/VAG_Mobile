package com.example.vagmobile.util;

import android.view.View;

import androidx.annotation.NonNull;

import io.noties.markwon.LinkResolver;

public class MarkdownLinkResolver implements LinkResolver {

    private final CustomLinkResolver customLinkResolver;

    public MarkdownLinkResolver(CustomLinkResolver customLinkResolver) {
        this.customLinkResolver = customLinkResolver;
    }

    @Override
    public void resolve(@NonNull View view, @NonNull String link) {
        // Передаем все ссылки в наш кастомный обработчик
        customLinkResolver.resolveLink(link);
    }
}
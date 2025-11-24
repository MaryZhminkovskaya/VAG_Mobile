package com.example.vagmobile.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.vagmobile.R;

public class DocumentationFragment extends Fragment {

    private WebView webView;
    private ProgressBar progressBar;
    private static final String DOCS_URL = "https://maryzhminkovskaya.github.io/VAG_Mobile/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_documentation, container, false);
        initViews(view);
        setupWebView();
        loadDocumentation();
        return view;
    }

    private void initViews(View view) {
        webView = view.findViewById(R.id.webView);
        progressBar = view.findViewById(R.id.progressBar);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        // Обработка загрузки страниц
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                // Если ссылка ведет на нашу документацию - открываем в WebView
                if (url.contains("maryzhminkovskaya.github.io/VAG_Mobile")) {
                    return false; // Позволяем WebView обработать ссылку
                }

                // Для внешних ссылок открываем в браузере
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });

        // Отслеживание прогресса загрузки
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadDocumentation() {
        progressBar.setVisibility(View.VISIBLE);
        webView.loadUrl(DOCS_URL);
    }

    // Обработка кнопки "Назад" для навигации в WebView
    public boolean canGoBack() {
        return webView != null && webView.canGoBack();
    }

    public void goBack() {
        if (webView != null) {
            webView.goBack();
        }
    }

    @Override
    public void onDestroyView() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroyView();
    }
}
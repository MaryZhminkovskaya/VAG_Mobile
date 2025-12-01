//package com.example.vagmobile.ui.activity;
//
//import androidx.appcompat.app.AppCompatActivity;
//import android.os.Bundle;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import com.example.vagmobile.R;
//
//public class DocumentationActivity extends AppCompatActivity {
//
//    private WebView webView;
//    private ProgressBar progressBar;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_documentation);
//
//        initViews();
//        loadDocumentation();
//    }
//
//    private void initViews() {
//        webView = findViewById(R.id.webView);
//        progressBar = findViewById(R.id.progressBar);
//
//        // Настройка WebView
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                progressBar.setVisibility(android.view.View.GONE);
//            }
//        });
//    }
//
//    private void loadDocumentation() {
//        // Загрузка документации (можно заменить на ваш URL)
//        String docsUrl = "https://github.com/your-username/your-repo/wiki";
//        webView.loadUrl(docsUrl);
//
//        // Или загрузка локального HTML
//        // webView.loadUrl("file:///android_asset/documentation.html");
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (webView.canGoBack()) {
//            webView.goBack();
//        } else {
//            super.onBackPressed();
//        }
//    }
//}
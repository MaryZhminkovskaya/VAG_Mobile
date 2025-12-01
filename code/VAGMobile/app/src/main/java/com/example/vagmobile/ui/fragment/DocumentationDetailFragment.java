package com.example.vagmobile.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.vagmobile.R;
import com.example.vagmobile.model.DocPage;
import com.example.vagmobile.util.CustomLinkMovementMethod;
import com.example.vagmobile.util.MarkdownHelper;
import com.example.vagmobile.viewmodel.DocumentationViewModel;

import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;

public class DocumentationDetailFragment extends Fragment {

    private static final String ARG_DOC_PAGE = "doc_page";
    private static final String TAG = "DocDetailFragment";

    private Markwon markwon;
    private ProgressBar progressBar;
    private TextView contentTextView;
    private DocumentationViewModel viewModel;
    private CustomLinkMovementMethod linkMovementMethod;

    public static DocumentationDetailFragment newInstance(DocPage docPage) {
        DocumentationDetailFragment fragment = new DocumentationDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DOC_PAGE, docPage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        markwon = Markwon.builder(requireContext())
                .usePlugin(CorePlugin.create())
                .usePlugin(GlideImagesPlugin.create(requireContext()))
                .usePlugin(LinkifyPlugin.create())
                .build();

        linkMovementMethod = new CustomLinkMovementMethod();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_documentation_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DocPage docPage = (DocPage) getArguments().getSerializable(ARG_DOC_PAGE);
        if (docPage != null) {
            TextView titleTextView = view.findViewById(R.id.tv_doc_detail_title);
            contentTextView = view.findViewById(R.id.tv_doc_detail_content);
            progressBar = view.findViewById(R.id.progressBar);

            titleTextView.setText(docPage.getTitle());
            setupLinkHandler();
            setupViewModel();
            viewModel.loadMarkdownContent(docPage.getRawUrl());
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DocumentationViewModel.class);

        viewModel.getCurrentContent().observe(getViewLifecycleOwner(), content -> {
            if (content != null) {
                String processedContent = MarkdownHelper.processMarkdown(content);
                markwon.setMarkdown(contentTextView, processedContent);
                contentTextView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                contentTextView.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                contentTextView.setText("Ошибка загрузки: " + error);
                contentTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupLinkHandler() {
        linkMovementMethod.setLinkClickListener(url -> handleLinkClick(url));
        contentTextView.setMovementMethod(linkMovementMethod);
    }

    private void handleLinkClick(String url) {
        try {
            DocPage targetPage = findDocPageByUrl(url);

            if (targetPage != null) {
                DocumentationDetailFragment detailFragment = DocumentationDetailFragment.newInstance(targetPage);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack("documentation")
                        .commit();
            } else {
                android.content.Intent browserIntent = new android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(url)
                );
                startActivity(browserIntent);
            }
        } catch (Exception e) {
            android.content.Intent browserIntent = new android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(url)
            );
            startActivity(browserIntent);
        }
    }

    private DocPage findDocPageByUrl(String url) {
        if (url == null || viewModel.docPages == null) return null;

        for (DocPage page : viewModel.docPages) {
            if (url.equals(page.getRawUrl())) {
                return page;
            }

            String normalizedUrl = url.replace("README.md", "");
            String normalizedPageUrl = page.getRawUrl().replace("README.md", "");
            if (normalizedUrl.equals(normalizedPageUrl)) {
                return page;
            }
        }
        return null;
    }
}
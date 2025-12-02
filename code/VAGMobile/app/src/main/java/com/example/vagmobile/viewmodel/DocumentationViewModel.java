package com.example.vagmobile.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.vagmobile.model.DocPage;
import com.example.vagmobile.repository.DocumentationRepository;

import java.util.Arrays;
import java.util.List;

public class DocumentationViewModel extends ViewModel {

    private final DocumentationRepository repository = new DocumentationRepository();
    private final MutableLiveData<String> currentContent = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private static final String DOCS_BASE_URL = "https://maryzhminkovskaya.github.io/VAG_Mobile/";

    public final List<DocPage> docPages = Arrays.asList(
            new DocPage("Главная", DOCS_BASE_URL + "README.md"),

            new DocPage("Шутова: главная", DOCS_BASE_URL + "shutova/README.md"),
            new DocPage("Шутова: руководство по выставкам", DOCS_BASE_URL + "shutova/guide/README.md"),
            new DocPage("Шутова: добавление работ", DOCS_BASE_URL + "shutova/guide/adding_works.md"),
            new DocPage("Шутова: управление выставками", DOCS_BASE_URL + "shutova/guide/managing.md"),
            new DocPage("Шутова: о проекте", DOCS_BASE_URL + "shutova/about/README.md"),
            new DocPage("Шутова: ЧаВо", DOCS_BASE_URL + "shutova/about/faq.md"),

            new DocPage("Жминьковская: главная", DOCS_BASE_URL + "zhminkovskaya/README.md"),
            new DocPage("Жминьковская: публикации", DOCS_BASE_URL + "zhminkovskaya/publications/README.md"),
            new DocPage("Жминьковская: создание публикаций",
                    DOCS_BASE_URL + "zhminkovskaya/publications/creating/README.md"),
            new DocPage("Жминьковская: управление публикациями",
                    DOCS_BASE_URL + "zhminkovskaya/publications/managing/README.md"),
            new DocPage("Жминьковская: формы публикаций", DOCS_BASE_URL + "zhminkovskaya/forms/README.md"),
            new DocPage("Жминьковская: ЧаВо", DOCS_BASE_URL + "zhminkovskaya/faq/README.md"));

    public void loadMarkdownContent(String rawUrl) {
        isLoading.setValue(true);

        repository.getMarkdownContent(rawUrl, new DocumentationRepository.DocumentationCallback() {
            @Override
            public void onSuccess(String markdownContent) {
                isLoading.setValue(false);
                currentContent.setValue(markdownContent);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public LiveData<String> getCurrentContent() {
        return currentContent;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
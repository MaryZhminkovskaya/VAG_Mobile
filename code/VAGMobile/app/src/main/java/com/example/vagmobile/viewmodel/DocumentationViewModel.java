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

    public final List<DocPage> docPages = Arrays.asList(
            new DocPage("Главная", "https://raw.githubusercontent.com/MaryZhminkovskaya/VAG_Mobile/Mary/docs/README.md"),
            new DocPage("Публикации", "https://raw.githubusercontent.com/MaryZhminkovskaya/VAG_Mobile/Mary/docs/publications/README.md"),
            new DocPage("Создание публикаций", "https://raw.githubusercontent.com/MaryZhminkovskaya/VAG_Mobile/Mary/docs/publications/creating/README.md"),
            new DocPage("Управление публикациями", "https://raw.githubusercontent.com/MaryZhminkovskaya/VAG_Mobile/Mary/docs/publications/managing/README.md"),
            new DocPage("Формы публикаций", "https://raw.githubusercontent.com/MaryZhminkovskaya/VAG_Mobile/Mary/docs/forms/README.md"),
            new DocPage("Частые вопросы", "https://raw.githubusercontent.com/MaryZhminkovskaya/VAG_Mobile/Mary/docs/faq/README.md")
    );

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

    public LiveData<String> getCurrentContent() { return currentContent; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
}
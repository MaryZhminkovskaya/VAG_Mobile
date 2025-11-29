package com.example.vag.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUploadUtil {

    private final String UPLOAD_BASE = "D:/Java/apache-tomcat-9.0.97/webapps/vag/uploads/";

    public String saveFile(Long userId, MultipartFile file) throws IOException {
        return saveFile(userId, file.getOriginalFilename(), file);
    }

    public String saveFile(Long userId, String fileName, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Генерируем уникальное имя файла
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // Создаем путь для сохранения - используем artwork-images
        String userDir = UPLOAD_BASE + "artwork-images/" + userId + "/";
        Path uploadPath = Paths.get(userDir);

        System.out.println("Saving file to: " + uploadPath.toString());

        if (!Files.exists(uploadPath)) {
            System.out.println("Creating directory: " + uploadPath);
            Files.createDirectories(uploadPath);
        }

        // Сохраняем файл
        Path filePath = uploadPath.resolve(uniqueFileName);
        System.out.println("Full file path: " + filePath.toString());

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File saved successfully: " + uniqueFileName);
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            throw e;
        }

        // Возвращаем относительный путь БЕЗ /uploads/ в начале
        // Так как mapping в Spring уже добавляет /uploads/
        return "artwork-images/" + userId + "/" + uniqueFileName;
    }

    /**
     * Метод для нормализации пути к изображению
     * Убирает лишние /uploads/ если они есть
     */
    public static String normalizeImagePath(String imagePath) {
        if (imagePath == null) return null;

        // Убираем начальный слеш если есть
        if (imagePath.startsWith("/")) {
            imagePath = imagePath.substring(1);
        }

        // Убираем дублирующиеся uploads/
        if (imagePath.startsWith("uploads/")) {
            imagePath = imagePath.substring("uploads/".length());
        }

        return imagePath;
    }

    /**
     * Метод для получения полного URL для клиента
     */
    public static String getFullImageUrl(String baseUrl, String imagePath) {
        if (imagePath == null) return null;

        String normalizedPath = normalizeImagePath(imagePath);
        return baseUrl + "/uploads/" + normalizedPath;
    }
}
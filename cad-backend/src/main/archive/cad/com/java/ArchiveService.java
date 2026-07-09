package com.cad.archive;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class ArchiveService {

    private final String supabaseUrl;
    private final String supabaseAnonKey;
    private final String supabaseBucket;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ArchiveService() {
        this.supabaseUrl = System.getenv("SUPABASE_URL");
        this.supabaseAnonKey = System.getenv("SUPABASE_ANON_KEY");
        this.supabaseBucket = System.getenv("SUPABASE_BUCKET");

        if (supabaseUrl == null || supabaseAnonKey == null || supabaseBucket == null) {
            throw new IllegalStateException(
                "Supabase credentials not configured. Set SUPABASE_URL, SUPABASE_ANON_KEY, and SUPABASE_BUCKET environment variables."
            );
        }
    }

    public String save(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new IOException("Filename is empty");
        }

        // Sanitize filename to prevent path traversal
        fileName = sanitizeFilename(fileName);

        // Build proper Supabase Storage URL
        // Format: https://xxxxx.supabase.co/storage/v1/object/public/{bucket}/{path}
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        String storageUrl = String.format(
            "%s/storage/v1/object/public/%s/%s",
            supabaseUrl,
            supabaseBucket,
            encodedFileName
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(storageUrl))
                .header("Authorization", "Bearer " + supabaseAnonKey)
                .header("Content-Type", file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                .timeout(java.time.Duration.ofSeconds(30))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Accept 2xx status codes (200 OK, 201 Created, etc.)
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException(
                    String.format("Cloud storage upload failed with status %d: %s", 
                        response.statusCode(), response.body())
                );
            }

            return fileName;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrupted", e);
        }
    }

    /**
     * Sanitize filename to prevent path traversal attacks
     */
    private String sanitizeFilename(String fileName) {
        // Remove path separators and special characters
        return fileName
                .replaceAll("\\.\\.[\\\\/]", "")  // Remove ../ and ..\
                .replaceAll("[<>:\"|?*]", "")     // Remove invalid filename chars
                .replaceAll("^[\\\\/]+", "")      // Remove leading slashes
                .trim();
    }
}

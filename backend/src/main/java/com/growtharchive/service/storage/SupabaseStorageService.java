package com.growtharchive.service.storage;

import com.growtharchive.config.properties.AppProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SupabaseStorageService implements StorageService {

    private final AppProperties properties;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public SupabaseStorageService(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public StoredImage storeReviewImage(Long memberId, OptimizedImage image) throws IOException {
        String bucket = properties.getStorage().getSupabase().getBucket();
        String objectKey = "meeting-reviews/%d/%s/%s.jpg".formatted(
            memberId,
            LocalDate.now(),
            UUID.randomUUID()
        );
        if (!isConfigured()) {
            return new StoredImage(bucket, "local-dev/not-stored/" + objectKey, null, image.mimeType(), image.sizeBytes());
        }
        String baseUrl = trimTrailingSlash(properties.getStorage().getSupabase().getUrl());
        String encodedKey = objectKey.replace(" ", "%20");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("%s/storage/v1/object/%s/%s".formatted(baseUrl, bucket, encodedKey)))
            .header("Authorization", "Bearer " + properties.getStorage().getSupabase().getServiceRoleKey())
            .header("Content-Type", image.mimeType())
            .PUT(HttpRequest.BodyPublishers.ofByteArray(image.bytes()))
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Supabase Storage upload failed: " + response.statusCode());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Supabase Storage upload interrupted", exception);
        }
        return new StoredImage(
            bucket,
            objectKey,
            "%s/storage/v1/object/public/%s/%s".formatted(baseUrl, bucket, encodedKey),
            image.mimeType(),
            image.sizeBytes()
        );
    }

    private boolean isConfigured() {
        return hasText(properties.getStorage().getSupabase().getUrl())
            && hasText(properties.getStorage().getSupabase().getServiceRoleKey());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

}

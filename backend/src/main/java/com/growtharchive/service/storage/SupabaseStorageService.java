package com.growtharchive.service.storage;

import com.growtharchive.config.properties.AppProperties;
import java.io.IOException;
import java.net.URLEncoder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SupabaseStorageService implements StorageService {

    private final AppProperties properties;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Path localUploadRoot = Path.of(System.getProperty("java.io.tmpdir"), "growth-archive-uploads");

    public SupabaseStorageService(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public StoredImage storeImage(Long memberId, String imageType, OptimizedImage image) throws IOException {
        String bucket = properties.getStorage().getSupabase().getBucket();
        String ownerSegment = memberId == null ? "signup" : memberId.toString();
        String objectKey = "%s/%s/%s/%s%s".formatted(
            folderFor(imageType),
            ownerSegment,
            LocalDate.now(),
            UUID.randomUUID(),
            extensionFor(image.mimeType())
        );
        if (!isConfigured()) {
            if (!properties.getStorage().isLocalFallbackEnabled()) {
                throw new IOException("Supabase Storage is not configured");
            }
            Path target = safeLocalPath(objectKey);
            Files.createDirectories(target.getParent());
            Files.write(target, image.bytes());
            return new StoredImage(bucket, objectKey, localPublicUrl(objectKey), image.mimeType(), image.sizeBytes(), image.width(), image.height());
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
            image.sizeBytes(),
            image.width(),
            image.height()
        );
    }

    @Override
    public Optional<StoredLocalImage> loadLocalImage(String objectKey) throws IOException {
        if (isConfigured() || objectKey == null || objectKey.isBlank()) {
            return Optional.empty();
        }
        Path path = safeLocalPath(objectKey);
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        String mimeType = Files.probeContentType(path);
        return Optional.of(new StoredLocalImage(Files.readAllBytes(path), mimeType == null ? "image/jpeg" : mimeType));
    }

    private String folderFor(String imageType) {
        return switch (imageType) {
            case "PROFILE" -> "profiles";
            case "READING_RECORD" -> "reading-records";
            case "MEETING_COVER" -> "meetings";
            case "MEETING_REVIEW" -> "meeting-reviews";
            case "BOOK" -> "books";
            default -> "misc";
        };
    }

    private String extensionFor(String mimeType) {
        if ("image/webp".equals(mimeType)) {
            return ".webp";
        }
        return ".jpg";
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

    private Path safeLocalPath(String objectKey) throws IOException {
        Path root = localUploadRoot.toAbsolutePath().normalize();
        Path path = root.resolve(objectKey).normalize();
        if (!path.startsWith(root)) {
            throw new IOException("Invalid local upload path");
        }
        return path;
    }

    private String localPublicUrl(String objectKey) {
        return publicBackendBaseUrl() + "/api/v1/uploads/local?key=" + URLEncoder.encode(objectKey, StandardCharsets.UTF_8);
    }

    private String publicBackendBaseUrl() {
        try {
            URI redirectUri = URI.create(properties.getKakao().getRedirectUri());
            String scheme = redirectUri.getScheme() == null ? "http" : redirectUri.getScheme();
            int port = redirectUri.getPort();
            return scheme + "://" + redirectUri.getHost() + (port > 0 ? ":" + port : "");
        } catch (RuntimeException exception) {
            return "http://localhost:8080";
        }
    }
}

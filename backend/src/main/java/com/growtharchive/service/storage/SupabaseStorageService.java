package com.growtharchive.service.storage;

import com.growtharchive.config.properties.AppProperties;
import com.growtharchive.support.KstDateTimes;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class SupabaseStorageService implements StorageService {

    private static final DateTimeFormatter AMZ_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final DateTimeFormatter DATE_STAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String SIGNING_ALGORITHM = "AWS4-HMAC-SHA256";
    private static final String S3_SERVICE = "s3";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    private final AppProperties properties;
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(CONNECT_TIMEOUT)
        .build();
    private final Path localUploadRoot = Path.of(System.getProperty("java.io.tmpdir"), "growth-archive-uploads");

    public SupabaseStorageService(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public StoredImage storeImage(Long memberId, String imageType, OptimizedImage image) throws IOException {
        String bucket = properties.getStorage().getSupabase().getBucket();
        String ownerSegment = memberId == null ? "signup" : memberId.toString();
        String objectKey = "%s/%s/%s/%s/%s%s".formatted(
            environmentPrefix(),
            folderFor(imageType),
            ownerSegment,
            KstDateTimes.today(),
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
        if (isS3Configured()) {
            uploadWithS3(bucket, objectKey, image);
        } else {
            uploadWithRest(bucket, objectKey, image);
        }
        return new StoredImage(
            bucket,
            objectKey,
            publicUrl(bucket, objectKey),
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

    @Override
    public void deleteImage(String bucket, String objectKey) throws IOException {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        if (!isConfigured()) {
            Files.deleteIfExists(safeLocalPath(objectKey));
            return;
        }
        if (isS3Configured()) {
            deleteWithS3(bucket, objectKey);
            return;
        }
        deleteWithRest(bucket, objectKey);
    }

    private String folderFor(String imageType) {
        return switch (imageType) {
            case "PROFILE" -> "profile-images";
            case "BOOK" -> "book-covers";
            case "READING_RECORD" -> "reading-record-images";
            case "MEETING_REVIEW" -> "meeting-review-images";
            case "MEETING_COVER" -> "meeting-cover-images";
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
        return isS3Configured() || isRestConfigured();
    }

    private boolean isS3Configured() {
        AppProperties.Supabase supabase = properties.getStorage().getSupabase();
        return hasText(supabase.getS3Endpoint())
            && hasText(supabase.getS3AccessKeyId())
            && hasText(supabase.getS3SecretAccessKey());
    }

    private boolean isRestConfigured() {
        AppProperties.Supabase supabase = properties.getStorage().getSupabase();
        return hasText(supabase.getUrl()) && hasText(supabase.getServiceRoleKey());
    }

    private void uploadWithRest(String bucket, String objectKey, OptimizedImage image) throws IOException {
        String baseUrl = trimTrailingSlash(properties.getStorage().getSupabase().getUrl());
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("%s/storage/v1/object/%s/%s".formatted(baseUrl, encodePath(bucket), encodePath(objectKey))))
            .timeout(REQUEST_TIMEOUT)
            .header("Authorization", "Bearer " + properties.getStorage().getSupabase().getServiceRoleKey())
            .header("Content-Type", image.mimeType())
            .PUT(HttpRequest.BodyPublishers.ofByteArray(image.bytes()))
            .build();
        sendUploadRequest(request, "Supabase Storage upload failed");
    }

    private void uploadWithS3(String bucket, String objectKey, OptimizedImage image) throws IOException {
        AppProperties.Supabase supabase = properties.getStorage().getSupabase();
        URI endpoint = URI.create(trimTrailingSlash(supabase.getS3Endpoint()));
        String requestPath = "%s/%s/%s".formatted(endpointPath(endpoint), encodePath(bucket), encodePath(objectKey));
        URI requestUri = URI.create(endpointOrigin(endpoint) + requestPath);
        String payloadHash = sha256Hex(image.bytes());
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String amzDate = AMZ_DATE_FORMAT.format(now);
        String dateStamp = DATE_STAMP_FORMAT.format(now);
        String host = requestUri.getHost();
        String canonicalHeaders = "content-type:%s\nhost:%s\nx-amz-content-sha256:%s\nx-amz-date:%s\n".formatted(
            image.mimeType(),
            host,
            payloadHash,
            amzDate
        );
        String signedHeaders = "content-type;host;x-amz-content-sha256;x-amz-date";
        String canonicalRequest = "PUT\n%s\n\n%s\n%s\n%s".formatted(
            requestUri.getRawPath(),
            canonicalHeaders,
            signedHeaders,
            payloadHash
        );
        String credentialScope = "%s/%s/%s/aws4_request".formatted(dateStamp, supabase.getS3Region(), S3_SERVICE);
        String stringToSign = "%s\n%s\n%s\n%s".formatted(
            SIGNING_ALGORITHM,
            amzDate,
            credentialScope,
            sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8))
        );
        String signature = hex(hmac(signingKey(supabase.getS3SecretAccessKey(), dateStamp, supabase.getS3Region()), stringToSign));
        String authorization = "%s Credential=%s/%s, SignedHeaders=%s, Signature=%s".formatted(
            SIGNING_ALGORITHM,
            supabase.getS3AccessKeyId(),
            credentialScope,
            signedHeaders,
            signature
        );
        HttpRequest request = HttpRequest.newBuilder()
            .uri(requestUri)
            .timeout(REQUEST_TIMEOUT)
            .header("Authorization", authorization)
            .header("Content-Type", image.mimeType())
            .header("x-amz-content-sha256", payloadHash)
            .header("x-amz-date", amzDate)
            .PUT(HttpRequest.BodyPublishers.ofByteArray(image.bytes()))
            .build();
        sendUploadRequest(request, "Supabase S3 upload failed");
    }

    private void deleteWithRest(String bucket, String objectKey) throws IOException {
        String baseUrl = trimTrailingSlash(properties.getStorage().getSupabase().getUrl());
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("%s/storage/v1/object/%s/%s".formatted(baseUrl, encodePath(bucket), encodePath(objectKey))))
            .timeout(REQUEST_TIMEOUT)
            .header("Authorization", "Bearer " + properties.getStorage().getSupabase().getServiceRoleKey())
            .DELETE()
            .build();
        sendUploadRequest(request, "Supabase Storage delete failed");
    }

    private void deleteWithS3(String bucket, String objectKey) throws IOException {
        AppProperties.Supabase supabase = properties.getStorage().getSupabase();
        URI endpoint = URI.create(trimTrailingSlash(supabase.getS3Endpoint()));
        String requestPath = "%s/%s/%s".formatted(endpointPath(endpoint), encodePath(bucket), encodePath(objectKey));
        URI requestUri = URI.create(endpointOrigin(endpoint) + requestPath);
        String payloadHash = sha256Hex(new byte[0]);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String amzDate = AMZ_DATE_FORMAT.format(now);
        String dateStamp = DATE_STAMP_FORMAT.format(now);
        String host = requestUri.getHost();
        String canonicalHeaders = "host:%s\nx-amz-content-sha256:%s\nx-amz-date:%s\n".formatted(host, payloadHash, amzDate);
        String signedHeaders = "host;x-amz-content-sha256;x-amz-date";
        String canonicalRequest = "DELETE\n%s\n\n%s\n%s\n%s".formatted(
            requestUri.getRawPath(),
            canonicalHeaders,
            signedHeaders,
            payloadHash
        );
        String credentialScope = "%s/%s/%s/aws4_request".formatted(dateStamp, supabase.getS3Region(), S3_SERVICE);
        String stringToSign = "%s\n%s\n%s\n%s".formatted(
            SIGNING_ALGORITHM,
            amzDate,
            credentialScope,
            sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8))
        );
        String signature = hex(hmac(signingKey(supabase.getS3SecretAccessKey(), dateStamp, supabase.getS3Region()), stringToSign));
        String authorization = "%s Credential=%s/%s, SignedHeaders=%s, Signature=%s".formatted(
            SIGNING_ALGORITHM,
            supabase.getS3AccessKeyId(),
            credentialScope,
            signedHeaders,
            signature
        );
        HttpRequest request = HttpRequest.newBuilder()
            .uri(requestUri)
            .timeout(REQUEST_TIMEOUT)
            .header("Authorization", authorization)
            .header("x-amz-content-sha256", payloadHash)
            .header("x-amz-date", amzDate)
            .DELETE()
            .build();
        sendUploadRequest(request, "Supabase S3 delete failed");
    }

    private void sendUploadRequest(HttpRequest request, String message) throws IOException {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException(message + ": " + response.statusCode() + " " + summarizeBody(response.body()));
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException(message + ": interrupted", exception);
        }
    }

    private String summarizeBody(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String compact = body.replaceAll("\\s+", " ").trim();
        if (compact.length() <= 300) {
            return compact;
        }
        return compact.substring(0, 300) + "...";
    }

    String publicUrl(String bucket, String objectKey) {
        String baseUrl = publicBaseUrl();
        String encodedBucket = encodePath(bucket);
        String encodedObjectKey = encodePath(objectKey);
        if (baseUrl.endsWith("/" + encodedBucket)) {
            return "%s/%s".formatted(baseUrl, encodedObjectKey);
        }
        return "%s/%s/%s".formatted(baseUrl, encodedBucket, encodedObjectKey);
    }

    private String publicBaseUrl() {
        AppProperties.Supabase supabase = properties.getStorage().getSupabase();
        if (hasText(supabase.getPublicBaseUrl())) {
            return trimTrailingSlash(supabase.getPublicBaseUrl());
        }
        if (hasText(supabase.getUrl())) {
            return trimTrailingSlash(supabase.getUrl()) + "/storage/v1/object/public";
        }
        if (hasText(supabase.getS3Endpoint())) {
            URI endpoint = URI.create(trimTrailingSlash(supabase.getS3Endpoint()));
            String endpointPath = endpointPath(endpoint);
            String basePath = endpointPath.endsWith("/storage/v1/s3")
                ? endpointPath.substring(0, endpointPath.length() - "/storage/v1/s3".length())
                : "";
            return endpointOrigin(endpoint) + basePath + "/storage/v1/object/public";
        }
        return "";
    }

    private String environmentPrefix() {
        String prefix = properties.getStorage().getEnvironmentPrefix();
        if (!hasText(prefix)) {
            return "local";
        }
        return prefix.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "-");
    }

    private String endpointOrigin(URI endpoint) {
        int port = endpoint.getPort();
        return endpoint.getScheme() + "://" + endpoint.getHost() + (port > 0 ? ":" + port : "");
    }

    private String endpointPath(URI endpoint) {
        String path = endpoint.getRawPath();
        if (path == null || path.isBlank()) {
            return "";
        }
        return trimTrailingSlash(path);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String encodePath(String value) {
        return Arrays.stream(value.split("/", -1))
            .map(segment -> URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"))
            .reduce((left, right) -> left + "/" + right)
            .orElse("");
    }

    private String sha256Hex(byte[] value) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return hex(digest.digest(value));
        } catch (Exception exception) {
            throw new IOException("SHA-256 digest failed", exception);
        }
    }

    private byte[] signingKey(String secretKey, String dateStamp, String region) throws IOException {
        byte[] dateKey = hmac(("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8), dateStamp);
        byte[] dateRegionKey = hmac(dateKey, region);
        byte[] dateRegionServiceKey = hmac(dateRegionKey, S3_SERVICE);
        return hmac(dateRegionServiceKey, "aws4_request");
    }

    private byte[] hmac(byte[] key, String value) throws IOException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IOException("HMAC signing failed", exception);
        }
    }

    private String hex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
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

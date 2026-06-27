package com.growtharchive.service.upload;

import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.repository.ImageAssetRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.AuthCookieService;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.JwtService;
import com.growtharchive.security.MemberPrincipal;
import com.growtharchive.service.review.UploadedImageView;
import com.growtharchive.service.storage.OptimizedImage;
import com.growtharchive.service.storage.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageUploadService {

    private static final int MAX_REVIEW_IMAGES = 10;
    private static final long MAX_IMAGE_BYTES = 10L * 1024L * 1024L;
    private static final long MAX_TOTAL_IMAGE_BYTES = 50L * 1024L * 1024L;

    private final CurrentMemberResolver currentMemberResolver;
    private final AuthCookieService authCookieService;
    private final JwtService jwtService;
    private final StorageService storageService;
    private final ImageAssetRepository imageAssetRepository;

    public ImageUploadService(
        CurrentMemberResolver currentMemberResolver,
        AuthCookieService authCookieService,
        JwtService jwtService,
        StorageService storageService,
        ImageAssetRepository imageAssetRepository
    ) {
        this.currentMemberResolver = currentMemberResolver;
        this.authCookieService = authCookieService;
        this.jwtService = jwtService;
        this.storageService = storageService;
        this.imageAssetRepository = imageAssetRepository;
    }

    @Transactional
    public UploadedImageResponse uploadImage(HttpServletRequest request, MultipartFile file, String purpose) {
        String imageType = normalizePurpose(purpose);
        Long ownerMemberId = "PROFILE".equals(imageType)
            ? resolveProfileUploadOwner(request)
            : currentMemberResolver.require(request, AccessLevel.MEMBER).memberId();
        validateSingle(file);
        StoredUpload storedUpload = store(ownerMemberId, imageType, file);
        return new UploadedImageResponse(
            storedUpload.imageId(),
            storedUpload.publicUrl(),
            storedUpload.width(),
            storedUpload.height()
        );
    }

    @Transactional
    public List<UploadedImageView> uploadReviewImages(HttpServletRequest request, List<MultipartFile> files) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        validateReviewFiles(files);
        List<UploadedImageView> uploaded = new ArrayList<>();
        for (MultipartFile file : files) {
            StoredUpload storedUpload = store(member.memberId(), "MEETING_REVIEW", file);
            uploaded.add(new UploadedImageView(storedUpload.imageId(), storedUpload.publicUrl()));
        }
        return uploaded;
    }

    private StoredUpload store(Long memberId, String imageType, MultipartFile file) {
        try {
            var storedImage = storageService.storeImage(memberId, imageType, optimize(file));
            Long imageId = imageAssetRepository.create(memberId, imageType, storedImage);
            return new StoredUpload(imageId, storedImage.publicUrl(), storedImage.width(), storedImage.height());
        } catch (IOException exception) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "이미지 업로드에 실패했습니다.");
        }
    }

    private Long resolveProfileUploadOwner(HttpServletRequest request) {
        MemberPrincipal member = currentMemberResolver.resolveOptional(request);
        if (member != null) {
            currentMemberResolver.require(request, AccessLevel.INVITE_VERIFIED);
            return member.memberId();
        }
        String token = authCookieService.readCookie(request, AuthCookieService.SIGNUP_TOKEN_COOKIE);
        if (token == null || token.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        JwtService.SignupToken signupToken = jwtService.verifySignup(token);
        if (!signupToken.inviteVerified() || !signupToken.termsAgreed() || !signupToken.privacyAgreed()) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        return null;
    }

    private void validateSingle(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "업로드할 이미지를 선택해 주세요.");
        }
        validateImageFile(file);
    }

    private void validateReviewFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "업로드할 이미지를 선택해 주세요.");
        }
        if (files.size() > MAX_REVIEW_IMAGES) {
            throw new ApiException(ErrorCode.REVIEW_IMAGE_LIMIT_EXCEEDED);
        }
        long total = 0;
        for (MultipartFile file : files) {
            validateImageFile(file);
            total += file.getSize();
        }
        if (total > MAX_TOTAL_IMAGE_BYTES) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "후기 사진 전체 업로드는 50MB 이하를 권장합니다.");
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "빈 이미지는 업로드할 수 없습니다.");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "이미지는 10MB 이하만 업로드할 수 있습니다.");
        }
        String contentType = file.getContentType();
        String normalizedContentType = contentType == null ? null : contentType.toLowerCase();
        if (normalizedContentType == null || !List.of("image/jpeg", "image/jpg", "image/png", "image/webp").contains(normalizedContentType)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "jpg, png, webp 이미지만 업로드할 수 있습니다.");
        }
    }

    private OptimizedImage optimize(MultipartFile file) throws IOException {
        BufferedImage source = ImageIO.read(file.getInputStream());
        if (source == null) {
            if ("image/webp".equalsIgnoreCase(file.getContentType())) {
                byte[] bytes = file.getBytes();
                return new OptimizedImage(bytes, "image/webp", bytes.length, null, null);
            }
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "이미지를 처리할 수 없습니다.");
        }
        int maxDimension = 1600;
        double scale = Math.min(1.0, (double) maxDimension / Math.max(source.getWidth(), source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        Iterator<ImageWriter> webpWriters = ImageIO.getImageWritersByFormatName("webp");
        if (webpWriters.hasNext()) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (ImageIO.write(resized, "webp", output)) {
                byte[] bytes = output.toByteArray();
                return new OptimizedImage(bytes, "image/webp", bytes.length, width, height);
            }
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(resized, "jpg", output)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "이미지를 처리할 수 없습니다.");
        }
        byte[] bytes = output.toByteArray();
        return new OptimizedImage(bytes, "image/jpeg", bytes.length, width, height);
    }

    private String normalizePurpose(String purpose) {
        if (purpose == null || purpose.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "이미지 용도를 선택해 주세요.");
        }
        return switch (purpose.trim().toUpperCase()) {
            case "PROFILE" -> "PROFILE";
            case "READING_RECORD" -> "READING_RECORD";
            case "MEETING", "MEETING_COVER" -> "MEETING_COVER";
            case "REVIEW", "MEETING_REVIEW" -> "MEETING_REVIEW";
            case "BOOK" -> "BOOK";
            default -> throw new ApiException(ErrorCode.VALIDATION_ERROR, "지원하지 않는 이미지 용도입니다.");
        };
    }

    public record UploadedImageResponse(Long imageId, String url, Integer width, Integer height) {
    }

    private record StoredUpload(Long imageId, String publicUrl, Integer width, Integer height) {
    }
}

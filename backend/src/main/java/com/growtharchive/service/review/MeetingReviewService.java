package com.growtharchive.service.review;

import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.repository.MeetingReviewRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.AccessLevelCalculator;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import com.growtharchive.service.storage.OptimizedImage;
import com.growtharchive.service.storage.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MeetingReviewService {

    private static final int MAX_IMAGES = 10;
    private static final long MAX_IMAGE_BYTES = 10L * 1024L * 1024L;
    private static final long MAX_TOTAL_IMAGE_BYTES = 50L * 1024L * 1024L;

    private final CurrentMemberResolver currentMemberResolver;
    private final AccessLevelCalculator accessLevelCalculator;
    private final MeetingReviewRepository meetingReviewRepository;
    private final StorageService storageService;

    public MeetingReviewService(
        CurrentMemberResolver currentMemberResolver,
        AccessLevelCalculator accessLevelCalculator,
        MeetingReviewRepository meetingReviewRepository,
        StorageService storageService
    ) {
        this.currentMemberResolver = currentMemberResolver;
        this.accessLevelCalculator = accessLevelCalculator;
        this.meetingReviewRepository = meetingReviewRepository;
        this.storageService = storageService;
    }

    public List<MeetingReviewSummary> list(Long meetingId, int page, int size) {
        int boundedSize = Math.min(Math.max(size, 1), 50);
        return meetingReviewRepository.findPublic(meetingId, boundedSize, Math.max(page, 0) * boundedSize);
    }

    public List<MeetingReviewSummary> adminList(HttpServletRequest request, int page, int size) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        int boundedSize = Math.min(Math.max(size, 1), 100);
        return meetingReviewRepository.findAdmin(boundedSize, Math.max(page, 0) * boundedSize);
    }

    public MeetingReviewDetail adminDetail(HttpServletRequest request, Long reviewId) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        return meetingReviewRepository.findById(reviewId, false, null)
            .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));
    }

    public MeetingReviewDetail detail(HttpServletRequest request, Long reviewId) {
        MemberPrincipal viewer = currentMemberResolver.resolveOptional(request);
        if (viewer == null) {
            return meetingReviewRepository.findById(reviewId, true, null)
                .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));
        }
        MeetingReviewDetail detail = meetingReviewRepository.findById(reviewId, false, viewer.memberId())
            .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));
        boolean owner = detail.memberId().equals(viewer.memberId());
        boolean admin = accessLevelCalculator.hasAtLeast(accessLevelCalculator.calculate(viewer), AccessLevel.ADMIN);
        if (!"ACTIVE".equals(detail.status()) && !owner && !admin) {
            throw new ApiException(ErrorCode.REVIEW_NOT_FOUND);
        }
        return detail;
    }

    @Transactional
    public MeetingReviewDetail create(HttpServletRequest request, MeetingReviewCommand command) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        ValidatedReview validated = validate(member.memberId(), command);
        Long reviewId = meetingReviewRepository.create(
            member.memberId(),
            command.meetingId(),
            validated.title(),
            validated.content(),
            representativeImageId(validated.imageIds())
        );
        meetingReviewRepository.replaceImages(reviewId, validated.imageIds());
        meetingReviewRepository.insertActivityEvent(member.memberId(), reviewId, validated.title());
        return detail(request, reviewId);
    }

    @Transactional
    public MeetingReviewDetail update(HttpServletRequest request, Long reviewId, MeetingReviewCommand command) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        MeetingReviewDetail existing = meetingReviewRepository.findById(reviewId, false, member.memberId())
            .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));
        if (!existing.memberId().equals(member.memberId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "작성자만 모임 후기를 수정할 수 있습니다.");
        }
        ValidatedReview validated = validate(member.memberId(), command);
        meetingReviewRepository.update(reviewId, validated.title(), validated.content(), representativeImageId(validated.imageIds()));
        meetingReviewRepository.replaceImages(reviewId, validated.imageIds());
        return detail(request, reviewId);
    }

    @Transactional
    public void deleteByAuthor(HttpServletRequest request, Long reviewId) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        MeetingReviewDetail existing = meetingReviewRepository.findById(reviewId, false, member.memberId())
            .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));
        if (!existing.memberId().equals(member.memberId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "작성자만 모임 후기를 삭제할 수 있습니다.");
        }
        meetingReviewRepository.deleteByAuthor(reviewId, member.memberId());
    }

    @Transactional
    public void hide(HttpServletRequest request, Long reviewId) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        ensureExists(reviewId);
        meetingReviewRepository.hide(reviewId);
    }

    @Transactional
    public void restore(HttpServletRequest request, Long reviewId) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        ensureExists(reviewId);
        meetingReviewRepository.restore(reviewId);
    }

    @Transactional
    public void deleteByAdmin(HttpServletRequest request, Long reviewId) {
        currentMemberResolver.require(request, AccessLevel.ADMIN);
        ensureExists(reviewId);
        meetingReviewRepository.deleteByAdmin(reviewId);
    }

    @Transactional
    public List<UploadedImageView> uploadReviewImages(HttpServletRequest request, List<MultipartFile> files) {
        MemberPrincipal member = currentMemberResolver.require(request, AccessLevel.MEMBER);
        validateUpload(files);
        List<UploadedImageView> uploaded = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                var storedImage = storageService.storeReviewImage(member.memberId(), optimize(file));
                Long imageId = meetingReviewRepository.createImageAsset(member.memberId(), storedImage);
                String imageUrl = storedImage.publicUrl();
                uploaded.add(new UploadedImageView(imageId, imageUrl));
            } catch (IOException exception) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "이미지 업로드에 실패했습니다.");
            }
        }
        return uploaded;
    }

    private void validateUpload(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "업로드할 이미지를 선택해 주세요.");
        }
        if (files.size() > MAX_IMAGES) {
            throw new ApiException(ErrorCode.REVIEW_IMAGE_LIMIT_EXCEEDED);
        }
        long total = 0;
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "빈 이미지는 업로드할 수 없습니다.");
            }
            if (file.getSize() > MAX_IMAGE_BYTES) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "이미지는 10MB 이하만 업로드할 수 있습니다.");
            }
            total += file.getSize();
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "이미지 파일만 업로드할 수 있습니다.");
            }
        }
        if (total > MAX_TOTAL_IMAGE_BYTES) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "후기 사진 전체 업로드는 50MB 이하를 권장합니다.");
        }
    }

    private OptimizedImage optimize(MultipartFile file) throws IOException {
        BufferedImage source = ImageIO.read(file.getInputStream());
        if (source == null) {
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
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(resized, "jpg", output)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "이미지를 처리할 수 없습니다.");
        }
        byte[] bytes = output.toByteArray();
        return new OptimizedImage(bytes, "image/jpeg", bytes.length);
    }

    private ValidatedReview validate(Long memberId, MeetingReviewCommand command) {
        if (command.meetingId() == null || !meetingReviewRepository.meetingCanReceiveReview(command.meetingId())) {
            throw new ApiException(ErrorCode.MEETING_NOT_FOUND, "후기를 연결할 수 있는 모임을 선택해 주세요.");
        }
        String title = required(command.title(), "후기 제목을 입력해 주세요.");
        if (title.length() > 100) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "후기 제목은 100자 이하로 입력해 주세요.");
        }
        String content = required(command.content(), "후기 내용을 입력해 주세요.");
        if (content.length() > 5000) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "후기 내용은 5000자 이하로 입력해 주세요.");
        }
        List<Long> imageIds = command.imageIds() == null ? List.of() : command.imageIds();
        if (imageIds.size() > MAX_IMAGES) {
            throw new ApiException(ErrorCode.REVIEW_IMAGE_LIMIT_EXCEEDED);
        }
        if (meetingReviewRepository.countOwnedReviewImages(memberId, imageIds) != imageIds.size()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "사용할 수 없는 후기 이미지가 포함되어 있습니다.");
        }
        return new ValidatedReview(title, content, imageIds);
    }

    private Long representativeImageId(List<Long> imageIds) {
        return imageIds.isEmpty() ? null : imageIds.getFirst();
    }

    private void ensureExists(Long reviewId) {
        meetingReviewRepository.findById(reviewId, false, null).orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, message);
        }
        return value.trim();
    }

    private record ValidatedReview(String title, String content, List<Long> imageIds) {
    }
}

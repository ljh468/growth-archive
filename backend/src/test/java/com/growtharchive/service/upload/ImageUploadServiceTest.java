package com.growtharchive.service.upload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.growtharchive.repository.ImageAssetRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.AuthCookieService;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.JwtService;
import com.growtharchive.security.MemberPrincipal;
import com.growtharchive.service.storage.OptimizedImage;
import com.growtharchive.service.storage.StorageService;
import com.growtharchive.service.storage.StoredImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ImageUploadServiceTest {

    @Test
    void optimizesProfileImageToSmallProfileSize() throws Exception {
        AtomicReference<OptimizedImage> stored = new AtomicReference<>();
        ImageUploadService service = service(stored);
        MockMultipartFile file = jpegFile();

        service.uploadImage(null, file, "PROFILE");

        assertThat(stored.get().width()).isLessThanOrEqualTo(512);
        assertThat(stored.get().height()).isLessThanOrEqualTo(512);
        assertThat(stored.get().sizeBytes()).isLessThanOrEqualTo(950L * 1024L);
    }

    @Test
    void optimizesReviewImageToReviewSize() throws Exception {
        AtomicReference<OptimizedImage> stored = new AtomicReference<>();
        ImageUploadService service = service(stored);
        MockMultipartFile file = jpegFile();

        service.uploadImage(null, file, "REVIEW");

        assertThat(Math.max(stored.get().width(), stored.get().height())).isLessThanOrEqualTo(1400);
        assertThat(stored.get().sizeBytes()).isLessThanOrEqualTo(950L * 1024L);
    }

    @Test
    void optimizesPngBookCoverToUploadableBookSize() throws Exception {
        AtomicReference<OptimizedImage> stored = new AtomicReference<>();
        ImageUploadService service = service(stored);
        MockMultipartFile file = pngFile();

        service.uploadImage(null, file, "BOOK");

        assertThat(Math.max(stored.get().width(), stored.get().height())).isLessThanOrEqualTo(900);
        assertThat(stored.get().mimeType()).isIn("image/webp", "image/jpeg");
        assertThat(stored.get().sizeBytes()).isLessThanOrEqualTo(950L * 1024L);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("supportedBookCoverFiles")
    void uploadsSupportedBookCoverImageExtensions(String name, MockMultipartFile file) throws Exception {
        AtomicReference<OptimizedImage> stored = new AtomicReference<>();
        ImageUploadService service = service(stored);

        service.uploadImage(null, file, "BOOK");

        assertThat(stored.get()).isNotNull();
        assertThat(stored.get().mimeType()).isIn("image/webp", "image/jpeg");
        assertThat(stored.get().sizeBytes()).isLessThanOrEqualTo(950L * 1024L);
    }

    private static Stream<Arguments> supportedBookCoverFiles() throws Exception {
        return Stream.of(
            Arguments.of("jpg extension with image/jpeg", imageFile("book-cover.jpg", "image/jpeg", "jpg", BufferedImage.TYPE_INT_RGB)),
            Arguments.of("jpeg extension with image/jpeg", imageFile("book-cover.jpeg", "image/jpeg", "jpeg", BufferedImage.TYPE_INT_RGB)),
            Arguments.of("png extension with image/png", imageFile("book-cover.png", "image/png", "png", BufferedImage.TYPE_INT_ARGB)),
            Arguments.of("webp extension with image/webp", webpFile())
        );
    }

    private ImageUploadService service(AtomicReference<OptimizedImage> stored) throws Exception {
        CurrentMemberResolver resolver = mock(CurrentMemberResolver.class);
        when(resolver.require(any(), eq(AccessLevel.MEMBER))).thenReturn(member());
        when(resolver.require(any(), eq(AccessLevel.INVITE_VERIFIED))).thenReturn(member());
        when(resolver.resolveOptional(any())).thenReturn(member());
        StorageService storageService = mock(StorageService.class);
        when(storageService.storeImage(eq(1L), any(), any())).thenAnswer(invocation -> {
            OptimizedImage image = invocation.getArgument(2);
            stored.set(image);
            return new StoredImage("images", "key", "/image.webp", image.mimeType(), image.sizeBytes(), image.width(), image.height());
        });
        ImageAssetRepository imageAssetRepository = mock(ImageAssetRepository.class);
        when(imageAssetRepository.create(eq(1L), any(), any())).thenReturn(10L);
        return new ImageUploadService(
            resolver,
            mock(AuthCookieService.class),
            mock(JwtService.class),
            storageService,
            imageAssetRepository
        );
    }

    private MockMultipartFile jpegFile() throws Exception {
        BufferedImage image = new BufferedImage(3200, 2400, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(246, 241, 232));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.setColor(new Color(31, 77, 58));
            for (int index = 0; index < 120; index += 1) {
                graphics.fillRect(index * 27 % image.getWidth(), index * 19 % image.getHeight(), 180, 120);
            }
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        return new MockMultipartFile("file", "meeting.jpg", "image/jpeg", output.toByteArray());
    }

    private MockMultipartFile pngFile() throws Exception {
        return imageFile("book-cover.png", "image/png", "png", BufferedImage.TYPE_INT_ARGB);
    }

    private static MockMultipartFile imageFile(String fileName, String contentType, String formatName, int imageType) throws Exception {
        BufferedImage image = testImage(imageType);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        boolean written = ImageIO.write(image, formatName, output);
        assertThat(written).as("ImageIO writer for %s", formatName).isTrue();
        return new MockMultipartFile("file", fileName, contentType, output.toByteArray());
    }

    private static MockMultipartFile webpFile() {
        byte[] bytes = Base64.getDecoder().decode("UklGRiIAAABXRUJQVlA4IBYAAAAwAQCdASoBAAEAAUAmJaQAA3AA/vuUAAA=");
        return new MockMultipartFile("file", "book-cover.webp", "image/webp", bytes);
    }

    private static BufferedImage testImage(int imageType) {
        BufferedImage image = new BufferedImage(1800, 2600, BufferedImage.TYPE_INT_ARGB);
        if (imageType != BufferedImage.TYPE_INT_ARGB) {
            image = new BufferedImage(1800, 2600, imageType);
        }
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(255, 253, 248, 255));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.setColor(new Color(31, 77, 58, 220));
            graphics.fillRoundRect(160, 180, 1480, 2240, 90, 90);
            graphics.setColor(new Color(246, 241, 232, 240));
            for (int index = 0; index < 18; index += 1) {
                graphics.fillRect(300, 420 + index * 90, 1200, 26);
            }
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private MemberPrincipal member() {
        OffsetDateTime now = OffsetDateTime.now();
        return new MemberPrincipal(1L, "MEMBER", "NICKNAME", null, "member", null, now, now, now, now, null);
    }
}

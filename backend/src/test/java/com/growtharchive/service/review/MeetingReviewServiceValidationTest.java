package com.growtharchive.service.review;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.growtharchive.exception.ApiException;
import com.growtharchive.repository.ImageAssetRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.AuthCookieService;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.JwtService;
import com.growtharchive.security.MemberPrincipal;
import com.growtharchive.service.storage.StorageService;
import com.growtharchive.service.upload.ImageUploadService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

class MeetingReviewServiceValidationTest {

    @Test
    void rejectsMoreThanTenReviewImages() {
        CurrentMemberResolver resolver = Mockito.mock(CurrentMemberResolver.class);
        Mockito.when(resolver.require(Mockito.isNull(), Mockito.eq(AccessLevel.MEMBER))).thenReturn(member());
        ImageUploadService service = new ImageUploadService(
            resolver,
            Mockito.mock(AuthCookieService.class),
            Mockito.mock(JwtService.class),
            Mockito.mock(StorageService.class),
            Mockito.mock(ImageAssetRepository.class)
        );
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.isEmpty()).thenReturn(false);
        Mockito.when(file.getSize()).thenReturn(1L);
        Mockito.when(file.getContentType()).thenReturn("image/png");
        List<MultipartFile> files = List.of(file, file, file, file, file, file, file, file, file, file, file);

        assertThatThrownBy(() -> service.uploadReviewImages(null, files))
            .isInstanceOf(ApiException.class);
    }

    private MemberPrincipal member() {
        OffsetDateTime now = OffsetDateTime.now();
        return new MemberPrincipal(1L, "MEMBER", "NICKNAME", null, "member", null, now, now, now, now, null);
    }
}

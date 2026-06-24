package com.growtharchive.service.reading;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.growtharchive.exception.ApiException;
import com.growtharchive.repository.ReadingRecordRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ReadingRecordServiceTest {

    private final CurrentMemberResolver resolver = Mockito.mock(CurrentMemberResolver.class);
    private final ReadingRecordRepository repository = Mockito.mock(ReadingRecordRepository.class);
    private final ReadingRecordService service = new ReadingRecordService(resolver, repository);

    @Test
    void rejectsRatingOutsideOneToFive() {
        Mockito.when(resolver.require(Mockito.isNull(), Mockito.eq(AccessLevel.MEMBER))).thenReturn(member());

        ReadingRecordService.ReadingRecordCommand command = new ReadingRecordService.ReadingRecordCommand(
            1L,
            6,
            "좋은 책",
            "https://example.com/post",
            null
        );

        assertThatThrownBy(() -> service.create(null, command))
            .isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsInvalidBlogUrl() {
        Mockito.when(resolver.require(Mockito.isNull(), Mockito.eq(AccessLevel.MEMBER))).thenReturn(member());

        ReadingRecordService.ReadingRecordCommand command = new ReadingRecordService.ReadingRecordCommand(
            1L,
            null,
            "좋은 책",
            "not-a-url",
            null
        );

        assertThatThrownBy(() -> service.create(null, command))
            .isInstanceOf(ApiException.class);
    }

    private MemberPrincipal member() {
        OffsetDateTime now = OffsetDateTime.now();
        return new MemberPrincipal(1L, "MEMBER", "NICKNAME", null, "member", null, now, now, now, now, null);
    }
}

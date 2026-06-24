package com.growtharchive.service.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.repository.MeetingRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.AccessLevelCalculator;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MeetingServiceTest {

    @Test
    void guestDetailHidesExactLocationHostNameAndAttendees() {
        CurrentMemberResolver resolver = Mockito.mock(CurrentMemberResolver.class);
        MeetingRepository repository = Mockito.mock(MeetingRepository.class);
        MeetingService service = new MeetingService(resolver, new AccessLevelCalculator(), repository);
        Mockito.when(resolver.resolveOptional(Mockito.isNull())).thenReturn(null);
        Mockito.when(repository.findById(1L, true, null)).thenReturn(Optional.of(meeting(1L, "SCHEDULED", 20)));

        MeetingDetail detail = service.detail(null, 1L);

        assertThat(detail.exactLocation()).isNull();
        assertThat(detail.hostDisplayName()).isNull();
        assertThat(detail.attendees()).isEmpty();
        assertThat(detail.attendedByMe()).isFalse();
    }

    @Test
    void joinRejectsFullMeetingWhenMemberIsNotAlreadyJoined() {
        CurrentMemberResolver resolver = Mockito.mock(CurrentMemberResolver.class);
        MeetingRepository repository = Mockito.mock(MeetingRepository.class);
        MeetingService service = new MeetingService(resolver, new AccessLevelCalculator(), repository);
        Mockito.when(resolver.require(Mockito.isNull(), Mockito.eq(AccessLevel.MEMBER))).thenReturn(member());
        Mockito.when(repository.findEditableById(1L)).thenReturn(Optional.of(meeting(1L, "SCHEDULED", 1)));
        Mockito.when(repository.isJoined(1L, 2L)).thenReturn(false);
        Mockito.when(repository.joinedCount(1L)).thenReturn(1);

        assertThatThrownBy(() -> service.join(null, 1L))
            .isInstanceOf(ApiException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.MEETING_CAPACITY_FULL);
    }

    private MeetingDetail meeting(Long id, String status, Integer capacity) {
        return new MeetingDetail(
            id,
            "SMALL",
            "소소모임",
            "설명",
            OffsetDateTime.now().plusDays(1),
            "서울",
            "서울시 비공개 장소",
            capacity,
            0,
            null,
            2L,
            "host",
            status,
            null,
            false,
            1,
            false,
            true,
            List.of("https://example.com/a.jpg"),
            List.of(new MeetingAttendee(2L, "host", null, "/people/2"))
        );
    }

    private MemberPrincipal member() {
        OffsetDateTime now = OffsetDateTime.now();
        return new MemberPrincipal(2L, "MEMBER", "NICKNAME", null, "member", null, now, now, now, now, null);
    }
}

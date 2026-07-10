package com.growtharchive.service.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.growtharchive.exception.ApiException;
import com.growtharchive.exception.ErrorCode;
import com.growtharchive.repository.ImageAssetRepository;
import com.growtharchive.repository.MeetingRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.AccessLevelCalculator;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MeetingServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    void guestDetailHidesExactLocationHostNameAndAttendees() {
        CurrentMemberResolver resolver = Mockito.mock(CurrentMemberResolver.class);
        MeetingRepository repository = Mockito.mock(MeetingRepository.class);
        MeetingService service = new MeetingService(resolver, new AccessLevelCalculator(), repository, Mockito.mock(ImageAssetRepository.class));
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
        MeetingService service = new MeetingService(resolver, new AccessLevelCalculator(), repository, Mockito.mock(ImageAssetRepository.class));
        Mockito.when(resolver.require(Mockito.isNull(), Mockito.eq(AccessLevel.MEMBER))).thenReturn(member());
        Mockito.when(repository.findEditableById(1L)).thenReturn(Optional.of(meeting(1L, "SCHEDULED", 1)));
        Mockito.when(repository.isJoined(1L, 2L)).thenReturn(false);
        Mockito.when(repository.joinedCount(1L)).thenReturn(1);

        assertThatThrownBy(() -> service.join(null, 1L))
            .isInstanceOf(ApiException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.MEETING_CAPACITY_FULL);
    }

    @Test
    void joinRejectsPastScheduledMeeting() {
        CurrentMemberResolver resolver = Mockito.mock(CurrentMemberResolver.class);
        MeetingRepository repository = Mockito.mock(MeetingRepository.class);
        MeetingService service = new MeetingService(resolver, new AccessLevelCalculator(), repository, Mockito.mock(ImageAssetRepository.class));
        Mockito.when(resolver.require(Mockito.isNull(), Mockito.eq(AccessLevel.MEMBER))).thenReturn(member());
        Mockito.when(repository.findEditableById(1L)).thenReturn(Optional.of(meeting(1L, "SCHEDULED", 20, OffsetDateTime.now().minusMinutes(1))));

        assertThatThrownBy(() -> service.join(null, 1L))
            .isInstanceOf(ApiException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void listDefaultsToCurrentMonthInKst() {
        CurrentMemberResolver resolver = Mockito.mock(CurrentMemberResolver.class);
        MeetingRepository repository = Mockito.mock(MeetingRepository.class);
        MeetingService service = new MeetingService(resolver, new AccessLevelCalculator(), repository, Mockito.mock(ImageAssetRepository.class));

        service.list(null, "current", 0, 0, 30);

        LocalDate currentMonthStart = LocalDate.now(KST).withDayOfMonth(1);
        Mockito.verify(repository).findPublicInMonth(
            Mockito.isNull(),
            Mockito.eq(currentMonthStart.atStartOfDay(KST).toOffsetDateTime()),
            Mockito.eq(currentMonthStart.plusMonths(1).atStartOfDay(KST).toOffsetDateTime()),
            Mockito.eq(false),
            Mockito.eq(30),
            Mockito.eq(0)
        );
    }

    @Test
    void adminCannotRewriteSmallMeetingContent() {
        CurrentMemberResolver resolver = Mockito.mock(CurrentMemberResolver.class);
        MeetingRepository repository = Mockito.mock(MeetingRepository.class);
        MeetingService service = new MeetingService(resolver, new AccessLevelCalculator(), repository, Mockito.mock(ImageAssetRepository.class));
        Mockito.when(resolver.require(Mockito.isNull(), Mockito.eq(AccessLevel.ADMIN))).thenReturn(admin());
        Mockito.when(repository.findEditableById(1L)).thenReturn(Optional.of(meeting(1L, "SCHEDULED", 20)));
        MeetingCommand command = new MeetingCommand(
            "운영진 수정",
            "내용 수정",
            OffsetDateTime.now().plusDays(1),
            "서울",
            "비공개 장소",
            10,
            null,
            0,
            "SCHEDULED"
        );

        assertThatThrownBy(() -> service.updateRegular(null, 1L, command))
            .isInstanceOf(ApiException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FORBIDDEN);
        Mockito.verify(repository, Mockito.never()).updateRegular(Mockito.anyLong(), Mockito.any());
    }

    @Test
    void createSmallMeetingStoresCurrentMemberAsHostAndAttendee() {
        CurrentMemberResolver resolver = Mockito.mock(CurrentMemberResolver.class);
        MeetingRepository repository = Mockito.mock(MeetingRepository.class);
        ImageAssetRepository imageAssetRepository = Mockito.mock(ImageAssetRepository.class);
        MeetingService service = new MeetingService(resolver, new AccessLevelCalculator(), repository, imageAssetRepository);
        MeetingCommand command = new MeetingCommand(
            "퇴근 후 독서 모임",
            "짧게 읽고 기록을 나눕니다.",
            OffsetDateTime.now().plusDays(3),
            "하남 미사",
            "비공개 장소",
            8,
            null,
            0,
            null
        );
        Mockito.when(resolver.require(Mockito.isNull(), Mockito.eq(AccessLevel.MEMBER))).thenReturn(member());
        Mockito.when(imageAssetRepository.isOwnedImage(2L, null, "MEETING_COVER")).thenReturn(true);
        Mockito.when(repository.createSmall(Mockito.eq(2L), Mockito.any(MeetingCommand.class))).thenReturn(10L);
        Mockito.when(repository.findById(10L, false, 2L)).thenReturn(Optional.of(meeting(10L, "SCHEDULED", 8)));
        Mockito.when(resolver.resolveOptional(Mockito.isNull())).thenReturn(member());

        service.createSmall(null, command);

        Mockito.verify(repository).createSmall(Mockito.eq(2L), Mockito.any(MeetingCommand.class));
        Mockito.verify(repository).join(10L, 2L);
    }

    private MeetingDetail meeting(Long id, String status, Integer capacity) {
        return meeting(id, status, capacity, OffsetDateTime.now().plusDays(1));
    }

    private MeetingDetail meeting(Long id, String status, Integer capacity, OffsetDateTime meetingAt) {
        return new MeetingDetail(
            id,
            "SMALL",
            "소소모임",
            "설명",
            meetingAt,
            "서울",
            "서울시 비공개 장소",
            capacity,
            0,
            null,
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

    private MemberPrincipal admin() {
        OffsetDateTime now = OffsetDateTime.now();
        return new MemberPrincipal(1L, "ADMIN", "REAL_NAME", "운영진", "admin", null, now, now, now, now, null);
    }
}

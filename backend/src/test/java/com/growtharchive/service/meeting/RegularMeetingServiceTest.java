package com.growtharchive.service.meeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.growtharchive.repository.MeetingRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RegularMeetingServiceTest {

    @Test
    void regularMeetingDefaultsUseSecondAndFourthSundayAtTenKst() {
        RegularMeetingService service = new RegularMeetingService(Mockito.mock(MeetingRepository.class));

        assertThat(service.meetingAt(YearMonth.of(2026, 7), 2).toString()).isEqualTo("2026-07-12T10:00+09:00");
        assertThat(service.meetingAt(YearMonth.of(2026, 7), 4).toString()).isEqualTo("2026-07-26T10:00+09:00");
    }

    @Test
    void ensureRegularMeetingsUsesRepositoryIdempotentCreates() {
        MeetingRepository repository = Mockito.mock(MeetingRepository.class);
        when(repository.createRegularIfMissing(
            Mockito.eq("REGULAR_READING"),
            Mockito.eq("7월 독서기록모임"),
            Mockito.eq(OffsetDateTime.parse("2026-07-12T10:00:00+09:00")),
            Mockito.eq("하남 미사"),
            Mockito.eq("투썸플레이스 미사호수공원점"),
            Mockito.eq(10),
            Mockito.eq(0),
            Mockito.any(),
            Mockito.eq(LocalDate.of(2026, 7, 1))
        ))
            .thenReturn(true);
        when(repository.createRegularIfMissing(
            Mockito.eq("REGULAR_ACTION"),
            Mockito.eq("7월 실행수다모임"),
            Mockito.eq(OffsetDateTime.parse("2026-07-26T10:00:00+09:00")),
            Mockito.eq("하남 미사"),
            Mockito.eq("투썸플레이스 미사호수공원점"),
            Mockito.eq(10),
            Mockito.eq(0),
            Mockito.any(),
            Mockito.eq(LocalDate.of(2026, 7, 1))
        ))
            .thenReturn(false);

        int created = new RegularMeetingService(repository).ensureRegularMeetingsForMonth(YearMonth.of(2026, 7));

        assertThat(created).isEqualTo(1);
        verify(repository).createRegularIfMissing(
            Mockito.eq("REGULAR_READING"),
            Mockito.eq("7월 독서기록모임"),
            Mockito.eq(OffsetDateTime.parse("2026-07-12T10:00:00+09:00")),
            Mockito.eq("하남 미사"),
            Mockito.eq("투썸플레이스 미사호수공원점"),
            Mockito.eq(10),
            Mockito.eq(0),
            Mockito.any(),
            Mockito.eq(LocalDate.of(2026, 7, 1))
        );
        verify(repository).createRegularIfMissing(
            Mockito.eq("REGULAR_ACTION"),
            Mockito.eq("7월 실행수다모임"),
            Mockito.eq(OffsetDateTime.parse("2026-07-26T10:00:00+09:00")),
            Mockito.eq("하남 미사"),
            Mockito.eq("투썸플레이스 미사호수공원점"),
            Mockito.eq(10),
            Mockito.eq(0),
            Mockito.any(),
            Mockito.eq(LocalDate.of(2026, 7, 1))
        );
    }
}

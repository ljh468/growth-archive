package com.growtharchive.service.meeting;

import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RegularMeetingSchedulerTest {

    @Test
    void startupEnsuresCurrentMonthRegularMeetings() {
        RegularMeetingService service = Mockito.mock(RegularMeetingService.class);
        Clock clock = Clock.fixed(Instant.parse("2026-07-02T03:00:00Z"), ZoneId.of("Asia/Seoul"));

        new RegularMeetingScheduler(service, clock).createCurrentMonthRegularMeetingsOnStartup();

        verify(service).ensureRegularMeetingsForMonth(YearMonth.of(2026, 7));
    }
}

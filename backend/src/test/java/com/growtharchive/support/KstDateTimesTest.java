package com.growtharchive.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class KstDateTimesTest {

    @Test
    void monthBoundariesUseKstOffset() {
        LocalDate july = LocalDate.of(2026, 7, 15);

        OffsetDateTime start = KstDateTimes.startOfMonth(july);
        OffsetDateTime next = KstDateTimes.startOfNextMonth(july);

        assertThat(start).isEqualTo(OffsetDateTime.parse("2026-07-01T00:00:00+09:00"));
        assertThat(next).isEqualTo(OffsetDateTime.parse("2026-08-01T00:00:00+09:00"));
    }

    @Test
    void monthOfConvertsInstantToKstMonth() {
        OffsetDateTime utcTime = OffsetDateTime.parse("2026-06-30T15:30:00Z");

        assertThat(KstDateTimes.monthOf(utcTime)).isEqualTo(LocalDate.of(2026, 7, 1));
    }
}

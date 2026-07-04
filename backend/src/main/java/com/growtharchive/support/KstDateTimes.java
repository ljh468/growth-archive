package com.growtharchive.support;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.YearMonth;

public final class KstDateTimes {

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private KstDateTimes() {
    }

    public static LocalDate today() {
        return LocalDate.now(KST);
    }

    public static LocalDate currentMonth() {
        return today().withDayOfMonth(1);
    }

    public static YearMonth currentYearMonth() {
        return YearMonth.now(KST);
    }

    public static LocalDate monthOf(OffsetDateTime dateTime) {
        return dateTime.atZoneSameInstant(KST).toLocalDate().withDayOfMonth(1);
    }

    public static OffsetDateTime startOfMonth(LocalDate month) {
        return month.withDayOfMonth(1).atStartOfDay(KST).toOffsetDateTime();
    }

    public static OffsetDateTime startOfNextMonth(LocalDate month) {
        return month.withDayOfMonth(1).plusMonths(1).atStartOfDay(KST).toOffsetDateTime();
    }
}

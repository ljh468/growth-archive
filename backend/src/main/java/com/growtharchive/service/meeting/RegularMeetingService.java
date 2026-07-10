package com.growtharchive.service.meeting;

import com.growtharchive.repository.MeetingRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import org.springframework.stereotype.Service;

@Service
public class RegularMeetingService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final String DEFAULT_REGION = "하남 미사";
    private static final String DEFAULT_EXACT_LOCATION = "투썸플레이스 미사호수공원점";
    private static final int DEFAULT_CAPACITY = 10;
    private static final int DEFAULT_FEE_AMOUNT = 0;

    private final MeetingRepository meetingRepository;

    public RegularMeetingService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    public int ensureRegularMeetingsForMonth(YearMonth month) {
        LocalDate targetMonth = month.atDay(1);
        int created = 0;
        if (meetingRepository.createRegularIfMissing(
            "REGULAR_READING",
            month.getMonthValue() + "월 독서기록모임",
            meetingAt(month, 2),
            DEFAULT_REGION,
            DEFAULT_EXACT_LOCATION,
            DEFAULT_CAPACITY,
            DEFAULT_FEE_AMOUNT,
            "이번 달 독서기록을 함께 나누는 정기 모임입니다.",
            targetMonth
        )) {
            created++;
        }
        if (meetingRepository.createRegularIfMissing(
            "REGULAR_ACTION",
            month.getMonthValue() + "월 실행수다모임",
            meetingAt(month, 4),
            DEFAULT_REGION,
            DEFAULT_EXACT_LOCATION,
            DEFAULT_CAPACITY,
            DEFAULT_FEE_AMOUNT,
            "월초에 정한 실행목표를 함께 이야기하고 편하게 수다 나누는 정기 모임입니다.",
            targetMonth
        )) {
            created++;
        }
        return created;
    }

    OffsetDateTime meetingAt(YearMonth month, int sundayOrdinal) {
        LocalDate date = month.atDay(1)
            .with(TemporalAdjusters.dayOfWeekInMonth(sundayOrdinal, DayOfWeek.SUNDAY));
        return date.atTime(LocalTime.of(10, 0)).atZone(KST).toOffsetDateTime();
    }
}

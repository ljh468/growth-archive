package com.growtharchive.service.meeting;

import java.time.YearMonth;
import java.time.ZoneId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RegularMeetingScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final RegularMeetingService regularMeetingService;

    public RegularMeetingScheduler(RegularMeetingService regularMeetingService) {
        this.regularMeetingService = regularMeetingService;
    }

    @Scheduled(cron = "0 10 0 1 * *", zone = "Asia/Seoul")
    public void createMonthlyRegularMeetings() {
        regularMeetingService.ensureRegularMeetingsForMonth(YearMonth.now(KST));
    }
}

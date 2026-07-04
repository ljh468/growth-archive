package com.growtharchive.service.meeting;

import java.time.Clock;
import java.time.YearMonth;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RegularMeetingScheduler {

    private static final Logger log = LoggerFactory.getLogger(RegularMeetingScheduler.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final RegularMeetingService regularMeetingService;
    private final Clock clock;

    @Autowired
    public RegularMeetingScheduler(RegularMeetingService regularMeetingService) {
        this(regularMeetingService, Clock.system(KST));
    }

    RegularMeetingScheduler(RegularMeetingService regularMeetingService, Clock clock) {
        this.regularMeetingService = regularMeetingService;
        this.clock = clock;
    }

    @Scheduled(cron = "0 10 0 1 * *", zone = "Asia/Seoul")
    public void createMonthlyRegularMeetings() {
        ensureRegularMeetings("scheduled");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createCurrentMonthRegularMeetingsOnStartup() {
        ensureRegularMeetings("startup");
    }

    private void ensureRegularMeetings(String trigger) {
        YearMonth month = YearMonth.now(clock);
        int created = regularMeetingService.ensureRegularMeetingsForMonth(month);
        log.info("Regular meeting ensure completed: trigger={}, month={}, created={}", trigger, month, created);
    }
}

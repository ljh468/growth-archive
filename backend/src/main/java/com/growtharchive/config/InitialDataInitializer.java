package com.growtharchive.config;

import com.growtharchive.config.properties.AppProperties;
import com.growtharchive.repository.InviteCodeRepository;
import com.growtharchive.service.auth.CodeHashService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitialDataInitializer implements ApplicationRunner {

    private final AppProperties properties;
    private final InviteCodeRepository inviteCodeRepository;
    private final CodeHashService codeHashService;

    public InitialDataInitializer(
        AppProperties properties,
        InviteCodeRepository inviteCodeRepository,
        CodeHashService codeHashService
    ) {
        this.properties = properties;
        this.inviteCodeRepository = inviteCodeRepository;
        this.codeHashService = codeHashService;
    }

    @Override
    public void run(ApplicationArguments args) {
        String initialCode = properties.getInvite().getInitialCode();
        if (initialCode == null || initialCode.isBlank() || inviteCodeRepository.findActiveHash().isPresent()) {
            return;
        }
        inviteCodeRepository.replaceActiveCode(
            codeHashService.hashInviteCode(initialCode),
            codeHashService.preview(initialCode),
            null
        );
    }
}

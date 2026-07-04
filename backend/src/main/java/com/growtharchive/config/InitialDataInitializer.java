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
        seedInitialCode("MEMBER", properties.getInvite().getInitialCode());
        seedInitialCode("ADMIN", properties.getInvite().getAdminInitialCode());
    }

    private void seedInitialCode(String role, String code) {
        if (code == null || code.isBlank() || inviteCodeRepository.findActiveHash(role).isPresent()) {
            return;
        }
        inviteCodeRepository.replaceActiveCode(role, codeHashService.hashInviteCode(code), codeHashService.preview(code), null);
    }
}

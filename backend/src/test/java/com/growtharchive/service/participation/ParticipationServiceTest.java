package com.growtharchive.service.participation;

import static org.assertj.core.api.Assertions.assertThat;

import com.growtharchive.repository.ParticipationRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ParticipationServiceTest {

    @Test
    void adminStatusCountsOnlyCalculationTargetsAndCoffeeSupportTargets() {
        CurrentMemberResolver resolver = Mockito.mock(CurrentMemberResolver.class);
        ParticipationRepository repository = Mockito.mock(ParticipationRepository.class);
        ParticipationService service = new ParticipationService(resolver, repository);
        Mockito.when(resolver.require(Mockito.isNull(), Mockito.eq(AccessLevel.ADMIN))).thenReturn(admin());
        Mockito.when(repository.getAdminMembers(LocalDate.of(2026, 7, 1))).thenReturn(List.of(
            member(1L, true, true, false),
            member(2L, true, false, true),
            member(3L, false, false, false)
        ));

        AdminParticipationSummary summary = service.getAdminStatus(null, LocalDate.of(2026, 7, 15));

        assertThat(summary.totalTargetMemberCount()).isEqualTo(2);
        assertThat(summary.completedCount()).isEqualTo(1);
        assertThat(summary.incompleteCount()).isEqualTo(1);
    }

    private AdminParticipationMemberView member(Long id, boolean target, boolean completed, boolean coffeeSupport) {
        return new AdminParticipationMemberView(
            id,
            "member" + id,
            "member" + id,
            null,
            completed ? 1L : 0L,
            false,
            target,
            completed,
            false,
            coffeeSupport,
            ParticipationRepository.COFFEE_SUPPORT_ITEM,
            null
        );
    }

    private MemberPrincipal admin() {
        OffsetDateTime now = OffsetDateTime.now();
        return new MemberPrincipal(1L, "ADMIN", "NICKNAME", null, "admin", null, now, now, now, now, null);
    }
}

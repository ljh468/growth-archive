package com.growtharchive.repository;

import com.growtharchive.domain.member.OauthAccount;
import com.growtharchive.domain.member.QOauthAccount;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class OauthAccountRepository {

    private static final QOauthAccount oauthAccount = QOauthAccount.oauthAccount;

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public OauthAccountRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
    }

    public Optional<Long> findMemberId(String provider, String providerUserId) {
        return Optional.ofNullable(queryFactory
            .select(oauthAccount.memberId)
            .from(oauthAccount)
            .where(oauthAccount.provider.eq(provider), oauthAccount.providerUserId.eq(providerUserId))
            .fetchOne());
    }

    public Optional<String> findProviderUserId(String provider, Long memberId) {
        return Optional.ofNullable(queryFactory
            .select(oauthAccount.providerUserId)
            .from(oauthAccount)
            .where(oauthAccount.provider.eq(provider), oauthAccount.memberId.eq(memberId))
            .limit(1)
            .fetchOne());
    }

    public void upsert(String provider, String providerUserId, Long memberId, String email, String nickname, String profileImageUrl) {
        Long id = queryFactory
            .select(oauthAccount.id)
            .from(oauthAccount)
            .where(oauthAccount.provider.eq(provider), oauthAccount.providerUserId.eq(providerUserId))
            .fetchOne();
        OffsetDateTime now = OffsetDateTime.now();
        if (id == null) {
            entityManager.persist(new OauthAccount(provider, providerUserId, memberId, email, nickname, profileImageUrl, now));
            return;
        }
        queryFactory
            .update(oauthAccount)
            .set(oauthAccount.memberId, memberId)
            .set(oauthAccount.email, email)
            .set(oauthAccount.profileNickname, nickname)
            .set(oauthAccount.profileImageUrl, profileImageUrl)
            .set(oauthAccount.lastLoginAt, now)
            .where(oauthAccount.id.eq(id))
            .execute();
    }

    public void redactProfile(String provider, Long memberId) {
        queryFactory
            .update(oauthAccount)
            .setNull(oauthAccount.email)
            .setNull(oauthAccount.profileNickname)
            .setNull(oauthAccount.profileImageUrl)
            .where(oauthAccount.provider.eq(provider), oauthAccount.memberId.eq(memberId))
            .execute();
    }
}

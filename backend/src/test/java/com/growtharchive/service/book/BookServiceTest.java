package com.growtharchive.service.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.growtharchive.repository.AdminAuditLogRepository;
import com.growtharchive.repository.BookRepository;
import com.growtharchive.repository.ReadingRecordRepository;
import com.growtharchive.repository.RecommendedBookRepository;
import com.growtharchive.security.AccessLevel;
import com.growtharchive.security.CurrentMemberResolver;
import com.growtharchive.security.MemberPrincipal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BookServiceTest {

    @Test
    void manualBookCreationReturnsUnverifiedBook() {
        CurrentMemberResolver resolver = Mockito.mock(CurrentMemberResolver.class);
        BookRepository bookRepository = Mockito.mock(BookRepository.class);
        BookService service = new BookService(
            resolver,
            Mockito.mock(BookSearchProvider.class),
            bookRepository,
            Mockito.mock(ReadingRecordRepository.class),
            Mockito.mock(RecommendedBookRepository.class),
            Mockito.mock(AdminAuditLogRepository.class)
        );
        Mockito.when(resolver.require(Mockito.isNull(), Mockito.eq(AccessLevel.MEMBER))).thenReturn(member());
        Mockito.when(bookRepository.createManualBook(1L, "책 제목", "저자", null, null, null)).thenReturn(10L);
        Mockito.when(bookRepository.findSummaryById(10L)).thenReturn(Optional.of(new BookSummary(
            10L,
            "책 제목",
            "저자",
            null,
            null,
            null,
            "UNVERIFIED"
        )));

        BookSummary result = service.createManual(null, new BookService.ManualBookCommand(" 책 제목 ", " 저자 ", null, null, null));

        assertThat(result.status()).isEqualTo("UNVERIFIED");
        verify(bookRepository).createManualBook(1L, "책 제목", "저자", null, null, null);
    }

    @Test
    void adminCanVerifyUnverifiedBook() {
        CurrentMemberResolver resolver = Mockito.mock(CurrentMemberResolver.class);
        BookRepository bookRepository = Mockito.mock(BookRepository.class);
        AdminAuditLogRepository auditLogRepository = Mockito.mock(AdminAuditLogRepository.class);
        BookService service = new BookService(
            resolver,
            Mockito.mock(BookSearchProvider.class),
            bookRepository,
            Mockito.mock(ReadingRecordRepository.class),
            Mockito.mock(RecommendedBookRepository.class),
            auditLogRepository
        );
        Mockito.when(resolver.require(Mockito.isNull(), Mockito.eq(AccessLevel.ADMIN))).thenReturn(admin());
        Mockito.when(bookRepository.findSummaryById(10L))
            .thenReturn(Optional.of(new BookSummary(10L, "책 제목", "저자", null, null, null, "UNVERIFIED")))
            .thenReturn(Optional.of(new BookSummary(10L, "책 제목", "저자", null, null, null, "VERIFIED")));

        BookSummary result = service.verifyBookByAdmin(null, 10L);

        assertThat(result.status()).isEqualTo("VERIFIED");
        verify(bookRepository).verify(10L);
        verify(auditLogRepository).record(
            Mockito.eq(1L),
            Mockito.eq("VERIFY_BOOK"),
            Mockito.eq("BOOK"),
            Mockito.eq(10L),
            Mockito.eq("{\"status\":\"UNVERIFIED\"}"),
            Mockito.eq("{\"status\":\"VERIFIED\"}")
        );
    }

    private MemberPrincipal member() {
        OffsetDateTime now = OffsetDateTime.now();
        return new MemberPrincipal(1L, "MEMBER", "NICKNAME", null, "member", null, now, now, now, now, null);
    }

    private MemberPrincipal admin() {
        OffsetDateTime now = OffsetDateTime.now();
        return new MemberPrincipal(1L, "ADMIN", "NICKNAME", null, "admin", null, now, now, now, now, null);
    }
}

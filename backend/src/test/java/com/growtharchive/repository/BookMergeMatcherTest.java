package com.growtharchive.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.growtharchive.service.book.BookSearchResult;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class BookMergeMatcherTest {

    @Test
    void findsSingleManualCandidateIgnoringSpacingAndPunctuation() {
        BookSearchResult result = kakaoBook("불안한 그 마음을 내 앞에 꺼내 보아라", "김작가", "마음출판", LocalDate.of(2024, 5, 1));

        assertThat(BookMergeMatcher.findSingleStrongMatch(
            result,
            List.of(new BookMergeMatcher.ManualBookCandidate(10L, "불안한 그 마음을, 내 앞에 꺼내보아라", "김 작가", "마음출판", null))
        )).contains(10L);
    }

    @Test
    void doesNotMergeWhenPublisherConflicts() {
        BookSearchResult result = kakaoBook("인간 실격", "다자이 오사무", "민음사", LocalDate.of(2012, 4, 10));

        assertThat(BookMergeMatcher.findSingleStrongMatch(
            result,
            List.of(new BookMergeMatcher.ManualBookCandidate(10L, "인간실격", "다자이오사무", "문학동네", LocalDate.of(2012, 4, 10)))
        )).isEmpty();
    }

    @Test
    void doesNotMergeWhenMultipleManualCandidatesMatch() {
        BookSearchResult result = kakaoBook("돈의 속성", "김승호", "스노우폭스북스", null);

        assertThat(BookMergeMatcher.findSingleStrongMatch(
            result,
            List.of(
                new BookMergeMatcher.ManualBookCandidate(10L, "돈의 속성", "김승호", "스노우폭스북스", null),
                new BookMergeMatcher.ManualBookCandidate(11L, "돈의 속성", "김승호", null, null)
            )
        )).isEmpty();
    }

    private BookSearchResult kakaoBook(String title, String authorsText, String publisher, LocalDate publishedDate) {
        return new BookSearchResult(
            "KAKAO",
            title,
            authorsText,
            publisher,
            publishedDate,
            "https://example.com/cover.jpg",
            "1234567890",
            "9791190000000",
            "{}"
        );
    }
}

package com.growtharchive.repository;

import com.growtharchive.service.book.BookSearchResult;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

final class BookMergeMatcher {

    private BookMergeMatcher() {
    }

    static Optional<Long> findSingleStrongMatch(BookSearchResult result, List<ManualBookCandidate> candidates) {
        String targetTitle = canonical(result.title());
        String targetAuthors = canonical(result.authorsText());
        if (targetTitle.isBlank() || targetAuthors.isBlank()) {
            return Optional.empty();
        }
        List<Long> matchedIds = candidates.stream()
            .filter(candidate -> matches(result, targetTitle, targetAuthors, candidate))
            .map(ManualBookCandidate::id)
            .toList();
        return matchedIds.size() == 1 ? Optional.of(matchedIds.getFirst()) : Optional.empty();
    }

    private static boolean matches(BookSearchResult result, String targetTitle, String targetAuthors, ManualBookCandidate candidate) {
        if (!targetTitle.equals(canonical(candidate.title())) || !targetAuthors.equals(canonical(candidate.authorsText()))) {
            return false;
        }
        String sourcePublisher = canonical(result.publisher());
        String candidatePublisher = canonical(candidate.publisher());
        if (!sourcePublisher.isBlank() && !candidatePublisher.isBlank() && !sourcePublisher.equals(candidatePublisher)) {
            return false;
        }
        LocalDate sourcePublishedDate = result.publishedDate();
        LocalDate candidatePublishedDate = candidate.publishedDate();
        return sourcePublishedDate == null || candidatePublishedDate == null || sourcePublishedDate.equals(candidatePublishedDate);
    }

    private static String canonical(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
            .toLowerCase(Locale.KOREAN)
            .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", "");
    }

    record ManualBookCandidate(
        Long id,
        String title,
        String authorsText,
        String publisher,
        LocalDate publishedDate
    ) {
    }
}

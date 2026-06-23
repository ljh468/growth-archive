package com.growtharchive.service.book;

import com.growtharchive.config.properties.AppProperties;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class KakaoBookSearchProvider implements BookSearchProvider {

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public KakaoBookSearchProvider(AppProperties appProperties, ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public BookSearchProviderType providerType() {
        return BookSearchProviderType.KAKAO;
    }

    @Override
    public List<BookSearchResult> search(String query, int page, int size) {
        String apiKey = appProperties.getBookSearch().getKakao().getRestApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return List.of();
        }
        try {
            String uri = UriComponentsBuilder
                .fromUriString(appProperties.getBookSearch().getKakao().getEndpoint())
                .queryParam("query", query)
                .queryParam("page", page + 1)
                .queryParam("size", size)
                .build()
                .toUriString();
            String body = restClient.get()
                .uri(uri)
                .header("Authorization", "KakaoAK " + apiKey)
                .retrieve()
                .body(String.class);
            if (body == null || body.isBlank()) {
                return List.of();
            }
            return parse(body);
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    private List<BookSearchResult> parse(String body) {
        JsonNode documents = objectMapper.readTree(body).get("documents");
        if (documents == null || !documents.isArray()) {
            return List.of();
        }
        List<BookSearchResult> results = new ArrayList<>();
        for (JsonNode document : documents) {
            String rawPayload = document.toString();
            String title = text(document, "title");
            String authorsText = authorsText(document.get("authors"));
            String isbn = text(document, "isbn");
            Map<String, String> isbnParts = splitIsbn(isbn);
            results.add(new BookSearchResult(
                "KAKAO",
                title,
                authorsText.isBlank() ? "미상" : authorsText,
                text(document, "publisher"),
                parseDate(text(document, "datetime")),
                text(document, "thumbnail"),
                isbnParts.get("isbn10"),
                isbnParts.get("isbn13"),
                rawPayload
            ));
        }
        return results;
    }

    private String authorsText(JsonNode authors) {
        if (authors == null || !authors.isArray()) {
            return "";
        }
        List<String> values = new ArrayList<>();
        for (JsonNode author : authors) {
            String value = author.asString();
            if (value != null && !value.isBlank()) {
                values.add(value);
            }
        }
        return String.join(", ", values);
    }

    private Map<String, String> splitIsbn(String isbn) {
        String isbn10 = null;
        String isbn13 = null;
        if (isbn != null) {
            for (String part : isbn.split("\\s+")) {
                String trimmed = part.trim();
                if (trimmed.length() == 10) {
                    isbn10 = trimmed;
                } else if (trimmed.length() == 13) {
                    isbn13 = trimmed;
                }
            }
        }
        return Map.of(
            "isbn10", isbn10 == null ? "" : isbn10,
            "isbn13", isbn13 == null ? "" : isbn13
        );
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toLocalDate();
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asString();
    }
}

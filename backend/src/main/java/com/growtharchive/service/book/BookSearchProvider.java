package com.growtharchive.service.book;

import java.util.List;

public interface BookSearchProvider {
    BookSearchProviderType providerType();

    List<BookSearchResult> search(String query, int page, int size);
}

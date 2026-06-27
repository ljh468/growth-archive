package com.growtharchive.domain.book;

import com.growtharchive.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "books")
public class Book extends BaseEntity {

    @Column(nullable = false, length = 30)
    private String source;

    @Column(name = "source_book_id", length = 100)
    private String sourceBookId;

    @Column(length = 20)
    private String isbn10;

    @Column(length = 20)
    private String isbn13;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "authors_text", nullable = false, length = 255)
    private String authorsText;

    @Column(length = 120)
    private String publisher;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column(name = "thumbnail_url", columnDefinition = "text")
    private String thumbnailUrl;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_by_member_id")
    private Long createdByMemberId;

    @Column(name = "source_payload", columnDefinition = "jsonb")
    private String sourcePayload;

    protected Book() {
    }
}

package com.growtharchive.domain.book;

import com.growtharchive.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.JSON)
    private String sourcePayload;

    protected Book() {
    }

    public Book(
        String source,
        String sourceBookId,
        String isbn10,
        String isbn13,
        String title,
        String authorsText,
        String publisher,
        LocalDate publishedDate,
        String thumbnailUrl,
        String status,
        Long createdByMemberId,
        String sourcePayload
    ) {
        this.source = source;
        this.sourceBookId = sourceBookId;
        this.isbn10 = isbn10;
        this.isbn13 = isbn13;
        this.title = title;
        this.authorsText = authorsText;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.thumbnailUrl = thumbnailUrl;
        this.status = status;
        this.createdByMemberId = createdByMemberId;
        this.sourcePayload = sourcePayload;
    }
}

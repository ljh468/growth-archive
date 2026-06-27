package com.growtharchive.repository;

import com.growtharchive.service.storage.StoredImage;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ImageAssetRepository {

    private final JdbcTemplate jdbcTemplate;

    public ImageAssetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(Long memberId, String imageType, StoredImage image) {
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO image_assets (
                    owner_member_id, bucket, object_key, public_url, image_type,
                    mime_type, width, height, size_bytes, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, now())
                RETURNING id
                """,
            Long.class,
            memberId,
            image.bucket(),
            image.objectKey(),
            image.publicUrl(),
            imageType,
            image.mimeType(),
            image.width(),
            image.height(),
            image.sizeBytes()
        );
    }

    public boolean isOwnedImage(Long memberId, Long imageId, String imageType) {
        if (imageId == null) {
            return true;
        }
        Integer count = jdbcTemplate.queryForObject(
            """
                SELECT count(*)
                FROM image_assets
                WHERE owner_member_id = ? AND id = ? AND image_type = ?
                """,
            Integer.class,
            memberId,
            imageId,
            imageType
        );
        return count != null && count > 0;
    }

    public boolean isUnownedImage(Long imageId, String imageType) {
        if (imageId == null) {
            return true;
        }
        Integer count = jdbcTemplate.queryForObject(
            """
                SELECT count(*)
                FROM image_assets
                WHERE owner_member_id IS NULL AND id = ? AND image_type = ?
                """,
            Integer.class,
            imageId,
            imageType
        );
        return count != null && count > 0;
    }

    public void assignOwner(Long imageId, Long memberId) {
        if (imageId == null) {
            return;
        }
        jdbcTemplate.update(
            """
                UPDATE image_assets
                SET owner_member_id = ?
                WHERE id = ? AND owner_member_id IS NULL
                """,
            memberId,
            imageId
        );
    }

    public int countOwnedImages(Long memberId, List<Long> imageIds, String imageType) {
        if (imageIds == null || imageIds.isEmpty()) {
            return 0;
        }
        String placeholders = String.join(",", imageIds.stream().map(id -> "?").toList());
        List<Object> args = new ArrayList<>();
        args.add(memberId);
        args.add(imageType);
        args.addAll(imageIds);
        Integer count = jdbcTemplate.queryForObject(
            """
                SELECT count(*)
                FROM image_assets
                WHERE owner_member_id = ? AND image_type = ? AND id IN (%s)
                """.formatted(placeholders),
            Integer.class,
            args.toArray()
        );
        return count == null ? 0 : count;
    }
}

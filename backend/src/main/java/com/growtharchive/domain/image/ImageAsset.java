package com.growtharchive.domain.image;

import com.growtharchive.domain.common.CreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "image_assets")
public class ImageAsset extends CreatedAtEntity {

    @Column(name = "owner_member_id")
    private Long ownerMemberId;

    @Column(nullable = false, length = 80)
    private String bucket;

    @Column(name = "object_key", nullable = false, columnDefinition = "text")
    private String objectKey;

    @Column(name = "public_url", columnDefinition = "text")
    private String publicUrl;

    @Column(name = "image_type", nullable = false, length = 50)
    private String imageType;

    @Column(name = "mime_type", nullable = false, length = 50)
    private String mimeType;

    @Column
    private Integer width;

    @Column
    private Integer height;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    protected ImageAsset() {
    }
}

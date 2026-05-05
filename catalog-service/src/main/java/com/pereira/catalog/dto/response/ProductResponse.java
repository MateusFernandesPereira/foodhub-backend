package com.pereira.catalog.dto.response;

import com.pereira.catalog.entity.ProductEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    String category,
    String imageUrl,
    Boolean available,
    Integer stockQuantity,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static ProductResponse from(ProductEntity entity) {
        return new ProductResponse(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getPrice(),
            entity.getCategory(),
            entity.getImageUrl(),
            entity.getAvailable(),
            entity.getStockQuantity(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

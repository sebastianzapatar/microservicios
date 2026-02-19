package com.nomelestar.productservice.dto;

import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        String category,
        String imageUrl
) {
}

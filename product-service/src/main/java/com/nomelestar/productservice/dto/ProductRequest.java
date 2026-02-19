package com.nomelestar.productservice.dto;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        String category,
        String imageUrl
) {
}

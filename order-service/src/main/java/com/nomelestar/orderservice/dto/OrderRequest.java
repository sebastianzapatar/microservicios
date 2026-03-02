package com.nomelestar.orderservice.dto;

public record OrderRequest(
        String productId,
        Integer quantity) {
}

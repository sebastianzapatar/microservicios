package com.nomelestar.orderservice.dto;

public record OrderResponse(
        Long id,
        String orderNumber,
        String productId,
        Integer quantity,
        Double totalPrice) {
}

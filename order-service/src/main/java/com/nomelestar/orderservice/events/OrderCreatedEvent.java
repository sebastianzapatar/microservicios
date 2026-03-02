package com.nomelestar.orderservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderCreatedEvent {
    private Long id;
    private String orderNumber;
    private String productId;
    private Integer quantity;
    private Double totalPrice;
}

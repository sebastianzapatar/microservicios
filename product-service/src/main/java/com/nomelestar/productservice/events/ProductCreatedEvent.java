package com.nomelestar.productservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductCreatedEvent {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private String category;
}

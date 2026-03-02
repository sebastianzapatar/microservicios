package com.nomelestar.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductResponse(
        String id,
        String name,
        Double price,
        Integer quantity) {
}

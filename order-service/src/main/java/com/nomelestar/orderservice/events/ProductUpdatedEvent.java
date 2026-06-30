package com.nomelestar.orderservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento consumido desde Kafka que representa la actualización de datos de un producto en product-service.
 * Notifica a order-service de cambios de stock, precio, etc., de manera reactiva y asíncrona.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductUpdatedEvent {
    // Identificador único del producto actualizado.
    private String id;
    
    // Nuevo nombre del producto.
    private String name;
    
    // Nueva descripción del producto.
    private String description;
    
    // Nuevo precio unitario.
    private Double price;
    
    // Nueva cantidad disponible en stock.
    private Integer quantity;
    
    // Categoría del producto.
    private String category;
}


package com.nomelestar.orderservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento consumido desde Kafka que representa la creación de un nuevo producto en product-service.
 * Permite a order-service enterarse de forma asíncrona de nuevos productos sin consultar vía REST.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductCreatedEvent {
    // Identificador único del producto.
    private String id;
    
    // Nombre del producto.
    private String name;
    
    // Descripción detallada del producto.
    private String description;
    
    // Precio unitario del producto.
    private Double price;
    
    // Cantidad inicial disponible en stock.
    private Integer quantity;
    
    // Categoría del producto.
    private String category;
}


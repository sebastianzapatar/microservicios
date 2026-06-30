package com.nomelestar.productservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Evento publicado en Kafka al registrar un nuevo producto en el catálogo.
 * Este DTO se envía al tópico "product.created" para que otros interesados (como order-service)
 * actualicen sus cachés o almacenes de lectura de forma reactiva.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductCreatedEvent {
    // Identificador único generado para el producto.
    private String id;
    
    // Nombre comercial del producto.
    private String name;
    
    // Descripción de características del producto.
    private String description;
    
    // Precio unitario representado de manera precisa con BigDecimal.
    private BigDecimal price;
    
    // Cantidad física disponible inicialmente.
    private Integer quantity;
    
    // Categoría del catálogo a la que pertenece.
    private String category;
}


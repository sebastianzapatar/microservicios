package com.nomelestar.productservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Evento publicado en Kafka cada vez que se modifican los atributos o el stock de un producto.
 * Este DTO se envía al tópico "product.updated" para mantener la consistencia eventual
 * entre servicios (como order-service).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductUpdatedEvent {
    // Identificador único del producto que sufrió cambios.
    private String id;
    
    // Nombre actualizado.
    private String name;
    
    // Descripción actualizada.
    private String description;
    
    // Precio actualizado.
    private BigDecimal price;
    
    // Nuevo stock disponible tras la actualización o venta.
    private Integer quantity;
    
    // Categoría del producto.
    private String category;
}


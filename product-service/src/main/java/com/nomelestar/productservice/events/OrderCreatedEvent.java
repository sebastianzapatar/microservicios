package com.nomelestar.productservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento consumido desde Kafka que representa la creación de una orden en order-service.
 * product-service escucha este evento para descontar de forma asíncrona la cantidad comprada
 * del catálogo de inventario.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderCreatedEvent {
    // Identificador de la orden en la base de datos de origen.
    private Long id;
    
    // Código único identificativo de la orden.
    private String orderNumber;
    
    // Identificador del producto comprado que debe reducir su inventario.
    private String productId;
    
    // Cantidad de artículos comprados.
    private Integer quantity;
    
    // Precio total cobrado por la orden.
    private Double totalPrice;
}


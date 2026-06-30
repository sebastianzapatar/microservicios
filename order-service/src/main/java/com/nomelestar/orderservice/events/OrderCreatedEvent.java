package com.nomelestar.orderservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento que representa la creación de una orden de compra en el sistema.
 * Este DTO se serializa a JSON y se envía a Apache Kafka al tópico "order.created"
 * para que otros microservicios (como product-service para reducir stock) lo consuman de forma asíncrona.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderCreatedEvent {
    // Identificador único de la orden en la base de datos de order-service.
    private Long id;
    
    // Código único autogenerado identificador de la orden (UUID).
    private String orderNumber;
    
    // Identificador del producto comprado.
    private String productId;
    
    // Cantidad de unidades solicitadas en la orden.
    private Integer quantity;
    
    // Precio total calculado de la orden.
    private Double totalPrice;
}


package com.nomelestar.productservice.listeners;

import com.nomelestar.productservice.events.OrderCreatedEvent;
import com.nomelestar.productservice.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Componente oyente (Listener) para eventos de órdenes procedentes de Kafka.
 * Se suscribe al tópico de creación de órdenes y desencadena los efectos colaterales
 * correspondientes dentro del catálogo de productos (por ejemplo, reducir el stock disponible).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final ProductService productService;

    /**
     * Escucha eventos de creación de órdenes y actualiza el stock físico de un producto en concordancia.
     *
     * @KafkaListener indica que este método consumirá del tópico indicado.
     * - topics: "order.created" (tópico donde order-service publica cuando se realiza un pedido exitoso).
     * - groupId: "product-group" (identifica al grupo de consumidores de product-service).
     *
     * Gracias al StringJacksonJsonMessageConverter configurado, el JSON crudo en String recibido
     * de Kafka se convierte automáticamente a un objeto {@link OrderCreatedEvent}.
     *
     * @param event DTO que representa los detalles de la orden creada.
     */
    @KafkaListener(topics = "order.created", groupId = "product-group")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Evento Recibido: OrderCreatedEvent - Número de Orden: {}, ID de Producto: {}", 
                event.getOrderNumber(), event.getProductId());
        try {
            // Invoca al servicio de productos para restar la cantidad vendida del inventario
            productService.updateQuantity(event.getProductId(), event.getQuantity());
            log.info("Stock actualizado exitosamente para el Producto ID: {}", event.getProductId());
        } catch (Exception e) {
            log.error("Error al actualizar la cantidad del producto ID: {}", event.getProductId(), e);
            // Relanzar la excepción causará que el oyente de Kafka aplique la política de reintentos
            // (Retry Policy / Dead Letter Queue) configurada por defecto o de forma personalizada.
            throw e; 
        }
    }
}


package com.nomelestar.productservice.listeners;

import com.nomelestar.productservice.config.RabbitMQConfig;
import com.nomelestar.productservice.events.OrderCreatedEvent;
import com.nomelestar.productservice.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Oyente (Listener) encargado de recibir mensajes desde las colas de RabbitMQ.
 * Escucha eventos relacionados con la creación de órdenes en order-service para
 * actualizar de forma reactiva y asíncrona el stock del inventario.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final ProductService productService;

    /**
     * Consume eventos de creación de órdenes y descuenta la cantidad vendida del inventario de productos.
     * 
     * @RabbitListener se suscribe a la cola "product.order.created" configurada en RabbitMQConfig.
     * JacksonJsonMessageConverter mapea automáticamente el cuerpo JSON del mensaje
     * al objeto OrderCreatedEvent.
     * 
     * @param event DTO con las propiedades de la orden creada.
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE_NAME)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Evento Recibido por RabbitMQ: OrderCreatedEvent - ID Producto: {}, Cantidad: {}", event.getProductId(), event.getQuantity());
        try {
            // Invoca al servicio local para restar el stock vendido
            productService.updateQuantity(event.getProductId(), event.getQuantity());
            log.info("Cantidad de stock actualizada con éxito para el Producto ID: {}", event.getProductId());
        } catch (Exception e) {
            log.error("Error al actualizar la cantidad de stock para el Producto ID: {}", event.getProductId(), e);
            // Lanzar la excepción causa que el listener intente procesar de nuevo según la política de RabbitMQ
            throw e;
        }
    }
}

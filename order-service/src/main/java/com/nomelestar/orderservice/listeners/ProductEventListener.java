package com.nomelestar.orderservice.listeners;

import com.nomelestar.orderservice.config.RabbitMQConfig;
import com.nomelestar.orderservice.events.ProductCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Oyente (Listener) encargado de recibir mensajes desde las colas de RabbitMQ.
 * Escucha eventos relacionados con el catálogo de productos publicados por product-service.
 */
@Component
@Slf4j
public class ProductEventListener {

    /**
     * Captura y procesa eventos de creación de productos.
     * 
     * @RabbitListener se suscribe a la cola "order.product.created" configurada en RabbitMQConfig.
     * JacksonJsonMessageConverter mapea automáticamente el cuerpo JSON del mensaje
     * al objeto ProductCreatedEvent.
     * 
     * @param event DTO con las propiedades del producto creado.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleProductCreatedEvent(ProductCreatedEvent event) {
        log.info("Evento Recibido por RabbitMQ: ProductCreatedEvent - ID de Producto: {}, Nombre: {}", event.getId(), event.getName());
        // Aquí se procesaría el evento según el dominio, por ejemplo, guardando una copia local (Read Replica)
        // en la base de datos de órdenes para consultas rápidas sin HTTP síncronos.
    }
}

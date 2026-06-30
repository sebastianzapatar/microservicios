package com.nomelestar.orderservice.listeners;

import com.nomelestar.orderservice.events.ProductCreatedEvent;
import com.nomelestar.orderservice.events.ProductUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Componente oyente (Listener) para eventos de productos procedentes de Kafka.
 * Se encarga de suscribirse a los tópicos relacionados con el ciclo de vida de los productos
 * (creación y actualización) y procesarlos de manera asíncrona dentro del contexto de order-service.
 */
@Component
@Slf4j
public class ProductEventListener {

    /**
     * Escucha y procesa eventos de creación de productos.
     *
     * @KafkaListener indica que este método actuará como un consumidor del tópico especificado.
     * - topics: "product.created" (tópico donde product-service pública cuando crea un producto).
     * - groupId: "order-group" (identifica al grupo de consumidores de order-service para balancear la carga).
     *
     * Debido al StringJacksonJsonMessageConverter configurado, el String JSON recibido de Kafka
     * se deserializa directamente en un objeto {@link ProductCreatedEvent}.
     *
     * @param event DTO que representa el evento del producto creado.
     */
    @KafkaListener(topics = "product.created", groupId = "order-group")
    public void handleProductCreatedEvent(ProductCreatedEvent event) {
        log.info("Evento Recibido: ProductCreatedEvent - ID de Producto: {}, Nombre: {}", event.getId(), event.getName());
        // Aquí se puede manejar el evento según sea necesario. Por ejemplo, guardar una copia de lectura local (Read Replica)
        // en la base de datos de órdenes para evitar llamadas síncronas HTTP posteriores, o notificar al sistema.
    }

    /**
     * Escucha y procesa eventos de actualización de productos (por ejemplo, cambios de precio o stock).
     *
     * @KafkaListener especifica el consumo del tópico correspondiente.
     * - topics: "product.updated" (tópico donde product-service publica actualizaciones de productos).
     * - groupId: "order-group".
     *
     * @param event DTO que representa el evento del producto actualizado.
     */
    @KafkaListener(topics = "product.updated", groupId = "order-group")
    public void handleProductUpdatedEvent(ProductUpdatedEvent event) {
        log.info("Evento Recibido: ProductUpdatedEvent - ID de Producto: {}, Nueva Cantidad en Stock: {}", event.getId(), event.getQuantity());
        // Maneja el evento actualizando cachés locales, réplicas de lectura de productos o notificando a usuarios con pedidos pendientes.
    }
}


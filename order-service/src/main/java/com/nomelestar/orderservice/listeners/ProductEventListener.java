package com.nomelestar.orderservice.listeners;

import com.nomelestar.orderservice.events.ProductCreatedEvent;
import com.nomelestar.orderservice.events.ProductUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductEventListener {

    @KafkaListener(topics = "product.created", groupId = "order-group")
    public void handleProductCreatedEvent(ProductCreatedEvent event) {
        log.info("Received ProductCreatedEvent - product id: {}, name: {}", event.getId(), event.getName());
        // Handle the event as needed, for example saving it to a local read-replica or
        // notifying someone
    }

    @KafkaListener(topics = "product.updated", groupId = "order-group")
    public void handleProductUpdatedEvent(ProductUpdatedEvent event) {
        log.info("Received ProductUpdatedEvent - product id: {}, new quantity: {}", event.getId(), event.getQuantity());
        // Handle the event as needed, for example updating local copies or notifying
        // users
    }
}

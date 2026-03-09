package com.nomelestar.productservice.listeners;

import com.nomelestar.productservice.events.OrderCreatedEvent;
import com.nomelestar.productservice.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final ProductService productService;

    @KafkaListener(topics = "order.created", groupId = "product-group")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent - order number: {}, for product id: {}", event.getOrderNumber(),
                event.getProductId());
        try {
            productService.updateQuantity(event.getProductId(), event.getQuantity());
            log.info("Successfully updated product quantity for productId: {}", event.getProductId());
        } catch (Exception e) {
            log.error("Failed to update product quantity for productId: {}", event.getProductId(), e);
            throw e; // Rethrowing will cause Kafka listener to handle it according to the retry
                     // policy
        }
    }
}

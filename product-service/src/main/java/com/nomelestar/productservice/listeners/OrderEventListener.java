package com.nomelestar.productservice.listeners;

import com.nomelestar.productservice.config.RabbitMQConfig;
import com.nomelestar.productservice.events.OrderCreatedEvent;
import com.nomelestar.productservice.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final ProductService productService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE_NAME)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent - productId: {}, quantity: {}", event.getProductId(), event.getQuantity());
        try {
            productService.updateQuantity(event.getProductId(), event.getQuantity());
            log.info("Successfully updated product quantity for productId: {}", event.getProductId());
        } catch (Exception e) {
            log.error("Failed to update product quantity for productId: {}", event.getProductId(), e);
            throw e;
        }
    }
}

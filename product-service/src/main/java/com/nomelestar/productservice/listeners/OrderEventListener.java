package com.nomelestar.productservice.listeners;

import com.nomelestar.productservice.events.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventListener {

    @KafkaListener(topics = "order.created", groupId = "product-group")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent - order number: {}, for product id: {}", event.getOrderNumber(),
                event.getProductId());
        // Handle the event as needed, for example logging or updating some internal
        // metrics
    }
}

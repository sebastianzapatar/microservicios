package com.nomelestar.orderservice.listeners;

import com.nomelestar.orderservice.config.RabbitMQConfig;
import com.nomelestar.orderservice.events.ProductCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductEventListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleProductCreatedEvent(ProductCreatedEvent event) {
        log.info("Received ProductCreatedEvent - product id: {}, name: {}", event.getId(), event.getName());
        // Handle the event as needed, for example saving it to a local read-replica or
        // notifying someone
    }
}

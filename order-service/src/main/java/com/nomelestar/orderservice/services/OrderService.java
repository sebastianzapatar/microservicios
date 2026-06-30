package com.nomelestar.orderservice.services;

import com.nomelestar.orderservice.client.ProductClient;
import com.nomelestar.orderservice.dto.OrderRequest;
import com.nomelestar.orderservice.dto.OrderResponse;
import com.nomelestar.orderservice.dto.ProductResponse;
import com.nomelestar.orderservice.models.Order;
import com.nomelestar.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import feign.FeignException;

import org.springframework.kafka.core.KafkaTemplate;
import com.nomelestar.orderservice.events.OrderCreatedEvent;
import com.nomelestar.orderservice.config.KafkaConfig;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    // Plantilla de Kafka inyectada para enviar mensajes a los tópicos de Apache Kafka.
    // La clave es un String (el número de orden para el particionado) y el valor es un objeto.
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderResponse createOrder(OrderRequest request) {
        // 1. Verificar que el producto exista y tenga cantidad suficiente en product-service
        ProductResponse product;
        try {
            product = productClient.getProductById(request.productId());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        if (product.quantity() < request.quantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Insufficient stock for product " + product.name());
        }


        // 3. Crear y guardar la entidad Order en la base de datos de órdenes
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setTotalPrice(product.price() * request.quantity());

        Order savedOrder = orderRepository.save(order);

        // 4. Construir el evento de creación de orden (OrderCreatedEvent) para la publicación asíncrona
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .id(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .productId(savedOrder.getProductId())
                .quantity(savedOrder.getQuantity())
                .totalPrice(savedOrder.getTotalPrice())
                .build();

        // 5. Enviar el evento al tópico de Kafka. 
        // - Nombre del tópico: KafkaConfig.ORDER_CREATED_TOPIC_NAME ("order.created")
        // - Clave de particionamiento: event.getOrderNumber() (garantiza que mensajes de la misma orden vayan a la misma partición y mantengan el orden)
        // - Payload: el objeto event (serializado automáticamente a JSON)
        kafkaTemplate.send(KafkaConfig.ORDER_CREATED_TOPIC_NAME,
                event.getOrderNumber(), event);

        return mapToResponse(savedOrder);
    }

    public java.util.List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotalPrice());
    }
}

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
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderResponse createOrder(OrderRequest request) {
        // 1. Verify product exists and quantity is sufficient
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

        // 2. Reduce product stock
        productClient.updateProductQuantity(request.productId(), request.quantity());

        // 3. Save the Order
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setTotalPrice(product.price() * request.quantity());

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .id(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .productId(savedOrder.getProductId())
                .quantity(savedOrder.getQuantity())
                .totalPrice(savedOrder.getTotalPrice())
                .build();

        kafkaTemplate.send(KafkaConfig.ORDER_CREATED_TOPIC_NAME, event.getOrderNumber(), event);

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

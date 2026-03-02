package com.nomelestar.orderservice.services;

import com.nomelestar.orderservice.client.ProductClient;
import com.nomelestar.orderservice.dto.OrderRequest;
import com.nomelestar.orderservice.dto.OrderResponse;
import com.nomelestar.orderservice.dto.ProductResponse;
import com.nomelestar.orderservice.models.Order;
import com.nomelestar.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderResponse createOrder(OrderRequest request) {
        // 1. Verify product exists and quantity is sufficient
        ProductResponse product = productClient.getProductById(request.productId());
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        if (product.quantity() < request.quantity()) {
            throw new RuntimeException("Insufficient stock for product " + product.name());
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

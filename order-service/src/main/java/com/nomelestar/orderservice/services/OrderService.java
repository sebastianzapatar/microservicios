package com.nomelestar.orderservice.services;

import com.nomelestar.orderservice.client.ProductClient;
import com.nomelestar.orderservice.config.RabbitMQConfig;
import com.nomelestar.orderservice.dto.OrderRequest;
import com.nomelestar.orderservice.dto.OrderResponse;
import com.nomelestar.orderservice.dto.ProductResponse;
import com.nomelestar.orderservice.events.OrderCreatedEvent;
import com.nomelestar.orderservice.models.Order;
import com.nomelestar.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import feign.FeignException;

import java.util.UUID;

/**
 * Servicio encargado de gestionar el ciclo de vida de las órdenes.
 * Coordina la validación de inventario con product-service, persiste órdenes locales
 * y publica eventos asíncronos en RabbitMQ para notificar la creación de nuevas órdenes.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Crea y registra una nueva orden en el sistema.
     * 
     * @param request Datos de la orden a crear (ID del producto y cantidad).
     * @return DTO con la respuesta de la orden creada.
     */
    public OrderResponse createOrder(OrderRequest request) {
        // 1. Verificar existencia del producto y validar que el stock sea suficiente (Llamada síncrona HTTP vía Feign)
        ProductResponse product;
        try {
            product = productClient.getProductById(request.productId());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }

        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        if (product.quantity() < request.quantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock insuficiente para el producto: " + product.name());
        }

        // 2. Guardar la Orden localmente en la base de datos de órdenes
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setTotalPrice(product.price() * request.quantity());

        Order savedOrder = orderRepository.save(order);

        // 3. Reducir el stock del producto de manera asíncrona mediante la publicación de un evento en RabbitMQ
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                 .productId(request.productId())
                .quantity(request.quantity())
                .build();
                
        // Se envía al Exchange especificado usando la clave de enrutamiento asignada
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                event);

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

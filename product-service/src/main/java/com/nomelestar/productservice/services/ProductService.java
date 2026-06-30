package com.nomelestar.productservice.services;

import com.nomelestar.productservice.dto.ProductRequest;
import com.nomelestar.productservice.dto.ProductResponse;
import com.nomelestar.productservice.models.Product;
import com.nomelestar.productservice.repository.ProductRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;

    private void productDTOtoEntity(Product product, ProductRequest dto) {
        product.setDescription(dto.description());
        product.setName(dto.name());
        product.setPrice(dto.price());
        product.setCategory(dto.category());
        product.setQuantity(dto.quantity());
        product.setImage(dto.imageUrl());
    }

    private void responseDTOtoEntity(Product product, ProductResponse dto) {
        product.setId(dto.id());
        product.setDescription(dto.description());
        product.setName(dto.name());
        product.setPrice(dto.price());
        product.setCategory(dto.category());
        product.setQuantity(dto.quantity());
        product.setImage(dto.imageUrl());
    }

    public void delete(String id) {
        Product p1 = productRepository.findByIdAndActiveTrue(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        productRepository.delete(p1);

    }

    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword)
                .stream().map(this::mapToResponse).toList();
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(product.getId(),
                product.getName(), product.getDescription(), product.getPrice(),
                product.getQuantity(), product.getCategory(), product.getImage());
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream().map(this::mapToResponse).toList();
    }

    public ProductResponse findByIdAndActiveTrue(String id) {
        Product p1 = productRepository.findByIdAndActiveTrue(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return mapToResponse(p1);
    }

    /**
     * Registra un nuevo producto en la base de datos local y publica un evento en RabbitMQ.
     * 
     * @param request DTO con la información del producto a registrar.
     * @return DTO con la información del producto guardado.
     */
    public ProductResponse save(ProductRequest request) {
        Product product = new Product();
        productDTOtoEntity(product, request);
        Product savedProduct = productRepository.save(product);

        // Construir el evento de creación de producto
        com.nomelestar.productservice.events.ProductCreatedEvent event = com.nomelestar.productservice.events.ProductCreatedEvent
                .builder()
                .id(savedProduct.getId())
                .name(savedProduct.getName())
                .description(savedProduct.getDescription())
                .price(savedProduct.getPrice())
                .quantity(savedProduct.getQuantity())
                .category(savedProduct.getCategory())
                .build();

        // Enviar el evento al Exchange de RabbitMQ con la clave de enrutamiento configurada
        rabbitTemplate.convertAndSend(
                com.nomelestar.productservice.config.RabbitMQConfig.EXCHANGE_NAME,
                com.nomelestar.productservice.config.RabbitMQConfig.ROUTING_KEY,
                event);

        return mapToResponse(savedProduct);
    }

    /**
     * Actualiza la información de un producto existente.
     */
    public ProductResponse update(String id, ProductRequest request) {
        Product product = productRepository.findByIdAndActiveTrue(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        productDTOtoEntity(product, request);
        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    /**
     * Disminuye la cantidad física de inventario para un producto debido a una orden procesada.
     * Método invocado por el listener asíncrono.
     */
    public void updateQuantity(String id, Integer quantity) {
        Product product = productRepository.findByIdAndActiveTrue(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        if (product.getQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente en el catálogo de productos");
        }
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
    }

}

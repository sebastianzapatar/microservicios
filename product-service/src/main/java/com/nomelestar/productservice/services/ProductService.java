package com.nomelestar.productservice.services;

import com.nomelestar.productservice.dto.ProductRequest;
import com.nomelestar.productservice.dto.ProductResponse;
import com.nomelestar.productservice.models.Product;
import com.nomelestar.productservice.repository.ProductRepository;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;

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

    public ProductResponse save(ProductRequest request) {
        Product product = new Product();
        productDTOtoEntity(product, request);
        Product savedProduct = productRepository.save(product);

        com.nomelestar.productservice.events.ProductCreatedEvent event = com.nomelestar.productservice.events.ProductCreatedEvent
                .builder()
                .id(savedProduct.getId())
                .name(savedProduct.getName())
                .description(savedProduct.getDescription())
                .price(savedProduct.getPrice())
                .quantity(savedProduct.getQuantity())
                .category(savedProduct.getCategory())
                .build();

        kafkaTemplate.send(
                com.nomelestar.productservice.config.KafkaConfig.TOPIC_NAME,
                event.getId(), // Use product ID as the Kafka partitioning key
                event);

        return mapToResponse(savedProduct);
    }

    public ProductResponse update(String id, ProductRequest request) {
        Product product = productRepository.findByIdAndActiveTrue(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        productDTOtoEntity(product, request);
        Product updatedProduct = productRepository.save(product);

        com.nomelestar.productservice.events.ProductUpdatedEvent event = com.nomelestar.productservice.events.ProductUpdatedEvent
                .builder()
                .id(updatedProduct.getId())
                .name(updatedProduct.getName())
                .description(updatedProduct.getDescription())
                .price(updatedProduct.getPrice())
                .quantity(updatedProduct.getQuantity())
                .category(updatedProduct.getCategory())
                .build();

        kafkaTemplate.send(
                com.nomelestar.productservice.config.KafkaConfig.UPDATED_TOPIC_NAME,
                event.getId(),
                event);

        return mapToResponse(updatedProduct);
    }

    public void updateQuantity(String id, Integer quantity) {
        Product product = productRepository.findByIdAndActiveTrue(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (product.getQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient product quantity");
        }
        product.setQuantity(product.getQuantity() - quantity);
        Product updatedProduct = productRepository.save(product);

        com.nomelestar.productservice.events.ProductUpdatedEvent event = com.nomelestar.productservice.events.ProductUpdatedEvent
                .builder()
                .id(updatedProduct.getId())
                .name(updatedProduct.getName())
                .description(updatedProduct.getDescription())
                .price(updatedProduct.getPrice())
                .quantity(updatedProduct.getQuantity())
                .category(updatedProduct.getCategory())
                .build();

        kafkaTemplate.send(
                com.nomelestar.productservice.config.KafkaConfig.UPDATED_TOPIC_NAME,
                event.getId(),
                event);
    }

}

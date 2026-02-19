package com.nomelestar.productservice.services;

import com.nomelestar.productservice.dto.ProductRequest;
import com.nomelestar.productservice.dto.ProductResponse;
import com.nomelestar.productservice.models.Product;
import com.nomelestar.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private void productDTOtoEntity(Product product, ProductRequest dto) {
        product.setDescription(dto.description());
        product.setName(dto.name());
        product.setPrice(dto.price());
        product.setCategory(dto.category());
    }
    private void responseDTOtoEntity(Product product, ProductResponse dto) {
        product.setId(dto.id());
        product.setDescription(dto.description());
        product.setName(dto.name());
        product.setPrice(dto.price());
        product.setCategory(dto.category());
    }
    public void delete(String id)  {
        Product p1=productRepository.findByIdAndActiveTrue(id).orElseThrow(
                () -> new RuntimeException("Product not found")
        );
        productRepository.delete(p1);

    }
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword)
                .stream().map(this::mapToResponse).toList();
    }
    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(product.getId(),
                product.getName(), product.getDescription(), product.getPrice(),
                product.getQuantity(),product.getCategory(), product.getImage());
    }
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream().map(this::mapToResponse).toList();
    }
    public ProductResponse findByIdAndActiveTrue(String id) {
        Product p1=productRepository.findByIdAndActiveTrue(id).orElseThrow(
                () -> new RuntimeException("Product not found")
        );
        return mapToResponse(p1);
    };

}

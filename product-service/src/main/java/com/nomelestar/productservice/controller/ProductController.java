package com.nomelestar.productservice.controller;

import com.nomelestar.productservice.dto.ProductResponse;
import com.nomelestar.productservice.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(productService.findByIdAndActiveTrue(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAll() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> save(@RequestBody com.nomelestar.productservice.dto.ProductRequest request) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(productService.save(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable String id,
            @RequestBody com.nomelestar.productservice.dto.ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/quantity")
    public ResponseEntity<Void> updateQuantity(@PathVariable String id,
            @org.springframework.web.bind.annotation.RequestParam Integer quantity) {
        productService.updateQuantity(id, quantity);
        return ResponseEntity.noContent().build();
    }
}

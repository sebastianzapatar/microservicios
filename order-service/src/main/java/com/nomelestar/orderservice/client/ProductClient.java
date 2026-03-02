package com.nomelestar.orderservice.client;

import com.nomelestar.orderservice.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service", url = "${product-service.url:http://localhost:8080}")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductResponse getProductById(@PathVariable("id") String id);

    @PatchMapping("/api/products/{id}/quantity")
    void updateProductQuantity(@PathVariable("id") String id, @RequestParam("quantity") Integer quantity);
}

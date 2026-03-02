package com.nomelestar.productservice.repository;

import com.nomelestar.productservice.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    @Query("SELECT p from products p WHERE p.active=true AND p.quantity>0 and " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%',:keyword,'%') )") // mi consulta personalizada
    List<Product> searchProducts(@Param("keyword") String keyword);

    Optional<Product> findByIdAndActiveTrue(String id);

    Optional<Product> findById(String id);
}

package com.cookie.app.repository;

import com.cookie.app.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long> {
    @Query(value = "SELECT p.* FROM PRODUCT p WHERE LOWER(p.product_name) LIKE LOWER(:productName) AND LOWER(p.category) LIKE LOWER(:category)", nativeQuery = true)
    Optional<Product> findByProductNameAndCategory(@Param("productName") String productName, @Param("category") String category);

    @Query(value = "SELECT p.* FROM PRODUCT p WHERE LOWER(p.product_name) LIKE LOWER(CONCAT(:productName, '%'))", nativeQuery = true)
    Page<Product> findProductsWithFilter(@Param("productName") String productName, PageRequest pageable);
}

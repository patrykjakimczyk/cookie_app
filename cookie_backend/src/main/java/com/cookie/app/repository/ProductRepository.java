package com.cookie.app.repository;

import com.cookie.app.model.entity.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long> {
    @Query(value = "SELECT p.* FROM PRODUCT p WHERE LOWER(p.product_name) LIKE LOWER(:productName)", nativeQuery = true)
    Optional<Product> findByProductName(@Param("productName") String productName);

    @Query(value = "SELECT p.* FROM PRODUCT p WHERE LOWER(p.product_name) LIKE LOWER(CONCAT(:productName, '%'))", nativeQuery = true)
    List<Product> findProductsWithFilter(@Param("productName") String productName);
}

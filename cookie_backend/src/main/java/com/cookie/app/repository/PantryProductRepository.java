package com.cookie.app.repository;

import com.cookie.app.model.entity.PantryProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PantryProductRepository extends CrudRepository<PantryProduct, Long> {
    void deleteByIdIn(List<Long> ids);

    @Query(value = "SELECT DISTINCT pp.* FROM pantry_product pp JOIN product p ON p.id = pp.product_id " +
            "where pp.pantry_id = ?1", nativeQuery = true)
    Page<PantryProduct> findProductsInPantry(long id, PageRequest pageable);

    @Query(value = "SELECT DISTINCT pp.* FROM pantry_product pp JOIN product p ON p.id = pp.product_id " +
            "WHERE pp.pantry_id = ?1 AND (LOWER(pp.placement) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
            "LOWER(p.product_name) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', ?2, '%')))", nativeQuery = true)
    Page<PantryProduct> findProductsInPantryWithFilter(long id, String filterValue, PageRequest pageable);
}

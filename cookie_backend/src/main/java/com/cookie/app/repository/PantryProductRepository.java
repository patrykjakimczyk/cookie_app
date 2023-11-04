package com.cookie.app.repository;

import com.cookie.app.model.entity.PantryProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PantryProductRepository extends CrudRepository<PantryProduct, Long> {
    @Query(value = "SELECT DISTINCT pp.* FROM pantry_product pp where pp.pantry_id = :id", nativeQuery = true)
    Page<PantryProduct> findProductsInPantry(@Param("id") long id, PageRequest pageable);

    @Query(value = "SELECT DISTINCT pp.* FROM pantry_product pp WHERE pp.pantry_id = :id AND :column LIKE LOWER(CONCAT('%', :filterValue, '%'))", nativeQuery = true)
    Page<PantryProduct> findProductsInPantryWithFilter(@Param("id") long id, @Param("column") String column, @Param("filterValue") String filterValue, PageRequest pageable);
}

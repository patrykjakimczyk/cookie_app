package com.cookie.app.repository;

import com.cookie.app.model.entity.PantryProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PantryProductRepository extends CrudRepository<PantryProduct, Long> {
    @Query(value = "SELECT pp.* FROM pantry_product pp where pp.id = :id")
    Page<PantryProduct> findProductsInPantry(@Param("id") long id, PageRequest pageable);
}

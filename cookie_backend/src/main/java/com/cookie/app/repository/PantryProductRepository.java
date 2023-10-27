package com.cookie.app.repository;

import com.cookie.app.model.entity.PantryProduct;
import org.springframework.data.repository.CrudRepository;

public interface PantryProductRepository extends CrudRepository<PantryProduct, Long> {
}

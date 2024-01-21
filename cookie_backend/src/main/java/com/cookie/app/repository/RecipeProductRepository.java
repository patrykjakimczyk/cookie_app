package com.cookie.app.repository;

import com.cookie.app.model.entity.RecipeProduct;
import org.springframework.data.repository.CrudRepository;

public interface RecipeProductRepository extends CrudRepository<RecipeProduct, Long> {
}

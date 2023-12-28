package com.cookie.app.repository;

import com.cookie.app.model.entity.ShoppingListProduct;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingListProductRepository extends CrudRepository<ShoppingListProduct, Long> {
}

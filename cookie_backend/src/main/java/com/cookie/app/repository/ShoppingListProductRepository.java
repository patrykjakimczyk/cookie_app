package com.cookie.app.repository;

import com.cookie.app.model.entity.PantryProduct;
import com.cookie.app.model.entity.ShoppingListProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingListProductRepository extends CrudRepository<ShoppingListProduct, Long> {
    @Query(value = "SELECT DISTINCT slp.* FROM shopping_list_product slp JOIN product p ON p.id = slp.product_id " +
            "where pp.pantry_id = ?1", nativeQuery = true)
    Page<ShoppingListProduct> findProductsInShoppingList(long id, PageRequest pageable);

    @Query(value = "SELECT DISTINCT slp.* FROM shopping_list_product slp JOIN product p ON p.id = slp.product_id " +
            "WHERE slp.pantry_id = ?1 AND (LOWER(slp.placement) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
            "LOWER(p.product_name) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', ?2, '%')))", nativeQuery = true)
    Page<ShoppingListProduct> findProductsInShoppingListWithFilter(long id, String filterValue, PageRequest pageable);
}

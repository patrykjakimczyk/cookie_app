package com.cookie.app.repository;

import com.cookie.app.model.entity.ShoppingListProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingListProductRepository extends CrudRepository<ShoppingListProduct, Long> {
    void deleteByIdIn(List<Long> ids);

    @Query(value = "SELECT DISTINCT slp.* FROM shopping_list_product slp JOIN product p ON p.id = slp.product_id " +
            "where slp.shopping_list_id = ?1", nativeQuery = true)
    Page<ShoppingListProduct> findProductsInShoppingList(long id, PageRequest pageable);

    @Query(value = "SELECT DISTINCT slp.* FROM shopping_list_product slp JOIN product p ON p.id = slp.product_id " +
            "WHERE slp.shopping_list_id = ?1 AND (LOWER(p.product_name) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', ?2, '%')))", nativeQuery = true)
    Page<ShoppingListProduct> findProductsInShoppingListWithFilter(long id, String filterValue, PageRequest pageable);
}

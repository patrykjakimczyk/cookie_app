package com.cookie.app.repository;

import com.cookie.app.model.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface RecipeRepository extends CrudRepository<Recipe, Long> {
    @Query(value = "SELECT DISTINCT r.* FROM recipe r" +
            "WHERE r.preparation_time <= CASE WHEN ?1 > 0 THEN ?1 ELSE 2880 END AND" +
            "r.portions LIKE CASE WHEN ?2 > 0 THEN ?2 ELSE 12", nativeQuery = true)
    Page<Recipe> findRecipes(int preparationTime, int portions, PageRequest pageable);

    @Query(value = "SELECT DISTINCT r.* FROM recipe r" +
            "WHERE (LOWER(r.recipe_name) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
            "LOWER(r.cuisine) LIKE LOWER(CONCAT('%', ?1, '%'))) AND" +
            "r.preparation_time <= CASE WHEN ?2 > 0 THEN ?2 ELSE 2880 END AND" +
            "r.portions LIKE CASE WHEN ?3 > 0 THEN ?3 ELSE 12", nativeQuery = true)
    Page<Recipe> findRecipesByFilter(String filterValue, int preparationTime, int portions, PageRequest pageable);
}

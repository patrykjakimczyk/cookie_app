package com.cookie.app.repository;

import com.cookie.app.model.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional
@Repository
public interface RecipeRepository extends CrudRepository<Recipe, Long> {
    @Query(value = "SELECT DISTINCT r.* FROM recipe r " +
            "WHERE r.preparation_time <= (CASE WHEN ?1 >= 5 THEN ?1 ELSE 2880 END) AND " +
            "r.portions <= (CASE WHEN ?2 >= 1 THEN ?2 ELSE 12 END) AND " +
            "r.meal_type IN ?3", nativeQuery = true)
    Page<Recipe> findRecipes(int preparationTime, int portions, Set<String> mealTypes, PageRequest pageable);

    @Query(value = "SELECT DISTINCT r.* FROM recipe r " +
            "WHERE (LOWER(r.recipe_name) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
            "LOWER(r.cuisine) LIKE LOWER(CONCAT('%', ?1, '%'))) AND " +
            "r.preparation_time <= (CASE WHEN ?2 >= 5 THEN ?2 ELSE 2880 END) AND " +
            "r.portions <= (CASE WHEN ?3 >= 1 THEN ?3 ELSE 12 END) AND " +
            "r.meal_type IN ?4", nativeQuery = true)
    Page<Recipe> findRecipesByFilter(String filterValue, int preparationTime, int portions, Set<String> mealTypes, PageRequest pageable);

    @Query(value = "SELECT DISTINCT r.* FROM recipe r " +
            "WHERE r.creator_id = ?1 AND r.preparation_time <= (CASE WHEN ?2 >= 5 THEN ?2 ELSE 2880 END) AND " +
            "r.portions <= (CASE WHEN ?3 >= 1 THEN ?3 ELSE 12 END) AND " +
            "r.meal_type IN ?4", nativeQuery = true)
    Page<Recipe> findUserRecipes(Long creatorId, int preparationTime, int portions, Set<String> mealTypes, PageRequest pageable);

    @Query(value = "SELECT DISTINCT r.* FROM recipe r " +
            "WHERE r.creator_id = ?1 AND (LOWER(r.recipe_name) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
            "LOWER(r.cuisine) LIKE LOWER(CONCAT('%', ?2, '%'))) AND " +
            "r.preparation_time <= (CASE WHEN ?3 >= 5 THEN ?3 ELSE 2880 END) AND " +
            "r.portions <= (CASE WHEN ?4 >= 1 THEN ?4 ELSE 12 END) AND " +
            "r.meal_type IN ?5", nativeQuery = true)
    Page<Recipe> findUserRecipesByFilter(Long creatorId, String filterValue, int preparationTime, int portions, Set<String> mealTypes, PageRequest pageable);
}


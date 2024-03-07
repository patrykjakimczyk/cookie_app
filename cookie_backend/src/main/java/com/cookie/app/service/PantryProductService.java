package com.cookie.app.service;

import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.entity.Pantry;
import com.cookie.app.model.entity.RecipeProduct;
import com.cookie.app.model.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PantryProductService {
    Page<PantryProductDTO> getPantryProducts(
            long pantryId,
            int page,
            String filterValue,
            String sortColName,
            String sortDirection,
            String userEmail
    );
    void addProductsToPantry(long pantryId, List<PantryProductDTO> pantryProductDTOS, String userEmail);
    void removeProductsFromPantry(long pantryId, List<Long> pantryProductIds, String userEmail);
    void modifyPantryProduct(long pantryId, PantryProductDTO pantryProduct, String userEmail);
    PantryProductDTO reservePantryProduct(long pantryId, long pantryProductId, int reserved, String userEmail);
    List<PantryProductDTO> reservePantryProductsFromRecipe(long pantryId, User user, List<RecipeProduct> recipeProducts);
    List<RecipeProduct> getRecipeProductsNotInPantry(Pantry pantry, List<RecipeProduct> recipeProducts);
}

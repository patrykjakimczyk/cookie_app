package com.cookie.app.service;

import com.cookie.app.model.dto.PageResult;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.entity.Pantry;
import com.cookie.app.model.entity.RecipeProduct;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.request.FilterRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface PantryProductService {
    PageResult<PantryProductDTO> getPantryProducts(
            long pantryId,
            int page,
            FilterRequest filterRequest,
            String userEmail
    );
    void addProductsToPantry(long pantryId, List<PantryProductDTO> pantryProducts, String userEmail);
    void addProductsToPantryFromList(Pantry pantry, List<PantryProductDTO> pantryProducts);
    void removeProductsFromPantry(long pantryId, List<Long> pantryProductsIds, String userEmail);
    void updatePantryProduct(long pantryId, PantryProductDTO pantryProductsToModify, String userEmail);
    PantryProductDTO reservePantryProduct(long pantryId, long pantryProductId, int reserved, String userEmail);
    List<RecipeProduct> reservePantryProductsFromRecipe(long pantryId, User user, List<RecipeProduct> recipeProducts);
    List<RecipeProduct> getRecipeProductsNotInPantry(Pantry pantry, List<RecipeProduct> recipeProducts);
}

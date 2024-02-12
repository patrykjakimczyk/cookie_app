package com.cookie.app.service;

import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.dto.RecipeDTO;
import com.cookie.app.model.dto.RecipeDetailsDTO;
import com.cookie.app.model.enums.MealType;
import com.cookie.app.model.request.CreateRecipeRequest;
import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.response.CreateRecipeResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface RecipeService {
    Page<RecipeDTO> getRecipes(int page, String filterValue, int prepTime, int portions, List<MealType> mealTypes, String sortColName, String sortDirection);
    Page<RecipeDTO> getUserRecipes(String userEmail, int page, String filterValue, int prepTime, int portions, List<MealType> mealTypes, String sortColName, String sortDirection);
    RecipeDetailsDTO getRecipeDetails(long recipeId);
    CreateRecipeResponse createRecipe(String userEmail, CreateRecipeRequest recipeDetailsDTO, MultipartFile recipeImage);
    void deleteRecipe(String userEmail, long recipeId);
    CreateRecipeResponse modifyRecipe(String userEmail, CreateRecipeRequest recipeDetailsDTO, MultipartFile recipeImage);
    List<PantryProductDTO> reserveRecipeProductsInPantry(String userEmail, long recipeId, long pantryId);
    List<ShoppingListProductDTO> addRecipeProductsToShoppingList(String userEmail, long recipeId, long listId, long groupId);

}

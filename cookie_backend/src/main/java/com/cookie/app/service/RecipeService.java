package com.cookie.app.service;

import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.dto.RecipeDTO;
import com.cookie.app.model.dto.RecipeDetailsDTO;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.Recipe;
import com.cookie.app.model.entity.RecipeProduct;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.MealType;
import com.cookie.app.model.request.CreateRecipeRequest;
import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.response.CreateRecipeResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RecipeService {
    Page<RecipeDTO> getRecipes(int page, String filterValue, int prepTime, int portions, List<MealType> mealTypes, String sortColName, String sortDirection);
    Page<RecipeDTO> getUserRecipes(String userEmail, int page, String filterValue, int prepTime, int portions, List<MealType> mealTypes, String sortColName, String sortDirection);
    RecipeDetailsDTO getRecipeDetails(long recipeId);
    CreateRecipeResponse createRecipe(String userEmail, CreateRecipeRequest recipeDetailsDTO, MultipartFile recipeImage);
    void deleteRecipe(String userEmail, long recipeId);
    CreateRecipeResponse modifyRecipe(String userEmail, CreateRecipeRequest recipeDetailsDTO, MultipartFile recipeImage);
    List<RecipeProduct> reserveRecipeProductsInPantry(User user, Recipe recipe, long pantryId);
    public List<RecipeProduct> getRecipeProductsNotInPantry(Group group, Recipe recipe);
    void addRecipeProductsToShoppingList(User user, long listId, List<RecipeProduct> productsToAdd);

}

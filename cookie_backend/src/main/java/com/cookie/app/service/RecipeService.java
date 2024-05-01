package com.cookie.app.service;

import com.cookie.app.model.dto.*;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.Recipe;
import com.cookie.app.model.entity.RecipeProduct;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.MealType;
import com.cookie.app.model.request.CreateRecipeRequest;
import com.cookie.app.model.request.UpdateRecipeRequest;
import com.cookie.app.model.response.CreateRecipeResponse;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RecipeService {
    PageResult<RecipeDTO> getRecipes(int page, Integer prepTime, Integer portions, List<MealType> mealTypes, String filterValue, String sortColName, Sort.Direction sortDirection);
    PageResult<RecipeDTO> getUserRecipes(String userEmail, int page, Integer prepTime, Integer portions, List<MealType> mealTypes, String filterValue, String sortColName, Sort.Direction sortDirection);
    RecipeDetailsDTO getRecipeDetails(long recipeId);
    CreateRecipeResponse createRecipe(String userEmail, CreateRecipeRequest recipeDetailsDTO, MultipartFile recipeImage);
    void deleteRecipe(String userEmail, long recipeId);
    CreateRecipeResponse updateRecipe(String userEmail, UpdateRecipeRequest recipeDetailsDTO, MultipartFile recipeImage);
    List<RecipeProduct> reserveRecipeProductsInPantry(User user, Recipe recipe, long pantryId);
    public List<RecipeProduct> getRecipeProductsNotInPantry(Group group, Recipe recipe);
    void addRecipeProductsToShoppingList(User user, long listId, List<RecipeProduct> productsToAdd);

}

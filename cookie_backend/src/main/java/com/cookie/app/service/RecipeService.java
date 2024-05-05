package com.cookie.app.service;

import com.cookie.app.model.dto.*;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.Recipe;
import com.cookie.app.model.entity.RecipeProduct;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.MealType;
import com.cookie.app.model.request.CreateRecipeRequest;
import com.cookie.app.model.request.RecipeFilterRequest;
import com.cookie.app.model.request.UpdateRecipeRequest;
import com.cookie.app.model.response.CreateRecipeResponse;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RecipeService {
    PageResult<RecipeDTO> getRecipes(int page, RecipeFilterRequest filterRequest);
    PageResult<RecipeDTO> getUserRecipes(String userEmail, int page, RecipeFilterRequest filterRequest);
    RecipeDetailsDTO getRecipeDetails(long recipeId);
    CreateRecipeResponse createRecipe(String userEmail, CreateRecipeRequest createRecipeRequest, MultipartFile recipeImage);
    void deleteRecipe(String userEmail, long recipeId);
    CreateRecipeResponse updateRecipe(String userEmail, UpdateRecipeRequest updateRecipeRequest, MultipartFile recipeImage);
    List<RecipeProduct> reserveRecipeProductsInPantry(User user, Recipe recipe, long pantryId);
    public List<RecipeProduct> getRecipeProductsNotInPantry(Group group, Recipe recipe);
    void addRecipeProductsToShoppingList(User user, long listId, List<RecipeProduct> productsToAdd);

}

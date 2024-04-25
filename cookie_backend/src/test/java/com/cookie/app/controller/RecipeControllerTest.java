package com.cookie.app.controller;

import com.cookie.app.exception.MappingJsonToObjectException;
import com.cookie.app.model.dto.*;
import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.MealType;
import com.cookie.app.model.enums.Unit;
import com.cookie.app.model.request.CreateRecipeRequest;
import com.cookie.app.model.request.UpdateRecipeRequest;
import com.cookie.app.model.response.CreateRecipeResponse;
import com.cookie.app.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest extends AbstractControllerTest {
    final long id = 1L;
    final String recipeName = "recipe name";
    final List<MealType> mealTypes = Collections.singletonList(MealType.APPETIZER);

    @Mock
    RecipeService recipeService;
    @InjectMocks
    RecipeController controller;

    @Test
    void test_getRecipesSuccessful() {
        final RecipeDTO recipeDTO = new RecipeDTO(id, recipeName, 5, MealType.APPETIZER, null, 1, null, username, 1);
        final List<RecipeDTO> foundRecipes = Collections.singletonList(recipeDTO);
        final PageResult<RecipeDTO> pageResponse = new PageResult<>(foundRecipes, foundRecipes.size(), 1, 0);

        doReturn(pageResponse).when(recipeService)
                .getRecipes(1, 5, 1, mealTypes, null, null, null);
        ResponseEntity<PageResult<RecipeDTO>> response = this.controller
                .getRecipes(1, 5, 1, mealTypes, null, null, null);

        assertEquals(foundRecipes.size(), response.getBody().totalElements());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_getUserRecipesSuccessful() {
        final RecipeDTO recipeDTO = new RecipeDTO(id, recipeName, 5, MealType.APPETIZER, null, 1, null, username, 1);
        final List<RecipeDTO> foundRecipes = Collections.singletonList(recipeDTO);
        final PageResult<RecipeDTO> pageResponse = new PageResult<>(foundRecipes, foundRecipes.size(), 1, 0);

        doReturn(pageResponse).when(recipeService)
                .getUserRecipes(authentication.getName(), 1, 5, 1, mealTypes, null, null, null);
        ResponseEntity<PageResult<RecipeDTO>> response = this.controller
                .getUserRecipes(1, 5, 1, mealTypes, null, null, null, authentication);

        assertEquals(foundRecipes.size(), response.getBody().totalElements());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_getRecipeDetailsSuccessful() {
        final RecipeDetailsDTO recipeDetailsDTO = new RecipeDetailsDTO(id, recipeName, "preparation",
                5, MealType.APPETIZER, null, 1, null, username, null);

        doReturn(recipeDetailsDTO).when(recipeService).getRecipeDetails(id);
        ResponseEntity<RecipeDetailsDTO> response = this.controller.getRecipeDetails(id);

        assertEquals(recipeDetailsDTO.id(), response.getBody().id());
        assertEquals(recipeDetailsDTO.recipeName(), response.getBody().recipeName());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_createRecipeSuccessful() throws IOException {
        final ProductDTO productDTO = new ProductDTO(id, "productName", Category.CEREAL);
        final RecipeProductDTO recipeProductDTO = new RecipeProductDTO(id, productDTO, 100, Unit.GRAMS);
        final CreateRecipeRequest request = new CreateRecipeRequest(recipeName, "preparationpreparationpreparation",
                5, MealType.APPETIZER, null, 1, false, Collections.singletonList(recipeProductDTO));
        final CreateRecipeResponse createRecipeResponse = new CreateRecipeResponse(id);
        ObjectMapper objectMapper = new ObjectMapper();

        doReturn(createRecipeResponse).when(recipeService).createRecipe(authentication.getName(), request, null);
        ResponseEntity<CreateRecipeResponse> response = this.controller.createRecipe(null, objectMapper.writeValueAsString(request), authentication);

        assertEquals(createRecipeResponse.recipeId(), response.getBody().recipeId());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void test_createRecipeIncorrectRequestBodyStructure() {

        assertThrows(MappingJsonToObjectException.class, () ->
                this.controller.createRecipe(null, "requestbody", authentication));
    }

    @Test
    void test_createRecipeInvalidRequestBodyData() throws IOException {
        final ProductDTO productDTO = new ProductDTO(id, "productName", Category.CEREAL);
        final RecipeProductDTO recipeProductDTO = new RecipeProductDTO(id, productDTO, 100, Unit.GRAMS);
        final CreateRecipeRequest request = new CreateRecipeRequest(recipeName, "tooshort",
                5, MealType.APPETIZER, null, 1, false, Collections.singletonList(recipeProductDTO));
        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(ConstraintViolationException.class, () ->
                this.controller.createRecipe(null, objectMapper.writeValueAsString(request), authentication));
    }

    @Test
    void test_deleteRecipeSuccessful() {

        doNothing().when(recipeService).deleteRecipe(authentication.getName(), id);
        ResponseEntity<Void> response = this.controller.deleteRecipe(id, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_updateRecipeSuccessful() throws IOException {
        final ProductDTO productDTO = new ProductDTO(id, "productName", Category.CEREAL);
        final RecipeProductDTO recipeProductDTO = new RecipeProductDTO(id, productDTO, 100, Unit.GRAMS);
        final UpdateRecipeRequest request = new UpdateRecipeRequest(id, recipeName, "preparationpreparationpreparation",
                5, MealType.APPETIZER, null, 1, false, Collections.singletonList(recipeProductDTO));
        final CreateRecipeResponse createRecipeResponse = new CreateRecipeResponse(id);
        ObjectMapper objectMapper = new ObjectMapper();

        doReturn(createRecipeResponse).when(recipeService).updateRecipe(authentication.getName(), request, null);
        ResponseEntity<CreateRecipeResponse> response = this.controller.updateRecipe(null, objectMapper.writeValueAsString(request), authentication);

        assertEquals(createRecipeResponse.recipeId(), response.getBody().recipeId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_updateRecipeIncorrectRequestBodyStructure() {

        assertThrows(MappingJsonToObjectException.class, () -> this.controller.updateRecipe(null, "requestbody", authentication));
    }

    @Test
    void test_updateRecipeInvalidRequestBodyData() throws IOException {
        final ProductDTO productDTO = new ProductDTO(id, "productName", Category.CEREAL);
        final RecipeProductDTO recipeProductDTO = new RecipeProductDTO(id, productDTO, 100, Unit.GRAMS);
        final UpdateRecipeRequest request = new UpdateRecipeRequest(id, recipeName, "tooshort",
                5, MealType.APPETIZER, null, 1, false, Collections.singletonList(recipeProductDTO));
        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(ConstraintViolationException.class, () ->
                this.controller.updateRecipe(null, objectMapper.writeValueAsString(request), authentication));
    }
}

package com.cookie.app.controller;

import com.cookie.app.exception.MappingJsonToObjectException;
import com.cookie.app.model.dto.*;
import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.MealType;
import com.cookie.app.model.enums.Unit;
import com.cookie.app.model.request.CreateRecipeRequest;
import com.cookie.app.model.request.RecipeFilterRequest;
import com.cookie.app.model.request.UpdateRecipeRequest;
import com.cookie.app.model.response.CreateRecipeResponse;
import com.cookie.app.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest extends AbstractControllerTest {
    final long id = 1L;
    final String recipeName = "recipe name";
    final List<MealType> mealTypes = Collections.singletonList(MealType.APPETIZER);

    @Spy
    ObjectMapper objectMapper;
    @Mock
    RecipeService recipeService;
    @InjectMocks
    RecipeController controller;

    @Test
    void test_getRecipesSuccessful() {
        final RecipeFilterRequest filterRequest = new RecipeFilterRequest(null, null, null, 5, 1, mealTypes);
        final RecipeDTO recipeDTO = new RecipeDTO(id, recipeName, 5, MealType.APPETIZER, null, 1, null, username, 1);
        final List<RecipeDTO> foundRecipes = Collections.singletonList(recipeDTO);
        final PageResult<RecipeDTO> pageResponse = new PageResult<>(foundRecipes, foundRecipes.size(), 1, 0);

        doReturn(pageResponse).when(recipeService).getRecipes(1, filterRequest);
        ResponseEntity<PageResult<RecipeDTO>> response = this.controller.getRecipes(1, filterRequest);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalElements()).isEqualTo(foundRecipes.size());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test_getUserRecipesSuccessful() {
        final RecipeFilterRequest filterRequest = new RecipeFilterRequest(null, null, null, 5, 1, mealTypes);
        final RecipeDTO recipeDTO = new RecipeDTO(id, recipeName, 5, MealType.APPETIZER, null, 1, null, username, 1);
        final List<RecipeDTO> foundRecipes = Collections.singletonList(recipeDTO);
        final PageResult<RecipeDTO> pageResponse = new PageResult<>(foundRecipes, foundRecipes.size(), 1, 0);

        doReturn(pageResponse).when(recipeService).getUserRecipes(authentication.getName(), 1, filterRequest);
        ResponseEntity<PageResult<RecipeDTO>> response = this.controller.getUserRecipes(1, filterRequest, authentication);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalElements()).isEqualTo(foundRecipes.size());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test_getRecipeDetailsSuccessful() {
        final RecipeDetailsDTO recipeDetailsDTO = new RecipeDetailsDTO(id, recipeName, "preparation",
                5, MealType.APPETIZER, null, 1, null, username, null);

        doReturn(recipeDetailsDTO).when(recipeService).getRecipeDetails(id);
        ResponseEntity<RecipeDetailsDTO> response = this.controller.getRecipeDetails(id);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(recipeDetailsDTO.id());
        assertThat(response.getBody().recipeName()).isEqualTo(recipeDetailsDTO.recipeName());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().recipeId()).isEqualTo(createRecipeResponse.recipeId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void test_createRecipeIncorrectRequestBodyStructure() {
        assertThatThrownBy(() -> this.controller.createRecipe(null, "requestbody", authentication))
                .isInstanceOf(MappingJsonToObjectException.class);
    }

    @Test
    void test_createRecipeInvalidRequestBodyData() throws IOException {
        final ProductDTO productDTO = new ProductDTO(id, "productName", Category.CEREAL);
        final RecipeProductDTO recipeProductDTO = new RecipeProductDTO(id, productDTO, 100, Unit.GRAMS);
        final CreateRecipeRequest request = new CreateRecipeRequest(recipeName, "tooshort",
                5, MealType.APPETIZER, null, 1, false, Collections.singletonList(recipeProductDTO));
        ObjectMapper objectMapper = new ObjectMapper();

        assertThatThrownBy(() -> this.controller.createRecipe(null, objectMapper.writeValueAsString(request), authentication))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void test_deleteRecipeSuccessful() {
        doNothing().when(recipeService).deleteRecipe(authentication.getName(), id);
        ResponseEntity<Void> response = this.controller.deleteRecipe(id, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().recipeId()).isEqualTo(createRecipeResponse.recipeId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test_updateRecipeIncorrectRequestBodyStructure() {
        assertThatThrownBy(() -> this.controller.updateRecipe(null, "requestbody", authentication))
                .isInstanceOf(MappingJsonToObjectException.class);
    }

    @Test
    void test_updateRecipeInvalidRequestBodyData() throws IOException {
        final ProductDTO productDTO = new ProductDTO(id, "productName", Category.CEREAL);
        final RecipeProductDTO recipeProductDTO = new RecipeProductDTO(id, productDTO, 100, Unit.GRAMS);
        final UpdateRecipeRequest request = new UpdateRecipeRequest(id, recipeName, "tooshort",
                5, MealType.APPETIZER, null, 1, false, Collections.singletonList(recipeProductDTO));
        ObjectMapper objectMapper = new ObjectMapper();

        assertThatThrownBy(() -> this.controller.updateRecipe(null, objectMapper.writeValueAsString(request), authentication))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
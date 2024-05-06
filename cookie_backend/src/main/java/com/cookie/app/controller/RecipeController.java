package com.cookie.app.controller;

import com.cookie.app.exception.MappingJsonToObjectException;
import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.dto.PageResult;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.dto.RecipeDTO;
import com.cookie.app.model.dto.RecipeDetailsDTO;
import com.cookie.app.model.enums.MealType;
import com.cookie.app.model.request.CreateRecipeRequest;
import com.cookie.app.model.request.FilterRequest;
import com.cookie.app.model.request.RecipeFilterRequest;
import com.cookie.app.model.request.UpdateRecipeRequest;
import com.cookie.app.model.response.CreateRecipeResponse;
import com.cookie.app.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;


@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/v1/recipes", produces = { MediaType.APPLICATION_JSON_VALUE })
@Validated
@RestController
public class RecipeController {
    private static final String RECIPES_ID_URL = "/{recipeId}";
    private static final String GET_RECIPES_URL = "/page/{page}";
    private static final String GET_USER_RECIPES_URL = "/user-recipes/{page}";

    private final RecipeService recipeService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Get recipes")
    @ApiResponse(responseCode = "200", description = "Recipes returned",
            content = { @Content(mediaType = "application/json") })
    @GetMapping(GET_RECIPES_URL)
    public ResponseEntity<PageResult<RecipeDTO>> getRecipes(
            @PathVariable @Positive(message = "Page nr must be greater than 0") int page,
            @ParameterObject RecipeFilterRequest filterRequest
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(this.recipeService.getRecipes(page, filterRequest));
    }

    @Operation(summary = "Get user's recipes")
    @ApiResponse(responseCode = "200", description = "User's recipes returned",
            content = { @Content(mediaType = "application/json") })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(GET_USER_RECIPES_URL)
    public ResponseEntity<PageResult<RecipeDTO>> getUserRecipes(
            @PathVariable @Positive(message = "Page nr must be greater than 0") int page,
            @ParameterObject RecipeFilterRequest filterRequest,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                this.recipeService.getUserRecipes(authentication.getName(), page, filterRequest)
        );
    }

    @Operation(summary = "Get recipe details")
    @ApiResponse(responseCode = "200", description = "Recipe details returned",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = RecipeDetailsDTO.class)) })
    @GetMapping(RECIPES_ID_URL)
    public ResponseEntity<RecipeDetailsDTO> getRecipeDetails(
            @PathVariable@Positive(message = "Recipe id must be greater than 0") long recipeId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.recipeService.getRecipeDetails(recipeId));
    }

    @Operation(summary = "Create recipe")
    @ApiResponse(responseCode = "201", description = "Recipe created",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CreateRecipeResponse.class)) })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateRecipeResponse> createRecipe(
            @RequestPart(value = "image", required = false) MultipartFile recipeImage,
            @RequestPart("recipe") String recipeJson,
            Authentication authentication
    ) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        log.info("Performing recipe creation by user with email={}", authentication.getName());
        CreateRecipeRequest recipe;

        try {
            recipe = this.objectMapper.readValue(recipeJson, CreateRecipeRequest.class);
        } catch (IOException exception) {
            throw new MappingJsonToObjectException("An error occured during request body reading. Its structure is probably incorrect");
        }

        Set<ConstraintViolation<CreateRecipeRequest>> violations = validator.validate(recipe);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.recipeService.createRecipe(authentication.getName(), recipe, recipeImage));
    }

    @Operation(summary = "Delete recipe")
    @ApiResponse(responseCode = "200", description = "Recipe deleted",
            content = { @Content(mediaType = "application/json") })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(RECIPES_ID_URL)
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable @Positive(message = "Recipe id must be greater than 0") long recipeId,
            Authentication authentication
    ) {
        log.info("Performing recipe deletion by user with email={}", authentication.getName());
        this.recipeService.deleteRecipe(authentication.getName(), recipeId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Update recipe")
    @ApiResponse(responseCode = "200", description = "Recipe updated",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CreateRecipeResponse.class)) })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateRecipeResponse> updateRecipe(
            @RequestPart(value = "image", required = false) MultipartFile recipeImage,
            @RequestPart("recipe") String recipeJson,
            Authentication authentication
    ) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        log.info("Performing recipe update by user with email={}", authentication.getName());
        UpdateRecipeRequest recipe;

        try {
            recipe = this.objectMapper.readValue(recipeJson, UpdateRecipeRequest.class);
        } catch (IOException exception) {
            throw new MappingJsonToObjectException("An error occured during request body reading. Its structure is probably incorrect");
        }

        Set<ConstraintViolation<UpdateRecipeRequest>> violations = validator.validate( recipe );

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(this.recipeService.updateRecipe(authentication.getName(), recipe, recipeImage));
    }
}

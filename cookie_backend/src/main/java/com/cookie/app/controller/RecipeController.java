package com.cookie.app.controller;

import com.cookie.app.exception.MappingJsonToObjectException;
import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.dto.PageResult;
import com.cookie.app.model.dto.RecipeDTO;
import com.cookie.app.model.dto.RecipeDetailsDTO;
import com.cookie.app.model.enums.MealType;
import com.cookie.app.model.request.CreateRecipeRequest;
import com.cookie.app.model.request.UpdateRecipeRequest;
import com.cookie.app.model.response.CreateRecipeResponse;
import com.cookie.app.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping(GET_RECIPES_URL)
    public ResponseEntity<PageResult<RecipeDTO>> getRecipes(
            @PathVariable @Positive(message = "Page nr must be greater than 0") int page,
            @RequestParam(required = false) @Min(value = 5, message = "Preparation time must be at least 5 minutes")
            @Max(value = 2880, message = "Preparation time must be lower or equals 2880 minutes") Integer prepTime,
            @RequestParam(required = false) @Positive(message = "Number of portions must be at least 1")
            @Max(value = 12, message = "Number of portions must be lower or equals 12") Integer portions,
            @RequestParam(required = false) List<MealType> mealTypes,
            @RequestParam(required = false) @Pattern(
                    regexp = RegexConstants.FILTER_VALUE_REGEX,
                    message = "Filter value can only contains letters, digits, whitespaces, dashes and its length must be greater than 0"
            ) String filterValue,
            @RequestParam(required = false) @Pattern(
                    regexp = RegexConstants.SORT_COL_REGEX,
                    message = "Filter value can only contains letters, underscores and its length must be greater than 0"
            ) String sortColName,
            @RequestParam(required = false) Sort.Direction sortDirection
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                this.recipeService.getRecipes(page, prepTime, portions, mealTypes, filterValue, sortColName, sortDirection)
        );
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(GET_USER_RECIPES_URL)
    public ResponseEntity<PageResult<RecipeDTO>> getUserRecipes(
            @PathVariable @Positive(message = "Page nr must be greater than 0") int page,
            @RequestParam(required = false) @Min(value = 5, message = "Preparation time must be at least 5 minutes")
            @Max(value = 2880, message = "Preparation time must be lower or equals 2880 minutes") Integer prepTime,
            @RequestParam(required = false) @Positive(message = "Number of portions must be at least 1")
            @Max(value = 12, message = "Number of portions must be lower or equals 12") Integer portions,
            @RequestParam(required = false) List<MealType> mealTypes,
            @RequestParam(required = false) @Pattern(
                    regexp = RegexConstants.FILTER_VALUE_REGEX,
                    message = "Filter value can only contains letters, digits, whitespaces, dashes and its length must be greater than 0"
            ) String filterValue,
            @RequestParam(required = false) @Pattern(
                    regexp = RegexConstants.SORT_COL_REGEX,
                    message = "Filter value can only contains letters, underscores and its length must be greater than 0"
            ) String sortColName,
            @RequestParam(required = false) Sort.Direction sortDirection,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                this.recipeService.getUserRecipes(
                        authentication.getName(),
                        page, prepTime, portions,
                        mealTypes, filterValue, sortColName, sortDirection
                )
        );
    }

    @GetMapping(RECIPES_ID_URL)
    public ResponseEntity<RecipeDetailsDTO> getRecipeDetails(
            @PathVariable@Positive(message = "Recipe id must be greater than 0") long recipeId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.recipeService.getRecipeDetails(recipeId));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateRecipeResponse> createRecipe(
            @RequestPart(value = "image", required = false) MultipartFile recipeImage,
            @RequestPart("recipe") String recipeJson,
            Authentication authentication
    ) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        log.info("Performing recipe creation by creator with email={}", authentication.getName());
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

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(RECIPES_ID_URL)
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable @Positive(message = "Recipe id must be greater than 0") long recipeId,
            Authentication authentication
    ) {
        log.info("Performing recipe deletion by creator with email={}", authentication.getName());
        this.recipeService.deleteRecipe(authentication.getName(), recipeId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateRecipeResponse> updateRecipe(
            @RequestPart(value = "image", required = false) MultipartFile recipeImage,
            @RequestPart("recipe") String recipeJson,
            Authentication authentication
    ) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        log.info("Performing recipe update by creator with email={}", authentication.getName());
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

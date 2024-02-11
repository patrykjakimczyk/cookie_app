package com.cookie.app.controller;

import com.cookie.app.model.dto.RecipeDTO;
import com.cookie.app.model.dto.RecipeDetailsDTO;
import com.cookie.app.model.dto.RecipeProductDTO;
import com.cookie.app.model.request.CreateRecipeRequest;
import com.cookie.app.model.response.CreateRecipeResponse;
import com.cookie.app.service.RecipeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Validated
@RestController
public class RecipeController {
    private static final String RECIPES_URL = "/recipes";
    private static final String RECIPES_ID_URL = "/recipes/{id}";
    private static final String GET_RECIPES_URL = "/recipes/page/{page}";
    private static final String GET_USER_RECIPES_URL = "/recipes/user-recipes/{page}";
    private final RecipeService recipeService;

    @GetMapping(GET_RECIPES_URL)
    public ResponseEntity<Page<RecipeDTO>> getRecipes(
            @PathVariable(value = "page") @Valid @Min(value = 0, message = "Page nr must be at least 0") int page,
            @RequestParam String filterValue,
            @RequestParam @Valid @Min(value = 0, message = "Preparation be at least 0")
            @Max(value = 2880, message = "Preparation time must be lower or equals 2880 minutes") int prepTime,
            @RequestParam @Valid @Min(value = 0, message = "Nr of portions must be at least 0")
            @Max(value = 12, message = "Nr of portions must be lower or equals 12") int portions,
            @RequestParam String sortColName,
            @RequestParam String sortDirection
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                this.recipeService.getRecipes(page, filterValue, prepTime, portions, sortColName, sortDirection)
        );
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(GET_USER_RECIPES_URL)
    public ResponseEntity<Page<RecipeDTO>> getUserRecipes(
            @PathVariable(value = "page") @Valid @Min(value = 0, message = "Page nr must be at least 0") int page,
            @RequestParam String filterValue,
            @RequestParam @Valid @Min(value = 0, message = "Preparation be at least 0")
            @Max(value = 2880, message = "Preparation time must be lower or equals 2880 minutes") int prepTime,
            @RequestParam @Valid @Min(value = 0, message = "Nr of portions must be at least 0")
            @Max(value = 12, message = "Nr of portions must be lower or equals 12") int portions,
            @RequestParam String sortColName,
            @RequestParam String sortDirection,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                this.recipeService.getUserRecipes(
                        authentication.getName(),
                        page, filterValue, prepTime, portions,
                        sortColName, sortDirection
                )
        );
    }

    @GetMapping(RECIPES_ID_URL)
    public ResponseEntity<RecipeDetailsDTO> getRecipeDetails(
            @PathVariable("id") @Valid @Min(value = 1, message = "Id must be greater than 0") long recipeId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.recipeService.getRecipeDetails(recipeId));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = RECIPES_URL, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateRecipeResponse> createRecipe(
            @RequestPart("image") MultipartFile recipeImage,
            @RequestPart("jsonString") String jsonString,
            Authentication authentication
    ) throws IOException {
        log.info("Performing recipe creation by user with email {}", authentication.getName());
        ObjectMapper objMapper = new ObjectMapper();
        CreateRecipeRequest recipe = objMapper.readValue(jsonString, CreateRecipeRequest.class);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.recipeService.createRecipe(authentication.getName(), recipe, recipeImage));
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(RECIPES_ID_URL)
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable("id") @Valid @Min(value = 1, message = "Id must be greater than 0") long recipeId,
            Authentication authentication
    ) {
        log.info("Performing recipe deletion by user with email {}", authentication.getName());
        this.recipeService.deleteRecipe(authentication.getName(), recipeId);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(value = RECIPES_URL, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateRecipeResponse> updateRecipe(
            @RequestPart("image") MultipartFile recipeImage,
            @RequestPart("jsonString") String jsonString,
            Authentication authentication
    ) throws JsonProcessingException {
        log.info("Performing recipe update by user with email {}", authentication.getName());
        ObjectMapper objMapper = new ObjectMapper();
        CreateRecipeRequest recipe = objMapper.readValue(jsonString, CreateRecipeRequest.class);

        return ResponseEntity.status(HttpStatus.OK)
                .body(this.recipeService.modifyRecipe(authentication.getName(), recipe, recipeImage));
    }
}

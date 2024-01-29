package com.cookie.app.controller;

import com.cookie.app.model.dto.RecipeDTO;
import com.cookie.app.model.dto.RecipeDetailsDTO;
import com.cookie.app.service.RecipeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@Validated
@RestController
public class RecipeController {
    private static final String RECIPES_URL = "/recipes";
    private static final String RECIPES_ID_URL = "/recipes/{id}";
    private static final String GET_RECIPES_URL = "/recipes/{page}";
    private static final String GET_USER_RECIPES_URL = "/recipes/user-recipes/{page}";
    private final RecipeService recipeService;

    @GetMapping(GET_RECIPES_URL)
    public ResponseEntity<Page<RecipeDTO>> getRecipes(
            @PathVariable(value = "page") @Valid @Min(value = 0, message = "Page nr must be at least 0") int page,
            @RequestParam String filterValue,
            @RequestParam @Valid @Size(max = 2880, message = "Preparation time must be between 0 and 2880 minutes") int prepTime,
            @RequestParam @Valid @Size(max = 12, message = "Nr of portions must be between 0 and 12") int portions,
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
            @RequestParam @Valid @Size(min = 5, max = 2880, message = "Preparation time must be between 5 and 2880 minutes") int prepTime,
            @RequestParam @Valid @Size(min = 1, max = 12, message = "Nr of portions must be between 1 and 12") int portions,
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
    @PostMapping(RECIPES_URL)
    public ResponseEntity<RecipeDetailsDTO> createRecipe(
            @RequestBody @Valid RecipeDetailsDTO recipe,
            Authentication authentication
    ) {
        log.info("Performing recipe creation by user with email {}", authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(this.recipeService.createRecipe(authentication.getName(), recipe));
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
    @PatchMapping(RECIPES_URL)
    public ResponseEntity<RecipeDetailsDTO> updateRecipe(
            @RequestBody @Valid RecipeDetailsDTO recipe,
            Authentication authentication
    ) {
        log.info("Performing recipe update by user with email {}", authentication.getName());

        return ResponseEntity.status(HttpStatus.OK).body(this.recipeService.modifyRecipe(authentication.getName(), recipe));
    }
}

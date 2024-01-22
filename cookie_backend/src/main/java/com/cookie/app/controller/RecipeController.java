package com.cookie.app.controller;

import com.cookie.app.model.dto.RecipeDTO;
import com.cookie.app.model.dto.RecipeDetailsDTO;
import com.cookie.app.service.RecipeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
public class RecipeController {
    private static final String RECIPES_URL = "/recipes";
    private static final String RECIPES_ID_URL = "/recipes/{id}";
    private static final String GET_RECIPES_URL = "/recipes/{page}";
    private final RecipeService recipeService;

    @GetMapping(GET_RECIPES_URL)
    public ResponseEntity<Page<RecipeDTO>> getRecipes(
            @PathVariable(value = "page") @Valid @Min(0) int page,
            @RequestParam String filterValue,
            @RequestParam @Valid @Size(max = 2880) int prepTime,
            @RequestParam @Valid @Size(max = 12) int portions,
            @RequestParam String sortColName,
            @RequestParam String sortDirection
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                this.recipeService.getRecipes(page, filterValue, prepTime, portions, sortColName, sortDirection)
        );
    }

    @GetMapping(RECIPES_ID_URL)
    public ResponseEntity<RecipeDetailsDTO> getRecipeDetails(
            @PathVariable("id") @Valid @Min(value = 1, message = "Id must be greater than 0") long recipeId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.recipeService.getRecipeDetails(recipeId));
    }
}

package com.cookie.app.controller;

import com.cookie.app.model.dto.MealDTO;
import com.cookie.app.model.request.AddMealRequest;
import com.cookie.app.service.MealService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/meals")
@Validated
@RestController
public class MealController {
    private static final String MEALS_ID_URL = "/{mealId}";

    private final MealService mealService;

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<MealDTO>> getMealsForUser(
            @RequestParam LocalDateTime dateAfter,
            @RequestParam LocalDateTime dateBefore,
            Authentication authentication
    ) {
        return ResponseEntity.ok(this.mealService.getMealsForUser(dateAfter, dateBefore, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<MealDTO> addMeal(
            @RequestParam boolean reserve,
            @RequestParam(required = false) @Positive(message = "List id must be greater than 0") Long listId,
            @RequestBody @Valid AddMealRequest request,
            Authentication authentication
    ) {
        log.info("User with email={} is adding meal for group with id={}", authentication.getName(), request.groupId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.mealService.addMeal(request, authentication.getName(), reserve, listId));
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(MEALS_ID_URL)
    public ResponseEntity<Void> deleteMeal(
            @PathVariable @Positive(message = "Meal id must be greater than 0") long mealId,
            Authentication authentication
    ) {
        log.info("User with email={} is deleting meal with id={}", authentication.getName(), mealId);
        this.mealService.deleteMeal(mealId, authentication.getName());

        return ResponseEntity.ok().build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(MEALS_ID_URL)
    public ResponseEntity<MealDTO> updateMeal(
            @PathVariable @Positive(message = "Meal id must be greater than 0") long mealId,
            @RequestBody @Valid AddMealRequest request,
            Authentication authentication
    ) {
        log.info("User with email={} is modifying meal with id={}", authentication.getName(), mealId);

        return ResponseEntity.ok(this.mealService.updateMeal(mealId, request, authentication.getName()));
    }
}

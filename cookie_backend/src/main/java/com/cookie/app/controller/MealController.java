package com.cookie.app.controller;

import com.cookie.app.model.dto.MealDTO;
import com.cookie.app.model.request.AddMealRequest;
import com.cookie.app.service.MealService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
public class MealController {
    private static final String MEALS_URL = "/meals";
    private static final String MEALS_ID_URL = "/meals/{id}";
    private final MealService mealService;

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(MEALS_URL)
    public ResponseEntity<List<MealDTO>> getMealsForUser(
            @RequestParam Timestamp dateAfter,
            @RequestParam Timestamp dateBefore,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.mealService.getMealsForUser(dateAfter, dateBefore, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(MEALS_URL)
    public ResponseEntity<MealDTO> addMeal(
            @RequestParam("reserve") boolean reserve,
            @RequestParam(value = "listId", required = false) @Min(value = 1, message = "List id must be greater than 0") Long listId,
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
            @PathVariable("id") @Valid @Min(value = 1, message = "Id must be greater than 0") long mealId,
            Authentication authentication
    ) {
        log.info("User with email={} is deleting meal with id={}", authentication.getName(), mealId);
        this.mealService.deleteMeal(mealId, authentication.getName());

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(MEALS_ID_URL)
    public ResponseEntity<MealDTO> modifyMeal(
            @PathVariable("id") @Valid @Min(value = 1, message = "Id must be greater than 0") long mealId,
            @RequestBody @Valid AddMealRequest request,
            Authentication authentication
    ) {
        log.info("User with email={} is deleting meal with id={}", authentication.getName(), mealId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(this.mealService.modifyMeal(mealId, request, authentication.getName()));
    }
}

package com.cookie.app.service;

import com.cookie.app.model.dto.MealDTO;
import com.cookie.app.model.request.AddMealRequest;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public interface MealService {
    List<MealDTO> getMealsForUser(LocalDateTime dateAfter, LocalDateTime dateBefore, String userEmail);
    MealDTO addMeal(AddMealRequest request, String userEmail, boolean reserve, @Nullable Long listId);
    void deleteMeal(long mealId, String userEmail);
    MealDTO updateMeal(long mealId, AddMealRequest request, String userEmail);
}

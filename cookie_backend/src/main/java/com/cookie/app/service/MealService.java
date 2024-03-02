package com.cookie.app.service;

import com.cookie.app.model.dto.MealDTO;
import com.cookie.app.model.request.AddMealRequest;

import java.sql.Timestamp;
import java.util.List;

public interface MealService {
    List<MealDTO> getMealsForUser(Timestamp dateAfter, Timestamp dateBefore, String userEmail);
    MealDTO addMeal(AddMealRequest request, String userEmail);
    void deleteMeal(long mealId, String userEmail);
    void modifyMeal(long mealId, AddMealRequest request, String userEmail);
}

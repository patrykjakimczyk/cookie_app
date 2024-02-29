package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.MealDTO;
import com.cookie.app.model.entity.Meal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class MealMapperDTO implements Function<Meal, MealDTO> {
    private final UserMapperDTO userMapperDTO;
    private final GroupMapperDTO groupMapperDTO;
    private final RecipeMapperDTO recipeMapperDTO;

    @Override
    public MealDTO apply(Meal meal) {
        return new MealDTO(
                meal.getId(),
                meal.getMealDate(),
                this.userMapperDTO.apply(meal.getUser()),
                this.groupMapperDTO.apply(meal.getGroup()),
                this.recipeMapperDTO.apply(meal.getRecipe())
        );
    }
}

package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.MealDTO;
import com.cookie.app.model.entity.Meal;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {GroupMapper.class, RecipeMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface MealMapper {

    @Mapping(source = "entity.user.username", target = "username")
    MealDTO mapToDto(Meal entity);
}

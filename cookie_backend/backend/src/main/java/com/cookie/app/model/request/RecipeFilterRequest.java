package com.cookie.app.model.request;

import com.cookie.app.model.enums.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.springframework.data.domain.Sort;

import java.util.List;

@Getter
public class RecipeFilterRequest extends FilterRequest {
    @Schema(example = "30")
    @Min(value = 5, message = "Preparation time must be at least 5 minutes")
    @Max(value = 2880, message = "Preparation time must be lower or equals 2880 minutes")
    private final Integer prepTime;

    @Schema(example = "4")
    @Positive(message = "Number of portions must be at least 1")
    @Max(value = 12, message = "Number of portions must be lower or equals 12")
    private final Integer portions;

    private final List<MealType> mealTypes;

    public RecipeFilterRequest(String filterValue,
                               String sortColName,
                               Sort.Direction sortDirection,
                               Integer prepTime,
                               Integer portions,
                               List<MealType> mealTypes
    ) {
        super(filterValue, sortColName, sortDirection);
        this.prepTime = prepTime;
        this.portions = portions;
        this.mealTypes = mealTypes;
    }
}

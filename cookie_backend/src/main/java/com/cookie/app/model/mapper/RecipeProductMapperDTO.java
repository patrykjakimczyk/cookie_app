package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.RecipeProductDTO;
import com.cookie.app.model.entity.RecipeProduct;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class RecipeProductMapperDTO implements Function<RecipeProduct, RecipeProductDTO> {
    @Override
    public RecipeProductDTO apply(RecipeProduct recipeProduct) {
        return new RecipeProductDTO(
                recipeProduct.getId(),
                recipeProduct.getProduct().getProductName(),
                recipeProduct.getProduct().getCategory(),
                recipeProduct.getQuantity(),
                recipeProduct.getUnit()
        );
    }
}

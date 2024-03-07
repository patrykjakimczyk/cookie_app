package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.RecipeProductDTO;
import com.cookie.app.model.entity.RecipeProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class RecipeProductMapperDTO implements Function<RecipeProduct, RecipeProductDTO> {
    private final ProductMapperDTO productMapperDTO;

    @Override
    public RecipeProductDTO apply(RecipeProduct recipeProduct) {
        return new RecipeProductDTO(
                recipeProduct.getId(),
                this.productMapperDTO.apply(recipeProduct.getProduct()),
                recipeProduct.getQuantity(),
                recipeProduct.getUnit()
        );
    }
}

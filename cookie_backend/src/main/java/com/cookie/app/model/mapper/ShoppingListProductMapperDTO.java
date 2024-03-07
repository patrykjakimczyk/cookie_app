package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.entity.ShoppingListProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class ShoppingListProductMapperDTO implements Function<ShoppingListProduct, ShoppingListProductDTO> {
    private final ProductMapperDTO productMapperDTO;

    @Override
    public ShoppingListProductDTO apply(ShoppingListProduct shoppingListProduct) {
        return new ShoppingListProductDTO(
                shoppingListProduct.getId(),
                this.productMapperDTO.apply(shoppingListProduct.getProduct()),
                shoppingListProduct.getQuantity(),
                shoppingListProduct.getUnit(),
                shoppingListProduct.isPurchased()
        );
    }
}

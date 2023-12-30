package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.entity.ShoppingListProduct;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ShoppingListProductMapperDTO implements Function<ShoppingListProduct, ShoppingListProductDTO> {
    @Override
    public ShoppingListProductDTO apply(ShoppingListProduct shoppingListProduct) {
        return new ShoppingListProductDTO(
                shoppingListProduct.getId(),
                shoppingListProduct.getProduct().getProductName(),
                shoppingListProduct.getProduct().getCategory(),
                shoppingListProduct.getQuantity(),
                shoppingListProduct.getUnit(),
                shoppingListProduct.isPurchased()
        );
    }
}

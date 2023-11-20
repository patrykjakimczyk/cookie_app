package com.cookie.app.model.mapper;

import com.cookie.app.model.entity.PantryProduct;
import com.cookie.app.model.dto.PantryProductDTO;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class PantryProductMapperDTO implements Function<PantryProduct, PantryProductDTO> {

    @Override
    public PantryProductDTO apply(PantryProduct pantryProduct) {
        return new PantryProductDTO(
                pantryProduct.getId(),
                pantryProduct.getProduct().getProductName(),
                pantryProduct.getProduct().getCategory(),
                pantryProduct.getPurchaseDate(),
                pantryProduct.getExpirationDate(),
                pantryProduct.getQuantity(),
                pantryProduct.getUnit(),
                pantryProduct.getPlacement()
        );
    }
}

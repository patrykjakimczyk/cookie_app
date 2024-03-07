package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.entity.PantryProduct;
import com.cookie.app.model.dto.PantryProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class PantryProductMapperDTO implements Function<PantryProduct, PantryProductDTO> {
    private final ProductMapperDTO productMapperDTO;

    @Override
    public PantryProductDTO apply(PantryProduct pantryProduct) {
        return new PantryProductDTO(
                pantryProduct.getId(),
                this.productMapperDTO.apply(pantryProduct.getProduct()),
                pantryProduct.getPurchaseDate(),
                pantryProduct.getExpirationDate(),
                pantryProduct.getQuantity(),
                pantryProduct.getUnit(),
                pantryProduct.getReserved(),
                pantryProduct.getPlacement()
        );
    }
}

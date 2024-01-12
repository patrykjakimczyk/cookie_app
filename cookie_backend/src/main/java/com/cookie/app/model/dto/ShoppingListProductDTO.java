package com.cookie.app.model.dto;

import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.Unit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public final class ShoppingListProductDTO extends ProductDTO{
        @NotNull(message = "Quantity must be present")
        @Min(value = 0, message = "Quantity must be equal or greater than 1")
        private final int quantity;
        private final Unit unit;
        private final boolean purchased;

        public ShoppingListProductDTO(Long id,
                                      String productName,
                                      Category category,
                                      int quantity,
                                      Unit unit,
                                      boolean purchased) {
                super(id, productName, category);
                this.quantity = quantity;
                this.unit = unit;
                this.purchased = purchased;
        }
}

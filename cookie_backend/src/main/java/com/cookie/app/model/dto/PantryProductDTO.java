package com.cookie.app.model.dto;

import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.Unit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
public final class PantryProductDTO extends ProductDTO {
        private final Timestamp purchaseDate;
        private final Timestamp expirationDate;
        @NotNull(message = "Quantity must be a positive number")
        @Min(value = 0, message = "Quantity must be equal or greater than 1")
        private final int quantity;
        private final Unit unit;
        @NotNull(message = "Reserved count must be present")
        @Min(value = 0, message = "Reserved count must be equal or greater than 0")
        private final int reserved;
        private final String placement;

        public PantryProductDTO(Long id,
                                String productName,
                                Category category,
                                Timestamp purchaseDate,
                                Timestamp expirationDate,
                                int quantity,
                                Unit unit,
                                int reserved,
                                String placement) {
                super(id, productName, category);
                this.purchaseDate = purchaseDate;
                this.expirationDate = expirationDate;
                this.quantity = quantity;
                this.unit = unit;
                this.reserved = reserved;
                this.placement = placement;
        }

        public Timestamp getPurchaseDate() {
                return purchaseDate;
        }

        public Timestamp getExpirationDate() {
                return expirationDate;
        }

        public int getQuantity() {
                return quantity;
        }

        public Unit getUnit() {
                return unit;
        }

        public int getReserved() {
                return reserved;
        }

        public String getPlacement() {
                return placement;
        }
}

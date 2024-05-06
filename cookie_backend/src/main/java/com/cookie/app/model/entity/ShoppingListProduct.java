package com.cookie.app.model.entity;

import com.cookie.app.model.enums.Unit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
@Entity
public class ShoppingListProduct {
    @Id
    @SequenceGenerator(
            name = "shopping_list_product_sequence",
            sequenceName = "shopping_list_product_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shopping_list_product_sequence")
    @Column(name = "id", insertable = false, updatable = false, unique = true, nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", referencedColumnName = "id")
    private ShoppingList shoppingList;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private Unit unit;

    @Column(nullable = false)
    private boolean purchased;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ShoppingListProduct shoppingListProduct)) {
            return false;
        }

        return this.getId() == shoppingListProduct.getId() && this.getProduct().equals(shoppingListProduct.getProduct());
    }
}

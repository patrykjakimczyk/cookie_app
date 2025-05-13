package com.cookie.app.model.entity;

import com.cookie.app.model.enums.Category;
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
public class Product {
    @Id
    @SequenceGenerator(
            name = "product_sequence",
            sequenceName = "product_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_sequence")
    @Column(insertable = false, updatable = false, unique = true, nullable = false)
    private long id;

    @Column(length = 50, nullable = false)
    private String productName;

    @Column(length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Product product)) {
            return false;
        }

        return this.getProductName().equals(product.getProductName()) && this.getCategory() == product.getCategory();
    }
}

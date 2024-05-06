package com.cookie.app.model.entity;

import com.cookie.app.model.enums.Unit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
@Entity
public class PantryProduct {
    @Id
    @SequenceGenerator(
            name = "pantry_product_sequence",
            sequenceName = "pantry_product_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pantry_product_sequence")
    @Column(insertable = false, updatable = false, unique = true, nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pantry_id", referencedColumnName = "id")
    private Pantry pantry;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @Column
    private LocalDate purchaseDate;

    @Column
    private LocalDate expirationDate;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private Unit unit;

    @Column(nullable = false)
    private int reserved;

    @Column(length = 60)
    private String placement;
}

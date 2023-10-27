package com.cookie.app.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "pantry_product")
@Entity
public class PantryProduct {
    @Id
    @SequenceGenerator(
            name = "pantry_product_sequence",
            sequenceName = "pantry_product_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pantry_product_sequence")
    @Column(name = "id", insertable = false, updatable = false, unique = true, nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pantry_id", referencedColumnName = "id")
    private Pantry pantry;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @Column(name = "purchase_date", nullable = false)
    private Timestamp purchaseDate;

    @Column(name = "expiration_date", nullable = false)
    private Timestamp expirationDate;

    @Column
    private String quantity;

    @Column
    private String placement;
}

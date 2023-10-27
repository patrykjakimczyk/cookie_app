package com.cookie.app.model.entity;

import com.cookie.app.model.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product")
@Entity
public class Product {
    @Id
    @SequenceGenerator(
            name = "product_sequence",
            sequenceName = "product_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_sequence")
    @Column(name = "id", insertable = false, updatable = false, unique = true, nullable = false)
    private long id;

    @Column(name = "product_name", length = 50, unique = true, nullable = false)
    private String productName;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;
}

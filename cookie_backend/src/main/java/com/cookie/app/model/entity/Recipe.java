package com.cookie.app.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
@Entity
public class Recipe {
    @Id
    @SequenceGenerator(
            name = "recipe_sequence",
            sequenceName = "recipe_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recipe_sequence")
    @Column(insertable = false, updatable = false, unique = true, nullable = false)
    private long id;

    @Column(length = 30, nullable = false)
    private String recipeName;

    @Column(length = 512, nullable = false)
    private String preparation;

    @Column(nullable = false)
    private int preparationTime;

    @Column(length = 30, nullable = false)
    private String cuisine;

    @Column(nullable = false)
    private int portions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", referencedColumnName = "id")
    private User creator;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.REMOVE)
    private List<RecipeProduct> recipeProducts;
}

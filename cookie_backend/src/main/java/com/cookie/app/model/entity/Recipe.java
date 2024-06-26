package com.cookie.app.model.entity;

import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.enums.MealType;
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

    @Column(length = 1000, nullable = false)
    private String preparation;

    @Column(nullable = false)
    private int preparationTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MealType mealType;

    @Column(length = 30)
    private String cuisine;

    @Column(nullable = false)
    private int portions;

    @Lob
    @Column
    private byte[] recipeImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", referencedColumnName = "id")
    private User creator;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE })
    @JoinColumn(name = "recipe_id")
    @OrderBy("id ASC")
    private List<RecipeProduct> recipeProducts;
}

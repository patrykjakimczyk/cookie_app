package com.cookie.app.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
@Entity
public class ShoppingList {
    @Id
    @SequenceGenerator(
            name = "shopping_list_sequence",
            sequenceName = "shopping_list_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shopping_list_sequence")
    @Column(insertable = false, updatable = false, unique = true, nullable = false)
    private long id;

    @Column(length = 30, nullable = false)
    private String listName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", referencedColumnName = "id", nullable = false)
    private User creator;

    @Column(name = "creation_date", nullable = false)
    private Timestamp creationDate;

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.REMOVE)
    private List<ShoppingListProduct> productsList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id", nullable = false)
    private Group group;
}

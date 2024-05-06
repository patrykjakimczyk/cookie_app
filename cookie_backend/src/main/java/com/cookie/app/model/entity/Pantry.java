package com.cookie.app.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
@Entity
public class Pantry {
    @Id
    @SequenceGenerator(
            name = "pantry_sequence",
            sequenceName = "pantry_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pantry_sequence")
    @Column(insertable = false, updatable = false, unique = true, nullable = false)
    private long id;

    @Column(length = 30, nullable = false)
    private String pantryName;

    @OneToMany(mappedBy = "pantry", cascade = CascadeType.REMOVE)
    @OrderBy("id ASC")
    private List<PantryProduct> pantryProducts;

    @OneToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id", unique = true)
    private Group group;
}

package com.cookie.app.model.entity;

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
@Table(name = "pantry")
@Entity
public class Pantry {
    @Id
    @SequenceGenerator(
            name = "pantry_sequence",
            sequenceName = "pantry_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pantry_sequence")
    @Column(name = "id", insertable = false, updatable = false, unique = true, nullable = false)
    private long id;

    @Column(length = 30, nullable = false)
    private String pantryName;

    @OneToOne
    @JoinColumn(name = "creator_id", referencedColumnName = "id", unique = true)
    private User user;

    @OneToMany(mappedBy = "pantry", cascade = CascadeType.REMOVE)
    List<PantryProduct> pantryProducts;
}

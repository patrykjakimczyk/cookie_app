package com.cookie.app.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "group_table")
@Entity
public class Group {
    @Id
    @SequenceGenerator(
            name = "group_sequence",
            sequenceName = "group_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "group_sequence")
    @Column(insertable = false, updatable = false, unique = true, nullable = false)
    private long id;

    @Column(length = 30, unique = true, nullable = false)
    private String groupName;

    @ManyToOne
    @JoinColumn(name = "creator_id", referencedColumnName = "id", nullable = false)
    private User creator;

    @Column(nullable = false)
    private Timestamp creationDate;

    @ManyToMany
    @JoinTable(
            name = "user_in_group",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private List<User> users;

    @OneToOne(mappedBy = "group", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Pantry pantry;

    @OneToMany(mappedBy = "group", cascade = CascadeType.REMOVE)
    private List<ShoppingList> shoppingLists;

    @OneToMany(mappedBy = "group", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Meal> meals;
}

package com.cookie.app.model.entity;

import com.cookie.app.model.enums.Gender;
import com.cookie.app.model.enums.Role;
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
@Table(name = "user_table")
@Entity
public class User {
    @Id
    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_sequence")
    @Column(name = "id", insertable = false, updatable = false, unique = true, nullable = false)
    private long id;

    @Column(updatable = false, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(length = 30, unique = true, nullable = false)
    private String username;

    @Column(length = 50, unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean activated;

    @Column(name = "creation_date", nullable = false)
    private Timestamp creationDate;

    @Column(name = "birth_date", nullable = false)
    private Timestamp birthDate;

    @Column(length = 6, updatable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToOne(mappedBy = "user")
    private Pantry pantry;
}

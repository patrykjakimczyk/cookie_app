package com.cookie.app.model.entity;

import com.cookie.app.model.enums.AuthorityEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
@Entity
public class Authority {
    @Id
    @SequenceGenerator(
            name = "authority_sequence",
            sequenceName = "authority_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authority_sequence")
    @Column(name = "id", insertable = false, updatable = false, unique = true, nullable = false)
    private long id;

    @Column(name = "authority_name", length = 25, nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthorityEnum authorityName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group group;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authority authority1 = (Authority) o;
        return this.authorityName == authority1.authorityName &&
                this.user.getId() == authority1.user.getId() &&
                this.group.getId() == authority1.group.getId();
    }
}

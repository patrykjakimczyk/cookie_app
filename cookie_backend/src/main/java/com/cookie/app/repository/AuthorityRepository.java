package com.cookie.app.repository;

import com.cookie.app.model.entity.Authority;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface AuthorityRepository extends CrudRepository<Authority, Long> {

    Set<Authority> findAuthoritiesByUserAndGroup(User user, Group group);
    void deleteByUserAndGroup(User user, Group group);
    void deleteByGroup(Group group);
}

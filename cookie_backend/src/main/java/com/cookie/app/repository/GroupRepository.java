package com.cookie.app.repository;

import com.cookie.app.model.entity.Group;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface GroupRepository extends CrudRepository<Group, Long> {
    Optional<Group> findByGroupName(String groupName);
}

package com.cookie.app.repository;

import com.cookie.app.model.entity.Pantry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PantryRepository extends CrudRepository<Pantry, Long> {
}

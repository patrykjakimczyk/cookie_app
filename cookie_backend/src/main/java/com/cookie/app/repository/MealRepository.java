package com.cookie.app.repository;

import com.cookie.app.model.entity.Meal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface MealRepository extends CrudRepository<Meal, Long> {
    @Query(value = "SELECT DISTINCT m.* FROM meal m " +
            "WHERE m.group_id IN ?1 AND m.meal_date BETWEEN ?2 AND ?3", nativeQuery = true)
    List<Meal> findMealsForGroupsAndWithDateBetween(List<Long> groups, Timestamp dateAfter, Timestamp dateBefore, Pageable pageable);
}

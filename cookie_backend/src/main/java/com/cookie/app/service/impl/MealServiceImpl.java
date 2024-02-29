package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.ValidationException;
import com.cookie.app.model.dto.MealDTO;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.Meal;
import com.cookie.app.model.entity.Recipe;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.MealMapperDTO;
import com.cookie.app.model.request.AddMealRequest;
import com.cookie.app.repository.MealRepository;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.RecipeRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.MealService;
import com.cookie.app.service.RecipeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@Transactional
@Slf4j
@Service
public class MealServiceImpl extends AbstractCookieService implements MealService {
    private static final String MEAL_DATE_COLUMN = "meal_date";
    private final MealRepository mealRepository;
    private final RecipeRepository recipeRepository;
    private final MealMapperDTO mealMapperDTO;

    protected MealServiceImpl(UserRepository userRepository,
                              ProductRepository productRepository,
                              AuthorityMapperDTO authorityMapperDTO,
                              MealRepository mealRepository,
                              RecipeRepository recipeRepository,
                              MealMapperDTO mealMapperDTO) {
        super(userRepository, productRepository, authorityMapperDTO);
        this.mealRepository = mealRepository;
        this.recipeRepository = recipeRepository;
        this.mealMapperDTO = mealMapperDTO;
    }

    @Override
    public List<MealDTO> getMealsForUser(Timestamp dateAfter, Timestamp dateBefore, String userEmail) {
        if (dateAfter.after(dateAfter)) {
            throw new ValidationException("Date before must be after date after.");
        }

        User user = super.getUserByEmail(userEmail);
        List<Long> userGroups = user.getGroups()
                .stream()
                .map(Group::getId)
                .toList();
        PageRequest pageRequest = PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, MEAL_DATE_COLUMN));

        List<Meal> userMeals = this.mealRepository.findMealsForGroupsAndWithDateBetween(userGroups, dateAfter, dateBefore, pageRequest);

        if (userMeals.isEmpty()) {
            return Collections.emptyList();
        }

        return userMeals
                .stream()
                .map(mealMapperDTO::apply)
                .toList();
    }

    @Override
    public void addMeal(AddMealRequest request, String userEmail) {
        User user = super.getUserByEmail(userEmail);
        Group group = super.findUserGroupById(user, request.groupId()).orElseThrow(() -> {
            log.info("User: {} tried to add a meal to a group which he does not belong", userEmail);
            throw new UserPerformedForbiddenActionException("You tried to add a meal to a group which does not exist");
        });
        Recipe recipe = this.recipeRepository.findById(request.recipeId()).orElseThrow(() -> {
            log.info("User: {} tried to add a meal based on non existing recipe", userEmail);
            throw new UserPerformedForbiddenActionException("You tried to add a meal based on non existing recipe");
        });

        if (!super.userHasAuthority(user, group.getId(), AuthorityEnum.ADD_MEALS)) {
            log.info("User: {} tried to add a meal to group with id: {} without permission", userEmail, group.getId());
            throw new UserPerformedForbiddenActionException("You tried to add a meal to a group without permission");
        }

        Meal meal = mapToMeal(request.mealDate(), user, group, recipe);
        this.mealRepository.save(meal);
    }

    @Override
    public void deleteMeal(long mealId, String userEmail) {
        User user = super.getUserByEmail(userEmail);
        Meal meal = this.mealRepository.findById(mealId).orElseThrow(() -> {
            log.info("User: {} tried to delete a meal which does not exist", userEmail);
            throw new UserPerformedForbiddenActionException("You tried to delete a meal which does not exist");
        });
        Group group = meal.getGroup();

        if (
                !super.userHasAuthority(user, group.getId(), AuthorityEnum.MODIFY_MEALS) ||
                meal.getUser().getId() != user.getId()
        ) {
            log.info("User: {} tried to delete a meal from a group with id: {} without permission", userEmail, group.getId());
            throw new UserPerformedForbiddenActionException("You tried to delete a meal from group without permission");
        }

        this.mealRepository.deleteById(mealId);
    }

    @Override
    public void modifyMeal(long mealId, AddMealRequest request, String userEmail) {
        User user = super.getUserByEmail(userEmail);
        Meal meal = this.mealRepository.findById(mealId).orElseThrow(() -> {
            log.info("User: {} tried to modify a meal which does not exist", userEmail);
            throw new UserPerformedForbiddenActionException("You tried to modify a meal which does not exist");
        });
        Group group = meal.getGroup();

        if (
                !super.userHasAuthority(user, group.getId(), AuthorityEnum.MODIFY_MEALS) ||
                        meal.getUser().getId() == user.getId()
        ) {
            log.info("User: {} tried to modify a meal from a group with id: {} without permission", userEmail, group.getId());
            throw new UserPerformedForbiddenActionException("You tried to modify a meal from group without permission");
        }

        modifyMeal(meal, user, request);
        this.mealRepository.save(meal);
    }

    private void modifyMeal(Meal meal, User user, AddMealRequest request) {
        if (!meal.getMealDate().equals(request.mealDate())) {
            meal.setMealDate(request.mealDate());
        }
        if (meal.getGroup().getId() != request.groupId()) {
            Group newGroup = super.findUserGroupById(user, request.groupId()).orElseThrow(() -> {
                log.info("User: {} tried to assign a meal to group which he does not belongs", user.getEmail());
                throw new UserPerformedForbiddenActionException("You tried to assign a meal to group which he does not belongs");
            });

            meal.setGroup(newGroup);
        }
        if (meal.getRecipe().getId() != request.recipeId()) {
            Recipe recipe = this.recipeRepository.findById(request.recipeId()).orElseThrow(() -> {
                log.info("User: {} tried to add a meal based on non existing recipe", user.getEmail());
                throw new UserPerformedForbiddenActionException("You tried to add a meal based on non existing recipe");
            });

            meal.setRecipe(recipe);
        }
    }

    private Meal mapToMeal(Timestamp mealDate, User user, Group group, Recipe recipe) {
        return Meal
                .builder()
                .mealDate(mealDate)
                .group(group)
                .user(user)
                .recipe(recipe)
                .build();
    }
}

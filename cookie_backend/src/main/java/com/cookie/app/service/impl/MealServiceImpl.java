package com.cookie.app.service.impl;

import com.cookie.app.exception.ResourceNotFoundException;
import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.ValidationException;
import com.cookie.app.model.dto.MealDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapper;
import com.cookie.app.model.mapper.MealMapper;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public non-sealed class MealServiceImpl extends AbstractCookieService implements MealService {
    private static final String MEAL_DATE_COLUMN = "meal_date";
    private final MealRepository mealRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeService recipeService;
    private final MealMapper mealMapper;

    public MealServiceImpl(UserRepository userRepository,
                           ProductRepository productRepository,
                           AuthorityMapper authorityMapper,
                           MealRepository mealRepository,
                           RecipeRepository recipeRepository,
                           RecipeService recipeService,
                           MealMapper mealMapper) {
        super(userRepository, productRepository, authorityMapper);
        this.mealRepository = mealRepository;
        this.recipeRepository = recipeRepository;
        this.recipeService = recipeService;
        this.mealMapper = mealMapper;
    }

    @Transactional
    @Override
    public List<MealDTO> getMealsForUser(LocalDateTime dateAfter, LocalDateTime dateBefore, String userEmail) {
        if (dateAfter.isAfter(dateBefore)) {
            throw new ValidationException("Date before must be after date after.");
        }

        User user = super.getUserByEmail(userEmail);
        List<Long> userGroups = user.getGroups()
                .stream()
                .map(Group::getId)
                .toList();
        PageRequest pageRequest = PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, MEAL_DATE_COLUMN));
        List<Meal> userMeals = this.mealRepository.findMealsForGroupsAndWithDateBetween(userGroups, dateAfter, dateBefore, pageRequest);

        return userMeals
                .stream()
                .map(mealMapper::mapToDto)
                .toList();
    }

    @Transactional
    @Override
    public MealDTO addMeal(AddMealRequest request, String userEmail, boolean reserve, Long listId) {
        User user = super.getUserByEmail(userEmail);
        Group group = super.findUserGroupById(user, request.groupId()).orElseThrow(() -> {
            log.info("User={} tried to add a meal to a group which he does not belong", userEmail);
            return new ResourceNotFoundException("You tried to add a meal to a group which does not exist");
        });
        Recipe recipe = this.recipeRepository.findById(request.recipeId()).orElseThrow(() -> {
            log.info("User={} tried to add a meal based on non existing recipe", userEmail);
            return new ResourceNotFoundException("You tried to add a meal based on non existing recipe");
        });

        if (!super.userHasAuthority(user, group.getId(), AuthorityEnum.ADD_MEALS)) {
            log.info("User={} tried to add a meal to group with id={} without permission", userEmail, group.getId());
            throw new UserPerformedForbiddenActionException("You tried to add a meal to a group without permission");
        }

        Meal meal = mapToMeal(request.mealDate(), user, group, recipe);
        this.mealRepository.save(meal);

        if (group.getPantry() != null) {
            if (reserve && listId == null) {
                this.recipeService.reserveRecipeProductsInPantry(user, meal.getRecipe(), group.getPantry().getId());
            } else if (listId != null) {
                List<RecipeProduct> productsToShoppingList = new ArrayList<>(reserve ?
                        this.recipeService.reserveRecipeProductsInPantry(
                                user, meal.getRecipe(), group.getPantry().getId()
                        ) :
                        this.recipeService.getRecipeProductsNotInPantry(group, recipe)
                );

                this.recipeService.addRecipeProductsToShoppingList(user, listId, productsToShoppingList);
            }
        } else if (listId != null) {
            this.recipeService.addRecipeProductsToShoppingList(user, listId, recipe.getRecipeProducts());
        }

        return this.mealMapper.mapToDto(meal);
    }

    @Transactional
    @Override
    public void deleteMeal(long mealId, String userEmail) {
        // If this method doesn't throw any exception, it means that meal exists, so we can delete it
        findMealAndUserIfUserHasModifyAuthority(userEmail, mealId, "delete");

        this.mealRepository.deleteById(mealId);
    }

    @Transactional
    @Override
    public MealDTO updateMeal(long mealId, AddMealRequest request, String userEmail) {
        MealAndUser mealAndUser = findMealAndUserIfUserHasModifyAuthority(userEmail, mealId, "update");

        if (updateMeal(mealAndUser.meal(), mealAndUser.user(), request)) {
            this.mealRepository.save(mealAndUser.meal());
        }

        return this.mealMapper.mapToDto(mealAndUser.meal());
    }

    private boolean updateMeal(Meal meal, User user, AddMealRequest request) {
        boolean mealUpdated = false;

        if (!meal.getMealDate().equals(request.mealDate())) {
            meal.setMealDate(request.mealDate());
            mealUpdated = true;
        }

        if (meal.getRecipe().getId() != request.recipeId()) {
            Recipe recipe = this.recipeRepository.findById(request.recipeId()).orElseThrow(() -> {
                log.info("User: {} tried to add a meal based on non existing recipe", user.getEmail());
                return new ResourceNotFoundException("You tried to update a meal based on non existing recipe");
            });
            meal.setRecipe(recipe);
            mealUpdated = true;
        }

        return mealUpdated;
    }

    private MealAndUser findMealAndUserIfUserHasModifyAuthority(String userEmail, long mealId, String action) {
        User user = super.getUserByEmail(userEmail);
        Meal meal = this.mealRepository.findById(mealId).orElseThrow(() -> {
            log.info("User={} tried to {} a meal which does not exist", userEmail, action);
            return new ResourceNotFoundException(
                    String.format("You tried to %s a meal which does not exist", action)
            );
        });
        Group group = meal.getGroup();

        if (
                !super.userHasAuthority(user, group.getId(), AuthorityEnum.MODIFY_MEALS) &&
                        meal.getUser().getId() != user.getId()
        ) {
            log.info("User={} tried to {} a meal from a group with id={} without permission", userEmail, action, group.getId());
            throw new UserPerformedForbiddenActionException(
                    String.format("You tried to %s a meal from group without permission", action)
            );
        }

        return new MealAndUser(meal, user);
    }

    private Meal mapToMeal(LocalDateTime mealDate, User user, Group group, Recipe recipe) {
        return Meal
                .builder()
                .mealDate(mealDate)
                .group(group)
                .user(user)
                .recipe(recipe)
                .build();
    }

    private record MealAndUser(Meal meal, User user) {}
}

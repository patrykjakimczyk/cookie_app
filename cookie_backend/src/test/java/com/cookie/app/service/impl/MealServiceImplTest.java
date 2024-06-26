package com.cookie.app.service.impl;

import com.cookie.app.exception.ResourceNotFoundException;
import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.ValidationException;
import com.cookie.app.model.dto.MealDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.enums.MealType;
import com.cookie.app.model.enums.Unit;
import com.cookie.app.model.mapper.*;
import com.cookie.app.model.request.AddMealRequest;
import com.cookie.app.repository.MealRepository;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.RecipeRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealServiceImplTest {
    final String email = "email@email.com";
    final Long id = 1L;

    @Spy
    AuthorityMapper authorityMapper = new AuthorityMapperImpl();
    UserMapper userMapper = new UserMapperImpl(authorityMapper);
    @Spy
    MealMapper mealMapperDTO = new MealMapperImpl(new GroupMapperImpl(userMapper), new RecipeMapperImpl());
    @Mock
    UserRepository userRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    MealRepository mealRepository;
    @Mock
    RecipeRepository recipeRepository;
    @Mock
    RecipeService recipeService;
    @InjectMocks
    MealServiceImpl service;

    Authority authority;
    User user;
    Group group;
    Recipe recipe;
    Meal meal;

    @BeforeEach
    void init() {
        group = Group.builder()
                .id(id)
                .groupName("groupName")
                .build();
        authority = Authority.builder()
                .id(id)
                .group(group)
                .authorityName(AuthorityEnum.ADD_MEALS)
                .build();
        user = User.builder()
                .id(id)
                .email(email)
                .groups(List.of(group))
                .authorities(Set.of(authority))
                .build();
        authority.setUser(user);
        RecipeProduct recipeProduct = RecipeProduct.builder().id(id).quantity(100).unit(Unit.GRAMS).product(new Product()).build();
        recipe = Recipe.builder()
                .id(id)
                .recipeProducts(List.of(recipeProduct))
                .recipeName("recipeName")
                .preparationTime(15)
                .preparation("asdasdasd")
                .portions(1)
                .mealType(MealType.APPETIZER)
                .creator(user)
                .build();
        meal = Meal.builder()
                .id(id)
                .mealDate(LocalDateTime.now().minusSeconds(504800))
                .group(group)
                .recipe(recipe)
                .user(user)
                .build();
        group.setMeals(Collections.singletonList(meal));
        group.setCreator(user);
        group.setUsers(Collections.singletonList(user));
        group.setPantry(Pantry.builder().id(id).build());
    }

    @Test
    void test_getMealsForUserSuccessful() {
        final LocalDateTime dateAfter = LocalDateTime.now().minusSeconds(604800);
        final LocalDateTime dateBefore = LocalDateTime.now();

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(group.getMeals()).when(mealRepository)
                .findMealsForGroupsAndWithDateBetween(eq(Collections.singletonList(id)), eq(dateAfter), eq(dateBefore), any(PageRequest.class));
        List<MealDTO> response = this.service.getMealsForUser(dateAfter, dateBefore, email);

        assertEquals(group.getMeals().size(), response.size());
        assertEquals(group.getMeals().get(0).getId(), response.get(0).id());
        assertEquals(group.getMeals().get(0).getMealDate(), response.get(0).mealDate());
        assertEquals(group.getMeals().get(0).getUser().getUsername(), response.get(0).username());
        assertEquals(group.getMeals().get(0).getGroup().getId(), response.get(0).group().id());
        assertEquals(group.getMeals().get(0).getGroup().getGroupName(), response.get(0).group().groupName());
        assertEquals(group.getMeals().get(0).getRecipe().getId(), response.get(0).group().id());
        assertEquals(group.getMeals().get(0).getRecipe().getRecipeName(), response.get(0).recipe().recipeName());
        assertEquals(group.getMeals().get(0).getRecipe().getRecipeProducts().size(), response.get(0).recipe().nrOfProducts());
        assertEquals(group.getMeals().get(0).getRecipe().getMealType(), response.get(0).recipe().mealType());
    }

    @Test
    void test_getMealsForUserSuccessfulNoMealsFound() {
        final LocalDateTime dateAfter = LocalDateTime.now().minusSeconds(604800);
        final LocalDateTime dateBefore = LocalDateTime.now();

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Collections.emptyList()).when(mealRepository)
                .findMealsForGroupsAndWithDateBetween(eq(Collections.singletonList(id)), eq(dateAfter), eq(dateBefore), any(PageRequest.class));
        List<MealDTO> response = this.service.getMealsForUser(dateAfter, dateBefore, email);

        assertEquals(0, response.size());
        verify(mealMapperDTO, times(0)).mapToDto(any(Meal.class));
    }

    @Test
    void test_getMealsForUserDateAfterIsLaterThanDateBefore() {
        final LocalDateTime dateAfter = LocalDateTime.now().plusSeconds(604800);
        final LocalDateTime dateBefore = LocalDateTime.now();


        assertThrows(ValidationException.class, () -> this.service.getMealsForUser(dateAfter, dateBefore, email));
        verify(userRepository, times(0)).findByEmail(email);
        verify(mealRepository, times(0))
                .findMealsForGroupsAndWithDateBetween(anyList(), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class));
    }

    @Test
    void test_addMealSuccessfulWithAddingToShoppingListAndReserving() {
        final AddMealRequest request = new AddMealRequest(LocalDateTime.now(), id, id);
        
        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(recipe)).when(recipeRepository).findById(request.recipeId());
        doReturn(recipe.getRecipeProducts()).when(recipeService)
                .reserveRecipeProductsInPantry(user, recipe, group.getPantry().getId());

        MealDTO response = this.service.addMeal(request, email, true, id);

        verify(mealRepository).save(any(Meal.class));
        verify(recipeService, times(0)).getRecipeProductsNotInPantry(group, recipe);
        verify(recipeService).addRecipeProductsToShoppingList(user, id, recipe.getRecipeProducts());
        assertEquals(request.mealDate(), response.mealDate());
        assertEquals(user.getUsername(), response.username());
        assertEquals(group.getId(), response.group().id());
        assertEquals(group.getGroupName(), response.group().groupName());
        assertEquals(recipe.getId(), response.group().id());
        assertEquals(recipe.getRecipeName(), response.recipe().recipeName());
        assertEquals(recipe.getRecipeProducts().size(), response.recipe().nrOfProducts());
        assertEquals(recipe.getMealType(), response.recipe().mealType());
    }

    @Test
    void test_addMealSuccessfulWithAddingToShoppingListAndWithoutReserving() {
        final AddMealRequest request = new AddMealRequest(LocalDateTime.now(), id, id);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(recipe)).when(recipeRepository).findById(request.recipeId());
        doReturn(recipe.getRecipeProducts()).when(recipeService)
                .getRecipeProductsNotInPantry(group, recipe);
        MealDTO response = this.service.addMeal(request, email, false, id);

        verify(mealRepository).save(any(Meal.class));
        verify(recipeService, times(0))
                .reserveRecipeProductsInPantry(user, recipe, group.getPantry().getId());
        verify(recipeService).addRecipeProductsToShoppingList(user, id, recipe.getRecipeProducts());
        assertEquals(request.mealDate(), response.mealDate());
        assertEquals(user.getUsername(), response.username());
        assertEquals(group.getId(), response.group().id());
        assertEquals(group.getGroupName(), response.group().groupName());
        assertEquals(recipe.getId(), response.group().id());
        assertEquals(recipe.getRecipeName(), response.recipe().recipeName());
        assertEquals(recipe.getRecipeProducts().size(), response.recipe().nrOfProducts());
        assertEquals(recipe.getMealType(), response.recipe().mealType());
    }

    @Test
    void test_addMealSuccessfulWithAddingToShoppingWithoutGroupPantry() {
        group.setPantry(null);
        final AddMealRequest request = new AddMealRequest(LocalDateTime.now(), id, id);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(recipe)).when(recipeRepository).findById(request.recipeId());
        MealDTO response = this.service.addMeal(request, email, false, id);

        verify(mealRepository).save(any(Meal.class));
        verify(recipeService, times(0))
                .reserveRecipeProductsInPantry(eq(user), eq(recipe), anyLong());
        verify(recipeService, times(0))
                .getRecipeProductsNotInPantry(group, recipe);
        verify(recipeService).addRecipeProductsToShoppingList(user, id, recipe.getRecipeProducts());
        assertEquals(request.mealDate(), response.mealDate());
        assertEquals(user.getUsername(), response.username());
        assertEquals(group.getId(), response.group().id());
        assertEquals(group.getGroupName(), response.group().groupName());
        assertEquals(recipe.getId(), response.group().id());
        assertEquals(recipe.getRecipeName(), response.recipe().recipeName());
        assertEquals(recipe.getRecipeProducts().size(), response.recipe().nrOfProducts());
        assertEquals(recipe.getMealType(), response.recipe().mealType());
    }

    @Test
    void test_addMealSuccessfulWithReservingAndWithoutAddingToShopping() {
        final AddMealRequest request = new AddMealRequest(LocalDateTime.now(), id, id);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(recipe)).when(recipeRepository).findById(request.recipeId());
        doReturn(recipe.getRecipeProducts()).when(recipeService)
                .reserveRecipeProductsInPantry(user, recipe, group.getPantry().getId());
        MealDTO response = this.service.addMeal(request, email, true, null);

        verify(mealRepository).save(any(Meal.class));
        verify(recipeService, times(0)).getRecipeProductsNotInPantry(group, recipe);
        verify(recipeService, times(0))
                .addRecipeProductsToShoppingList(user, id, recipe.getRecipeProducts());
        assertEquals(request.mealDate(), response.mealDate());
        assertEquals(user.getUsername(), response.username());
        assertEquals(group.getId(), response.group().id());
        assertEquals(group.getGroupName(), response.group().groupName());
        assertEquals(recipe.getId(), response.group().id());
        assertEquals(recipe.getRecipeName(), response.recipe().recipeName());
        assertEquals(recipe.getRecipeProducts().size(), response.recipe().nrOfProducts());
        assertEquals(recipe.getMealType(), response.recipe().mealType());
    }

    @Test
    void test_addMealGroupNotFound() {
        user.setGroups(Collections.emptyList());
        final AddMealRequest request = new AddMealRequest(LocalDateTime.now(), id, id);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                this.service.addMeal(request, email, true, id));
        assertEquals("You tried to add a meal to a group which does not exist", ex.getMessage());
        verify(recipeRepository, times(0)).findById(request.recipeId());
        verify(mealRepository, times(0)).save(any(Meal.class));
        verify(recipeService, times(0)).getRecipeProductsNotInPantry(group, recipe);
        verify(recipeService, times(0)).reserveRecipeProductsInPantry(user, recipe, group.getPantry().getId());
        verify(recipeService, times(0)).addRecipeProductsToShoppingList(user, id, recipe.getRecipeProducts());
    }

    @Test
    void test_addMealRecipeNotFound() {
        final AddMealRequest request = new AddMealRequest(LocalDateTime.now(), id, id);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.empty()).when(recipeRepository).findById(request.recipeId());

        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                this.service.addMeal(request, email, true, id));
        assertEquals("You tried to add a meal based on non existing recipe", ex.getMessage());
        verify(mealRepository, times(0)).save(any(Meal.class));
        verify(recipeService, times(0)).getRecipeProductsNotInPantry(group, recipe);
        verify(recipeService, times(0)).reserveRecipeProductsInPantry(user, recipe, group.getPantry().getId());
        verify(recipeService, times(0)).addRecipeProductsToShoppingList(user, id, recipe.getRecipeProducts());
    }

    @Test
    void test_addMealNoRequiredAuthority() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        final AddMealRequest request = new AddMealRequest(LocalDateTime.now(), id, id);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(recipe)).when(recipeRepository).findById(request.recipeId());

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.addMeal(request, email, true, id));
        assertEquals("You tried to add a meal to a group without permission", ex.getMessage());
        verify(mealRepository, times(0)).save(any(Meal.class));
        verify(recipeService, times(0)).getRecipeProductsNotInPantry(group, recipe);
        verify(recipeService, times(0)).reserveRecipeProductsInPantry(user, recipe, group.getPantry().getId());
        verify(recipeService, times(0)).addRecipeProductsToShoppingList(user, id, recipe.getRecipeProducts());
    }

    @Test
    void test_deleteMealSuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(meal)).when(mealRepository).findById(id);
        this.service.deleteMeal(id, email);

        verify(mealRepository).deleteById(id);
    }

    @Test
    void test_deleteMealSuccessfulMealNotFound() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.empty()).when(mealRepository).findById(id);

        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                this.service.deleteMeal(id, email));
        assertEquals("You tried to delete a meal which does not exist", ex.getMessage());
        verify(mealRepository, times(0)).deleteById(id);
    }

    @Test
    void test_deleteMealSuccessfulNoRequiredAuthority() {
        meal.setUser(new User());

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(meal)).when(mealRepository).findById(id);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.deleteMeal(id, email));
        assertEquals("You tried to delete a meal from group without permission", ex.getMessage());
        verify(mealRepository, times(0)).deleteById(id);
    }

    @Test
    void test_updateMealSuccessful() {
        final AddMealRequest request = new AddMealRequest(LocalDateTime.now().plusSeconds(3600), id, 2L);
        final Recipe newRecipe = Recipe.builder().id(2L).recipeProducts(Collections.emptyList()).creator(user).build();

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(meal)).when(mealRepository).findById(id);
        doReturn(Optional.of(newRecipe)).when(recipeRepository).findById(request.recipeId());
        MealDTO response = this.service.updateMeal(id, request, email);

        verify(mealRepository).save(meal);
        assertEquals(request.mealDate(), response.mealDate());
        assertEquals(user.getUsername(), response.username());
        assertEquals(group.getId(), response.group().id());
        assertEquals(group.getGroupName(), response.group().groupName());
        assertEquals(group.getId(), response.group().id());
        assertEquals(newRecipe.getId(), response.recipe().id());
        assertEquals(newRecipe.getRecipeName(), response.recipe().recipeName());
        assertEquals(newRecipe.getRecipeProducts().size(), response.recipe().nrOfProducts());
        assertEquals(newRecipe.getMealType(), response.recipe().mealType());
    }

    @Test
    void test_updateMealSuccessfulWithoutSaving() {
        final AddMealRequest request = new AddMealRequest(meal.getMealDate(), id, recipe.getId());

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(meal)).when(mealRepository).findById(id);
        MealDTO response = this.service.updateMeal(id, request, email);

        verify(mealRepository, times(0)).save(meal);
        verify(recipeRepository, times(0)).findById(request.recipeId());
        assertEquals(request.mealDate(), response.mealDate());
        assertEquals(user.getUsername(), response.username());
        assertEquals(group.getId(), response.group().id());
        assertEquals(group.getGroupName(), response.group().groupName());
        assertEquals(group.getId(), response.group().id());
        assertEquals(recipe.getId(), response.recipe().id());
        assertEquals(recipe.getRecipeName(), response.recipe().recipeName());
        assertEquals(recipe.getRecipeProducts().size(), response.recipe().nrOfProducts());
        assertEquals(recipe.getMealType(), response.recipe().mealType());
    }

    @Test
    void test_updateMealRecipeNotFound() {
        final AddMealRequest request = new AddMealRequest(LocalDateTime.now().plusSeconds(3600), id, 2L);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(meal)).when(mealRepository).findById(id);
        doReturn(Optional.empty()).when(recipeRepository).findById(request.recipeId());

        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                this.service.updateMeal(id, request, email));
        assertEquals("You tried to update a meal based on non existing recipe", ex.getMessage());
        verify(mealRepository, times(0)).save(meal);

    }
}

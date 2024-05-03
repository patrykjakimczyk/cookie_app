package com.cookie.app.controller;

import com.cookie.app.model.dto.GroupDTO;
import com.cookie.app.model.dto.MealDTO;
import com.cookie.app.model.dto.RecipeDTO;
import com.cookie.app.model.dto.UserDTO;
import com.cookie.app.model.enums.MealType;
import com.cookie.app.model.request.AddMealRequest;
import com.cookie.app.service.MealService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MealControllerTest extends AbstractControllerTest {
    final String username = "username";
    final Long id = 1L;

    @Mock
    MealService mealService;
    @InjectMocks
    MealController controller;

    @Test
    void test_getMealsForUserSuccessful() {
        final LocalDateTime dateAfter = LocalDateTime.now().minusSeconds(604800);
        final LocalDateTime dateBefore = LocalDateTime.now();
        final UserDTO userDTO = new UserDTO(id, username, Collections.emptySet());
        final GroupDTO groupDTO = new GroupDTO(id, username, userDTO, 1, id);
        final RecipeDTO recipeDTO = new RecipeDTO(id, username, 15, MealType.APPETIZER, null, 1, new byte[0], userDTO.username(), 2);
        final MealDTO mealDTO = new MealDTO(id, LocalDateTime.now().minusSeconds(3600), username, groupDTO, recipeDTO);
        final List<MealDTO> meals = Collections.singletonList(mealDTO);

        doReturn(meals).when(mealService).getMealsForUser(dateAfter, dateBefore, authentication.getName());
        ResponseEntity<List<MealDTO>> response = this.controller.getMealsForUser(dateAfter, dateBefore, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(meals.size(), response.getBody().size());
        assertEquals(meals.get(0).id(), response.getBody().get(0).id());
        assertEquals(meals.get(0).mealDate(), response.getBody().get(0).mealDate());
        assertEquals(meals.get(0).username(), response.getBody().get(0).username());
        assertEquals(meals.get(0).group().groupName(), response.getBody().get(0).group().groupName());
        assertEquals(meals.get(0).recipe().recipeName(), response.getBody().get(0).recipe().recipeName());
    }

    @Test
    void test_addMealSuccessful() {
        final AddMealRequest request = new AddMealRequest(LocalDateTime.now(), id, id);
        final UserDTO userDTO = new UserDTO(id, username, Collections.emptySet());
        final GroupDTO groupDTO = new GroupDTO(id, username, userDTO, 1, id);
        final RecipeDTO recipeDTO = new RecipeDTO(id, username, 15, MealType.APPETIZER,
                null, 1, new byte[0], userDTO.username(), 2);
        final MealDTO mealDTO = new MealDTO(id, LocalDateTime.now().minusSeconds(3600), username, groupDTO, recipeDTO);

        doReturn(mealDTO).when(mealService).addMeal(request, authentication.getName(), false, id);
        ResponseEntity<MealDTO> response = this.controller.addMeal(false, id, request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mealDTO.id(), response.getBody().id());
        assertEquals(mealDTO.mealDate(), response.getBody().mealDate());
        assertEquals(mealDTO.username(), response.getBody().username());
        assertEquals(mealDTO.group().groupName(), response.getBody().group().groupName());
        assertEquals(mealDTO.recipe().recipeName(), response.getBody().recipe().recipeName());
    }

    @Test
    void test_deleteMealSuccessful() {

        doNothing().when(mealService).deleteMeal(id, authentication.getName());
        ResponseEntity<Void> response = this.controller.deleteMeal(id, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_updateMealSuccessful() {
        final AddMealRequest request = new AddMealRequest(LocalDateTime.now(), id, id);
        final UserDTO userDTO = new UserDTO(id, username, Collections.emptySet());
        final GroupDTO groupDTO = new GroupDTO(id, username, userDTO, 1, id);
        final RecipeDTO recipeDTO = new RecipeDTO(id, username, 15, MealType.APPETIZER, null, 1, new byte[0], userDTO.username(), 2);
        final MealDTO mealDTO = new MealDTO(id, LocalDateTime.now().minusSeconds(3600), username, groupDTO, recipeDTO);

        doReturn(mealDTO).when(mealService).updateMeal(id, request, authentication.getName());
        ResponseEntity<MealDTO> response = this.controller.updateMeal(id, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mealDTO.id(), response.getBody().id());
        assertEquals(mealDTO.mealDate(), response.getBody().mealDate());
        assertEquals(mealDTO.username(), response.getBody().username());
        assertEquals(mealDTO.group().groupName(), response.getBody().group().groupName());
        assertEquals(mealDTO.recipe().recipeName(), response.getBody().recipe().recipeName());
    }
}

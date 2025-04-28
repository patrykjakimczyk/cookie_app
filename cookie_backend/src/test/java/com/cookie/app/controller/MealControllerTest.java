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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MealControllerTest extends AbstractControllerTest {
    private final String username = "username";
    private final Long id = 1L;

    @Mock
    private MealService mealService;
    @InjectMocks
    private MealController controller;

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
        ResponseEntity<List<MealDTO>> response = controller.getMealsForUser(dateAfter, dateBefore, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(meals.size());
        assertThat(response.getBody().get(0).id()).isEqualTo(meals.get(0).id());
        assertThat(response.getBody().get(0).mealDate()).isEqualTo(meals.get(0).mealDate());
        assertThat(response.getBody().get(0).username()).isEqualTo(meals.get(0).username());
        assertThat(response.getBody().get(0).group().groupName()).isEqualTo(meals.get(0).group().groupName());
        assertThat(response.getBody().get(0).recipe().recipeName()).isEqualTo(meals.get(0).recipe().recipeName());
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
        ResponseEntity<MealDTO> response = controller.addMeal(false, id, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(mealDTO.id());
        assertThat(response.getBody().mealDate()).isEqualTo(mealDTO.mealDate());
        assertThat(response.getBody().username()).isEqualTo(mealDTO.username());
        assertThat(response.getBody().group().groupName()).isEqualTo(mealDTO.group().groupName());
        assertThat(response.getBody().recipe().recipeName()).isEqualTo(mealDTO.recipe().recipeName());
    }

    @Test
    void test_deleteMealSuccessful() {

        doNothing().when(mealService).deleteMeal(id, authentication.getName());
        ResponseEntity<Void> response = controller.deleteMeal(id, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test_updateMealSuccessful() {
        final AddMealRequest request = new AddMealRequest(LocalDateTime.now(), id, id);
        final UserDTO userDTO = new UserDTO(id, username, Collections.emptySet());
        final GroupDTO groupDTO = new GroupDTO(id, username, userDTO, 1, id);
        final RecipeDTO recipeDTO = new RecipeDTO(id, username, 15, MealType.APPETIZER, null, 1, new byte[0], userDTO.username(), 2);
        final MealDTO mealDTO = new MealDTO(id, LocalDateTime.now().minusSeconds(3600), username, groupDTO, recipeDTO);

        doReturn(mealDTO).when(mealService).updateMeal(id, request, authentication.getName());
        ResponseEntity<MealDTO> response = controller.updateMeal(id, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(mealDTO.id());
        assertThat(response.getBody().mealDate()).isEqualTo(mealDTO.mealDate());
        assertThat(response.getBody().username()).isEqualTo(mealDTO.username());
        assertThat(response.getBody().group().groupName()).isEqualTo(mealDTO.group().groupName());
        assertThat(response.getBody().recipe().recipeName()).isEqualTo(mealDTO.recipe().recipeName());
    }
}
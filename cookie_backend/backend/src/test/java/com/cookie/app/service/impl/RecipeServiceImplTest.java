package com.cookie.app.service.impl;

import com.cookie.app.exception.ResourceNotFoundException;
import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.model.dto.*;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.MealType;
import com.cookie.app.model.enums.Unit;
import com.cookie.app.model.mapper.*;
import com.cookie.app.model.request.CreateRecipeRequest;
import com.cookie.app.model.request.RecipeFilterRequest;
import com.cookie.app.model.request.UpdateRecipeRequest;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.RecipeProductRepository;
import com.cookie.app.repository.RecipeRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryProductService;
import com.cookie.app.service.ShoppingListProductService;
import com.cookie.app.util.ImageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {
    private final String email = "email@email.com";
    private final String recipeName = "recipeName";
    private final String productName = "productName";
    private final String filter = "filter";
    private final String col = "col";
    private final Sort.Direction direction = Sort.Direction.DESC;
    private final Long id = 1L;

    @Captor
    private ArgumentCaptor<Recipe> recipeArgumentCaptor;

    private RecipeProductMapper recipeProductMapper = new RecipeProductMapperImpl(new ProductMapperImpl());
    @Spy
    private RecipeDetailsMapper recipeDetailsMapperDTO = new RecipeDetailsMapperImpl(recipeProductMapper);
    @Spy
    private RecipeMapper recipeMapper = new RecipeMapperImpl();
    @Spy
    private AuthorityMapper authorityMapper = new AuthorityMapperImpl();
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private RecipeProductRepository recipeProductRepository;
    @Mock
    private PantryProductService pantryProductService;
    @Mock
    private ShoppingListProductService shoppingListProductService;
    @InjectMocks
    private RecipeServiceImpl service;

    private RecipeProduct recipeProduct;
    private Recipe recipe;
    private User user;
    private Group group;
    private Authority authority;

    @BeforeEach
    void init() {
        group = Group.builder()
                .id(id)
                .build();
        authority = Authority.builder()
                .id(id)
                .group(group)
                .authorityName(AuthorityEnum.MODIFY_SHOPPING_LIST)
                .build();
        user = User.builder()
                .id(id)
                .email(email)
                .groups(List.of(group))
                .authorities(Set.of(authority))
                .build();
        Product product = Product.builder().id(id).productName(productName).category(Category.CEREAL).build();
        recipeProduct = RecipeProduct.builder().id(id).product(product).unit(Unit.GRAMS).quantity(100).build();
        recipe = Recipe.builder()
                .id(id)
                .recipeName(recipeName)
                .creator(user)
                .portions(1)
                .preparationTime(15)
                .mealType(MealType.APPETIZER)
                .recipeProducts(new ArrayList<>(Collections.singletonList(recipeProduct)))
                .preparation("preparation preparation")
                .build();
    }

    @Test
    void test_getRecipesSuccessful() {
        final RecipeFilterRequest filterRequest = new RecipeFilterRequest(filter, col, direction, 15, 1, List.of(MealType.APPETIZER));
        final PageImpl<Recipe> pageResponse = new PageImpl<>(List.of(recipe));

        doReturn(pageResponse).when(recipeRepository)
                .findRecipesByFilter(eq(filter), eq(15), eq(1), eq(Set.of("APPETIZER")), any(PageRequest.class));

        try (MockedStatic<ImageUtil> imageUtilMockedStatic = mockStatic(ImageUtil.class)) {
            imageUtilMockedStatic.when(() -> ImageUtil.decompressImage(any()))
                    .thenReturn(new byte[0]);
            PageResult<RecipeDTO> response = this.service.getRecipes(1, filterRequest);

            verify(recipeRepository, times(0)).findRecipes(anyInt(), anyInt(), anySet(), any(PageRequest.class));
            assertThat(response.pageNr()).isZero();
            assertThat(response.totalElements()).isEqualTo(pageResponse.getTotalElements());
            assertThat(response.content().get(0).id()).isEqualTo(pageResponse.getContent().get(0).getId());
            assertThat(response.content().get(0).recipeName()).isEqualTo(pageResponse.getContent().get(0).getRecipeName());
        }
    }

    @Test
    void test_getRecipesSuccessfulWithNullMealTypesPrepTimeAndPortions() {
        final RecipeFilterRequest filterRequest = new RecipeFilterRequest(filter, col, direction, null, null, null);
        final PageImpl<Recipe> pageResponse = new PageImpl<>(List.of(recipe));
        final Set<String> mealTypesStrings = MealType.ALL_MEAL_TYPES
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        doReturn(pageResponse).when(recipeRepository)
                .findRecipesByFilter(eq(filter), eq(0), eq(0), eq(mealTypesStrings), any(PageRequest.class));

        try (MockedStatic<ImageUtil> imageUtilMockedStatic = mockStatic(ImageUtil.class)) {
            imageUtilMockedStatic.when(() -> ImageUtil.decompressImage(any()))
                    .thenReturn(new byte[0]);
            PageResult<RecipeDTO> response = this.service.getRecipes(1, filterRequest);

            verify(recipeRepository, times(0)).findRecipes(anyInt(), anyInt(), anySet(), any(PageRequest.class));
            assertThat(response.pageNr()).isZero();
            assertThat(response.totalElements()).isEqualTo(pageResponse.getTotalElements());
            assertThat(response.content().get(0).id()).isEqualTo(pageResponse.getContent().get(0).getId());
            assertThat(response.content().get(0).recipeName()).isEqualTo(pageResponse.getContent().get(0).getRecipeName());
        }
    }

    @Test
    void test_getRecipesSuccessfulWithoutFilter() {
        final RecipeFilterRequest filterRequest = new RecipeFilterRequest(null, col, direction, 15, 1, List.of(MealType.APPETIZER));
        final PageImpl<Recipe> pageResponse = new PageImpl<>(List.of(recipe));

        doReturn(pageResponse).when(recipeRepository)
                .findRecipes(eq(15), eq(1), eq(Set.of("APPETIZER")), any(PageRequest.class));

        try (MockedStatic<ImageUtil> imageUtilMockedStatic = mockStatic(ImageUtil.class)) {
            imageUtilMockedStatic.when(() -> ImageUtil.decompressImage(any()))
                    .thenReturn(new byte[0]);
            PageResult<RecipeDTO> response = this.service.getRecipes(1, filterRequest);

            verify(recipeRepository, times(0)).findRecipesByFilter(anyString(), anyInt(), anyInt(), anySet(), any(PageRequest.class));
            assertThat(response.pageNr()).isZero();
            assertThat(response.totalElements()).isEqualTo(pageResponse.getTotalElements());
            assertThat(response.content().get(0).id()).isEqualTo(pageResponse.getContent().get(0).getId());
            assertThat(response.content().get(0).recipeName()).isEqualTo(pageResponse.getContent().get(0).getRecipeName());
        }
    }

    @Test
    void test_getUserRecipesSuccessful() {
        final RecipeFilterRequest filterRequest = new RecipeFilterRequest(filter, col, direction, 15, 1, List.of(MealType.APPETIZER));
        final PageImpl<Recipe> pageResponse = new PageImpl<>(List.of(recipe));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(pageResponse).when(recipeRepository)
                .findUserRecipesByFilter(eq(user.getId()), eq(filter), eq(15), eq(1), eq(Set.of("APPETIZER")), any(PageRequest.class));

        try (MockedStatic<ImageUtil> imageUtilMockedStatic = mockStatic(ImageUtil.class)) {
            imageUtilMockedStatic.when(() -> ImageUtil.decompressImage(any()))
                    .thenReturn(new byte[0]);
            PageResult<RecipeDTO> response = this.service.getUserRecipes(email, 1, filterRequest);

            verify(recipeRepository, times(0)).findUserRecipes(anyLong(), anyInt(), anyInt(), anySet(), any(PageRequest.class));
            assertThat(response.pageNr()).isZero();
            assertThat(response.totalElements()).isEqualTo(pageResponse.getTotalElements());
            assertThat(response.content().get(0).id()).isEqualTo(pageResponse.getContent().get(0).getId());
            assertThat(response.content().get(0).recipeName()).isEqualTo(pageResponse.getContent().get(0).getRecipeName());
        }
    }

    @Test
    void test_getUserRecipesSuccessfulWithNullMealTypesPrepTimeAndPortions() {
        final RecipeFilterRequest filterRequest = new RecipeFilterRequest(filter, null, null, null, null, null);
        final PageImpl<Recipe> pageResponse = new PageImpl<>(List.of(recipe));
        final Set<String> mealTypesStrings = MealType.ALL_MEAL_TYPES
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(pageResponse).when(recipeRepository)
                .findUserRecipesByFilter(eq(user.getId()), eq(filter), eq(0), eq(0), eq(mealTypesStrings), any(PageRequest.class));

        try (MockedStatic<ImageUtil> imageUtilMockedStatic = mockStatic(ImageUtil.class)) {
            imageUtilMockedStatic.when(() -> ImageUtil.decompressImage(any()))
                    .thenReturn(new byte[0]);
            PageResult<RecipeDTO> response = this.service.getUserRecipes(email, 1, filterRequest);

            verify(recipeRepository, times(0)).findUserRecipes(anyLong(), anyInt(), anyInt(), anySet(), any(PageRequest.class));
            assertThat(response.pageNr()).isZero();
            assertThat(response.totalElements()).isEqualTo(pageResponse.getTotalElements());
            assertThat(response.content().get(0).id()).isEqualTo(pageResponse.getContent().get(0).getId());
            assertThat(response.content().get(0).recipeName()).isEqualTo(pageResponse.getContent().get(0).getRecipeName());
        }
    }

    @Test
    void test_getUserRecipesSuccessfulWithoutFilter() {
        final RecipeFilterRequest filterRequest = new RecipeFilterRequest(null, col, direction, 15, 1, List.of(MealType.APPETIZER));
        final PageImpl<Recipe> pageResponse = new PageImpl<>(List.of(recipe));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(pageResponse).when(recipeRepository)
                .findUserRecipes(eq(user.getId()), eq(15), eq(1), eq(Set.of("APPETIZER")), any(PageRequest.class));

        try (MockedStatic<ImageUtil> imageUtilMockedStatic = mockStatic(ImageUtil.class)) {
            imageUtilMockedStatic.when(() -> ImageUtil.decompressImage(any()))
                    .thenReturn(new byte[0]);
            PageResult<RecipeDTO> response = this.service.getUserRecipes(email, 1, filterRequest);

            verify(recipeRepository, times(0)).findUserRecipesByFilter(anyLong(), anyString(), anyInt(), anyInt(), anySet(), any(PageRequest.class));
            assertThat(response.pageNr()).isZero();
            assertThat(response.totalElements()).isEqualTo(pageResponse.getTotalElements());
            assertThat(response.content().get(0).id()).isEqualTo(pageResponse.getContent().get(0).getId());
            assertThat(response.content().get(0).recipeName()).isEqualTo(pageResponse.getContent().get(0).getRecipeName());
        }
    }

    @Test
    void test_getRecipeDetailsSuccessful() {

        doReturn(Optional.of(recipe)).when(recipeRepository).findById(id);

        try (MockedStatic<ImageUtil> imageUtilMockedStatic = mockStatic(ImageUtil.class)) {
            imageUtilMockedStatic.when(() -> ImageUtil.decompressImage(any()))
                    .thenReturn(new byte[0]);
            RecipeDetailsDTO response = this.service.getRecipeDetails(id);

            assertThat(response.id()).isEqualTo(recipe.getId());
            assertThat(response.recipeName()).isEqualTo(recipe.getRecipeName());
            assertThat(response.mealType()).isEqualTo(recipe.getMealType());
            assertThat(response.portions()).isEqualTo(recipe.getPortions());
            assertThat(response.preparationTime()).isEqualTo(recipe.getPreparationTime());
            assertThat(response.preparation()).isEqualTo(recipe.getPreparation());
            assertThat(response.creatorUserName()).isEqualTo(recipe.getCreator().getUsername());
            assertThat(response.products()).hasSize(recipe.getRecipeProducts().size());
            assertThat(response.products().get(0).id()).isEqualTo(recipe.getRecipeProducts().get(0).getId());
            assertThat(response.products().get(0).unit()).isEqualTo(recipe.getRecipeProducts().get(0).getUnit());
            assertThat(response.products().get(0).quantity()).isEqualTo(recipe.getRecipeProducts().get(0).getQuantity());
            assertThat(response.products().get(0).product().productName()).isEqualTo(recipe.getRecipeProducts().get(0).getProduct().getProductName());
            assertThat(response.recipeImage()).isEmpty();
            assertThat(response.cuisine()).isNull();
        }
    }

    @Test
    void test_getRecipeDetailsRecipeNotFound() {

        doReturn(Optional.empty()).when(recipeRepository).findById(id);
        RecipeDetailsDTO response = this.service.getRecipeDetails(id);

        assertThat(response.id()).isZero();
        assertThat(response.recipeName()).isNull();
        assertThat(response.mealType()).isNull();
        assertThat(response.portions()).isZero();
        assertThat(response.preparationTime()).isZero();
        assertThat(response.preparation()).isNull();
        assertThat(response.creatorUserName()).isNull();
        assertThat(response.products()).isNull();
        assertThat(response.recipeImage()).isNull();
        assertThat(response.cuisine()).isNull();
    }

    @Test
    void test_createRecipeSuccessful() {
        final byte[] imageData = new byte[0];
        final ProductDTO productDTO = new ProductDTO(id, productName, Category.CEREAL);
        final RecipeProductDTO recipeProductDTO = new RecipeProductDTO(id, productDTO, 100, Unit.GRAMS);
        final CreateRecipeRequest request = new CreateRecipeRequest(
                recipeName, "preparation preparation", 15,
                MealType.APPETIZER, "cuisine", 1, true, Collections.singletonList(recipeProductDTO)
        );
        MultipartFile image = new MockMultipartFile("image.jpg", "image.jpg", "image/jpeg", new byte[0]);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        try (MockedStatic<ImageUtil> imageUtilMockedStatic = mockStatic(ImageUtil.class)) {
            imageUtilMockedStatic.when(() -> ImageUtil.compressImage(image.getBytes()))
                    .thenReturn(imageData);

            doReturn(null).when(recipeRepository).save(this.recipeArgumentCaptor.capture());
            this.service.createRecipe(email, request, image);

            Recipe createdRecipe = this.recipeArgumentCaptor.getValue();
            assertThat(createdRecipe.getCreator()).isEqualTo(user);
            assertThat(createdRecipe.getRecipeImage()).isEqualTo(imageData);
            assertThat(createdRecipe.getRecipeName()).isEqualTo(request.recipeName());
            assertThat(createdRecipe.getPreparation()).isEqualTo(request.preparation());
            assertThat(createdRecipe.getPreparationTime()).isEqualTo(request.preparationTime());
            assertThat(createdRecipe.getPortions()).isEqualTo(request.portions());
            assertThat(createdRecipe.getCuisine()).isEqualTo(request.cuisine());
            assertThat(createdRecipe.getMealType()).isEqualTo(request.mealType());
            assertThat(createdRecipe.getRecipeProducts()).hasSize(request.products().size());
            assertThat(createdRecipe.getRecipeProducts().get(0).getProduct().getProductName()).isEqualTo(request.products().get(0).product().productName());
            assertThat(createdRecipe.getRecipeProducts().get(0).getProduct().getCategory()).isEqualTo(request.products().get(0).product().category());
            assertThat(createdRecipe.getRecipeProducts().get(0).getUnit()).isEqualTo(request.products().get(0).unit());
            assertThat(createdRecipe.getRecipeProducts().get(0).getQuantity()).isEqualTo(request.products().get(0).quantity());
        }
    }

    @Test
    void test_createRecipeImageContentTypeNull() {
        final ProductDTO productDTO = new ProductDTO(id, productName, Category.CEREAL);
        final RecipeProductDTO recipeProductDTO = new RecipeProductDTO(id, productDTO, 100, Unit.GRAMS);
        final CreateRecipeRequest request = new CreateRecipeRequest(
                recipeName, "preparation preparation", 15,
                MealType.APPETIZER, "cuisine", 1, true, Collections.singletonList(recipeProductDTO)
        );
        MultipartFile image = new MockMultipartFile("image.jpg", "image.jpg", null, new byte[0]);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> this.service.createRecipe(email, request, image))
                .isInstanceOf(UserPerformedForbiddenActionException.class);
        verify(recipeRepository, times(0)).save(any(Recipe.class));
    }

    @Test
    void test_createRecipeSuccessfulReadingImageDataFailed() throws IOException {
        final ProductDTO productDTO = new ProductDTO(id, productName, Category.CEREAL);
        final RecipeProductDTO recipeProductDTO = new RecipeProductDTO(id, productDTO, 100, Unit.GRAMS);
        final CreateRecipeRequest request = new CreateRecipeRequest(
                recipeName, "preparation preparation", 15,
                MealType.APPETIZER, "cuisine", 1, true, Collections.singletonList(recipeProductDTO)
        );
        MultipartFile image = mock(MultipartFile.class);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn("image/jpeg").when(image).getContentType();
        doThrow(new IOException()).when(image).getBytes();
        doReturn(null).when(recipeRepository).save(this.recipeArgumentCaptor.capture());
        this.service.createRecipe(email, request, image);

        Recipe createdRecipe = this.recipeArgumentCaptor.getValue();
        assertThat(createdRecipe.getCreator()).isEqualTo(user);
        assertThat(createdRecipe.getRecipeImage()).isNull();
        assertThat(createdRecipe.getRecipeName()).isEqualTo(request.recipeName());
        assertThat(createdRecipe.getPreparation()).isEqualTo(request.preparation());
        assertThat(createdRecipe.getPreparationTime()).isEqualTo(request.preparationTime());
        assertThat(createdRecipe.getPortions()).isEqualTo(request.portions());
        assertThat(createdRecipe.getCuisine()).isEqualTo(request.cuisine());
        assertThat(createdRecipe.getMealType()).isEqualTo(request.mealType());
        assertThat(createdRecipe.getRecipeProducts()).hasSize(request.products().size());
        assertThat(createdRecipe.getRecipeProducts().get(0).getProduct().getProductName()).isEqualTo(request.products().get(0).product().productName());
        assertThat(createdRecipe.getRecipeProducts().get(0).getProduct().getCategory()).isEqualTo(request.products().get(0).product().category());
        assertThat(createdRecipe.getRecipeProducts().get(0).getUnit()).isEqualTo(request.products().get(0).unit());
        assertThat(createdRecipe.getRecipeProducts().get(0).getQuantity()).isEqualTo(request.products().get(0).quantity());
    }

    @Test
    void test_deleteRecipeSuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(recipe)).when(recipeRepository).findById(id);
        this.service.deleteRecipe(email, id);

        verify(recipeRepository).delete(this.recipeArgumentCaptor.capture());
        Recipe deletedRecipe = this.recipeArgumentCaptor.getValue();
        assertThat(deletedRecipe.getCreator()).isEqualTo(user);
        assertThat(deletedRecipe.getRecipeImage()).isNull();
        assertThat(deletedRecipe.getId()).isEqualTo(recipe.getId());
        assertThat(deletedRecipe.getRecipeName()).isEqualTo(recipe.getRecipeName());
        assertThat(deletedRecipe.getPreparation()).isEqualTo(recipe.getPreparation());
        assertThat(deletedRecipe.getPreparationTime()).isEqualTo(recipe.getPreparationTime());
        assertThat(deletedRecipe.getPortions()).isEqualTo(recipe.getPortions());
        assertThat(deletedRecipe.getCuisine()).isEqualTo(recipe.getCuisine());
        assertThat(deletedRecipe.getMealType()).isEqualTo(recipe.getMealType());
        assertThat(deletedRecipe.getRecipeProducts()).hasSize(recipe.getRecipeProducts().size());
        assertThat(deletedRecipe.getRecipeProducts().get(0).getProduct().getProductName())
                .isEqualTo(recipe.getRecipeProducts().get(0).getProduct().getProductName());
        assertThat(deletedRecipe.getRecipeProducts().get(0).getProduct().getCategory())
                .isEqualTo(recipe.getRecipeProducts().get(0).getProduct().getCategory());
        assertThat(deletedRecipe.getRecipeProducts().get(0).getUnit())
                .isEqualTo(recipe.getRecipeProducts().get(0).getUnit());
        assertThat(deletedRecipe.getRecipeProducts().get(0).getQuantity())
                .isEqualTo(recipe.getRecipeProducts().get(0).getQuantity());
    }

    @Test
    void test_deleteRecipeNotFoundRecipe() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.empty()).when(recipeRepository).findById(id);

        assertThatThrownBy(() -> this.service.deleteRecipe(email, id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Recipe does not exists");
        verify(recipeRepository, times(0)).delete(any(Recipe.class));
    }

    @Test
    void test_deleteRecipeUserDeletingNotHisRecipe() {
        recipe.setCreator(User.builder().id(100).build());

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(recipe)).when(recipeRepository).findById(id);

        assertThatThrownBy(() -> this.service.deleteRecipe(email, id))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("You did not create this recipe so you cannot delete it");
        verify(recipeRepository, times(0)).delete(any(Recipe.class));
    }

    @Test
    void test_updateRecipeSuccessful() {
        final byte[] imageData = new byte[0];
        final ProductDTO productDTO = new ProductDTO(id, "productName", Category.CEREAL);
        final RecipeProductDTO recipeProductDTO = new RecipeProductDTO(0L, productDTO, 200, Unit.PIECES);
        final UpdateRecipeRequest request = new UpdateRecipeRequest(
                1L, "newRecipeName", "new preparation preparation", 30,
                MealType.BREAKFAST, "cuisine2", 2, true, Collections.singletonList(recipeProductDTO)
        );
        MultipartFile image = new MockMultipartFile("image.jpg", "image.jpg", "image/jpeg", new byte[0]);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(recipe)).when(recipeRepository).findById(id);

        try (MockedStatic<ImageUtil> imageUtilMockedStatic = mockStatic(ImageUtil.class)) {
            imageUtilMockedStatic.when(() -> ImageUtil.compressImage(image.getBytes()))
                    .thenReturn(imageData);
            this.service.updateRecipe(email, request, image);

            verify(recipeProductRepository).deleteAll(List.of(recipeProduct));
            verify(recipeRepository).save(this.recipeArgumentCaptor.capture());
            Recipe updatedRecipe = this.recipeArgumentCaptor.getValue();
            assertThat(updatedRecipe.getRecipeImage()).isEqualTo(imageData);
            assertThat(updatedRecipe.getRecipeName()).isEqualTo(request.recipeName());
            assertThat(updatedRecipe.getPreparation()).isEqualTo(request.preparation());
            assertThat(updatedRecipe.getPreparationTime()).isEqualTo(request.preparationTime());
            assertThat(updatedRecipe.getPortions()).isEqualTo(request.portions());
            assertThat(updatedRecipe.getCuisine()).isEqualTo(request.cuisine());
            assertThat(updatedRecipe.getMealType()).isEqualTo(request.mealType());
            assertThat(updatedRecipe.getRecipeProducts()).hasSize(request.products().size());
            assertThat(updatedRecipe.getRecipeProducts().get(0).getUnit())
                    .isEqualTo(request.products().get(0).unit());
            assertThat(updatedRecipe.getRecipeProducts().get(0).getQuantity())
                    .isEqualTo(request.products().get(0).quantity());
        }
    }

    @Test
    void test_updateRecipeUserUpdatingNotHisRecipe() {
        recipe.setCreator(User.builder().id(2L).build());
        final ProductDTO productDTO = new ProductDTO(id, "productName", Category.CEREAL);
        final RecipeProductDTO recipeProductDTO = new RecipeProductDTO(0L, productDTO, 200, Unit.PIECES);
        final UpdateRecipeRequest request = new UpdateRecipeRequest(
                1L, "newRecipeName", "new preparation preparation", 30,
                MealType.BREAKFAST, "cuisine2", 2, true, Collections.singletonList(recipeProductDTO)
        );
        MultipartFile image = new MockMultipartFile("image.jpg", "image.jpg", "image/jpeg", new byte[0]);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(recipe)).when(recipeRepository).findById(id);

        assertThatThrownBy(() -> this.service.updateRecipe(email, request, image))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("You did not create this recipe so you cannot update it");
        verify(recipeProductRepository, times(0)).deleteAll(List.of(recipeProduct));
        verify(recipeRepository, times(0)).save(recipe);
    }

    @Test
    void test_updateRecipeImageContentTypeNull() throws IOException {
        final ProductDTO productDTO = new ProductDTO(id, "productName", Category.CEREAL);
        final RecipeProductDTO recipeProductDTO = new RecipeProductDTO(0L, productDTO, 200, Unit.PIECES);
        final UpdateRecipeRequest request = new UpdateRecipeRequest(
                1L, "newRecipeName", "new preparation preparation", 30,
                MealType.BREAKFAST, "cuisine2", 2, true, Collections.singletonList(recipeProductDTO)
        );
        MultipartFile image = mock(MultipartFile.class);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(recipe)).when(recipeRepository).findById(id);
        doThrow(new IOException()).when(image).getBytes();
        doReturn("wrongContentType").when(image).getContentType();

        assertThatThrownBy(() -> this.service.updateRecipe(email, request, image))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("You tried to save file in forbidden format");
        verify(recipeProductRepository, times(0)).deleteAll(List.of(recipeProduct));
        verify(recipeRepository, times(0)).save(recipe);
    }

    @Test
    void test_updateRecipeModifyingRecipeProductsFromDifferentRecipe() {
        final byte[] imageData = new byte[0];
        final ProductDTO productDTO = new ProductDTO(id, "productName", Category.CEREAL);
        final RecipeProductDTO recipeProductDTO = new RecipeProductDTO(2L, productDTO, 200, Unit.PIECES);
        final UpdateRecipeRequest request = new UpdateRecipeRequest(
                1L, "newRecipeName", "new preparation preparation", 30,
                MealType.BREAKFAST, "cuisine2", 2, true, Collections.singletonList(recipeProductDTO)
        );
        MultipartFile image = new MockMultipartFile("image.jpg", "image.jpg", "image/jpeg", new byte[0]);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(recipe)).when(recipeRepository).findById(id);

        try (MockedStatic<ImageUtil> imageUtilMockedStatic = mockStatic(ImageUtil.class)) {
            imageUtilMockedStatic.when(() -> ImageUtil.compressImage(image.getBytes()))
                    .thenReturn(imageData);

            assertThatThrownBy(() -> this.service.updateRecipe(email, request, image))
                    .isInstanceOf(UserPerformedForbiddenActionException.class)
                    .hasMessage("You tried to modify recipe product from different recipe");
            verify(recipeProductRepository, times(0)).deleteAll(List.of(recipeProduct));
            verify(recipeRepository, times(0)).save(recipe);
        }
    }

    @Test
    void test_updateRecipeSuccessfulWithModifyingProduct() {
        final byte[] imageData = new byte[0];
        final ProductDTO productDTO = new ProductDTO(id, "productName", Category.CEREAL);
        final RecipeProductDTO recipeProductDTO = new RecipeProductDTO(id, productDTO, 200, Unit.PIECES);
        final UpdateRecipeRequest request = new UpdateRecipeRequest(
                1L, "newRecipeName", "new preparation preparation", 30,
                MealType.BREAKFAST, "cuisine2", 2, true, Collections.singletonList(recipeProductDTO)
        );
        MultipartFile image = new MockMultipartFile("image.jpg", "image.jpg", "image/jpeg", new byte[0]);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(recipe)).when(recipeRepository).findById(id);

        try (MockedStatic<ImageUtil> imageUtilMockedStatic = mockStatic(ImageUtil.class)) {
            imageUtilMockedStatic.when(() -> ImageUtil.compressImage(image.getBytes()))
                    .thenReturn(imageData);
            this.service.updateRecipe(email, request, image);

            verify(recipeProductRepository).deleteAll(anyList());
            verify(recipeRepository).save(this.recipeArgumentCaptor.capture());
            Recipe updatedRecipe = this.recipeArgumentCaptor.getValue();
            assertThat(updatedRecipe.getRecipeImage()).isEqualTo(imageData);
            assertThat(updatedRecipe.getRecipeName()).isEqualTo(request.recipeName());
            assertThat(updatedRecipe.getPreparation()).isEqualTo(request.preparation());
            assertThat(updatedRecipe.getPreparationTime()).isEqualTo(request.preparationTime());
            assertThat(updatedRecipe.getPortions()).isEqualTo(request.portions());
            assertThat(updatedRecipe.getCuisine()).isEqualTo(request.cuisine());
            assertThat(updatedRecipe.getMealType()).isEqualTo(request.mealType());
            assertThat(updatedRecipe.getRecipeProducts()).hasSize(request.products().size());
            assertThat(updatedRecipe.getRecipeProducts().get(0).getUnit())
                    .isEqualTo(request.products().get(0).unit());
            assertThat(updatedRecipe.getRecipeProducts().get(0).getQuantity())
                    .isEqualTo(request.products().get(0).quantity());
        }
    }

    @Test
    void test_reserveRecipeProductsInPantrySuccessful() {
        final List<RecipeProduct> listToReturn = Collections.singletonList(recipeProduct);

        doReturn(listToReturn).when(pantryProductService).reservePantryProductsFromRecipe(id, user, recipe.getRecipeProducts());
        List<RecipeProduct> response = this.service.reserveRecipeProductsInPantry(user, recipe, id);

        assertThat(response).hasSize(listToReturn.size());
    }

    @Test
    void test_getRecipeProductsNotInPantrySuccessful() {
        group.setPantry(new Pantry());
        final List<RecipeProduct> listToReturn = Collections.singletonList(recipeProduct);

        doReturn(listToReturn).when(pantryProductService).getRecipeProductsNotInPantry(group.getPantry(), recipe.getRecipeProducts());
        List<RecipeProduct> response = this.service.getRecipeProductsNotInPantry(group, recipe);

        assertThat(response).hasSize(listToReturn.size());
    }

    @Test
    void test_addRecipeProductsToShoppingListSuccessful() {

        this.service.addRecipeProductsToShoppingList(user, id, recipe.getRecipeProducts());
        verify(shoppingListProductService).addRecipeProductsToShoppingList(id, user, recipe.getRecipeProducts());
    }
}

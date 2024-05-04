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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {
    final String email = "email@email.com";
    final String recipeName = "recipeName";
    final String productName = "productName";
    final String filter = "filter";
    final String col = "col";
    final Sort.Direction direction = Sort.Direction.DESC;
    final Long id = 1L;

    @Captor
    ArgumentCaptor<Recipe> recipeArgumentCaptor;

    RecipeProductMapper recipeProductMapper = new RecipeProductMapperImpl(new ProductMapperImpl());
    @Spy
    RecipeDetailsMapper recipeDetailsMapperDTO = new RecipeDetailsMapperImpl(recipeProductMapper);
    @Spy
    RecipeMapper recipeMapper = new RecipeMapperImpl();
    @Spy
    AuthorityMapper authorityMapper = new AuthorityMapperImpl();
    @Mock
    UserRepository userRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    RecipeRepository recipeRepository;
    @Mock
    RecipeProductRepository recipeProductRepository;
    @Mock
    PantryProductService pantryProductService;
    @Mock
    ShoppingListProductService shoppingListProductService;
    @InjectMocks
    RecipeServiceImpl service;

    RecipeProduct recipeProduct;
    Recipe recipe;
    User user;
    Group group;
    Authority authority;

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
            assertEquals(0 ,response.pageNr());
            assertEquals(pageResponse.getTotalElements() ,response.totalElements());
            assertEquals(pageResponse.getContent().get(0).getId(), response.content().get(0).id());
            assertEquals(pageResponse.getContent().get(0).getRecipeName(), response.content().get(0).recipeName());
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
            assertEquals(0 ,response.pageNr());
            assertEquals(pageResponse.getTotalElements() ,response.totalElements());
            assertEquals(pageResponse.getContent().get(0).getId(), response.content().get(0).id());
            assertEquals(pageResponse.getContent().get(0).getRecipeName(), response.content().get(0).recipeName());
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
            assertEquals(0 ,response.pageNr());
            assertEquals(pageResponse.getTotalElements() ,response.totalElements());
            assertEquals(pageResponse.getContent().get(0).getId(), response.content().get(0).id());
            assertEquals(pageResponse.getContent().get(0).getRecipeName(), response.content().get(0).recipeName());
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
            assertEquals(0 ,response.pageNr());
            assertEquals(pageResponse.getTotalElements() ,response.totalElements());
            assertEquals(pageResponse.getContent().get(0).getId(), response.content().get(0).id());
            assertEquals(pageResponse.getContent().get(0).getRecipeName(), response.content().get(0).recipeName());
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
            assertEquals(0 ,response.pageNr());
            assertEquals(pageResponse.getTotalElements() ,response.totalElements());
            assertEquals(pageResponse.getContent().get(0).getId(), response.content().get(0).id());
            assertEquals(pageResponse.getContent().get(0).getRecipeName(), response.content().get(0).recipeName());
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
            assertEquals(0 ,response.pageNr());
            assertEquals(pageResponse.getTotalElements() ,response.totalElements());
            assertEquals(pageResponse.getContent().get(0).getId(), response.content().get(0).id());
            assertEquals(pageResponse.getContent().get(0).getRecipeName(), response.content().get(0).recipeName());
        }
    }

    @Test
    void test_getRecipeDetailsSuccessful() {

        doReturn(Optional.of(recipe)).when(recipeRepository).findById(id);

        try (MockedStatic<ImageUtil> imageUtilMockedStatic = mockStatic(ImageUtil.class)) {
            imageUtilMockedStatic.when(() -> ImageUtil.decompressImage(any()))
                    .thenReturn(new byte[0]);
            RecipeDetailsDTO response = this.service.getRecipeDetails(id);

            assertEquals(recipe.getId(), response.id());
            assertEquals(recipe.getRecipeName(), response.recipeName());
            assertEquals(recipe.getMealType(), response.mealType());
            assertEquals(recipe.getPortions(), response.portions());
            assertEquals(recipe.getPreparationTime(), response.preparationTime());
            assertEquals(recipe.getPreparation(), response.preparation());
            assertEquals(recipe.getCreator().getUsername(), response.creatorUserName());
            assertEquals(recipe.getRecipeProducts().size(), response.products().size());
            assertEquals(recipe.getRecipeProducts().get(0).getId(), response.products().get(0).id());
            assertEquals(recipe.getRecipeProducts().get(0).getUnit(), response.products().get(0).unit());
            assertEquals(recipe.getRecipeProducts().get(0).getQuantity(), response.products().get(0).quantity());
            assertEquals(recipe.getRecipeProducts().get(0).getProduct().getProductName(), response.products().get(0).product().productName());
            assertEquals(0, response.recipeImage().length);
            assertNull(response.cuisine());
        }
    }

    @Test
    void test_getRecipeDetailsRecipeNotFound() {

        doReturn(Optional.empty()).when(recipeRepository).findById(id);
        RecipeDetailsDTO response = this.service.getRecipeDetails(id);

        assertEquals(0, response.id());
        assertNull(response.recipeName());
        assertNull(response.mealType());
        assertEquals(0, response.portions());
        assertEquals(0, response.preparationTime());
        assertNull(response.preparation());
        assertNull(response.creatorUserName());
        assertNull(response.products());
        assertNull(response.recipeImage());
        assertNull(response.cuisine());
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
            assertEquals(user, createdRecipe.getCreator());
            assertEquals(imageData, createdRecipe.getRecipeImage());
            assertEquals(request.recipeName(), createdRecipe.getRecipeName());
            assertEquals(request.preparation(), createdRecipe.getPreparation());
            assertEquals(request.preparationTime(), createdRecipe.getPreparationTime());
            assertEquals(request.portions(), createdRecipe.getPortions());
            assertEquals(request.cuisine(), createdRecipe.getCuisine());
            assertEquals(request.mealType(), createdRecipe.getMealType());
            assertEquals(request.products().size(), createdRecipe.getRecipeProducts().size());
            assertEquals(request.products().get(0).product().productName(), createdRecipe.getRecipeProducts().get(0).getProduct().getProductName());
            assertEquals(request.products().get(0).product().category(), createdRecipe.getRecipeProducts().get(0).getProduct().getCategory());
            assertEquals(request.products().get(0).unit(), createdRecipe.getRecipeProducts().get(0).getUnit());
            assertEquals(request.products().get(0).quantity(), createdRecipe.getRecipeProducts().get(0).getQuantity());
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

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.service.createRecipe(email, request, image));
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
        assertEquals(user, createdRecipe.getCreator());
        assertNull(createdRecipe.getRecipeImage());
        assertEquals(request.recipeName(), createdRecipe.getRecipeName());
        assertEquals(request.preparation(), createdRecipe.getPreparation());
        assertEquals(request.preparationTime(), createdRecipe.getPreparationTime());
        assertEquals(request.portions(), createdRecipe.getPortions());
        assertEquals(request.cuisine(), createdRecipe.getCuisine());
        assertEquals(request.mealType(), createdRecipe.getMealType());
        assertEquals(request.products().size(), createdRecipe.getRecipeProducts().size());
        assertEquals(request.products().get(0).product().productName(), createdRecipe.getRecipeProducts().get(0).getProduct().getProductName());
        assertEquals(request.products().get(0).product().category(), createdRecipe.getRecipeProducts().get(0).getProduct().getCategory());
        assertEquals(request.products().get(0).unit(), createdRecipe.getRecipeProducts().get(0).getUnit());
        assertEquals(request.products().get(0).quantity(), createdRecipe.getRecipeProducts().get(0).getQuantity());
    }

    @Test
    void test_deleteRecipeSuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(recipe)).when(recipeRepository).findById(id);
        this.service.deleteRecipe(email, id);
        
        verify(recipeRepository).delete(this.recipeArgumentCaptor.capture());
        Recipe deletedRecipe = this.recipeArgumentCaptor.getValue();
        assertEquals(user, deletedRecipe.getCreator());
        assertNull(deletedRecipe.getRecipeImage());
        assertEquals(recipe.getId(), deletedRecipe.getId());
        assertEquals(recipe.getRecipeName(), deletedRecipe.getRecipeName());
        assertEquals(recipe.getPreparation(), deletedRecipe.getPreparation());
        assertEquals(recipe.getPreparationTime(), deletedRecipe.getPreparationTime());
        assertEquals(recipe.getPortions(), deletedRecipe.getPortions());
        assertEquals(recipe.getCuisine(), deletedRecipe.getCuisine());
        assertEquals(recipe.getMealType(), deletedRecipe.getMealType());
        assertEquals(recipe.getRecipeProducts().size(), deletedRecipe.getRecipeProducts().size());
        assertEquals(recipe.getRecipeProducts().get(0).getProduct().getProductName(), deletedRecipe.getRecipeProducts().get(0).getProduct().getProductName());
        assertEquals(recipe.getRecipeProducts().get(0).getProduct().getCategory(), deletedRecipe.getRecipeProducts().get(0).getProduct().getCategory());
        assertEquals(recipe.getRecipeProducts().get(0).getUnit(), deletedRecipe.getRecipeProducts().get(0).getUnit());
        assertEquals(recipe.getRecipeProducts().get(0).getQuantity(), deletedRecipe.getRecipeProducts().get(0).getQuantity());
    }

    @Test
    void test_deleteRecipeNotFoundRecipe() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.empty()).when(recipeRepository).findById(id);

        Exception ex = assertThrows(ResourceNotFoundException.class, () -> this.service.deleteRecipe(email, id));
        assertEquals("Recipe does not exists", ex.getMessage());
        verify(recipeRepository, times(0)).delete(any(Recipe.class));
    }

    @Test
    void test_deleteRecipeUserDeletingNotHisRecipe() {
        recipe.setCreator(User.builder().id(100).build());

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(recipe)).when(recipeRepository).findById(id);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () -> this.service.deleteRecipe(email, id));
        assertEquals("You did not create this recipe so you cannot delete it", ex.getMessage());
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
            assertEquals(imageData, updatedRecipe.getRecipeImage());
            assertEquals(request.recipeName(), updatedRecipe.getRecipeName());
            assertEquals(request.preparation(), updatedRecipe.getPreparation());
            assertEquals(request.preparationTime(), updatedRecipe.getPreparationTime());
            assertEquals(request.portions(), updatedRecipe.getPortions());
            assertEquals(request.cuisine(), updatedRecipe.getCuisine());
            assertEquals(request.mealType(), updatedRecipe.getMealType());
            assertEquals(imageData, updatedRecipe.getRecipeImage());
            assertEquals(request.products().size(), updatedRecipe.getRecipeProducts().size());
            assertEquals(request.products().get(0).unit(), updatedRecipe.getRecipeProducts().get(0).getUnit());
            assertEquals(request.products().get(0).quantity(), updatedRecipe.getRecipeProducts().get(0).getQuantity());
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

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.updateRecipe(email, request, image));
        assertEquals("You did not create this recipe so you cannot update it", ex.getMessage());
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

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.updateRecipe(email, request, image));
        assertEquals("You tried to save file in forbidden format", ex.getMessage());
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

            Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                    this.service.updateRecipe(email, request, image));
            assertEquals("You tried to modify recipe product from different recipe", ex.getMessage());
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
            assertEquals(imageData, updatedRecipe.getRecipeImage());
            assertEquals(request.recipeName(), updatedRecipe.getRecipeName());
            assertEquals(request.preparation(), updatedRecipe.getPreparation());
            assertEquals(request.preparationTime(), updatedRecipe.getPreparationTime());
            assertEquals(request.portions(), updatedRecipe.getPortions());
            assertEquals(request.cuisine(), updatedRecipe.getCuisine());
            assertEquals(request.mealType(), updatedRecipe.getMealType());
            assertEquals(imageData, updatedRecipe.getRecipeImage());
            assertEquals(request.products().size(), updatedRecipe.getRecipeProducts().size());
            assertEquals(recipeProductDTO.unit(), updatedRecipe.getRecipeProducts().get(0).getUnit());
            assertEquals(recipeProductDTO.quantity(), updatedRecipe.getRecipeProducts().get(0).getQuantity());
        }
    }

    @Test
    void test_reserveRecipeProductsInPantrySuccessful() {
        final List<RecipeProduct> listToReturn = Collections.singletonList(recipeProduct);

        doReturn(listToReturn).when(pantryProductService).reservePantryProductsFromRecipe(id, user, recipe.getRecipeProducts());
        List<RecipeProduct> response = this.service.reserveRecipeProductsInPantry(user, recipe, id);

        assertEquals(listToReturn.size(), response.size());
    }

    @Test
    void test_getRecipeProductsNotInPantrySuccessful() {
        group.setPantry(new Pantry());
        final List<RecipeProduct> listToReturn = Collections.singletonList(recipeProduct);

        doReturn(listToReturn).when(pantryProductService).getRecipeProductsNotInPantry(group.getPantry(), recipe.getRecipeProducts());
        List<RecipeProduct> response = this.service.getRecipeProductsNotInPantry(group, recipe);

        assertEquals(listToReturn.size(), response.size());
    }

    @Test
    void test_addRecipeProductsToShoppingListSuccessful() {

        this.service.addRecipeProductsToShoppingList(user, id, recipe.getRecipeProducts());
        verify(shoppingListProductService).addRecipeProductsToShoppingList(id, user, recipe.getRecipeProducts());
    }
}

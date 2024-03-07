package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.ValidationException;
import com.cookie.app.model.dto.*;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.MealType;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.RecipeDetailsMapperDTO;
import com.cookie.app.model.mapper.RecipeMapperDTO;
import com.cookie.app.model.request.CreateRecipeRequest;
import com.cookie.app.model.response.CreateRecipeResponse;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.RecipeRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryProductService;
import com.cookie.app.service.RecipeService;
import com.cookie.app.service.ShoppingListProductService;
import com.cookie.app.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecipeServiceImpl extends AbstractCookieService implements RecipeService {
    private final RecipeRepository recipeRepository;
    private final PantryProductService pantryProductService;
    private final ShoppingListProductService shoppingListProductService;
    private final RecipeMapperDTO recipeMapperDTO;
    private final RecipeDetailsMapperDTO recipeDetailsMapperDTO;

    protected RecipeServiceImpl(UserRepository userRepository,
                                ProductRepository productRepository,
                                AuthorityMapperDTO authorityMapperDTO,
                                RecipeRepository recipeRepository,
                                PantryProductService pantryProductService,
                                ShoppingListProductService shoppingListProductService,
                                RecipeMapperDTO recipeMapperDTO,
                                RecipeDetailsMapperDTO recipeDetailsMapperDTO) {
        super(userRepository, productRepository, authorityMapperDTO);
        this.recipeRepository = recipeRepository;
        this.pantryProductService = pantryProductService;
        this.shoppingListProductService = shoppingListProductService;
        this.recipeMapperDTO = recipeMapperDTO;
        this.recipeDetailsMapperDTO = recipeDetailsMapperDTO;
    }

    @Override
    public Page<RecipeDTO> getRecipes(int page,
                                      String filterValue,
                                      int prepTime,
                                      int portions,
                                      List<MealType> mealTypes,
                                      String sortColName,
                                      String sortDirection) {
        PageRequest pageRequest = super.createPageRequest(page, sortColName, sortDirection);
        Set<String> selectedMealTypes = getMealTypesAsStrings(mealTypes);

        if (StringUtils.isBlank(filterValue)) {
            return this.recipeRepository
                    .findRecipes(prepTime, portions, selectedMealTypes, pageRequest)
                    .map(recipeMapperDTO::apply);
        }

        return this.recipeRepository
                .findRecipesByFilter(filterValue, prepTime, portions, selectedMealTypes, pageRequest)
                .map(recipeMapperDTO::apply);
    }

    @Override
    public Page<RecipeDTO> getUserRecipes(String userEmail,
                                          int page,
                                          String filterValue,
                                          int prepTime,
                                          int portions,
                                          List<MealType> mealTypes,
                                          String sortColName,
                                          String sortDirection) {
        User user = super.getUserByEmail(userEmail);
        PageRequest pageRequest = super.createPageRequest(page, sortColName, sortDirection);
        Set<String> selectedMealTypes = getMealTypesAsStrings(mealTypes);

        if (StringUtils.isBlank(filterValue)) {
            return this.recipeRepository
                    .findCreatorRecipes(user.getId(), prepTime, portions, selectedMealTypes, pageRequest)
                    .map(recipeMapperDTO::apply);
        }

        return this.recipeRepository
                .findCreatorRecipesByFilter(user.getId(), filterValue, prepTime, portions, selectedMealTypes, pageRequest)
                .map(recipeMapperDTO::apply);
    }


    @Override
    public RecipeDetailsDTO getRecipeDetails(long recipeId) {
        Optional<Recipe> recipeOptional = this.recipeRepository.findById(recipeId);

        if (recipeOptional.isEmpty()) {
            return new RecipeDetailsDTO(0, null, null, 0, null, null,0, null, null, null);
        }

        return this.recipeDetailsMapperDTO.apply(recipeOptional.get());
    }

    @Override
    public CreateRecipeResponse createRecipe(String userEmail, CreateRecipeRequest recipeDetailsDTO, MultipartFile recipeImage) {
        User user = super.getUserByEmail(userEmail);

        if (recipeDetailsDTO.id() != 0) {
            log.info("User: {} tried to create recipe with id set to {}", userEmail, recipeDetailsDTO.id());
            throw new ValidationException("Recipe id must be 0 while creating it");
        }

        Recipe recipe = mapRecipeDetailsToRecipe(user, recipeDetailsDTO, recipeImage);
        this.recipeRepository.save(recipe);

        return new CreateRecipeResponse(recipe.getId());
    }

    @Override
    public void deleteRecipe(String userEmail, long recipeId) {
        User user = super.getUserByEmail(userEmail);
        Recipe recipe = getRecipeById(recipeId, userEmail);

        if (recipe.getCreator().getId() != user.getId()) {
            log.info("User: {} tried to delete recipe which they did not created", userEmail);
            throw new UserPerformedForbiddenActionException("You did not create this recipe so you cannot delete it");
        }

        this.recipeRepository.delete(recipe);
    }

    @Override
    public CreateRecipeResponse modifyRecipe(String userEmail, CreateRecipeRequest recipeDetailsDTO, MultipartFile recipeImage) {
        User user = super.getUserByEmail(userEmail);
        Recipe recipe = getRecipeById(recipeDetailsDTO.id(), userEmail);

        if (recipe.getCreator().getId() != user.getId()) {
            log.info("User: {} tried to modify recipe which they did not created", userEmail);
            throw new UserPerformedForbiddenActionException("You did not create this recipe so you cannot delete it");
        }

        modifyRecipe(recipe, recipeDetailsDTO, recipeImage);
        this.recipeRepository.save(recipe);

        return new CreateRecipeResponse(recipe.getId());
    }

    @Override
    public List<PantryProductDTO> reserveRecipeProductsInPantry(User user, Recipe recipe, long pantryId) {
        return this.pantryProductService.reservePantryProductsFromRecipe(pantryId, user, recipe.getRecipeProducts());
    }

    @Override
    public List<ShoppingListProductDTO> addRecipeProductsToShoppingList(User user,
                                                                        Recipe recipe,
                                                                        long listId,
                                                                        Group group) {
        Optional<ShoppingList> shoppingList = group.getShoppingLists()
                .stream()
                .filter(list -> list.getId() == listId)
                .findAny();

        if (shoppingList.isEmpty()) {
            log.info("User: {} tried to add recipe products to shopping list which does not belong to their group", user.getEmail());
            throw new UserPerformedForbiddenActionException("You tried to add products to " +
                    "shopping list which does not belong to your group");
        }

        List<RecipeProduct> recipeProductsToAdd = this.pantryProductService
                .getRecipeProductsNotInPantry(group.getPantry(), recipe.getRecipeProducts());

        return this.shoppingListProductService.addRecipeProductsToShoppingList(listId, user, recipeProductsToAdd);
    }

    private Set<String> getMealTypesAsStrings(List<MealType> mealTypes) {
        if (mealTypes.isEmpty()) {
            return MealType.ALL_MEAL_TYPES
                    .stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet());
        }

        return mealTypes
                .stream()
                .map(Enum::name).collect(Collectors.toSet());
    }

    private void modifyRecipe(Recipe recipe, CreateRecipeRequest recipeDetailsDTO, MultipartFile recipeImage) {
        if (!recipe.getRecipeName().equals(recipeDetailsDTO.recipeName())) {
            recipe.setRecipeName(recipeDetailsDTO.recipeName());
        }
        if (!recipe.getPreparation().equals(recipeDetailsDTO.preparation())) {
            recipe.setPreparation(recipeDetailsDTO.preparation());
        }
        if (recipe.getPreparationTime() != recipeDetailsDTO.preparationTime()) {
            recipe.setPreparationTime(recipeDetailsDTO.preparationTime());
        }
        if (!recipe.getCuisine().equals(recipeDetailsDTO.cuisine())) {
            recipe.setCuisine(recipeDetailsDTO.cuisine());
        }
        if (recipe.getPortions() != recipeDetailsDTO.portions()) {
            recipe.setPortions(recipeDetailsDTO.portions());
        }

        if (recipeDetailsDTO.updateImage()) {
            byte[] newImage = new byte[0];

            if (recipeImage != null) {
                try {
                    newImage = recipeImage.getBytes();
                } catch (IOException exception) {
                    log.info("Reading file data failed!");
                }

                String contentType = recipeImage.getContentType();

                if (contentType != null && !contentType.equals("image/jpeg") &&
                        !contentType.equals("image/png") && newImage.length <= 0
                ) {
                    throw new UserPerformedForbiddenActionException("You tried to save file in forbidden format");
                }
            }

            recipe.setRecipeImage(ImageUtil.compressImage(newImage));
        }

        Map<Long, RecipeProductDTO> recipeProductDTOMap = recipeDetailsDTO.products()
                .stream()
                .filter(recipeProductDTO -> recipeProductDTO.id() > 0)
                .collect(Collectors.toMap(RecipeProductDTO::id, Function.identity()));

        for (RecipeProduct recipeProduct : recipe.getRecipeProducts()) {
            if (!recipeProductDTOMap.containsKey(recipeProduct.getId())) {
                recipe.getRecipeProducts().remove(recipeProduct);
                continue;
            }

            RecipeProductDTO modifiedProduct = recipeProductDTOMap.get(recipeProduct.getId());
            Optional<Product> productOptional = this.productRepository.findById(modifiedProduct.product().productId());
            Product product = productOptional.orElse(
                    Product.builder()
                            .productName(modifiedProduct.product().productName())
                            .category(modifiedProduct.product().category())
                            .build()
            );

            if (!recipeProduct.getProduct().equals(product)) {
                recipeProduct.setProduct(product);
            }
            if (recipeProduct.getQuantity() != modifiedProduct.quantity()) {
                recipeProduct.setQuantity(modifiedProduct.quantity());
            }
            if (recipeProduct.getUnit() !=  modifiedProduct.unit()) {
                recipeProduct.setUnit(recipeProduct.getUnit());
            }

            recipeProductDTOMap.remove(recipeProduct.getId());
        }

        if (!recipeProductDTOMap.values().isEmpty()) {
            log.info("User tried to modify recipe product from different recipe");
            throw new UserPerformedForbiddenActionException("You tried to modify recipe product from different recipe");
        }

        List<RecipeProduct> addedProducts = recipeDetailsDTO.products()
                .stream()
                .filter(recipeProductDTO -> recipeProductDTO.id() == 0)
                .map(recipeProductDTO -> this.mapToRecipeProduct(recipeProductDTO, recipe))
                .toList();

        recipe.getRecipeProducts().addAll(addedProducts);
    }

    private Recipe getRecipeById(long recipeId, String userEmail) {
        if (recipeId == 0) {
            log.info("User: {} tried to access recipe which does not exists", userEmail);
            throw new UserPerformedForbiddenActionException("Recipe does not exists");
        }

        Optional<Recipe> recipeOptional = this.recipeRepository.findById(recipeId);

        if (recipeOptional.isEmpty()) {
            log.info("User: {} tried to access recipe which does not exists", userEmail);
            throw new UserPerformedForbiddenActionException("Recipe does not exists");
        }

        return recipeOptional.get();
    }


    private Recipe mapRecipeDetailsToRecipe(User creator, CreateRecipeRequest createRecipeRequest, MultipartFile recipeImage) {
        byte[] recipeImg = null;

        if (recipeImage != null) {
            String contentType = recipeImage.getContentType();

            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                throw new UserPerformedForbiddenActionException("You tried to save file in forbidden format");
            }

            try {
                recipeImg = ImageUtil.compressImage(recipeImage.getBytes());
            } catch (IOException exception) {
                log.info("Reading file data failed!");
            }
        }


        Recipe recipe =  Recipe.builder()
                .recipeName(createRecipeRequest.recipeName())
                .preparation(createRecipeRequest.preparation())
                .preparationTime(createRecipeRequest.preparationTime())
                .mealType(createRecipeRequest.mealType())
                .cuisine(createRecipeRequest.cuisine())
                .portions(createRecipeRequest.portions())
                .recipeImage(recipeImg)
                .creator(creator)
                .build();

        List<RecipeProduct> recipeProducts = createRecipeRequest
                .products()
                .stream()
                .map(recipeProduct -> this.mapToRecipeProduct(recipeProduct, recipe))
                .toList();

        recipe.setRecipeProducts(recipeProducts);

        return recipe;
    }

    private RecipeProduct mapToRecipeProduct(RecipeProductDTO recipeProductDTO, Recipe recipe) {
        return RecipeProduct.builder()
                .product(super.checkIfProductExists(recipeProductDTO.product()))
                .quantity(recipeProductDTO.quantity())
                .unit(recipeProductDTO.unit())
                .recipe(recipe)
                .build();
    }
}

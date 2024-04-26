package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.model.dto.*;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.MealType;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.RecipeDetailsMapperDTO;
import com.cookie.app.model.mapper.RecipeMapperDTO;
import com.cookie.app.model.request.CreateRecipeRequest;
import com.cookie.app.model.request.UpdateRecipeRequest;
import com.cookie.app.model.response.CreateRecipeResponse;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.RecipeProductRepository;
import com.cookie.app.repository.RecipeRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryProductService;
import com.cookie.app.service.RecipeService;
import com.cookie.app.service.ShoppingListProductService;
import com.cookie.app.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public non-sealed class RecipeServiceImpl extends AbstractCookieService implements RecipeService {
    private final RecipeRepository recipeRepository;
    private final RecipeProductRepository recipeProductRepository;
    private final PantryProductService pantryProductService;
    private final ShoppingListProductService shoppingListProductService;
    private final RecipeMapperDTO recipeMapperDTO;
    private final RecipeDetailsMapperDTO recipeDetailsMapperDTO;

    public RecipeServiceImpl(UserRepository userRepository,
                             ProductRepository productRepository,
                             AuthorityMapperDTO authorityMapperDTO,
                             RecipeRepository recipeRepository,
                             RecipeProductRepository recipeProductRepository,
                             PantryProductService pantryProductService,
                             ShoppingListProductService shoppingListProductService,
                             RecipeMapperDTO recipeMapperDTO,
                             RecipeDetailsMapperDTO recipeDetailsMapperDTO) {
        super(userRepository, productRepository, authorityMapperDTO);
        this.recipeRepository = recipeRepository;
        this.recipeProductRepository = recipeProductRepository;
        this.pantryProductService = pantryProductService;
        this.shoppingListProductService = shoppingListProductService;
        this.recipeMapperDTO = recipeMapperDTO;
        this.recipeDetailsMapperDTO = recipeDetailsMapperDTO;
    }

    @Override
    public PageResult<RecipeDTO> getRecipes(int page,
                                      Integer prepTime,
                                      Integer portions,
                                      List<MealType> mealTypes,
                                      String filterValue,
                                      String sortColName,
                                      String sortDirection) {
        PageRequest pageRequest = super.createPageRequest(page - 1, sortColName, sortDirection);
        Set<String> selectedMealTypes = getMealTypesAsStrings(mealTypes);
        prepTime = prepTime == null ? 0 : prepTime;
        portions = portions == null ? 0 : portions;

        if (StringUtils.isBlank(filterValue)) {
            return new PageResult<>(this.recipeRepository
                    .findRecipes(prepTime, portions, selectedMealTypes, pageRequest)
                    .map(recipeMapperDTO::apply));
        }

        return new PageResult<>(this.recipeRepository
                .findRecipesByFilter(filterValue, prepTime, portions, selectedMealTypes, pageRequest)
                .map(recipeMapperDTO::apply));
    }

    @Override
    public PageResult<RecipeDTO> getUserRecipes(String userEmail,
                                          int page,
                                          Integer prepTime,
                                          Integer portions,
                                          List<MealType> mealTypes,
                                          String filterValue,
                                          String sortColName,
                                          String sortDirection) {
        User user = super.getUserByEmail(userEmail);
        PageRequest pageRequest = super.createPageRequest(page - 1, sortColName, sortDirection);
        Set<String> selectedMealTypes = getMealTypesAsStrings(mealTypes);
        prepTime = prepTime == null ? 0 : prepTime;
        portions = portions == null ? 0 : portions;

        if (StringUtils.isBlank(filterValue)) {
            return new PageResult<>(this.recipeRepository
                    .findCreatorRecipes(user.getId(), prepTime, portions, selectedMealTypes, pageRequest)
                    .map(recipeMapperDTO::apply));
        }

        return new PageResult<>(this.recipeRepository
                .findCreatorRecipesByFilter(user.getId(), filterValue, prepTime, portions, selectedMealTypes, pageRequest)
                .map(recipeMapperDTO::apply));
    }


    @Override
    public RecipeDetailsDTO getRecipeDetails(long recipeId) {
        Optional<Recipe> recipeOptional = this.recipeRepository.findById(recipeId);

        return recipeOptional.map(this.recipeDetailsMapperDTO::apply)
                .orElseGet(() -> new RecipeDetailsDTO(0, null, null, 0, null, null,0, null, null, null));
    }

    @Override
    public CreateRecipeResponse createRecipe(String userEmail, CreateRecipeRequest createRecipeRequest, MultipartFile recipeImage) {
        User user = super.getUserByEmail(userEmail);

        Recipe recipe = mapRecipeRequestToRecipe(user, createRecipeRequest, recipeImage);
        this.recipeRepository.save(recipe);

        return new CreateRecipeResponse(recipe.getId());
    }

    @Override
    public void deleteRecipe(String userEmail, long recipeId) {
        Recipe recipe = findRecipeIfUserIsCreator(userEmail, recipeId, "delete");

        this.recipeRepository.delete(recipe);
    }

    @Override
    public CreateRecipeResponse updateRecipe(String userEmail, UpdateRecipeRequest updateRecipeRequest, MultipartFile recipeImage) {
        Recipe recipe = findRecipeIfUserIsCreator(userEmail, updateRecipeRequest.id(), "update");

        updateRecipe(recipe, updateRecipeRequest, recipeImage);
        this.recipeRepository.save(recipe);

        return new CreateRecipeResponse(recipe.getId());
    }

    @Override
    public List<RecipeProduct> reserveRecipeProductsInPantry(User user, Recipe recipe, long pantryId) {
        return this.pantryProductService.reservePantryProductsFromRecipe(pantryId, user, recipe.getRecipeProducts());
    }

    @Override
    public List<RecipeProduct> getRecipeProductsNotInPantry(Group group, Recipe recipe) {
       return this.pantryProductService
                .getRecipeProductsNotInPantry(group.getPantry(), recipe.getRecipeProducts());
    }

    @Override
    public void addRecipeProductsToShoppingList(User user,
                                                long listId,
                                                List<RecipeProduct> productsToAdd) {
        this.shoppingListProductService.addRecipeProductsToShoppingList(listId, user, productsToAdd);
    }

    private Recipe findRecipeIfUserIsCreator(String userEmail, long recipeId, String action) {
        User user = super.getUserByEmail(userEmail);
        Recipe recipe = getRecipeById(recipeId, userEmail);

        if (recipe.getCreator().getId() != user.getId()) {
            log.info("User={} tried to {} recipe which they did not created", userEmail, action);
            throw new UserPerformedForbiddenActionException(
                    String.format("You did not create this recipe so you cannot %s it", action)
            );
        }

        return recipe;
    }

    private Set<String> getMealTypesAsStrings(List<MealType> mealTypes) {
        if (mealTypes == null || mealTypes.isEmpty()) {
            return MealType.ALL_MEAL_TYPES
                    .stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet());
        }

        return mealTypes
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    private void updateRecipe(Recipe recipe, UpdateRecipeRequest recipeDetailsDTO, MultipartFile recipeImage) {
        if (!recipe.getRecipeName().equals(recipeDetailsDTO.recipeName())) {
            recipe.setRecipeName(recipeDetailsDTO.recipeName());
        }
        if (!recipe.getPreparation().equals(recipeDetailsDTO.preparation())) {
            recipe.setPreparation(recipeDetailsDTO.preparation());
        }
        if (recipe.getPreparationTime() != recipeDetailsDTO.preparationTime()) {
            recipe.setPreparationTime(recipeDetailsDTO.preparationTime());
        }
        if (!Objects.equals(recipe.getCuisine(), recipeDetailsDTO.cuisine())) {
            recipe.setCuisine(recipeDetailsDTO.cuisine());
        }
        if (recipe.getPortions() != recipeDetailsDTO.portions()) {
            recipe.setPortions(recipeDetailsDTO.portions());
        }
        if (recipe.getMealType() != recipeDetailsDTO.mealType()) {
            recipe.setMealType(recipeDetailsDTO.mealType());
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

                if (contentType != null && !contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
                    throw new UserPerformedForbiddenActionException("You tried to save file in forbidden format");
                }
            }

            recipe.setRecipeImage(ImageUtil.compressImage(newImage));
        }

        Map<Long, RecipeProductDTO> recipeProductDTOMap = recipeDetailsDTO.products()
                .stream()
                .filter(recipeProductDTO -> recipeProductDTO.id() > 0)
                .collect(Collectors.toMap(RecipeProductDTO::id, Function.identity()));
        List<RecipeProduct> productsToRemove = new ArrayList<>();

        for (RecipeProduct recipeProduct : new ArrayList<>(recipe.getRecipeProducts())) {
            if (!recipeProductDTOMap.containsKey(recipeProduct.getId())) {
                recipe.getRecipeProducts().remove(recipeProduct);
                productsToRemove.add(recipeProduct);
                continue;
            }

            RecipeProductDTO modifiedProduct = recipeProductDTOMap.get(recipeProduct.getId());
            if (recipeProduct.getQuantity() != modifiedProduct.quantity()) {
                recipeProduct.setQuantity(modifiedProduct.quantity());
            }
            if (recipeProduct.getUnit() != modifiedProduct.unit()) {
                recipeProduct.setUnit(modifiedProduct.unit());
            }

            recipeProductDTOMap.remove(recipeProduct.getId());
        }

        if (!recipeProductDTOMap.values().isEmpty()) {
            log.info("User tried to modify recipe product from different recipe");
            throw new UserPerformedForbiddenActionException("You tried to modify recipe product from different recipe");
        }

        this.recipeProductRepository.deleteAll(productsToRemove);

        List<RecipeProduct> addedProducts = recipeDetailsDTO.products()
                .stream()
                .filter(recipeProductDTO -> recipeProductDTO.id() == 0)
                .map(recipeProductDTO -> this.mapToRecipeProduct(recipeProductDTO, recipe))
                .toList();

        recipe.getRecipeProducts().addAll(addedProducts);
    }

    private Recipe getRecipeById(long recipeId, String userEmail) {
        Optional<Recipe> recipeOptional = this.recipeRepository.findById(recipeId);

        return recipeOptional.orElseThrow(() -> {
            log.info("User={} tried to access recipe which does not exists", userEmail);
            return new UserPerformedForbiddenActionException("Recipe does not exists");
        });
    }


    private Recipe mapRecipeRequestToRecipe(User creator, CreateRecipeRequest createRecipeRequest, MultipartFile recipeImage) {
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

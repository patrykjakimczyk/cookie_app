package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.ValidationException;
import com.cookie.app.model.dto.RecipeDTO;
import com.cookie.app.model.dto.RecipeDetailsDTO;
import com.cookie.app.model.dto.RecipeProductDTO;
import com.cookie.app.model.entity.Recipe;
import com.cookie.app.model.entity.RecipeProduct;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.RecipeDetailsMapperDTO;
import com.cookie.app.model.mapper.RecipeMapperDTO;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.RecipeRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.RecipeService;
import com.cookie.app.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class RecipeServiceImpl extends AbstractCookieService implements RecipeService {
    private final RecipeRepository recipeRepository;
    private final RecipeMapperDTO recipeMapperDTO;
    private final RecipeDetailsMapperDTO recipeDetailsMapperDTO;

    protected RecipeServiceImpl(UserRepository userRepository,
                                ProductRepository productRepository,
                                AuthorityMapperDTO authorityMapperDTO,
                                RecipeRepository recipeRepository,
                                RecipeMapperDTO recipeMapperDTO,
                                RecipeDetailsMapperDTO recipeDetailsMapperDTO) {
        super(userRepository, productRepository, authorityMapperDTO);
        this.recipeRepository = recipeRepository;
        this.recipeMapperDTO = recipeMapperDTO;
        this.recipeDetailsMapperDTO = recipeDetailsMapperDTO;
    }

    @Override
    public Page<RecipeDTO> getRecipes(int page,
                                      String filterValue,
                                      int prepTime,
                                      int portions,
                                      String sortColName,
                                      String sortDirection) {
        PageRequest pageRequest = this.createPageRequest(page, sortColName, sortDirection);

        if (StringUtils.isBlank(filterValue)) {
            return this.recipeRepository
                    .findRecipes(prepTime, portions, pageRequest)
                    .map(recipeMapperDTO::apply);
        }

        return this.recipeRepository
                .findRecipesByFilter(filterValue, prepTime, portions, pageRequest)
                .map(recipeMapperDTO::apply);
    }

    @Override
    public Page<RecipeDTO> getUserRecipes(String userEmail,
                                          int page,
                                          String filterValue,
                                          int prepTime,
                                          int portions,
                                          String sortColName,
                                          String sortDirection) {
        User user = this.getUserByEmail(userEmail);
        PageRequest pageRequest = this.createPageRequest(page, sortColName, sortDirection);

        if (StringUtils.isBlank(filterValue)) {
            return this.recipeRepository
                    .findCreatorRecipes(user.getId(), prepTime, portions, pageRequest)
                    .map(recipeMapperDTO::apply);
        }

        return this.recipeRepository
                .findCreatorRecipesByFilter(user.getId(), filterValue, prepTime, portions, pageRequest)
                .map(recipeMapperDTO::apply);
    }


    @Override
    public RecipeDetailsDTO getRecipeDetails(long recipeId) {
        Optional<Recipe> recipeOptional = this.recipeRepository.findById(recipeId);

        if (recipeOptional.isEmpty()) {
            return new RecipeDetailsDTO(0, null, null, 0, null, 0, null, null, null);
        }

        return this.recipeDetailsMapperDTO.apply(recipeOptional.get());
    }

    @Override
    public RecipeDetailsDTO createRecipe(String userEmail, RecipeDetailsDTO recipeDetailsDTO) {
        User user = this.getUserByEmail(userEmail);

        if (recipeDetailsDTO.id() != 0) {
            throw new ValidationException("Recipe id must be 0 while creating it");
        }

        Recipe recipe = this.mapRecipeDetailsToRecipe(user, recipeDetailsDTO);
        this.recipeRepository.save(recipe);

        return this.recipeDetailsMapperDTO.apply(recipe);
    }

    @Override
    public void deleteRecipe(String userEmail, long recipeId) {
        User user = this.getUserByEmail(userEmail);
        Optional<Recipe> recipeOptional = this.recipeRepository.findById(recipeId);

        if (recipeOptional.isEmpty()) {
            log.info(String.format("User: %s tried to delete recipe which does not exists", userEmail));
            throw new UserPerformedForbiddenActionException("Group does not exists");
        }

        Recipe recipe = recipeOptional.get();

        if (recipe.getCreator().getId() != user.getId()) {
            log.info(String.format("User: %s tried to delete recipe which they did not created", userEmail));
            throw new UserPerformedForbiddenActionException("You did not create this recipe so you cannot delete it");
        }

        this.recipeRepository.delete(recipe);
    }

    private Recipe mapRecipeDetailsToRecipe(User creator, RecipeDetailsDTO recipeDetailsDTO) {
        List<RecipeProduct> recipeProducts = recipeDetailsDTO
                .products()
                .stream()
                .map(this::mapToRecipeProduct)
                .toList();

        return Recipe.builder()
                .recipeName(recipeDetailsDTO.recipeName())
                .preparation(recipeDetailsDTO.preparation())
                .preparationTime(recipeDetailsDTO.preparationTime())
                .cuisine(recipeDetailsDTO.cuisine())
                .portions(recipeDetailsDTO.portions())
                .recipeImage(ImageUtil.compressImage(recipeDetailsDTO.recipeImage()))
                .creator(creator)
                .recipeProducts(recipeProducts)
                .build();
    }

    private RecipeProduct mapToRecipeProduct(RecipeProductDTO recipeProductDTO) {
        return RecipeProduct.builder()
                .product(this.checkIfProductExists(recipeProductDTO))
                .quantity(recipeProductDTO.getQuantity())
                .unit(recipeProductDTO.getUnit())
                .build();
    }
}

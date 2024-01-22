package com.cookie.app.service.impl;

import com.cookie.app.model.dto.RecipeDTO;
import com.cookie.app.model.dto.RecipeDetailsDTO;
import com.cookie.app.model.entity.Recipe;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.RecipeDetailsMapperDTO;
import com.cookie.app.model.mapper.RecipeMapperDTO;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.RecipeRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.RecipeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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
    public RecipeDetailsDTO getRecipeDetails(long recipeId) {
        Optional<Recipe> recipeOptional = this.recipeRepository.findById(recipeId);

        if (recipeOptional.isEmpty()) {
            return new RecipeDetailsDTO(0, null, null, 0, null, 0, null, null);
        }

        return this.recipeDetailsMapperDTO.apply(recipeOptional.get());
    }
}

package com.cookie.app.service.impl;

import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.dto.AuthorityDTO;
import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapper;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public abstract sealed class AbstractCookieService permits
        AbstractPantryService, AbstractShoppingListService, GroupServiceImpl,
        LoginServiceImpl, MealServiceImpl, RecipeServiceImpl {
    private static final int PRODUCTS_PAGE_SIZE = 20;
    final UserRepository userRepository;
    final ProductRepository productRepository;
    final AuthorityMapper authorityMapper;

    protected AbstractCookieService(UserRepository userRepository,
                                    ProductRepository productRepository,
                                    AuthorityMapper authorityMapper) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.authorityMapper = authorityMapper;
    }

    protected User getUserByEmail(String userEmail) {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        return userOptional.orElseThrow(() ->
                new UserWasNotFoundAfterAuthException("User was not found in database after authentication"));
    }

    protected Optional<Group> findUserGroupById(User user, long groupId) {
        return user.getGroups()
                .stream()
                .filter(group -> group.getId() == groupId)
                .findFirst();
    }

    protected boolean userHasAuthority(User user, long groupId, AuthorityEnum authorityEnum) {
        return user.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getGroup().getId() == groupId &&
                                authority.getAuthorityName() == authorityEnum);
    }

    protected Set<AuthorityDTO> getAuthorityDTOsForSpecificGroup(User user, Group userGroup) {
        return user.getAuthorities()
                .stream()
                .filter(authority -> authority.getGroup().getId() == userGroup.getId())
                .map(this.authorityMapper::mapToDto)
                .collect(Collectors.toSet());
    }

    protected PageRequest createPageRequest(int page, String sortColName, Sort.Direction sortDirection) {
        PageRequest pageRequest = PageRequest.of(page, PRODUCTS_PAGE_SIZE);
        Sort idSort = Sort.by(Sort.Direction.DESC, "id");
        Sort sort = null;

        if (sortColName == null || StringUtils.isBlank(sortColName.trim())) {
            return pageRequest.withSort(idSort);
        }

        if (sortDirection == Sort.Direction.DESC) {
            sort = Sort.by(Sort.Direction.DESC, sortColName);
        } else {
            sort = Sort.by(Sort.Direction.ASC, sortColName);
        }

        sort = sort.and(idSort);
        return pageRequest.withSort(sort);
    }

    protected Product checkIfProductExists(ProductDTO productDTO) {
        Optional<Product> productOptional = this.productRepository
                .findByProductNameAndCategory(productDTO.productName(), productDTO.category().name());


        if (productOptional.isPresent()) {
            return productOptional.get();
        }

        Product product = new Product();
        product.setProductName(productDTO.productName());
        product.setCategory(productDTO.category());
        this.productRepository.save(product);

        return product;
    }

    protected <T> boolean isAnyProductNotOnList(List<T> products, List<T> productsToPerformAction) {
        return !products.containsAll(productsToPerformAction);
    }
}

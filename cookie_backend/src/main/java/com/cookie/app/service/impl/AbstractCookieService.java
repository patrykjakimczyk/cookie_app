package com.cookie.app.service.impl;

import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.dto.AuthorityDTO;
import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public abstract class AbstractCookieService {
    private static final int PRODUCTS_PAGE_SIZE = 20;
    protected final UserRepository userRepository;
    protected final ProductRepository productRepository;
    protected final AuthorityMapperDTO authorityMapperDTO;

    AbstractCookieService(UserRepository userRepository,
                                    ProductRepository productRepository,
                                    AuthorityMapperDTO authorityMapperDTO) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.authorityMapperDTO = authorityMapperDTO;
    }

    User getUserByEmail(String userEmail) {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        return userOptional.orElseThrow(() ->
                new UserWasNotFoundAfterAuthException("User was not found in database after authentication"));
    }

    Optional<Group> findUserGroupById(User user, long groupId) {
        return user.getGroups()
                .stream()
                .filter(group -> group.getId() == groupId)
                .findFirst();
    }

    boolean userHasAuthority(User user, long groupId, AuthorityEnum authorityEnum) {
        return user.getAuthorities()
                .stream()
                .anyMatch(authority ->
                            authority.getGroup().getId() == groupId &&
                                authority.getAuthorityName() == authorityEnum);
    }

    Set<AuthorityDTO> getAuthorityDTOsForSpecificGroup(User user, Group userGroup) {
        return user.getAuthorities()
                .stream()
                .filter(authority -> authority.getGroup().getId() == userGroup.getId())
                .map(authorityMapperDTO::apply)
                .collect(Collectors.toSet());
    }

    PageRequest createPageRequest(int page, String sortColName, String sortDirection) {
        PageRequest pageRequest = PageRequest.of(page, PRODUCTS_PAGE_SIZE);
        Sort idSort = Sort.by(Sort.Direction.DESC, "id");
        Sort sort = null;

        if (StringUtils.isBlank(sortColName) && StringUtils.isBlank(sortDirection)) {
            return pageRequest.withSort(idSort);
        }

        if (sortDirection.equals("DESC")) {
            sort = Sort.by(Sort.Direction.DESC, sortColName);
        } else {
            sort = Sort.by(Sort.Direction.ASC, sortColName);
        }

        sort = sort.and(idSort);
        return pageRequest.withSort(sort);
    }

    Product checkIfProductExists(ProductDTO productDTO) {
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

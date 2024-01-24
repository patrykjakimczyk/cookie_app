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

    protected AbstractCookieService(UserRepository userRepository,
                                    ProductRepository productRepository,
                                    AuthorityMapperDTO authorityMapperDTO) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.authorityMapperDTO = authorityMapperDTO;
    }

    protected User getUserByEmail(String userEmail) {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (userOptional.isEmpty()) {
            throw new UserWasNotFoundAfterAuthException("User was not found in database after authentication");
        }

        return userOptional.get();
    }

    protected Optional<Group> findUserGroupById(User user, long groupId) {
        for (Group group : user.getGroups()) {
            if (group.getId() == groupId) {
                return Optional.of(group);
            }
        }

        return Optional.empty();
    }

    protected boolean userHasAuthority(User user, long groupId, AuthorityEnum authorityEnum) {
        for (Authority authority : user.getAuthorities()) {
            if (authority.getGroup().getId() == groupId && authority.getAuthority() == authorityEnum) {
                return true;
            }
        }

        return false;
    }

    protected Set<AuthorityDTO> getAuthorityDTOsForSpecificGroup(User user, Group userGroup) {
        return user.getAuthorities()
                .stream()
                .filter(authority -> authority.getGroup().getId() == userGroup.getId())
                .map(authorityMapperDTO::apply)
                .collect(Collectors.toSet());
    }

    protected PageRequest createPageRequest(int page, String sortColName, String sortDirection) {
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

    protected Product checkIfProductExists(ProductDTO productDTO) {
        Product product;
        Optional<Product> productOptional = this.productRepository
                .findByProductNameAndCategory(productDTO.getProductName(), productDTO.getCategory().name());

        if (productOptional.isPresent()) {
            product = productOptional.get();
        } else {
            product = new Product();
            product.setProductName(productDTO.getProductName());
            product.setCategory(productDTO.getCategory());
            this.productRepository.save(product);
        }

        return product;
    }

    protected <T> boolean isAnyProductNotOnList(List<T> products, List<T> productsToPerformAction) {
        return !products.containsAll(productsToPerformAction);
    }
}

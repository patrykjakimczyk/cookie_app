package com.cookie.app.service.impl;

import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.entity.ShoppingList;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.ShoppingListProductMapperDTO;
import com.cookie.app.repository.ShoppingListProductRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.ShoppingListProductService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShoppingListProductServiceImpl extends AbstractCookieService implements ShoppingListProductService {
    private final ShoppingListProductRepository shoppingListProductRepository;
    private final ShoppingListProductMapperDTO shoppingListProductMapper;

    public ShoppingListProductServiceImpl(
            UserRepository userRepository,
            AuthorityMapperDTO authorityMapperDTO,
            ShoppingListProductRepository shoppingListProductRepository,
            ShoppingListProductMapperDTO shoppingListProductMapper
    ) {
        super(userRepository, authorityMapperDTO);
        this.shoppingListProductRepository = shoppingListProductRepository;
        this.shoppingListProductMapper = shoppingListProductMapper;
    }

    @Override
    public Page<ShoppingListProductDTO> getShoppingListProducts(
            long id,
            int page,
            String filterValue,
            String sortColName,
            String sortDirection,
            String userEmail
    ) {
        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(id, user, null);

        PageRequest pageRequest = this.createPageRequest(page, sortColName, sortDirection);

        if(StringUtils.isBlank(filterValue)) {
            return this.shoppingListProductRepository
                    .findProductsInShoppingList(shoppingList.getId(), pageRequest)
                    .map(shoppingListProductMapper);
        }

        return this.shoppingListProductRepository
                .findProductsInShoppingListWithFilter(shoppingList.getId(), filterValue, pageRequest)
                .map(shoppingListProductMapper);
    }
}

package com.cookie.app.service.impl;

import com.cookie.app.exception.ResourceNotFoundException;
import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.model.entity.ShoppingList;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapper;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public abstract sealed class AbstractShoppingListService extends AbstractCookieService permits ShoppingListServiceImpl, ShoppingListProductServiceImpl {

    protected AbstractShoppingListService(UserRepository userRepository,
                                ProductRepository productRepository,
                                AuthorityMapper authorityMapper) {
        super(userRepository, productRepository, authorityMapper);
    }

    protected ShoppingList getShoppingListIfUserHasAuthority(long shoppingListId, String userEmail, AuthorityEnum requiredAuthority) {
        User user = super.getUserByEmail(userEmail);

        return getShoppingListIfUserHasAuthority(shoppingListId, user, requiredAuthority);
    }

    protected ShoppingList getShoppingListIfUserHasAuthority(long shoppingListId, User user, AuthorityEnum requiredAuthority) {
        ShoppingList shoppingList = this.findShoppingListInUserGroups(shoppingListId, user).orElseThrow(
                () -> {
                    log.info("User: {} tried to access shopping list without being a member of the shopping list's group", user.getEmail());
                    return new ResourceNotFoundException("You cannot access the shopping list because it could not be found in your groups");
                }
        );

        if (requiredAuthority!= null && !this.userHasAuthority(user, shoppingList.getGroup().getId(), requiredAuthority)) {
            log.info("User: {} tried to perform action in pantry without required permission", user.getEmail());
            throw new UserPerformedForbiddenActionException("You have not permissions to do that");
        }

        return shoppingList;
    }

    protected Optional<ShoppingList> findShoppingListInUserGroups(long listId, User user) {
        return user.getGroups().stream()
                .flatMap(group -> group.getShoppingLists().stream())
                .filter(shoppingList -> shoppingList.getId() == listId)
                .findFirst();
    }
}

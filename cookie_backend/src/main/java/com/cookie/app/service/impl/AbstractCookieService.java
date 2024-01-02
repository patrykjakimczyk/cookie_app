package com.cookie.app.service.impl;

import com.cookie.app.exception.PantryNotFoundException;
import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.dto.AuthorityDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public abstract class AbstractCookieService {
    private static final int PRODUCTS_PAGE_SIZE = 20;
    protected final UserRepository userRepository;
    protected final AuthorityMapperDTO authorityMapperDTO;

    protected AbstractCookieService(UserRepository userRepository, AuthorityMapperDTO authorityMapperDTO) {
        this.userRepository = userRepository;
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

    protected Pantry getPantryIfUserHasAuthority(long pantryId, String userEmail, AuthorityEnum requiredAuthority) {
        User user = this.getUserByEmail(userEmail);
        Pantry pantry = this.findPantryInUserGroups(pantryId, user).orElseThrow(
                () -> {
                    log.info("User: {} tried to access pantry without being a member of the pantry's group", userEmail);
                    return new PantryNotFoundException("You cannot access the pantry because you are not member of it");
                }
        );

        if (requiredAuthority!= null && !this.userHasAuthority(user, pantry.getGroup().getId(), requiredAuthority)) {
            log.info("User: {} tried to perform action in pantry without required permission", userEmail);
            throw new UserPerformedForbiddenActionException("You have not permissions to do that");
        }

        return pantry;
    }

    protected Optional<Pantry> findPantryInUserGroups(long pantryId, User user) {
        for (Group group : user.getGroups()) {
            if (group.getPantry() != null && group.getPantry().getId() == pantryId) {
                return Optional.of(group.getPantry());
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

    protected ShoppingList getShoppingListIfUserHasAuthority(long shoppingListId, User user, AuthorityEnum requiredAuthority) {
        ShoppingList shoppingList = this.findShoppingListInUserGroups(shoppingListId, user).orElseThrow(
                () -> {
                    log.info("User: {} tried to access shopping list without being a member of the shopping list's group", user.getEmail());
                    return new UserPerformedForbiddenActionException("You cannot access the shopping list because you are not member of it");
                }
        );

        if (requiredAuthority!= null && !this.userHasAuthority(user, shoppingList.getGroup().getId(), requiredAuthority)) {
            log.info("User: {} tried to perform action in pantry without required permission", user.getEmail());
            throw new UserPerformedForbiddenActionException("You have not permissions to do that");
        }

        return shoppingList;
    }

    protected Optional<ShoppingList> findShoppingListInUserGroups(long listId, User user) {
        for (Group group : user.getGroups()) {
            for (ShoppingList shoppingList : group.getShoppingLists()) {
                if (shoppingList.getId() == listId) {
                    return Optional.of(shoppingList);
                }
            }
        }

        return Optional.empty();
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
}

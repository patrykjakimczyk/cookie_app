package com.cookie.app.service.impl;

import com.cookie.app.exception.PantryNotFoundException;
import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.ShoppingList;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.ShoppingListMapperDTO;
import com.cookie.app.model.request.CreateShoppingListRequest;
import com.cookie.app.model.request.UpdateShoppingListRequest;
import com.cookie.app.model.response.DeleteShoppingListResponse;
import com.cookie.app.model.response.GetShoppingListResponse;
import com.cookie.app.model.response.GetUserShoppingListsResponse;
import com.cookie.app.repository.ShoppingListRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.ShoppingListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ShoppingListServiceImpl extends AbstractCookieService implements ShoppingListService {
    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListMapperDTO shoppingListMapperDTO;

    protected ShoppingListServiceImpl(
            UserRepository userRepository,
            AuthorityMapperDTO authorityMapperDTO,
            ShoppingListRepository shoppingListRepository,
            ShoppingListMapperDTO shoppingListMapperDTO) {
        super(userRepository, authorityMapperDTO);
        this.shoppingListRepository = shoppingListRepository;
        this.shoppingListMapperDTO = shoppingListMapperDTO;
    }

    @Override
    public GetShoppingListResponse createShoppingList(CreateShoppingListRequest request, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        Optional<Group> userGroupOptional = this.findUserGroupById(user, request.groupId());
        Group userGroup = userGroupOptional.orElseThrow(
                () -> new UserPerformedForbiddenActionException("You tried to create shopping list for non existing group")
        );

        if (!this.userHasAuthority(user, userGroup.getId(), AuthorityEnum.CREATE_SHOPPING_LIST)) {
            log.info(String.format("User: %s tried to create shopping list without permission", userEmail));
            throw new UserPerformedForbiddenActionException("You tried to create shopping list without permission");
        }

        ShoppingList shoppingList = ShoppingList.builder()
                .listName(request.shoppingListName())
                .creator(user)
                .creationDate(Timestamp.from(Instant.now()))
                .purchased(false)
                .group(userGroup)
                .build();

        this.shoppingListRepository.save(shoppingList);

        return new GetShoppingListResponse(
                shoppingList.getId(),
                shoppingList.getListName(),
                this.getAuthorityDTOsForSpecificGroup(user, userGroup)
        );
    }

    @Override
    public GetShoppingListResponse getShoppingList(long shoppingListId, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        Optional<ShoppingList> listOptional = this.findShoppingListInUserGroups(shoppingListId, user);

        if (listOptional.isEmpty()) {
            return new GetShoppingListResponse(null, null, null);
        }

        ShoppingList shoppingList = listOptional.get();

        return new GetShoppingListResponse(
                shoppingList.getId(),
                shoppingList.getListName(),
                this.getAuthorityDTOsForSpecificGroup(user, shoppingList.getGroup())
        );
    }

    @Override
    public GetUserShoppingListsResponse getUserShoppingLists(String userEmail) {
        User user = this.getUserByEmail(userEmail);

        return new GetUserShoppingListsResponse(
                user.getGroups()
                        .stream()
                        .filter(group -> group.getShoppingLists() != null && !group.getShoppingLists().isEmpty())
                        .map(group -> group.getShoppingLists()
                                .stream()
                                .map(this.shoppingListMapperDTO::apply)
                                .toList()
                        )
                        .flatMap(List::stream)
                        .toList()
        );
    }

    @Override
    public DeleteShoppingListResponse deleteShoppingList(long shoppingListId, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(shoppingListId, user);

        this.shoppingListRepository.delete(shoppingList);

        return new DeleteShoppingListResponse(shoppingList.getListName());
    }

    @Override
    public GetShoppingListResponse modifyShoppingList(long shoppingListId, UpdateShoppingListRequest request, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(shoppingListId, user);

        shoppingList.setListName(request.shoppingListName());
        this.shoppingListRepository.save(shoppingList);

        return new GetShoppingListResponse(
                shoppingList.getId(),
                shoppingList.getListName(),
                this.getAuthorityDTOsForSpecificGroup(user, shoppingList.getGroup())
        );
    }

    private ShoppingList getShoppingListIfUserHasAuthority(long shoppingListId, User user) {
        ShoppingList shoppingList = this.findShoppingListInUserGroups(shoppingListId, user).orElseThrow(
                () -> {
                    log.info("User: {} tried to access shopping list without being a member of the shopping list's group", user.getEmail());
                    return new UserPerformedForbiddenActionException("You cannot access the shopping list because you are not member of it");
                }
        );

        if (!this.userHasAuthority(user, shoppingList.getGroup().getId(), AuthorityEnum.MODIFY_SHOPPING_LIST)) {
            log.info("User: {} tried to perform action in pantry without required permission", user.getEmail());
            throw new UserPerformedForbiddenActionException("You have not permissions to do that");
        }

        return shoppingList;
    }

    private Optional<ShoppingList> findShoppingListInUserGroups(long listId, User user) {
        for (Group group : user.getGroups()) {
            for (ShoppingList shoppingList : group.getShoppingLists()) {
                if (shoppingList.getId() == listId) {
                    return Optional.of(shoppingList);
                }
            }
        }

        return Optional.empty();
    }
}

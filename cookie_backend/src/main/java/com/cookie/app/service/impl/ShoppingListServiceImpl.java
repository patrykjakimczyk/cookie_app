package com.cookie.app.service.impl;

import com.cookie.app.exception.ResourceNotFoundException;
import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.ShoppingList;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapper;
import com.cookie.app.model.mapper.ShoppingListMapper;
import com.cookie.app.model.request.CreateShoppingListRequest;
import com.cookie.app.model.request.UpdateShoppingListRequest;
import com.cookie.app.model.response.DeleteShoppingListResponse;
import com.cookie.app.model.response.GetShoppingListResponse;
import com.cookie.app.model.response.GetUserShoppingListsResponse;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.ShoppingListRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.ShoppingListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public non-sealed class ShoppingListServiceImpl extends AbstractShoppingListService implements ShoppingListService {
    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListMapper shoppingListMapper;

    public ShoppingListServiceImpl(UserRepository userRepository,
                                      ProductRepository productRepository,
                                      AuthorityMapper authorityMapper,
                                      ShoppingListRepository shoppingListRepository,
                                      ShoppingListMapper shoppingListMapper) {
        super(userRepository, productRepository, authorityMapper);
        this.shoppingListRepository = shoppingListRepository;
        this.shoppingListMapper = shoppingListMapper;
    }

    @Override
    public GetShoppingListResponse createShoppingList(CreateShoppingListRequest request, String userEmail) {
        User user = super.getUserByEmail(userEmail);
        Group userGroup = super.findUserGroupById(user, request.groupId()).orElseThrow(
                () -> new ResourceNotFoundException("You tried to create shopping list for non existing group")
        );

        if (!super.userHasAuthority(user, userGroup.getId(), AuthorityEnum.CREATE_SHOPPING_LIST)) {
            log.info("User={} tried to create shopping list without permission", userEmail);
            throw new UserPerformedForbiddenActionException("You tried to create shopping list without permission");
        }

        ShoppingList shoppingList = ShoppingList.builder()
                .listName(request.shoppingListName())
                .creator(user)
                .group(userGroup)
                .build();

        this.shoppingListRepository.save(shoppingList);

        log.info("User with email={} created shopping list with id={} in group with id={}",
                userEmail,
                shoppingList.getId(),
                request.groupId()
        );

        return createGetShoppingListResponse(shoppingList, user);
    }

    @Override
    public GetShoppingListResponse getShoppingList(long shoppingListId, String userEmail) {
        User user = super.getUserByEmail(userEmail);
        Optional<ShoppingList> listOptional = super.findShoppingListInUserGroups(shoppingListId, user);

        return listOptional.map(shoppingList -> createGetShoppingListResponse(shoppingList, user))
                .orElseGet(() -> new GetShoppingListResponse(0L, null, null, false));
    }

    @Override
    public GetUserShoppingListsResponse getUserShoppingLists(String userEmail) {
        User user = super.getUserByEmail(userEmail);

        return new GetUserShoppingListsResponse(
                user.getGroups()
                        .stream()
                        .filter(group -> group.getShoppingLists() != null && !group.getShoppingLists().isEmpty())
                        .map(group -> group.getShoppingLists()
                                .stream()
                                .map(this.shoppingListMapper::mapToDto)
                                .toList()
                        )
                        .flatMap(List::stream)
                        .toList()
        );
    }

    @Transactional
    @Override
    public DeleteShoppingListResponse deleteShoppingList(long shoppingListId, String userEmail) {
        User user = super.getUserByEmail(userEmail);
        ShoppingList shoppingList = super.getShoppingListIfUserHasAuthority(
                shoppingListId,
                user,
                AuthorityEnum.MODIFY_SHOPPING_LIST
        );

        this.shoppingListRepository.delete(shoppingList);
        log.info("User with email={} deleted shopping list with id={}", userEmail, shoppingList.getId());

        return new DeleteShoppingListResponse(shoppingList.getListName());
    }

    @Transactional
    @Override
    public GetShoppingListResponse updateShoppingList(long shoppingListId, UpdateShoppingListRequest request, String userEmail) {
        User user = super.getUserByEmail(userEmail);
        ShoppingList shoppingList = super.getShoppingListIfUserHasAuthority(shoppingListId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);

        shoppingList.setListName(request.shoppingListName());
        this.shoppingListRepository.save(shoppingList);
        log.info("User with email={} modified shopping list with id={}", userEmail, shoppingList.getId());

        return createGetShoppingListResponse(shoppingList, user);
    }

    private GetShoppingListResponse createGetShoppingListResponse(ShoppingList shoppingList, User user) {
        return new GetShoppingListResponse(
                shoppingList.getId(),
                shoppingList.getListName(),
                this.getAuthorityDTOsForSpecificGroup(user, shoppingList.getGroup()),
                shoppingList.getGroup().getPantry() != null
        );
    }
}

package com.cookie.app.service.impl;

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
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.ShoppingListRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.ShoppingListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ShoppingListServiceImpl extends AbstractShoppingListService implements ShoppingListService {
    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListMapperDTO shoppingListMapperDTO;

    protected ShoppingListServiceImpl(UserRepository userRepository,
                                      ProductRepository productRepository,
                                      AuthorityMapperDTO authorityMapperDTO,
                                      ShoppingListRepository shoppingListRepository,
                                      ShoppingListMapperDTO shoppingListMapperDTO) {
        super(userRepository, productRepository, authorityMapperDTO);
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


        log.info("User with email {} created shopping list with id {} in group with id {}",
                userEmail,
                shoppingList.getId(),
                request.groupId());
        return new GetShoppingListResponse(
                shoppingList.getId(),
                shoppingList.getListName(),
                this.getAuthorityDTOsForSpecificGroup(user, userGroup),
                shoppingList.getGroup().getPantry() != null
        );
    }

    @Override
    public GetShoppingListResponse getShoppingList(long shoppingListId, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        Optional<ShoppingList> listOptional = this.findShoppingListInUserGroups(shoppingListId, user);

        if (listOptional.isEmpty()) {
            return new GetShoppingListResponse(0L, "", Collections.emptySet(), false);
        }

        ShoppingList shoppingList = listOptional.get();

        return new GetShoppingListResponse(
                shoppingList.getId(),
                shoppingList.getListName(),
                this.getAuthorityDTOsForSpecificGroup(user, shoppingList.getGroup()),
                shoppingList.getGroup().getPantry() != null
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
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(shoppingListId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);

        this.shoppingListRepository.delete(shoppingList);

        log.info("User with email {} deleted shopping list with id {}",
                userEmail,
                shoppingList.getId());
        return new DeleteShoppingListResponse(shoppingList.getListName());
    }

    @Override
    public GetShoppingListResponse modifyShoppingList(long shoppingListId, UpdateShoppingListRequest request, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(shoppingListId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);

        shoppingList.setListName(request.shoppingListName());
        this.shoppingListRepository.save(shoppingList);

        log.info("User with email {} modified shopping list with id {}",
                userEmail,
                shoppingList.getId());
        return new GetShoppingListResponse(
                shoppingList.getId(),
                shoppingList.getListName(),
                this.getAuthorityDTOsForSpecificGroup(user, shoppingList.getGroup()),
                shoppingList.getGroup().getPantry() != null
        );
    }
}

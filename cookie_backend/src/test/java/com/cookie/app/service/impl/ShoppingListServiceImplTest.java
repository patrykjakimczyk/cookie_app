package com.cookie.app.service.impl;

import com.cookie.app.exception.ResourceNotFoundException;
import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.dto.AuthorityDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.*;
import com.cookie.app.model.request.CreateShoppingListRequest;
import com.cookie.app.model.request.UpdateShoppingListRequest;
import com.cookie.app.model.response.DeleteShoppingListResponse;
import com.cookie.app.model.response.GetShoppingListResponse;
import com.cookie.app.model.response.GetUserShoppingListsResponse;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.ShoppingListRepository;
import com.cookie.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceImplTest {
    private final String email = "email@email.com";
    private final String listName = "listName";
    private final Long id = 1L;

    @Spy
    private ShoppingListMapper shoppingListMapper = new ShoppingListMapperImpl();
    @Spy
    private AuthorityMapper authorityMapper = new AuthorityMapperImpl();
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ShoppingListRepository shoppingListRepository;
    @InjectMocks
    private ShoppingListServiceImpl service;

    private ShoppingListProduct product;
    private ShoppingList shoppingList;
    private Group group;
    private Authority authority;
    private User user;

    @BeforeEach
    void init() {
        product = ShoppingListProduct.builder().build();
        shoppingList = ShoppingList.builder()
                .id(id)
                .listName(listName)
                .productsList(List.of(product))
                .build();
        group = Group.builder()
                .id(id)
                .shoppingLists(List.of(shoppingList))
                .build();
        shoppingList.setGroup(group);
        authority = Authority.builder()
                .id(id)
                .group(group)
                .authorityName(AuthorityEnum.CREATE_SHOPPING_LIST)
                .build();
        user = User.builder()
                .id(id)
                .email(email)
                .groups(List.of(group))
                .authorities(Set.of(authority))
                .build();
        shoppingList.setCreator(user);
        authority.setUser(user);
    }

    @Test
    void test_createShoppingListSuccessful() {
        final CreateShoppingListRequest request = new CreateShoppingListRequest(listName, id);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetShoppingListResponse response = this.service.createShoppingList(request, email);

        verify(shoppingListRepository).save(any(ShoppingList.class));
        assertThat(response.listName()).isEqualTo(request.shoppingListName());
        assertThat(response.authorities()).isNotEmpty();

        List<AuthorityDTO> responseAuthorities = response.authorities().stream()
                .filter(authorityDTO -> authorityDTO.groupId() == id)
                .toList();

        assertThat(responseAuthorities).hasSize(1);
        assertThat(responseAuthorities.get(0).authority()).isEqualTo(authority.getAuthorityName());
        assertThat(responseAuthorities.get(0).groupId()).isEqualTo(group.getId());
    }

    @Test
    void test_createShoppingListUserNotFound() {
        final CreateShoppingListRequest request = new CreateShoppingListRequest(listName, id);

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.createShoppingList(request, email))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class);
        verify(shoppingListRepository, times(0)).save(any(ShoppingList.class));
    }

    @Test
    void test_createShoppingListWrongGroupId() {
        final CreateShoppingListRequest request = new CreateShoppingListRequest(listName, 2L);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.createShoppingList(request, email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("You tried to create shopping list for non existing group");
        verify(shoppingListRepository, times(0)).save(any(ShoppingList.class));
    }

    @Test
    void test_createShoppingListNoRequiredAuthority() {
        authority.setAuthorityName(AuthorityEnum.MODIFY_SHOPPING_LIST);
        final CreateShoppingListRequest request = new CreateShoppingListRequest(listName, id);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.createShoppingList(request, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("You tried to create shopping list without permission");
        verify(shoppingListRepository, times(0)).save(any(ShoppingList.class));
    }

    @Test
    void test_getShoppingListSuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetShoppingListResponse response = this.service.getShoppingList(id, email);

        assertThat(response.listName()).isEqualTo(shoppingList.getListName());
        assertThat(response.authorities()).isNotEmpty();

        List<AuthorityDTO> responseAuthorities = response.authorities().stream()
                .filter(authorityDTO -> authorityDTO.groupId() == id)
                .toList();

        assertThat(responseAuthorities).hasSize(1);
        assertThat(responseAuthorities.get(0).authority()).isEqualTo(authority.getAuthorityName());
        assertThat(responseAuthorities.get(0).groupId()).isEqualTo(group.getId());
    }

    @Test
    void test_getShoppingListUserNotFound() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.getShoppingList(id, email))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class);
    }

    @Test
    void test_getShoppingListNotFound() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetShoppingListResponse response = this.service.getShoppingList(2L, email);

        assertThat(response.id()).isZero();
        assertThat(response.listName()).isNull();
        assertThat(response.authorities()).isNull();
    }

    @Test
    void test_getUserShoppingListsSuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetUserShoppingListsResponse response = this.service.getUserShoppingLists(email);

        assertThat(response.shoppingLists()).isNotEmpty();
        assertThat(response.shoppingLists().get(0).listId()).isEqualTo(shoppingList.getId());
        assertThat(response.shoppingLists().get(0).listName()).isEqualTo(shoppingList.getListName());
        assertThat(response.shoppingLists().get(0).nrOfProducts()).isEqualTo(1);
        assertThat(response.shoppingLists().get(0).groupId()).isEqualTo(group.getId());
        assertThat(response.shoppingLists().get(0).groupName()).isEqualTo(group.getGroupName());
    }

    @Test
    void test_getUserShoppingListsNoLists() {
        group.setShoppingLists(Collections.emptyList());

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetUserShoppingListsResponse response = this.service.getUserShoppingLists(email);

        assertThat(response.shoppingLists()).isEmpty();
    }

    @Test
    void test_getUserShoppingListsUserNotFound() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.getUserShoppingLists(email))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class);
    }

    @Test
    void test_deleteShoppingListSuccessful() {
        authority.setAuthorityName(AuthorityEnum.MODIFY_SHOPPING_LIST);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        DeleteShoppingListResponse response = this.service.deleteShoppingList(id, email);

        verify(shoppingListRepository).delete(shoppingList);
        assertThat(response.deletedListName()).isEqualTo(shoppingList.getListName());
    }

    @Test
    void test_deleteShoppingListUserNotFound() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.deleteShoppingList(id, email))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class);
        verify(shoppingListRepository, times(0)).delete(shoppingList);
    }

    @Test
    void test_deleteShoppingListListNotFound() {
        authority.setAuthorityName(AuthorityEnum.MODIFY_SHOPPING_LIST);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.deleteShoppingList(2L, email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("You cannot access the shopping list because it could not be found in your groups");
        verify(shoppingListRepository, times(0)).delete(shoppingList);
    }

    @Test
    void test_deleteShoppingListNoRequiredAuthority() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.deleteShoppingList(id, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("You have not permissions to do that");
        verify(shoppingListRepository, times(0)).delete(shoppingList);
    }

    @Test
    void test_updateShoppingListSuccessful() {
        authority.setAuthorityName(AuthorityEnum.MODIFY_SHOPPING_LIST);
        final UpdateShoppingListRequest request = new UpdateShoppingListRequest("newListName");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetShoppingListResponse response = this.service.updateShoppingList(id, request, email);

        verify(shoppingListRepository).save(shoppingList);
        assertThat(response.listName()).isEqualTo(request.shoppingListName());
        assertThat(response.authorities()).isNotEmpty();

        List<AuthorityDTO> responseAuthorities = response.authorities().stream()
                .filter(authorityDTO -> authorityDTO.groupId() == id)
                .toList();

        assertThat(responseAuthorities).hasSize(1);
        assertThat(responseAuthorities.get(0).authority()).isEqualTo(authority.getAuthorityName());
        assertThat(responseAuthorities.get(0).groupId()).isEqualTo(group.getId());
    }

    @Test
    void test_updateShoppingListUserNotFound() {
        authority.setAuthorityName(AuthorityEnum.MODIFY_SHOPPING_LIST);
        final UpdateShoppingListRequest request = new UpdateShoppingListRequest("newListName");

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.updateShoppingList(id, request, email))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class);
        verify(shoppingListRepository, times(0)).save(shoppingList);
    }

    @Test
    void test_updateShoppingListListNotFound() {
        authority.setAuthorityName(AuthorityEnum.MODIFY_SHOPPING_LIST);
        final UpdateShoppingListRequest request = new UpdateShoppingListRequest("newListName");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.updateShoppingList(2L, request, email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("You cannot access the shopping list because it could not be found in your groups");
        verify(shoppingListRepository, times(0)).save(shoppingList);
    }

    @Test
    void test_updateShoppingListNoRequiredAuthority() {
        final UpdateShoppingListRequest request = new UpdateShoppingListRequest("newListName");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.updateShoppingList(id, request, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("You have not permissions to do that");
        verify(shoppingListRepository, times(0)).save(shoppingList);
    }
}

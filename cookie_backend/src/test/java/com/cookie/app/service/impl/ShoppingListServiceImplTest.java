package com.cookie.app.service.impl;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceImplTest {
    final String email = "email@email.com";
    final String listName = "listName";
    final Long id = 1L;

    @Spy
    ShoppingListMapper shoppingListMapper = new ShoppingListMapperImpl();
    @Spy
    AuthorityMapper authorityMapper = new AuthorityMapperImpl();
    @Mock
    UserRepository userRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    ShoppingListRepository shoppingListRepository;
    @InjectMocks
    ShoppingListServiceImpl service;

    ShoppingListProduct product;
    ShoppingList shoppingList;
    Group group;
    Authority authority;
    User user;

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
        assertEquals(request.shoppingListName(), response.listName());
        assertFalse(response.authorities().isEmpty());

        List<AuthorityDTO> responseAuthorities = response.authorities().stream()
                .filter(authorityDTO -> authorityDTO.groupId() == id)
                .toList();

        assertEquals(1, responseAuthorities.size());
        assertEquals(authority.getAuthorityName(), responseAuthorities.get(0).authority());
        assertEquals(group.getId(), responseAuthorities.get(0).groupId());
    }

    @Test
    void test_createShoppingListUserNotFound() {
        final CreateShoppingListRequest request = new CreateShoppingListRequest(listName, id);

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> service.createShoppingList(request, email));
        verify(shoppingListRepository, times(0)).save(any(ShoppingList.class));
    }

    @Test
    void test_createShoppingListWrongGroupId() {
        final CreateShoppingListRequest request = new CreateShoppingListRequest(listName, 2L);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () -> service.createShoppingList(request, email));
        assertEquals("You tried to create shopping list for non existing group", ex.getMessage());
        verify(shoppingListRepository, times(0)).save(any(ShoppingList.class));
    }

    @Test
    void test_createShoppingListNoRequiredAuthority() {
        authority.setAuthorityName(AuthorityEnum.MODIFY_SHOPPING_LIST);
        final CreateShoppingListRequest request = new CreateShoppingListRequest(listName, id);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () -> service.createShoppingList(request, email));
        assertEquals("You tried to create shopping list without permission", ex.getMessage());
        verify(shoppingListRepository, times(0)).save(any(ShoppingList.class));
    }

    @Test
    void test_getShoppingListSuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetShoppingListResponse response = this.service.getShoppingList(id, email);

        assertEquals(shoppingList.getListName(), response.listName());
        assertFalse(response.authorities().isEmpty());

        List<AuthorityDTO> responseAuthorities = response.authorities().stream()
                .filter(authorityDTO -> authorityDTO.groupId() == id)
                .toList();

        assertEquals(1, responseAuthorities.size());
        assertEquals(authority.getAuthorityName(), responseAuthorities.get(0).authority());
        assertEquals(group.getId(), responseAuthorities.get(0).groupId());
    }

    @Test
    void test_getShoppingListUserNotFound() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> service.getShoppingList(id, email));
    }

    @Test
    void test_getShoppingListNotFound() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetShoppingListResponse response = this.service.getShoppingList(2L, email);

        assertEquals(0, response.id());
        assertNull(response.listName());
        assertNull(response.authorities());
    }

    @Test
    void test_getUserShoppingListsSuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetUserShoppingListsResponse response = this.service.getUserShoppingLists(email);

        assertFalse(response.shoppingLists().isEmpty());
        assertEquals(shoppingList.getId(), response.shoppingLists().get(0).listId());
        assertEquals(shoppingList.getListName(), response.shoppingLists().get(0).listName());
        assertEquals(1, response.shoppingLists().get(0).nrOfProducts());
        assertEquals(group.getId(), response.shoppingLists().get(0).groupId());
        assertEquals(group.getGroupName(), response.shoppingLists().get(0).groupName());
    }

    @Test
    void test_getUserShoppingListsNoLists() {
        group.setShoppingLists(Collections.emptyList());

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetUserShoppingListsResponse response = this.service.getUserShoppingLists(email);

        assertTrue(response.shoppingLists().isEmpty());
    }

    @Test
    void test_getUserShoppingListsUserNotFound() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> service.getUserShoppingLists(email));
    }

    @Test
    void test_deleteShoppingListSuccessful() {
        authority.setAuthorityName(AuthorityEnum.MODIFY_SHOPPING_LIST);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        DeleteShoppingListResponse response = this.service.deleteShoppingList(id, email);

        verify(shoppingListRepository).delete(shoppingList);
        assertEquals(shoppingList.getListName(), response.deletedListName());
    }

    @Test
    void test_deleteShoppingListUserNotFound() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> service.deleteShoppingList(id, email));
        verify(shoppingListRepository, times(0)).delete(shoppingList);
    }

    @Test
    void test_deleteShoppingListListNotFound() {
        authority.setAuthorityName(AuthorityEnum.MODIFY_SHOPPING_LIST);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () -> service.deleteShoppingList(2L, email));
        assertEquals("You cannot access the shopping list because you are not member of its group", ex.getMessage());
        verify(shoppingListRepository, times(0)).delete(shoppingList);
    }

    @Test
    void test_deleteShoppingListNoRequiredAuthority() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () -> service.deleteShoppingList(id, email));
        assertEquals("You have not permissions to do that", ex.getMessage());
        verify(shoppingListRepository, times(0)).delete(shoppingList);
    }

    @Test
    void test_updateShoppingListSuccessful() {
        authority.setAuthorityName(AuthorityEnum.MODIFY_SHOPPING_LIST);
        final UpdateShoppingListRequest request = new UpdateShoppingListRequest("newListName");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetShoppingListResponse response = this.service.updateShoppingList(id, request, email);

        verify(shoppingListRepository).save(shoppingList);
        assertEquals(request.shoppingListName(), response.listName());
        assertFalse(response.authorities().isEmpty());

        List<AuthorityDTO> responseAuthorities = response.authorities().stream()
                .filter(authorityDTO -> authorityDTO.groupId() == id)
                .toList();

        assertEquals(1, responseAuthorities.size());
        assertEquals(authority.getAuthorityName(), responseAuthorities.get(0).authority());
        assertEquals(group.getId(), responseAuthorities.get(0).groupId());
    }

    @Test
    void test_updateShoppingListUserNotFound() {
        authority.setAuthorityName(AuthorityEnum.MODIFY_SHOPPING_LIST);
        final UpdateShoppingListRequest request = new UpdateShoppingListRequest("newListName");

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> service.updateShoppingList(id, request, email));
        verify(shoppingListRepository, times(0)).save(shoppingList);
    }

    @Test
    void test_updateShoppingListListNotFound() {
        authority.setAuthorityName(AuthorityEnum.MODIFY_SHOPPING_LIST);
        final UpdateShoppingListRequest request = new UpdateShoppingListRequest("newListName");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () -> service.updateShoppingList(2L, request, email));
        assertEquals("You cannot access the shopping list because you are not member of its group", ex.getMessage());
        verify(shoppingListRepository, times(0)).save(shoppingList);
    }

    @Test
    void test_updateShoppingListNoRequiredAuthority() {
        final UpdateShoppingListRequest request = new UpdateShoppingListRequest("newListName");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () -> service.updateShoppingList(id, request, email));
        assertEquals("You have not permissions to do that", ex.getMessage());
        verify(shoppingListRepository, times(0)).save(shoppingList);
    }
}

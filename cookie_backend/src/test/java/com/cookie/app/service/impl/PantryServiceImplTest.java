package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.dto.AuthorityDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.PantryMapperDTO;
import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.request.UpdatePantryRequest;
import com.cookie.app.model.response.DeletePantryResponse;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.model.response.GetUserPantriesResponse;
import com.cookie.app.repository.PantryRepository;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PantryServiceImplTest {
    final String email = "email@email.com";
    final String pantryName = "pantryName";
    final Long id = 1L;

    @Spy
    PantryMapperDTO pantryMapperDTO;
    @Spy
    AuthorityMapperDTO authorityMapperDTO;
    @Mock
    UserRepository userRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    PantryRepository pantryRepository;
    @InjectMocks
    PantryServiceImpl service;

    PantryProduct pantryProduct;
    Pantry pantry;
    Group group;
    Authority authority;
    User user;

    @BeforeEach
    void init() {
        pantryProduct = PantryProduct.builder().build();
        pantry = Pantry.builder()
                .id(id)
                .pantryName(pantryName)
                .pantryProducts(List.of(pantryProduct))
                .build();
        group = Group.builder()
                .id(id)
                .pantry(pantry)
                .build();
        pantry.setGroup(group);
        authority = Authority.builder()
                .id(id)
                .group(group)
                .authorityName(AuthorityEnum.MODIFY_PANTRY)
                .build();
        user = User.builder()
                .id(id)
                .email(email)
                .groups(List.of(group))
                .authorities(Set.of(authority))
                .build();
        authority.setUser(user);
    }

    @Test
    void test_createPantrySuccessful() {
        final CreatePantryRequest request = new CreatePantryRequest(pantryName, id);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetPantryResponse response = service.createPantry(request, email);

        verify(pantryRepository).save(any(Pantry.class));
        assertEquals(request.pantryName(), response.pantryName());
        assertFalse(response.authorities().isEmpty());

        List<AuthorityDTO> responseAuthorities = response.authorities().stream()
                .filter(authorityDTO -> authorityDTO.groupId() == id)
                .toList();

        assertEquals(1, responseAuthorities.size());
        assertEquals(authority.getAuthorityName(), responseAuthorities.get(0).authority());
        assertEquals(group.getId(), responseAuthorities.get(0).groupId());
        assertEquals(user.getId(), responseAuthorities.get(0).userId());
    }

    @Test
    void test_createPantryUserNotFound() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> service.getAllUserPantries(email));
        verify(pantryRepository, times(0)).save(any(Pantry.class));
    }

    @Test
    void test_createPantryWrongGroupId() {
        final CreatePantryRequest request = new CreatePantryRequest(pantryName, 2L);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThrows(UserPerformedForbiddenActionException.class, () -> service.createPantry(request, email));
        verify(pantryRepository, times(0)).save(any(Pantry.class));
    }

    @Test
    void test_createPantryNoRequiredAuthority() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final CreatePantryRequest request = new CreatePantryRequest(pantryName, id);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThrows(UserPerformedForbiddenActionException.class, () -> service.createPantry(request, email));
        verify(pantryRepository, times(0)).save(any(Pantry.class));
    }

    @Test
    void test_getPantrySuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetPantryResponse response = service.getPantry(id, email);

        assertEquals(pantry.getId(), response.pantryId());
        assertEquals(pantry.getPantryName(), response.pantryName());
        assertFalse(response.authorities().isEmpty());

        List<AuthorityDTO> responseAuthorities = response.authorities().stream()
                .filter(authorityDTO -> authorityDTO.groupId() == id)
                .toList();

        assertEquals(1, responseAuthorities.size());
        assertEquals(authority.getAuthorityName(), responseAuthorities.get(0).authority());
        assertEquals(group.getId(), responseAuthorities.get(0).groupId());
        assertEquals(user.getId(), responseAuthorities.get(0).userId());
    }

    @Test
    void test_getPantryUserNotFound() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> service.getPantry(id, email));
    }

    @Test
    void test_getPantryPantryNotFound() {
        group.setPantry(null);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetPantryResponse response = service.getPantry(id, email);

        assertEquals(0, response.pantryId());
        assertNull(response.pantryName());
        assertNull(response.authorities());
    }

    @Test
    void test_getAllUserPantriesSuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetUserPantriesResponse response = service.getAllUserPantries(email);

        assertFalse(response.pantries().isEmpty());
        assertEquals(pantry.getId(), response.pantries().get(0).pantryId());
        assertEquals(pantry.getPantryName(), response.pantries().get(0).pantryName());
        assertEquals(1, response.pantries().get(0).nrOfProducts());
        assertEquals(group.getId(), response.pantries().get(0).groupId());
        assertEquals(group.getGroupName(), response.pantries().get(0).groupName());
    }

    @Test
    void test_getAllUserPantriesNoPantries() {
        group.setPantry(null);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        GetUserPantriesResponse response = service.getAllUserPantries(email);
        assertTrue(response.pantries().isEmpty());
    }

    @Test
    void test_getAllUserPantriesUserNotFound() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> service.getAllUserPantries(email));
    }

    @Test
    void test_deletePantrySuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        DeletePantryResponse response = service.deletePantry(id, email);

        assertEquals(pantry.getPantryName(), response.deletedPantryName());
        verify(pantryRepository, times(1)).delete(pantry);
    }

    @Test
    void test_deletePantryUserNotFound() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> service.deletePantry(id, email));
        verify(pantryRepository, times(0)).delete(pantry);
    }

    @Test
    void test_deletePantryPantryNotFound() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThrows(UserPerformedForbiddenActionException.class, () -> service.deletePantry(2L, email));
        verify(pantryRepository, times(0)).delete(pantry);
    }

    @Test
    void test_deletePantryNotRequiredAuthority() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThrows(UserPerformedForbiddenActionException.class, () -> service.deletePantry(id, email));
        verify(pantryRepository, times(0)).delete(pantry);
    }

    @Test
    void test_updatePantrySuccessful() {
        final UpdatePantryRequest request = new UpdatePantryRequest("newPantryName");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetPantryResponse response = service.updatePantry(id, request, email);

        assertEquals(request.pantryName(), response.pantryName());
        assertFalse(response.authorities().isEmpty());

        List<AuthorityDTO> responseAuthorities = response.authorities().stream()
                .filter(authorityDTO -> authorityDTO.groupId() == id)
                .toList();

        assertEquals(1, responseAuthorities.size());
        assertEquals(authority.getAuthorityName(), responseAuthorities.get(0).authority());
        assertEquals(group.getId(), responseAuthorities.get(0).groupId());
        assertEquals(user.getId(), responseAuthorities.get(0).userId());
        verify(pantryRepository, times(1)).save(pantry);
    }

    @Test
    void test_updatePantryUserNotFound() {
        final UpdatePantryRequest request = new UpdatePantryRequest("newPantryName");

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> service.updatePantry(id, request, email));
        verify(pantryRepository, times(0)).save(pantry);
    }

    @Test
    void test_updatePantryPantryNotFound() {
        final UpdatePantryRequest request = new UpdatePantryRequest("newPantryName");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThrows(UserPerformedForbiddenActionException.class, () -> service.updatePantry(2L, request, email));
        verify(pantryRepository, times(0)).save(pantry);
    }

    @Test
    void test_updatePantryRequiredAuthority() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final UpdatePantryRequest request = new UpdatePantryRequest("newPantryName");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThrows(UserPerformedForbiddenActionException.class, () -> service.updatePantry(id, request, email));
        verify(pantryRepository, times(0)).save(pantry);
    }
}

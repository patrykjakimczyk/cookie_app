package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.model.dto.AuthorityDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.PantryMapperDTO;
import com.cookie.app.model.request.CreatePantryRequest;
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
import static org.mockito.ArgumentMatchers.anyString;
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
                .authority(AuthorityEnum.MODIFY_PANTRY)
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

        doReturn(Optional.of(user)).when(userRepository).findByEmail(anyString());

        GetPantryResponse response = service.createPantry(request, email);
        verify(pantryRepository).save(any(Pantry.class));

        assertEquals(request.pantryName(), response.pantryName());
        assertFalse(response.authorities().isEmpty());

        List<AuthorityDTO> responseAuthorities = response.authorities().stream()
                .filter(authorityDTO -> authorityDTO.groupId() == id)
                .toList();

        assertEquals(1, responseAuthorities.size());
        assertEquals(authority.getAuthority(), responseAuthorities.get(0).authority());
        assertEquals(group.getId(), responseAuthorities.get(0).groupId());
        assertEquals(user.getId(), responseAuthorities.get(0).userId());
    }

    @Test
    void test_createPantryThrowsErrorOnWrongGroupId() {
        final CreatePantryRequest request = new CreatePantryRequest(pantryName, 2L);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(anyString());

        assertThrows(UserPerformedForbiddenActionException.class, () -> service.createPantry(request, email));
        verify(pantryRepository, times(0)).save(any(Pantry.class));
    }

    @Test
    void test_createPantryThrowsErrorOnNoRequiredAuthority() {
        authority.setAuthority(AuthorityEnum.MODIFY);
        final CreatePantryRequest request = new CreatePantryRequest(pantryName, id);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(anyString());

        assertThrows(UserPerformedForbiddenActionException.class, () -> service.createPantry(request, email));
        verify(pantryRepository, times(0)).save(any(Pantry.class));
    }

    @Test
    void test_getPantrySuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(anyString());

        GetPantryResponse response = service.getPantry(id, email);
        assertEquals(pantry.getId(), response.id());
        assertEquals(pantry.getPantryName(), response.pantryName());
        assertFalse(response.authorities().isEmpty());

        List<AuthorityDTO> responseAuthorities = response.authorities().stream()
                .filter(authorityDTO -> authorityDTO.groupId() == id)
                .toList();

        assertEquals(1, responseAuthorities.size());
        assertEquals(authority.getAuthority(), responseAuthorities.get(0).authority());
        assertEquals(group.getId(), responseAuthorities.get(0).groupId());
        assertEquals(user.getId(), responseAuthorities.get(0).userId());
    }

    @Test
    void test_getPantryPantryNotFound() {
        group.setPantry(null);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(anyString());

        GetPantryResponse response = service.getPantry(id, email);
        assertNull(response.id());
        assertNull(response.pantryName());
        assertNull(response.authorities());
    }

    @Test
    void test_getAllUserPantriesSuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(anyString());

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

        doReturn(Optional.of(user)).when(userRepository).findByEmail(anyString());

        GetUserPantriesResponse response = service.getAllUserPantries(email);
        assertTrue(response.pantries().isEmpty());
    }
}

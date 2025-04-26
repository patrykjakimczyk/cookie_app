package com.cookie.app.service.impl;

import com.cookie.app.exception.ResourceNotFoundException;
import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.dto.AuthorityDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PantryServiceImplTest {
    final String email = "email@email.com";
    final String pantryName = "pantryName";
    final Long id = 1L;

    @Spy
    PantryMapper pantryMapper = new PantryMapperImpl();
    @Spy
    AuthorityMapper authorityMapper = new AuthorityMapperImpl();
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
        assertThat(response.pantryName()).isEqualTo(request.pantryName());
        assertThat(response.authorities()).isNotEmpty();

        List<AuthorityDTO> responseAuthorities = response.authorities().stream()
                .filter(authorityDTO -> authorityDTO.groupId() == id)
                .toList();

        assertThat(responseAuthorities).hasSize(1);
        assertThat(responseAuthorities.get(0).authority()).isEqualTo(authority.getAuthorityName());
        assertThat(responseAuthorities.get(0).groupId()).isEqualTo(group.getId());
    }

    @Test
    void test_createPantryUserNotFound() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.getAllUserPantries(email))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class);
        verify(pantryRepository, times(0)).save(any(Pantry.class));
    }

    @Test
    void test_createPantryWrongGroupId() {
        final CreatePantryRequest request = new CreatePantryRequest(pantryName, 2L);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.createPantry(request, email))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(pantryRepository, times(0)).save(any(Pantry.class));
    }

    @Test
    void test_createPantryNoRequiredAuthority() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final CreatePantryRequest request = new CreatePantryRequest(pantryName, id);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.createPantry(request, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class);
        verify(pantryRepository, times(0)).save(any(Pantry.class));
    }

    @Test
    void test_getPantrySuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetPantryResponse response = service.getPantry(id, email);

        assertThat(response.pantryId()).isEqualTo(pantry.getId());
        assertThat(response.pantryName()).isEqualTo(pantry.getPantryName());
        assertThat(response.authorities()).isNotEmpty();

        List<AuthorityDTO> responseAuthorities = response.authorities().stream()
                .filter(authorityDTO -> authorityDTO.groupId() == id)
                .toList();

        assertThat(responseAuthorities).hasSize(1);
        assertThat(responseAuthorities.get(0).authority()).isEqualTo(authority.getAuthorityName());
        assertThat(responseAuthorities.get(0).groupId()).isEqualTo(group.getId());
    }

    @Test
    void test_getPantryUserNotFound() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.getPantry(id, email))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class);
    }

    @Test
    void test_getPantryPantryNotFound() {
        group.setPantry(null);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetPantryResponse response = service.getPantry(id, email);

        assertThat(response.pantryId()).isEqualTo(0);
        assertThat(response.pantryName()).isNull();
        assertThat(response.authorities()).isNull();
    }

    @Test
    void test_getAllUserPantriesSuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetUserPantriesResponse response = service.getAllUserPantries(email);

        assertThat(response.pantries()).isNotEmpty();
        assertThat(response.pantries().get(0).pantryId()).isEqualTo(pantry.getId());
        assertThat(response.pantries().get(0).pantryName()).isEqualTo(pantry.getPantryName());
        assertThat(response.pantries().get(0).nrOfProducts()).isEqualTo(1);
        assertThat(response.pantries().get(0).groupId()).isEqualTo(group.getId());
        assertThat(response.pantries().get(0).groupName()).isEqualTo(group.getGroupName());
    }

    @Test
    void test_getAllUserPantriesNoPantries() {
        group.setPantry(null);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        GetUserPantriesResponse response = service.getAllUserPantries(email);
        assertThat(response.pantries()).isEmpty();
    }

    @Test
    void test_getAllUserPantriesUserNotFound() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.getAllUserPantries(email))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class);
    }

    @Test
    void test_deletePantrySuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        DeletePantryResponse response = service.deletePantry(id, email);

        assertThat(response.deletedPantryName()).isEqualTo(pantry.getPantryName());
        verify(pantryRepository, times(1)).delete(pantry);
    }

    @Test
    void test_deletePantryUserNotFound() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.deletePantry(id, email))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class);
        verify(pantryRepository, times(0)).delete(pantry);
    }

    @Test
    void test_deletePantryPantryNotFound() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.deletePantry(2L, email))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(pantryRepository, times(0)).delete(pantry);
    }

    @Test
    void test_deletePantryNotRequiredAuthority() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.deletePantry(id, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class);
        verify(pantryRepository, times(0)).delete(pantry);
    }

    @Test
    void test_updatePantrySuccessful() {
        final UpdatePantryRequest request = new UpdatePantryRequest("newPantryName");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetPantryResponse response = service.updatePantry(id, request, email);

        assertThat(response.pantryName()).isEqualTo(request.pantryName());
        assertThat(response.authorities()).isNotEmpty();

        List<AuthorityDTO> responseAuthorities = response.authorities().stream()
                .filter(authorityDTO -> authorityDTO.groupId() == id)
                .toList();

        assertThat(responseAuthorities).hasSize(1);
        assertThat(responseAuthorities.get(0).authority()).isEqualTo(authority.getAuthorityName());
        assertThat(responseAuthorities.get(0).groupId()).isEqualTo(group.getId());
        verify(pantryRepository, times(1)).save(pantry);
    }

    @Test
    void test_updatePantryUserNotFound() {
        final UpdatePantryRequest request = new UpdatePantryRequest("newPantryName");

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.updatePantry(id, request, email))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class);
        verify(pantryRepository, times(0)).save(pantry);
    }

    @Test
    void test_updatePantryPantryNotFound() {
        final UpdatePantryRequest request = new UpdatePantryRequest("newPantryName");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.updatePantry(2L, request, email))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(pantryRepository, times(0)).save(pantry);
    }

    @Test
    void test_updatePantryRequiredAuthority() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final UpdatePantryRequest request = new UpdatePantryRequest("newPantryName");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> service.updatePantry(id, request, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class);
        verify(pantryRepository, times(0)).save(pantry);
    }
}

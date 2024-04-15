package com.cookie.app.service.impl;

import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.PantryProductMapperDTO;
import com.cookie.app.model.mapper.ProductMapperDTO;
import com.cookie.app.repository.PantryProductRepository;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.RequestEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PantryProductServiceImplTest {
    final String email = "email@email.com";
    final String pantryName = "pantryName";
    final String filter = "filter";
    final String col = "col";
    final String idCol = "id";
    final Long id = 1L;

    @Spy
    AuthorityMapperDTO authorityMapperDTO;
    @Spy
    PantryProductMapperDTO pantryProductMapper = new PantryProductMapperDTO(new ProductMapperDTO());
    @Mock
    UserRepository userRepository;
    @Mock
    PantryProductRepository pantryProductRepository;
    @Mock
    ProductRepository productRepository;
    @InjectMocks
    PantryProductServiceImpl service;

    @Captor
    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;

    PantryProduct pantryProduct;
    Pantry pantry;
    Group group;
    Authority authority;
    User user;

    @BeforeEach
    void init() {
        Product product = Product.builder()
                .productName("productName")
                .build();
        pantryProduct = PantryProduct.builder()
                .id(this.id)
                .product(product)
                .build();
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
    void test_getPantryProductsSuccessfulWithAscSort() {
        PageImpl<PantryProduct> pageResponse = new PageImpl<>(List.of(pantryProduct));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(anyString());
        doReturn(pageResponse).when(pantryProductRepository).findProductsInPantry(
                eq(id),
                this.pageRequestArgumentCaptor.capture()
        );
        Page<PantryProductDTO> result = this.service.getPantryProducts(id, 0, null, col, "ASC", email);
        PageRequest pageRequest = this.pageRequestArgumentCaptor.getValue();

        verify(pantryProductRepository).findProductsInPantry(anyLong(), any(PageRequest.class));
        assertEquals(pageResponse.getTotalElements(), result.getTotalElements());
        assertEquals(pageResponse.getContent().get(0).getProduct().getProductName(), result.getContent().get(0).product().productName());
        assertEquals(pageResponse.getContent().get(0).getId(), result.getContent().get(0).id());
        assertNotNull(pageRequest.getSort().getOrderFor(col));
        assertEquals(Sort.Direction.ASC, pageRequest.getSort().getOrderFor(col).getDirection());
        assertEquals(0, pageRequest.getPageNumber());
    }

    @Test
    void test_getPantryProductsSuccessfulWithFilterAndDescSort() {
        PageImpl<PantryProduct> pageResponse = new PageImpl<>(List.of(pantryProduct));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(anyString());
        doReturn(pageResponse).when(pantryProductRepository).findProductsInPantryWithFilter(
                eq(id),
                eq(filter),
                this.pageRequestArgumentCaptor.capture()
        );

        Page<PantryProductDTO> result = this.service.getPantryProducts(id, 0, filter, col, "DESC", email);
        PageRequest pageRequest = this.pageRequestArgumentCaptor.getValue();

        verify(pantryProductRepository).findProductsInPantryWithFilter(anyLong(), anyString(), any(PageRequest.class));
        assertEquals(pageResponse.getTotalElements(), result.getTotalElements());
        assertEquals(pageResponse.getContent().get(0).getProduct().getProductName(), result.getContent().get(0).product().productName());
        assertEquals(pageResponse.getContent().get(0).getId(), result.getContent().get(0).id());
        assertNotNull(pageRequest.getSort().getOrderFor(idCol));
        assertEquals(Sort.Direction.DESC, pageRequest.getSort().getOrderFor(idCol).getDirection());
        assertNotNull(pageRequest.getSort().getOrderFor(col));
        assertEquals(Sort.Direction.DESC, pageRequest.getSort().getOrderFor(col).getDirection());
        assertEquals(0, pageRequest.getPageNumber());
    }
}

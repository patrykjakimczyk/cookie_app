package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.ValidationException;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.Unit;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    ArgumentCaptor<PageRequest> pageRequestArgCaptor;
    @Captor
    ArgumentCaptor<List<PantryProduct>> listOfProductsArgCaptor;
    @Captor
    ArgumentCaptor<PantryProduct> pantryProductArgCaptor;

    PantryProduct pantryProduct;
    Pantry pantry;
    Group group;
    Authority authority;
    User user;

    @BeforeEach
    void init() {
        Product product = Product.builder()
                .productName("productName")
                .category(Category.CEREAL)
                .build();
        pantryProduct = PantryProduct.builder()
                .id(this.id)
                .product(product)
                .quantity(100)
                .unit(Unit.GRAMS)
                .build();
        pantry = Pantry.builder()
                .id(id)
                .pantryName(pantryName)
                .pantryProducts(new ArrayList<>(List.of(pantryProduct)))
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
                this.pageRequestArgCaptor.capture()
        );
        Page<PantryProductDTO> result = this.service.getPantryProducts(id, 0, null, col, "ASC", email);
        PageRequest pageRequest = this.pageRequestArgCaptor.getValue();

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
                this.pageRequestArgCaptor.capture()
        );

        Page<PantryProductDTO> result = this.service.getPantryProducts(id, 0, filter, col, "DESC", email);
        PageRequest pageRequest = this.pageRequestArgCaptor.getValue();

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

    @Test
    void test_addProductsToPantrySuccessfulAsNewProducts() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        ProductDTO productDTO = new ProductDTO(0L, "productName2", Category.CEREAL);
        PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, productDTO, null, null, 100, Unit.GRAMS, 0, null);
        ProductDTO productDTO2 = new ProductDTO(0L, "productName2", Category.CEREAL);
        PantryProductDTO pantryProductDTO2 = new PantryProductDTO(0L, productDTO2, null, null, 200, Unit.PIECES, 0, null);
        List<PantryProductDTO> productsToAdd = List.of(pantryProductDTO, pantryProductDTO2);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.empty(), Optional.empty()).when(productRepository).findByProductNameAndCategory(
                productDTO.productName(),
                productDTO.category().name()
        );
        doReturn(Collections.emptyList()).when(pantryProductRepository).saveAll(
                this.listOfProductsArgCaptor.capture()
        );
        this.service.addProductsToPantry(id, productsToAdd, email);
        List<PantryProduct> addedProducts = this.listOfProductsArgCaptor.getValue();

        verify(productRepository, times(2)).findByProductNameAndCategory(anyString(), anyString());
        verify(productRepository, times(2)).save(any(Product.class));
        verify(pantryProductRepository, times(0)).deleteByIdIn(any(List.class));
        assertEquals(addedProducts.get(0).getProduct().getProductName(), productDTO.productName());
        assertEquals(addedProducts.get(0).getProduct().getCategory(), productDTO.category());
        assertEquals(addedProducts.get(0).getQuantity(), pantryProductDTO.quantity());
        assertEquals(addedProducts.get(0).getUnit(), pantryProductDTO.unit());
        assertEquals(addedProducts.get(1).getProduct().getProductName(), productDTO2.productName());
        assertEquals(addedProducts.get(1).getProduct().getCategory(), productDTO2.category());
        assertEquals(addedProducts.get(1).getQuantity(), pantryProductDTO2.quantity());
        assertEquals(addedProducts.get(1).getUnit(), pantryProductDTO2.unit());
        assertEquals(3, pantry.getPantryProducts().size());
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(0)));
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(1)));
    }

    @Test
    void test_addProductsToPantrySuccessfulExistingProducts() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        ProductDTO productDTO = new ProductDTO(0L, "productName2", Category.CEREAL);
        PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, productDTO, null, null, 100, Unit.GRAMS, 0, null);
        ProductDTO productDTO2 = new ProductDTO(0L, "productName2", Category.CEREAL);
        PantryProductDTO pantryProductDTO2 = new PantryProductDTO(0L, productDTO2, null, null, 200, Unit.PIECES, 0, null);
        Product product2 = Product.builder().id(id).productName("productName2").category(Category.CEREAL).build();
        List<PantryProductDTO> productsToAdd = List.of(pantryProductDTO, pantryProductDTO2);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(product2), Optional.of(product2)).when(productRepository).findByProductNameAndCategory(
                productDTO.productName(),
                productDTO.category().name()
        );
        doReturn(Collections.emptyList()).when(pantryProductRepository).saveAll(
                this.listOfProductsArgCaptor.capture()
        );
        this.service.addProductsToPantry(id, productsToAdd, email);
        List<PantryProduct> addedProducts = this.listOfProductsArgCaptor.getValue();

        verify(productRepository, times(2)).findByProductNameAndCategory(anyString(), anyString());
        verify(productRepository, times(0)).save(any(Product.class));
        verify(pantryProductRepository, times(0)).deleteByIdIn(any(List.class));
        assertEquals(addedProducts.get(0).getProduct().getProductName(), productDTO.productName());
        assertEquals(addedProducts.get(0).getProduct().getCategory(), productDTO.category());
        assertEquals(addedProducts.get(0).getQuantity(), pantryProductDTO.quantity());
        assertEquals(addedProducts.get(0).getUnit(), pantryProductDTO.unit());
        assertEquals(addedProducts.get(1).getProduct().getProductName(), productDTO2.productName());
        assertEquals(addedProducts.get(1).getProduct().getCategory(), productDTO2.category());
        assertEquals(addedProducts.get(1).getQuantity(), pantryProductDTO2.quantity());
        assertEquals(addedProducts.get(1).getUnit(), pantryProductDTO2.unit());
        assertEquals(3, pantry.getPantryProducts().size());
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(0)));
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(1)));
    }

    @Test
    void test_addProductsToPantrySuccessfulEmptyPantry() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        pantry.setPantryProducts(new ArrayList<>());
        ProductDTO productDTO = new ProductDTO(0L, "productName2", Category.CEREAL);
        PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, productDTO, null, null, 100, Unit.GRAMS, 0, null);
        ProductDTO productDTO2 = new ProductDTO(0L, "productName2", Category.CEREAL);
        PantryProductDTO pantryProductDTO2 = new PantryProductDTO(0L, productDTO2, null, null, 200, Unit.PIECES, 0, null);
        Product product2 = Product.builder().id(id).productName("productName2").category(Category.CEREAL).build();
        List<PantryProductDTO> productsToAdd = List.of(pantryProductDTO, pantryProductDTO2);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(product2), Optional.of(product2)).when(productRepository).findByProductNameAndCategory(
                productDTO.productName(),
                productDTO.category().name()
        );
        doReturn(Collections.emptyList()).when(pantryProductRepository).saveAll(
                this.listOfProductsArgCaptor.capture()
        );
        this.service.addProductsToPantry(id, productsToAdd, email);
        List<PantryProduct> addedProducts = this.listOfProductsArgCaptor.getValue();

        verify(productRepository, times(2)).findByProductNameAndCategory(anyString(), anyString());
        verify(productRepository, times(0)).save(any(Product.class));
        verify(pantryProductRepository, times(0)).deleteByIdIn(any(List.class));
        assertEquals(addedProducts.get(0).getProduct().getProductName(), productDTO.productName());
        assertEquals(addedProducts.get(0).getProduct().getCategory(), productDTO.category());
        assertEquals(addedProducts.get(0).getQuantity(), pantryProductDTO.quantity());
        assertEquals(addedProducts.get(0).getUnit(), pantryProductDTO.unit());
        assertEquals(addedProducts.get(1).getProduct().getProductName(), productDTO2.productName());
        assertEquals(addedProducts.get(1).getProduct().getCategory(), productDTO2.category());
        assertEquals(addedProducts.get(1).getQuantity(), pantryProductDTO2.quantity());
        assertEquals(addedProducts.get(1).getUnit(), pantryProductDTO2.unit());
        assertEquals(2, pantry.getPantryProducts().size());
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(0)));
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(1)));
    }

    @Test
    void test_addProductsToPantrySuccessfulStackedPantryProduct() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, productDTO, null, null, 100, Unit.GRAMS, 0, null);
        ProductDTO productDTO2 = new ProductDTO(0L, "productName", Category.CEREAL);
        PantryProductDTO pantryProductDTO2 = new PantryProductDTO(0L, productDTO2, null, null, 200, Unit.PIECES, 0, null);
        Product product2 = Product.builder().id(id).productName("productName").category(Category.CEREAL).build();
        List<PantryProductDTO> productsToAdd = List.of(pantryProductDTO, pantryProductDTO2);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(product2), Optional.of(product2)).when(productRepository).findByProductNameAndCategory(
                productDTO.productName(),
                productDTO.category().name()
        );
        doReturn(Collections.emptyList()).when(pantryProductRepository).saveAll(
                this.listOfProductsArgCaptor.capture()
        );
        this.service.addProductsToPantry(id, productsToAdd, email);
        List<PantryProduct> addedProducts = this.listOfProductsArgCaptor.getValue();

        verify(productRepository, times(2)).findByProductNameAndCategory(anyString(), anyString());
        verify(productRepository, times(0)).save(any(Product.class));
        verify(pantryProductRepository, times(0)).deleteByIdIn(any(List.class));
        assertEquals(pantry.getPantryProducts().get(0).getProduct().getProductName(), productDTO.productName());
        assertEquals(pantry.getPantryProducts().get(0).getProduct().getCategory(), productDTO.category());
        assertEquals(pantry.getPantryProducts().get(0).getQuantity(), pantryProduct.getQuantity());
        assertEquals(pantry.getPantryProducts().get(0).getUnit(), pantryProductDTO.unit());
        assertEquals(addedProducts.get(1).getProduct().getProductName(), productDTO2.productName());
        assertEquals(addedProducts.get(1).getProduct().getCategory(), productDTO2.category());
        assertEquals(addedProducts.get(1).getQuantity(), pantryProductDTO2.quantity());
        assertEquals(addedProducts.get(1).getUnit(), pantryProductDTO2.unit());
        assertEquals(2, pantry.getPantryProducts().size());
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(0)));
    }

    @Test
    void test_addProductsToPantryThrowsValidationExceptionPantryProductTOIdBiggerThan0() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        PantryProductDTO pantryProductDTO = new PantryProductDTO(1L, productDTO, null, null, 100, Unit.GRAMS, 0, null);
        List<PantryProductDTO> productsToAdd = Collections.singletonList(pantryProductDTO);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThrows(ValidationException.class, () -> this.service.addProductsToPantry(id, productsToAdd, email));
    }

    @Test
    void test_addProductsToPantryThrowsValidationExceptionReservedOver0() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, productDTO, null, null, 100, Unit.GRAMS, 10, null);
        List<PantryProductDTO> productsToAdd = Collections.singletonList(pantryProductDTO);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThrows(ValidationException.class, () -> this.service.addProductsToPantry(id, productsToAdd, email));
    }

    @Test
    void test_addProductsToPantryFromListSuccessfulAsNewProducts() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        ProductDTO productDTO = new ProductDTO(0L, "productName2", Category.CEREAL);
        PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, productDTO, null, null, 100, Unit.GRAMS, 0, null);
        ProductDTO productDTO2 = new ProductDTO(0L, "productName2", Category.CEREAL);
        PantryProductDTO pantryProductDTO2 = new PantryProductDTO(0L, productDTO2, null, null, 200, Unit.PIECES, 0, null);
        List<PantryProductDTO> productsToAdd = List.of(pantryProductDTO, pantryProductDTO2);

        doReturn(Optional.empty(), Optional.empty()).when(productRepository).findByProductNameAndCategory(
                productDTO.productName(),
                productDTO.category().name()
        );
        doReturn(Collections.emptyList()).when(pantryProductRepository).saveAll(
                this.listOfProductsArgCaptor.capture()
        );
        this.service.addProductsToPantryFromList(pantry, productsToAdd);
        List<PantryProduct> addedProducts = this.listOfProductsArgCaptor.getValue();

        verify(productRepository, times(2)).findByProductNameAndCategory(anyString(), anyString());
        verify(productRepository, times(2)).save(any(Product.class));
        verify(pantryProductRepository, times(0)).deleteByIdIn(any(List.class));
        assertEquals(addedProducts.get(0).getProduct().getProductName(), productDTO.productName());
        assertEquals(addedProducts.get(0).getProduct().getCategory(), productDTO.category());
        assertEquals(addedProducts.get(0).getQuantity(), pantryProductDTO.quantity());
        assertEquals(addedProducts.get(0).getUnit(), pantryProductDTO.unit());
        assertEquals(addedProducts.get(1).getProduct().getProductName(), productDTO2.productName());
        assertEquals(addedProducts.get(1).getProduct().getCategory(), productDTO2.category());
        assertEquals(addedProducts.get(1).getQuantity(), pantryProductDTO2.quantity());
        assertEquals(addedProducts.get(1).getUnit(), pantryProductDTO2.unit());
        assertEquals(3, pantry.getPantryProducts().size());
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(0)));
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(1)));
    }

    @Test
    void test_removeProductsFromPantrySuccessful() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        List<Long> productsToRemoveIds = Collections.singletonList(1L);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        this.service.removeProductsFromPantry(id, productsToRemoveIds, email);

        verify(pantryProductRepository).deleteByIdIn(productsToRemoveIds);
    }

    @Test
    void test_removeProductsFromPantryThrowsException() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        List<Long> productsToRemoveIds = Collections.singletonList(2L);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.service.removeProductsFromPantry(id, productsToRemoveIds, email));
        verify(pantryProductRepository, times(0)).deleteByIdIn(productsToRemoveIds);
    }

    @Test
    void test_modifyPantryProductSuccessful() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        PantryProductDTO pantryProductDTO = new PantryProductDTO(1L, productDTO, null, null, 200, Unit.PIECES, 0, null);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(null).when(pantryProductRepository).save(
                this.pantryProductArgCaptor.capture()
        );
        this.service.modifyPantryProduct(id, pantryProductDTO, email);
        PantryProduct modifiedProduct = this.pantryProductArgCaptor.getValue();

        verify(pantryProductRepository).save(pantryProduct);
        assertEquals(modifiedProduct.getProduct().getProductName(), pantryProductDTO.product().productName());
        assertEquals(modifiedProduct.getProduct().getCategory(), pantryProductDTO.product().category());
        assertEquals(modifiedProduct.getQuantity(), pantryProductDTO.quantity());
        assertEquals(modifiedProduct.getUnit(), pantryProductDTO.unit());
        assertEquals(modifiedProduct.getReserved(), pantryProductDTO.reserved());
        assertEquals(modifiedProduct.getPurchaseDate(), pantryProductDTO.purchaseDate());
        assertEquals(modifiedProduct.getExpirationDate(), pantryProductDTO.expirationDate());
        assertEquals(modifiedProduct.getPlacement(), pantryProductDTO.placement());
        //pozmienaic kolejnosci w asercjach
    }
}

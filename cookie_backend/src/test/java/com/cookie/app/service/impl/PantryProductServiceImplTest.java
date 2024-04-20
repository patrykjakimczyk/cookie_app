package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.ValidationException;
import com.cookie.app.model.dto.PageResult;
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

    Product product;
    PantryProduct pantryProduct;
    Pantry pantry;
    Authority authority;
    User user;

    @BeforeEach
    void init() {
        product = Product.builder()
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
        pantryProduct.setPantry(pantry);
        Group group = Group.builder()
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
        final PageImpl<PantryProduct> pageResponse = new PageImpl<>(List.of(pantryProduct));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(anyString());
        doReturn(pageResponse).when(pantryProductRepository).findProductsInPantry(
                eq(id),
                this.pageRequestArgCaptor.capture()
        );
        PageResult<PantryProductDTO> result = this.service.getPantryProducts(id, 1, null, col, "ASC", email);
        PageRequest pageRequest = this.pageRequestArgCaptor.getValue();

        verify(pantryProductRepository).findProductsInPantry(anyLong(), any(PageRequest.class));
        assertEquals(pageResponse.getTotalElements(), result.totalElements());
        assertEquals(pageResponse.getContent().get(0).getProduct().getProductName(), result.content().get(0).product().productName());
        assertEquals(pageResponse.getContent().get(0).getId(), result.content().get(0).id());
        assertNotNull(pageRequest.getSort().getOrderFor(col));
        assertEquals(Sort.Direction.ASC, pageRequest.getSort().getOrderFor(col).getDirection());
        assertEquals(0, pageRequest.getPageNumber());
    }

    @Test
    void test_getPantryProductsSuccessfulWithFilterAndDescSort() {
        final PageImpl<PantryProduct> pageResponse = new PageImpl<>(List.of(pantryProduct));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(anyString());
        doReturn(pageResponse).when(pantryProductRepository).findProductsInPantryWithFilter(
                eq(id),
                eq(filter),
                this.pageRequestArgCaptor.capture()
        );

        PageResult<PantryProductDTO> result = this.service.getPantryProducts(id, 1, filter, col, "DESC", email);
        PageRequest pageRequest = this.pageRequestArgCaptor.getValue();

        verify(pantryProductRepository).findProductsInPantryWithFilter(anyLong(), anyString(), any(PageRequest.class));
        assertEquals(pageResponse.getTotalElements(), result.totalElements());
        assertEquals(pageResponse.getContent().get(0).getProduct().getProductName(), result.content().get(0).product().productName());
        assertEquals(pageResponse.getContent().get(0).getId(), result.content().get(0).id());
        assertNotNull(pageRequest.getSort().getOrderFor(idCol));
        assertEquals(Sort.Direction.DESC, pageRequest.getSort().getOrderFor(idCol).getDirection());
        assertNotNull(pageRequest.getSort().getOrderFor(col));
        assertEquals(Sort.Direction.DESC, pageRequest.getSort().getOrderFor(col).getDirection());
        assertEquals(0, pageRequest.getPageNumber());
    }

    @Test
    void test_getPantryProductsSuccessfulWithoutSort() {
        final PageImpl<PantryProduct> pageResponse = new PageImpl<>(List.of(pantryProduct));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(anyString());
        doReturn(pageResponse).when(pantryProductRepository).findProductsInPantryWithFilter(
                eq(id),
                eq(filter),
                this.pageRequestArgCaptor.capture()
        );

        PageResult<PantryProductDTO> result = this.service.getPantryProducts(id, 1, filter, null, null, email);
        PageRequest pageRequest = this.pageRequestArgCaptor.getValue();

        verify(pantryProductRepository).findProductsInPantryWithFilter(anyLong(), anyString(), any(PageRequest.class));
        assertEquals(pageResponse.getTotalElements(), result.totalElements());
        assertEquals(pageResponse.getContent().get(0).getProduct().getProductName(), result.content().get(0).product().productName());
        assertEquals(pageResponse.getContent().get(0).getId(), result.content().get(0).id());
        assertNotNull(pageRequest.getSort().getOrderFor(idCol));
        assertEquals(Sort.Direction.DESC, pageRequest.getSort().getOrderFor(idCol).getDirection());
        assertEquals(0, pageRequest.getPageNumber());
    }

    @Test
    void test_addProductsToPantrySuccessfulAsNewProducts() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        final ProductDTO productDTO = new ProductDTO(0L, "productName2", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, productDTO, null, null, 100, Unit.GRAMS, 0, null);
        final ProductDTO productDTO2 = new ProductDTO(0L, "productName2", Category.CEREAL);
        final PantryProductDTO pantryProductDTO2 = new PantryProductDTO(0L, productDTO2, null, null, 200, Unit.PIECES, 0, null);
        final List<PantryProductDTO> productsToAdd = List.of(pantryProductDTO, pantryProductDTO2);

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
        assertEquals(productDTO.productName(), addedProducts.get(0).getProduct().getProductName());
        assertEquals(productDTO.category(), addedProducts.get(0).getProduct().getCategory());
        assertEquals(pantryProductDTO.quantity(), addedProducts.get(0).getQuantity());
        assertEquals(pantryProductDTO.unit(), addedProducts.get(0).getUnit());
        assertEquals(productDTO2.productName(), addedProducts.get(1).getProduct().getProductName());
        assertEquals(productDTO2.category(), addedProducts.get(1).getProduct().getCategory());
        assertEquals(pantryProductDTO2.quantity(), addedProducts.get(1).getQuantity());
        assertEquals(pantryProductDTO2.unit(), addedProducts.get(1).getUnit());
        assertEquals(3, pantry.getPantryProducts().size());
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(0)));
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(1)));
    }

    @Test
    void test_addProductsToPantrySuccessfulExistingProducts() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        pantry.setPantryProducts(new ArrayList<>());
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, productDTO, null, null, 100, Unit.GRAMS, 0, null);
        final ProductDTO productDTO2 = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO2 = new PantryProductDTO(0L, productDTO2, null, null, 200, Unit.PIECES, 0, null);
        final List<PantryProductDTO> productsToAdd = List.of(pantryProductDTO, pantryProductDTO2);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(product), Optional.of(product)).when(productRepository).findByProductNameAndCategory(
                anyString(), anyString()
        );
        doReturn(Collections.emptyList()).when(pantryProductRepository).saveAll(
                this.listOfProductsArgCaptor.capture()
        );
        this.service.addProductsToPantry(id, productsToAdd, email);
        List<PantryProduct> addedProducts = this.listOfProductsArgCaptor.getValue();

        verify(productRepository, times(2)).findByProductNameAndCategory(anyString(), anyString());
        verify(productRepository, times(0)).save(any(Product.class));
        verify(pantryProductRepository, times(0)).deleteByIdIn(any(List.class));
        assertEquals(productDTO.productName(), addedProducts.get(0).getProduct().getProductName());
        assertEquals(productDTO.category(), addedProducts.get(0).getProduct().getCategory());
        assertEquals(pantryProductDTO.quantity(), addedProducts.get(0).getQuantity());
        assertEquals(pantryProductDTO.unit(), addedProducts.get(0).getUnit());
        assertEquals(productDTO2.productName(), addedProducts.get(1).getProduct().getProductName());
        assertEquals(productDTO2.category(), addedProducts.get(1).getProduct().getCategory());
        assertEquals(pantryProductDTO2.quantity(), addedProducts.get(1).getQuantity());
        assertEquals(pantryProductDTO2.unit(), addedProducts.get(1).getUnit());
        assertEquals(2, pantry.getPantryProducts().size());
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(0)));
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(1)));
    }

    @Test
    void test_addProductsToPantrySuccessfulEmptyPantry() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        pantry.setPantryProducts(new ArrayList<>());
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, productDTO, null, null, 100, Unit.GRAMS, 0, null);
        final ProductDTO productDTO2 = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO2 = new PantryProductDTO(0L, productDTO2, null, null, 200, Unit.PIECES, 0, null);
        final List<PantryProductDTO> productsToAdd = List.of(pantryProductDTO, pantryProductDTO2);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(product), Optional.of(product)).when(productRepository).findByProductNameAndCategory(
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
        assertEquals(productDTO.productName(), addedProducts.get(0).getProduct().getProductName());
        assertEquals(productDTO.category(), addedProducts.get(0).getProduct().getCategory());
        assertEquals(pantryProductDTO.quantity(), addedProducts.get(0).getQuantity());
        assertEquals(pantryProductDTO.unit(), addedProducts.get(0).getUnit());
        assertEquals(productDTO2.productName(), addedProducts.get(1).getProduct().getProductName());
        assertEquals(productDTO2.category(), addedProducts.get(1).getProduct().getCategory());
        assertEquals(pantryProductDTO2.quantity(), addedProducts.get(1).getQuantity());
        assertEquals(pantryProductDTO2.unit(), addedProducts.get(1).getUnit());
        assertEquals(2, pantry.getPantryProducts().size());
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(0)));
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(1)));
    }

    @Test
    void test_addProductsToPantrySuccessfulStackedPantryProduct() {
        final long productQuantityBeforeModifing = pantryProduct.getQuantity();
        authority.setAuthorityName(AuthorityEnum.ADD);
        product.setId(id);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, productDTO, null, null, 100, Unit.GRAMS, 0, null);
        final ProductDTO productDTO2 = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO2 = new PantryProductDTO(0L, productDTO2, null, null, 200, Unit.PIECES, 0, null);
        final List<PantryProductDTO> productsToAdd = List.of(pantryProductDTO, pantryProductDTO2);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(product), Optional.of(product)).when(productRepository).findByProductNameAndCategory(
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
        assertEquals(productDTO.productName(), addedProducts.get(0).getProduct().getProductName());
        assertEquals(productDTO.category(), addedProducts.get(0).getProduct().getCategory());
        assertEquals(pantryProductDTO.quantity() + productQuantityBeforeModifing, pantry.getPantryProducts().get(0).getQuantity());
        assertEquals(pantryProductDTO.unit(), addedProducts.get(0).getUnit());
        assertEquals(productDTO2.productName(), addedProducts.get(1).getProduct().getProductName());
        assertEquals(productDTO2.category(), addedProducts.get(1).getProduct().getCategory());
        assertEquals(pantryProductDTO2.quantity(), addedProducts.get(1).getQuantity());
        assertEquals(pantryProductDTO2.unit(), addedProducts.get(1).getUnit());
        assertEquals(2, pantry.getPantryProducts().size());
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(0)));
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(1)));
    }

    @Test
    void test_addProductsToPantryPantryProductDTOIdBiggerThan0() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(1L, productDTO, null, null, 100, Unit.GRAMS, 0, null);
        final List<PantryProductDTO> productsToAdd = Collections.singletonList(pantryProductDTO);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        Exception exception = assertThrows(ValidationException.class, () -> this.service.addProductsToPantry(id, productsToAdd, email));
        assertEquals("Pantry product id must be 0 while inserting it to pantry", exception.getMessage());
    }

    @Test
    void test_addProductsToPantryReservedQuantityOver0() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, productDTO, null, null, 100, Unit.GRAMS, 10, null);
        final List<PantryProductDTO> productsToAdd = Collections.singletonList(pantryProductDTO);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        Exception exception = assertThrows(ValidationException.class, () -> this.service.addProductsToPantry(id, productsToAdd, email));
        assertEquals("Pantry product reserved quantity must be 0 while inserting it to pantry", exception.getMessage());
    }

    @Test
    void test_addProductsToPantryFromListSuccessfulAsNewProducts() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        final ProductDTO productDTO = new ProductDTO(0L, "productName2", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, productDTO, null, null, 100, Unit.GRAMS, 0, null);
        final ProductDTO productDTO2 = new ProductDTO(0L, "productName2", Category.CEREAL);
        final PantryProductDTO pantryProductDTO2 = new PantryProductDTO(0L, productDTO2, null, null, 200, Unit.PIECES, 0, null);
        final List<PantryProductDTO> productsToAdd = List.of(pantryProductDTO, pantryProductDTO2);

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
        assertEquals(productDTO.productName(), addedProducts.get(0).getProduct().getProductName());
        assertEquals(productDTO.category(), addedProducts.get(0).getProduct().getCategory());
        assertEquals(pantryProductDTO.quantity(), addedProducts.get(0).getQuantity());
        assertEquals(pantryProductDTO.unit(), addedProducts.get(0).getUnit());
        assertEquals(productDTO2.productName(), addedProducts.get(1).getProduct().getProductName());
        assertEquals(productDTO2.category(), addedProducts.get(1).getProduct().getCategory());
        assertEquals(pantryProductDTO2.quantity(), addedProducts.get(1).getQuantity());
        assertEquals(pantryProductDTO2.unit(), addedProducts.get(1).getUnit());
        assertEquals(3, pantry.getPantryProducts().size());
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(0)));
        assertTrue(pantry.getPantryProducts().contains(addedProducts.get(1)));
    }

    @Test
    void test_removeProductsFromPantrySuccessful() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final List<Long> productsToRemoveIds = Collections.singletonList(1L);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        this.service.removeProductsFromPantry(id, productsToRemoveIds, email);

        verify(pantryProductRepository).deleteByIdIn(productsToRemoveIds);
    }

    @Test
    void test_removeProductsFromPantryProductsFromDifferentPantry() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final List<Long> productsToRemoveIds = Collections.singletonList(2L);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.service.removeProductsFromPantry(id, productsToRemoveIds, email));
        verify(pantryProductRepository, times(0)).deleteByIdIn(productsToRemoveIds);
    }

    @Test
    void test_modifyPantryProductSuccessful() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(2L, productDTO, null, null, 200, Unit.PIECES, 0, null);
        final PantryProduct foundPantryProduct = new PantryProduct(2L, pantry, product, null, null, 100, Unit.PIECES, 0, "placement");
        pantry.getPantryProducts().add(foundPantryProduct);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(foundPantryProduct)).when(pantryProductRepository).findById(pantryProductDTO.id());
        doReturn(null).when(pantryProductRepository).save(
                this.pantryProductArgCaptor.capture()
        );
        this.service.updatePantryProduct(id, pantryProductDTO, email);
        PantryProduct modifiedProduct = this.pantryProductArgCaptor.getValue();

        verify(pantryProductRepository).save(foundPantryProduct);
        verify(pantryProductRepository, times(0)).deleteById(pantryProductDTO.id());
        assertEquals(pantryProductDTO.product().productName(), modifiedProduct.getProduct().getProductName());
        assertEquals(pantryProductDTO.product().category(), modifiedProduct.getProduct().getCategory());
        assertEquals(pantryProductDTO.quantity(), modifiedProduct.getQuantity());
        assertEquals(pantryProductDTO.unit(), modifiedProduct.getUnit());
        assertEquals(pantryProductDTO.reserved(), modifiedProduct.getReserved());
        assertEquals(pantryProductDTO.purchaseDate(), modifiedProduct.getPurchaseDate());
        assertEquals(pantryProductDTO.expirationDate(), modifiedProduct.getExpirationDate());
        assertEquals(pantryProductDTO.placement(), modifiedProduct.getPlacement());
    }

    @Test
    void test_modifyPantryProductSuccessfulStackingWithDifferentProduct() {
        final long productQuantityBeforeModifing = pantryProduct.getQuantity();
        final long productIdBeforeModifing = pantryProduct.getId();
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(2L, productDTO, null, null, 200, Unit.GRAMS, 100, null);
        final PantryProduct foundPantryProduct = new PantryProduct(2L, pantry, product, null, null, 100, Unit.GRAMS, 100, "placement");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(foundPantryProduct)).when(pantryProductRepository).findById(pantryProductDTO.id());
        doReturn(null).when(pantryProductRepository).save(
                this.pantryProductArgCaptor.capture()
        );
        this.service.updatePantryProduct(id, pantryProductDTO, email);
        PantryProduct modifiedProduct = this.pantryProductArgCaptor.getValue();

        verify(pantryProductRepository).save(pantryProduct);
        verify(pantryProductRepository).deleteById(pantryProductDTO.id());
        assertEquals(pantryProductDTO.product().productName(), modifiedProduct.getProduct().getProductName());
        assertEquals(pantryProductDTO.product().category(), modifiedProduct.getProduct().getCategory());
        assertEquals(pantryProductDTO.quantity() + productQuantityBeforeModifing, modifiedProduct.getQuantity());
        assertEquals(pantryProductDTO.unit(), modifiedProduct.getUnit());
        assertEquals(pantryProductDTO.reserved(), modifiedProduct.getReserved());
        assertEquals(pantryProductDTO.reserved(), modifiedProduct.getReserved());
        assertEquals(pantryProductDTO.purchaseDate(), modifiedProduct.getPurchaseDate());
        assertEquals(pantryProductDTO.expirationDate(), modifiedProduct.getExpirationDate());
        assertEquals(pantryProductDTO.placement(), modifiedProduct.getPlacement());
        assertEquals(productIdBeforeModifing, modifiedProduct.getId());
    }

    @Test
    void test_modifyPantryProductProductDoesNotExist() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(2L, productDTO, null, null, 200, Unit.GRAMS, 100, null);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.empty()).when(pantryProductRepository).findById(pantryProductDTO.id());

        Exception exception = assertThrows(UserPerformedForbiddenActionException.class, () -> this.service.updatePantryProduct(id, pantryProductDTO, email));
        assertEquals("Pantry product was not found", exception.getMessage());
        verify(pantryProductRepository, times(0)).save(pantryProduct);
        verify(pantryProductRepository, times(0)).deleteById(pantryProductDTO.id());
    }

    @Test
    void test_modifyPantryProductProductFromDifferentPantry() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(2L, productDTO, null, null, 200, Unit.GRAMS, 100, null);
        final PantryProduct foundPantryProduct = new PantryProduct(2L, new Pantry(), product, null, null, 100, Unit.PIECES, 0, "placement");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(foundPantryProduct)).when(pantryProductRepository).findById(pantryProductDTO.id());

        Exception exception = assertThrows(UserPerformedForbiddenActionException.class, () -> this.service.updatePantryProduct(id, pantryProductDTO, email));
        assertEquals("Cannot modify products from different pantry", exception.getMessage());
        verify(pantryProductRepository, times(0)).save(pantryProduct);
        verify(pantryProductRepository, times(0)).deleteById(pantryProductDTO.id());
    }

    @Test
    void test_modifyPantryProductInvalidProduct() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.ANIMAL_PRODUCTS);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(2L, productDTO, null, null, 200, Unit.GRAMS, 100, null);
        final PantryProduct foundPantryProduct = new PantryProduct(2L, pantry, product, null, null, 100, Unit.PIECES, 0, "placement");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(foundPantryProduct)).when(pantryProductRepository).findById(pantryProductDTO.id());

        Exception exception = assertThrows(UserPerformedForbiddenActionException.class, () -> this.service.updatePantryProduct(id, pantryProductDTO, email));
        assertEquals("Cannot modify invalid pantry product", exception.getMessage());
        verify(pantryProductRepository, times(0)).save(pantryProduct);
        verify(pantryProductRepository, times(0)).deleteById(pantryProductDTO.id());
    }

    @Test
    void test_reservePantryProductSuccessful() {
        final int quantityBeforeReserving = pantryProduct.getQuantity();
        final int reservedQuantity = 100;
        authority.setAuthorityName(AuthorityEnum.RESERVE);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(pantryProduct)).when(pantryProductRepository).findById(id);
        PantryProductDTO productAfterReserve = this.service.reservePantryProduct(id, id, reservedQuantity, email);

        verify(pantryProductRepository).save(pantryProduct);
        assertEquals(reservedQuantity, productAfterReserve.reserved());
        assertEquals(quantityBeforeReserving - reservedQuantity, productAfterReserve.quantity());
    }

    @Test
    void test_reservePantryProductUnreservingSuccessful() {
        final int quantityBeforeUnreserving = pantryProduct.getQuantity();
        final int reserveQuantity = -100;
        pantryProduct.setReserved(100);
        final int reservedQuantityBeforeUnreserving = pantryProduct.getReserved();
        authority.setAuthorityName(AuthorityEnum.RESERVE);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(pantryProduct)).when(pantryProductRepository).findById(id);
        PantryProductDTO productAfterUnreserve = this.service.reservePantryProduct(id, id, reserveQuantity, email);

        verify(pantryProductRepository).save(pantryProduct);
        assertEquals(reservedQuantityBeforeUnreserving + reserveQuantity, productAfterUnreserve.reserved());
        assertEquals(quantityBeforeUnreserving - reserveQuantity, productAfterUnreserve.quantity());
    }

    @Test
    void test_reservePantryProductUnreserveQuantityIsBiggerThanReservedQuantity() {
        final int reservedQuantity = 200;
        authority.setAuthorityName(AuthorityEnum.RESERVE);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(pantryProduct)).when(pantryProductRepository).findById(id);
        PantryProductDTO productAfterReserve = this.service.reservePantryProduct(id, id, reservedQuantity, email);

        verify(pantryProductRepository, times(0)).save(pantryProduct);
        assertNull(productAfterReserve);
    }

    @Test
    void test_reservePantryProductReserveQuantityIsBiggerThanAvailableQuantity() {
        final int reservedQuantity = -100;
        authority.setAuthorityName(AuthorityEnum.RESERVE);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(pantryProduct)).when(pantryProductRepository).findById(id);
        PantryProductDTO productAfterReserve = this.service.reservePantryProduct(id, id, reservedQuantity, email);

        verify(pantryProductRepository, times(0)).save(pantryProduct);
        assertNull(productAfterReserve);
    }

    @Test
    void test_reservePantryProductsFromRecipeSuccessful() {
        final int reservedQuantityBeforeReserving = pantryProduct.getReserved();
        final int quantityBeforeReserving = pantryProduct.getQuantity();
        authority.setAuthorityName(AuthorityEnum.RESERVE);
        final RecipeProduct recipeProduct = new RecipeProduct(id, product, 100, Unit.GRAMS, null);
        final List<RecipeProduct> recipeProducts = List.of(recipeProduct);

        List<RecipeProduct> unreservedProducts = this.service.reservePantryProductsFromRecipe(id, user, recipeProducts);

        assertTrue(unreservedProducts.isEmpty());
        assertEquals(reservedQuantityBeforeReserving + recipeProduct.getQuantity(), pantryProduct.getReserved());
        assertEquals(quantityBeforeReserving - recipeProduct.getQuantity(), pantryProduct.getQuantity());
        verify(pantryProductRepository).save(pantryProduct);
    }

    @Test
    void test_reservePantryProductsFromRecipeNoProductsFound() {
        authority.setAuthorityName(AuthorityEnum.RESERVE);
        final RecipeProduct recipeProduct = new RecipeProduct(id, product, 100, Unit.PIECES, null);
        final List<RecipeProduct> recipeProducts = List.of(recipeProduct);

        List<RecipeProduct> unreservedProducts = this.service.reservePantryProductsFromRecipe(id, user, recipeProducts);

        assertEquals(recipeProducts.size(), unreservedProducts.size());
        assertTrue(unreservedProducts.contains(recipeProduct));
        verify(pantryProductRepository, times(0)).save(pantryProduct);
    }

    @Test
    void test_getRecipeProductsNotInPantryReturnsMissingProduct() {
        final RecipeProduct recipeProduct = new RecipeProduct(id, product, 100, Unit.PIECES, null);
        final List<RecipeProduct> recipeProducts = List.of(recipeProduct);

        List<RecipeProduct> missingProducts = this.service.getRecipeProductsNotInPantry(pantry, recipeProducts);

        assertEquals(recipeProducts.size(), missingProducts.size());
        assertTrue(missingProducts.contains(recipeProduct));
    }

    @Test
    void test_getRecipeProductsNotInPantryReturnsEmptyList() {
        final RecipeProduct recipeProduct = new RecipeProduct(id, product, 100, Unit.GRAMS, null);
        final List<RecipeProduct> recipeProducts = List.of(recipeProduct);

        List<RecipeProduct> missingProducts = this.service.getRecipeProductsNotInPantry(pantry, recipeProducts);

        assertTrue(missingProducts.isEmpty());
    }

    @Test
    void test_getRecipeProductsNotInPantryReturnsSameRecipeList() {
        final RecipeProduct recipeProduct = new RecipeProduct(id, product, 100, Unit.GRAMS, null);
        final List<RecipeProduct> recipeProducts = List.of(recipeProduct);

        List<RecipeProduct> missingProducts = this.service.getRecipeProductsNotInPantry(null, recipeProducts);

        assertEquals(recipeProducts.size(), missingProducts.size());
        assertTrue(missingProducts.contains(recipeProduct));
    }
}

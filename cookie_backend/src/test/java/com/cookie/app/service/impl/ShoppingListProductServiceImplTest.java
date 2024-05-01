package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.ValidationException;
import com.cookie.app.model.dto.PageResult;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.Unit;
import com.cookie.app.model.mapper.*;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.ShoppingListProductRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ShoppingListProductServiceImplTest {
    final String email = "email@email.com";
    final String pantryName = "pantryName";
    final String filter = "filter";
    final String col = "col";
    final String idCol = "id";
    final Long id = 1L;

    @Captor
    ArgumentCaptor<PageRequest> pageRequestArgCaptor;
    @Captor
    ArgumentCaptor<List<ShoppingListProduct>> listOfProductsArgCaptor;
    @Captor
    ArgumentCaptor<List<PantryProductDTO>> listOfPantryProductsArgCaptor;
    @Captor
    ArgumentCaptor<ShoppingListProduct> listProductArgCaptor;

    @Spy
    AuthorityMapper authorityMapper = new AuthorityMapperImpl();
    @Spy
    ShoppingListProductMapper shoppingListProductMapper = new ShoppingListProductMapperImpl(new ProductMapperImpl());
    @Mock
    UserRepository userRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    ShoppingListProductRepository shoppingListProductRepository;
    @Mock
    PantryProductService pantryProductService;
    @InjectMocks
    ShoppingListProductServiceImpl service;

    Product product;
    ShoppingListProduct shoppingListProduct;
    ShoppingList shoppingList;
    Authority authority;
    User user;
    Group group;

    @BeforeEach
    void init() {
        product = Product.builder()
                .productName("productName")
                .category(Category.CEREAL)
                .build();
        shoppingListProduct = ShoppingListProduct.builder()
                .id(this.id)
                .product(product)
                .quantity(100)
                .unit(Unit.GRAMS)
                .build();
        shoppingList = ShoppingList.builder()
                .id(id)
                .listName(pantryName)
                .productsList(new ArrayList<>(List.of(shoppingListProduct)))
                .build();
        shoppingListProduct.setShoppingList(shoppingList);
        group = Group.builder()
                .id(id)
                .shoppingLists(Collections.singletonList(shoppingList))
                .build();
        shoppingList.setGroup(group);
        authority = Authority.builder()
                .id(id)
                .group(group)
                .authorityName(AuthorityEnum.MODIFY_SHOPPING_LIST)
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
    void test_getShoppingListProductsWithAscSort() {
        final PageImpl<ShoppingListProduct> pageResponse = new PageImpl<>(List.of(shoppingListProduct));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(pageResponse).when(shoppingListProductRepository).findShoppingListProductByShoppingListId(
                eq(id),
                this.pageRequestArgCaptor.capture()
        );
        PageResult<ShoppingListProductDTO> result = this.service.getShoppingListProducts(id, 1, null, col, Sort.Direction.ASC, email);
        PageRequest pageRequest = this.pageRequestArgCaptor.getValue();

        verify(shoppingListProductRepository).findShoppingListProductByShoppingListId(eq(id), any(PageRequest.class));
        assertEquals(pageResponse.getTotalElements(), result.totalElements());
        assertEquals(pageResponse.getContent().get(0).getProduct().getProductName(), result.content().get(0).product().productName());
        assertEquals(pageResponse.getContent().get(0).getId(), result.content().get(0).id());
        assertNotNull(pageRequest.getSort().getOrderFor(col));
        assertEquals(Sort.Direction.ASC, pageRequest.getSort().getOrderFor(col).getDirection());
        assertEquals(0, pageRequest.getPageNumber());
    }

    @Test
    void test_getShoppingListProductsSuccessfulWithFilterAndDescSort() {
        final PageImpl<ShoppingListProduct> pageResponse = new PageImpl<>(List.of(shoppingListProduct));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(pageResponse).when(shoppingListProductRepository).findProductsInShoppingListWithFilter(
                eq(id),
                eq(filter),
                this.pageRequestArgCaptor.capture()
        );

        PageResult<ShoppingListProductDTO> result = this.service.getShoppingListProducts(id, 1, filter, col, Sort.Direction.DESC, email);
        PageRequest pageRequest = this.pageRequestArgCaptor.getValue();

        verify(shoppingListProductRepository).findProductsInShoppingListWithFilter(eq(id), eq(filter), any(PageRequest.class));
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
    void test_getShoppingListProductsSuccessfulWithoutSort() {
        final PageImpl<ShoppingListProduct> pageResponse = new PageImpl<>(List.of(shoppingListProduct));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(pageResponse).when(shoppingListProductRepository).findProductsInShoppingListWithFilter(
                eq(id),
                eq(filter),
                this.pageRequestArgCaptor.capture()
        );

        PageResult<ShoppingListProductDTO> result = this.service.getShoppingListProducts(id, 1, filter, null, null, email);
        PageRequest pageRequest = this.pageRequestArgCaptor.getValue();

        verify(shoppingListProductRepository).findProductsInShoppingListWithFilter(eq(id), eq(filter), any(PageRequest.class));
        assertEquals(pageResponse.getTotalElements(), result.totalElements());
        assertEquals(pageResponse.getContent().get(0).getProduct().getProductName(), result.content().get(0).product().productName());
        assertEquals(pageResponse.getContent().get(0).getId(), result.content().get(0).id());
        assertNotNull(pageRequest.getSort().getOrderFor(idCol));
        assertEquals(Sort.Direction.DESC, pageRequest.getSort().getOrderFor(idCol).getDirection());
        assertEquals(0, pageRequest.getPageNumber());
    }

    @Test
    void test_addProductsToShoppingListSuccessfulAsNewProducts() {
        authority.setAuthorityName(AuthorityEnum.ADD_TO_SHOPPING_LIST);
        final ProductDTO productDTO = new ProductDTO(0L, "productName2", Category.CEREAL);
        final ShoppingListProductDTO listProductDTO = new ShoppingListProductDTO(0L, productDTO, 100, Unit.GRAMS, false);
        final ProductDTO productDTO2 = new ProductDTO(0L, "productName2", Category.CEREAL);
        final ShoppingListProductDTO listProductDTO2 = new ShoppingListProductDTO(0L, productDTO, 200, Unit.PIECES, false);
        final List<ShoppingListProductDTO> productsToAdd = List.of(listProductDTO, listProductDTO2);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.empty(), Optional.empty()).when(productRepository).findByProductNameAndCategory(
                productDTO.productName(),
                productDTO.category().name()
        );
        doReturn(Collections.emptyList()).when(shoppingListProductRepository).saveAll(
                this.listOfProductsArgCaptor.capture()
        );
        this.service.addProductsToShoppingList(id, productsToAdd, email);
        List<ShoppingListProduct> addedProducts = this.listOfProductsArgCaptor.getValue();

        verify(productRepository, times(2)).findByProductNameAndCategory(anyString(), anyString());
        verify(productRepository, times(2)).save(any(Product.class));
        verify(shoppingListProductRepository, times(0)).deleteByIdIn(anyList());
        assertEquals(productDTO.productName(), addedProducts.get(0).getProduct().getProductName());
        assertEquals(productDTO.category(), addedProducts.get(0).getProduct().getCategory());
        assertEquals(listProductDTO.quantity(), addedProducts.get(0).getQuantity());
        assertEquals(listProductDTO.unit(), addedProducts.get(0).getUnit());
        assertEquals(productDTO2.productName(), addedProducts.get(1).getProduct().getProductName());
        assertEquals(productDTO2.category(), addedProducts.get(1).getProduct().getCategory());
        assertEquals(listProductDTO2.quantity(), addedProducts.get(1).getQuantity());
        assertEquals(listProductDTO2.unit(), addedProducts.get(1).getUnit());
        assertEquals(3, shoppingList.getProductsList().size());
        assertTrue(shoppingList.getProductsList().contains(addedProducts.get(0)));
        assertTrue(shoppingList.getProductsList().contains(addedProducts.get(1)));
    }

    @Test
    void test_addProductsToShoppingListSuccessfulEmptyList() {
        authority.setAuthorityName(AuthorityEnum.ADD_TO_SHOPPING_LIST);
        shoppingList.setProductsList(new ArrayList<>());
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final ShoppingListProductDTO listProductDTO = new ShoppingListProductDTO(0L, productDTO, 100, Unit.GRAMS, false);
        final ProductDTO productDTO2 = new ProductDTO(0L, "productName", Category.CEREAL);
        final ShoppingListProductDTO listProductDTO2 = new ShoppingListProductDTO(0L, productDTO, 200, Unit.PIECES, false);
        final List<ShoppingListProductDTO> productsToAdd = List.of(listProductDTO, listProductDTO2);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(product), Optional.of(product)).when(productRepository).findByProductNameAndCategory(
                productDTO.productName(),
                productDTO.category().name()
        );
        doReturn(Collections.emptyList()).when(shoppingListProductRepository).saveAll(
                this.listOfProductsArgCaptor.capture()
        );
        this.service.addProductsToShoppingList(id, productsToAdd, email);
        List<ShoppingListProduct> addedProducts = this.listOfProductsArgCaptor.getValue();

        verify(productRepository, times(2)).findByProductNameAndCategory(anyString(), anyString());
        verify(productRepository, times(0)).save(any(Product.class));
        verify(shoppingListProductRepository, times(0)).deleteByIdIn(anyList());
        assertEquals(productDTO.productName(), addedProducts.get(0).getProduct().getProductName());
        assertEquals(productDTO.category(), addedProducts.get(0).getProduct().getCategory());
        assertEquals(listProductDTO.quantity(), addedProducts.get(0).getQuantity());
        assertEquals(listProductDTO.unit(), addedProducts.get(0).getUnit());
        assertEquals(productDTO2.productName(), addedProducts.get(1).getProduct().getProductName());
        assertEquals(productDTO2.category(), addedProducts.get(1).getProduct().getCategory());
        assertEquals(listProductDTO2.quantity(), addedProducts.get(1).getQuantity());
        assertEquals(listProductDTO2.unit(), addedProducts.get(1).getUnit());
        assertEquals(2, shoppingList.getProductsList().size());
        assertTrue(shoppingList.getProductsList().contains(addedProducts.get(0)));
        assertTrue(shoppingList.getProductsList().contains(addedProducts.get(1)));
    }

    @Test
    void test_addProductsToShoppingListSuccessfulStackedListProduct() {
        final long productQuantityBeforeModifing = shoppingListProduct.getQuantity();
        authority.setAuthorityName(AuthorityEnum.ADD_TO_SHOPPING_LIST);
        product.setId(id);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final ShoppingListProductDTO listProductDTO = new ShoppingListProductDTO(0L, productDTO, 100, Unit.GRAMS, false);
        final ProductDTO productDTO2 = new ProductDTO(0L, "productName", Category.CEREAL);
        final ShoppingListProductDTO listProductDTO2 = new ShoppingListProductDTO(0L, productDTO, 200, Unit.PIECES, false);
        final List<ShoppingListProductDTO> productsToAdd = List.of(listProductDTO, listProductDTO2);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(product), Optional.of(product)).when(productRepository).findByProductNameAndCategory(
                productDTO.productName(),
                productDTO.category().name()
        );
        doReturn(Collections.emptyList()).when(shoppingListProductRepository).saveAll(
                this.listOfProductsArgCaptor.capture()
        );
        this.service.addProductsToShoppingList(id, productsToAdd, email);
        List<ShoppingListProduct> addedProducts = this.listOfProductsArgCaptor.getValue();

        verify(productRepository, times(2)).findByProductNameAndCategory(anyString(), anyString());
        verify(productRepository, times(0)).save(any(Product.class));
        verify(shoppingListProductRepository, times(0)).deleteByIdIn(anyList());
        assertEquals(productDTO.productName(), addedProducts.get(0).getProduct().getProductName());
        assertEquals(productDTO.category(), addedProducts.get(0).getProduct().getCategory());
        assertEquals(listProductDTO.quantity() + productQuantityBeforeModifing, shoppingList.getProductsList().get(0).getQuantity());
        assertEquals(listProductDTO.unit(), addedProducts.get(0).getUnit());
        assertEquals(productDTO2.productName(), addedProducts.get(1).getProduct().getProductName());
        assertEquals(productDTO2.category(), addedProducts.get(1).getProduct().getCategory());
        assertEquals(listProductDTO2.quantity(), addedProducts.get(1).getQuantity());
        assertEquals(listProductDTO2.unit(), addedProducts.get(1).getUnit());
        assertEquals(2, shoppingList.getProductsList().size());
        assertTrue(shoppingList.getProductsList().contains(addedProducts.get(0)));
        assertTrue(shoppingList.getProductsList().contains(addedProducts.get(1)));
    }

    @Test
    void test_addProductsToShoppingListListProductDTOIdBiggerThan0() {
        authority.setAuthorityName(AuthorityEnum.ADD_TO_SHOPPING_LIST);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final ShoppingListProductDTO listProductDTO = new ShoppingListProductDTO(1L, productDTO, 100, Unit.GRAMS, false);
        final List<ShoppingListProductDTO> productsToAdd = Collections.singletonList(listProductDTO);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        Exception exception = assertThrows(ValidationException.class, () -> this.service.addProductsToShoppingList(id, productsToAdd, email));
        assertEquals("Shopping list product id must be not set while inserting it to shopping list", exception.getMessage());
    }

    @Test
    void test_addProductsToShoppingListPurchasedWhileInserting() {
        authority.setAuthorityName(AuthorityEnum.ADD_TO_SHOPPING_LIST);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final ShoppingListProductDTO listProductDTO = new ShoppingListProductDTO(0L, productDTO, 100, Unit.GRAMS, true);
        final List<ShoppingListProductDTO> productsToAdd = Collections.singletonList(listProductDTO);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        Exception exception = assertThrows(ValidationException.class, () -> this.service.addProductsToShoppingList(id, productsToAdd, email));
        assertEquals("Shopping list product cannot be purchased while inserting it to shopping list", exception.getMessage());
    }

    @Test
    void test_removeProductsFromShoppingListSuccessful() {
        final List<Long> productsToRemoveIds = Collections.singletonList(1L);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        this.service.removeProductsFromShoppingList(id, productsToRemoveIds, email);

        verify(shoppingListProductRepository).deleteByIdIn(productsToRemoveIds);
    }

    @Test
    void test_removeProductsFromShoppingListProductsFromDifferentPantry() {
        final List<Long> productsToRemoveIds = Collections.singletonList(2L);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.service.removeProductsFromShoppingList(id, productsToRemoveIds, email));
        verify(shoppingListProductRepository, times(0)).deleteByIdIn(productsToRemoveIds);
    }

    @Test
    void test_updateShoppingListProductSuccessful() {
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final ShoppingListProductDTO shoppingListProductDTO = new ShoppingListProductDTO(2L, productDTO, 200, Unit.PIECES, false);
        final ShoppingListProduct foundPantryProduct = new ShoppingListProduct(2L, shoppingList, product, 100, Unit.PIECES, false);
        shoppingList.getProductsList().add(foundPantryProduct);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(foundPantryProduct)).when(shoppingListProductRepository).findById(shoppingListProductDTO.id());
        doReturn(null).when(shoppingListProductRepository).save(
                this.listProductArgCaptor.capture()
        );
        this.service.updateShoppingListProduct(id, shoppingListProductDTO, email);
        ShoppingListProduct modifiedProduct = this.listProductArgCaptor.getValue();

        verify(shoppingListProductRepository).save(foundPantryProduct);
        verify(shoppingListProductRepository, times(0)).deleteById(shoppingListProductDTO.id());
        assertEquals(shoppingListProductDTO.product().productName(), modifiedProduct.getProduct().getProductName());
        assertEquals(shoppingListProductDTO.product().category(), modifiedProduct.getProduct().getCategory());
        assertEquals(shoppingListProductDTO.quantity(), modifiedProduct.getQuantity());
        assertEquals(shoppingListProductDTO.unit(), modifiedProduct.getUnit());
        assertEquals(shoppingListProductDTO.purchased(), modifiedProduct.isPurchased());
    }

    @Test
    void test_updateShoppingListProductSuccessfulStackingWithDifferentProduct() {
        final long productQuantityBeforeModifing = shoppingListProduct.getQuantity();
        final long productIdBeforeModifing = shoppingListProduct.getId();
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final ShoppingListProductDTO shoppingListProductDTO = new ShoppingListProductDTO(2L, productDTO, 200, Unit.GRAMS, false);
        final ShoppingListProduct foundPantryProduct = new ShoppingListProduct(3L, shoppingList, product, 100, Unit.GRAMS, false);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(foundPantryProduct)).when(shoppingListProductRepository).findById(shoppingListProductDTO.id());
        doReturn(null).when(shoppingListProductRepository).save(
                this.listProductArgCaptor.capture()
        );
        this.service.updateShoppingListProduct(id, shoppingListProductDTO, email);
        ShoppingListProduct modifiedProduct = this.listProductArgCaptor.getValue();

        verify(shoppingListProductRepository).save(shoppingListProduct);
        verify(shoppingListProductRepository).deleteById(shoppingListProductDTO.id());
        assertEquals(shoppingListProductDTO.product().productName(), modifiedProduct.getProduct().getProductName());
        assertEquals(shoppingListProductDTO.product().category(), modifiedProduct.getProduct().getCategory());
        assertEquals(shoppingListProductDTO.quantity() + productQuantityBeforeModifing, modifiedProduct.getQuantity());
        assertEquals(shoppingListProductDTO.unit(), modifiedProduct.getUnit());
        assertEquals(shoppingListProductDTO.purchased(), modifiedProduct.isPurchased());
        assertEquals(productIdBeforeModifing, modifiedProduct.getId());
    }

    @Test
    void test_updateShoppingListProductProductDoesNotExist() {
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final ShoppingListProductDTO shoppingListProductDTO = new ShoppingListProductDTO(2L, productDTO, 200, Unit.GRAMS, false);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.empty()).when(shoppingListProductRepository).findById(shoppingListProductDTO.id());

        Exception exception = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.updateShoppingListProduct(id, shoppingListProductDTO, email));
        assertEquals("Shopping list product was not found", exception.getMessage());
        verify(shoppingListProductRepository, times(0)).save(shoppingListProduct);
        verify(shoppingListProductRepository, times(0)).deleteById(shoppingListProductDTO.id());
    }

    @Test
    void test_updateShoppingListProductProductFromDifferentList() {
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final ShoppingListProductDTO shoppingListProductDTO = new ShoppingListProductDTO(2L, productDTO, 200, Unit.GRAMS, false);
        final ShoppingListProduct foundPantryProduct = new ShoppingListProduct(3L, new ShoppingList(), product, 100, Unit.GRAMS, false);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(foundPantryProduct)).when(shoppingListProductRepository).findById(shoppingListProductDTO.id());

        Exception exception = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.updateShoppingListProduct(id, shoppingListProductDTO, email));
        assertEquals("Cannot update products from different shopping list", exception.getMessage());
        verify(shoppingListProductRepository, times(0)).save(shoppingListProduct);
        verify(shoppingListProductRepository, times(0)).deleteById(shoppingListProductDTO.id());
    }

    @Test
    void test_updateShoppingListProductInvalidProduct() {
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.ANIMAL_PRODUCTS);
        final ShoppingListProductDTO shoppingListProductDTO = new ShoppingListProductDTO(2L, productDTO, 200, Unit.GRAMS, false);
        final ShoppingListProduct foundPantryProduct = new ShoppingListProduct(3L, shoppingList, product, 100, Unit.GRAMS, false);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(foundPantryProduct)).when(shoppingListProductRepository).findById(shoppingListProductDTO.id());

        Exception exception = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.updateShoppingListProduct(id, shoppingListProductDTO, email));
        assertEquals("Cannot modify invalid shopping list product", exception.getMessage());
        verify(shoppingListProductRepository, times(0)).save(shoppingListProduct);
        verify(shoppingListProductRepository, times(0)).deleteById(shoppingListProductDTO.id());
    }

    @Test
    void test_updateShoppingListProductIdEquals0() {
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.ANIMAL_PRODUCTS);
        final ShoppingListProductDTO shoppingListProductDTO = new ShoppingListProductDTO(0L, productDTO, 200, Unit.GRAMS, false);

        Exception exception = assertThrows(ValidationException.class, () ->
                this.service.updateShoppingListProduct(id, shoppingListProductDTO, email));
        assertEquals("Cannot modify product because it doesn't exist", exception.getMessage());
        verify(shoppingListProductRepository, times(0)).save(shoppingListProduct);
        verify(shoppingListProductRepository, times(0)).deleteById(shoppingListProductDTO.id());
    }

    @Test
    void test_changePurchaseStatusForProductsSuccessful() {
        final List<Long> productsToStatusChangeIds = Collections.singletonList(1L);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        this.service.changePurchaseStatusForProducts(id, productsToStatusChangeIds, email);

        verify(shoppingListProductRepository).saveAll(anyList());
    }

    @Test
    void test_changePurchaseStatusForProductsNotAllProductsAreOnList() {
        authority.setAuthorityName(AuthorityEnum.MODIFY_SHOPPING_LIST);
        final List<Long> productsToStatusChangeIds = List.of(1L, 2L);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.changePurchaseStatusForProducts(id, productsToStatusChangeIds, email));
        verify(shoppingListProductRepository, times(0)).saveAll(anyList());
    }

    @Test
    void test_transferProductsToPantrySuccessful() {
        final Authority authority1 = Authority.builder().group(group).authorityName(AuthorityEnum.ADD).build();
        user.setAuthorities(Set.of(authority, authority1));
        final Pantry pantry = Pantry.builder().pantryName("pantryName").group(group).build();
        group.setPantry(pantry);
        shoppingListProduct.setPurchased(true);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doNothing().when(pantryProductService)
                .addProductsToPantryFromList(eq(pantry), this.listOfPantryProductsArgCaptor.capture());
        this.service.transferProductsToPantry(id, email);
        List<PantryProductDTO> transferedProducts = this.listOfPantryProductsArgCaptor.getValue();

        verify(shoppingListProductRepository).deleteAll(shoppingList.getProductsList());
        assertEquals(shoppingList.getProductsList().size(), transferedProducts.size());
        assertEquals(shoppingList.getProductsList().get(0).getProduct().getId(), transferedProducts.get(0).product().productId());
        assertEquals(shoppingList.getProductsList().get(0).getProduct().getCategory(), transferedProducts.get(0).product().category());
        assertEquals(shoppingList.getProductsList().get(0).getQuantity(), transferedProducts.get(0).quantity());
        assertEquals(shoppingList.getProductsList().get(0).getUnit(), transferedProducts.get(0).unit());
        assertEquals(Timestamp.from(Instant.now().truncatedTo(ChronoUnit.DAYS)), transferedProducts.get(0).purchaseDate());
    }

    @Test
    void test_transferProductsToPantryGroupDoesNotHaveAssignedPantry() {
        final Authority authority1 = Authority.builder().group(group).authorityName(AuthorityEnum.ADD).build();
        user.setAuthorities(Set.of(authority, authority1));
        shoppingListProduct.setPurchased(true);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        Exception exception = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.transferProductsToPantry(id, email));
        assertEquals("Cannot transfer products because your group does not have assigned pantry", exception.getMessage());
        verify(shoppingListProductRepository, times(0)).deleteAll(shoppingList.getProductsList());
        verify(pantryProductService, times(0)).addProductsToPantryFromList(any(Pantry.class), anyList());
    }

    @Test
    void test_transferProductsToPantryNoPermissionToAddToPantry() {
        final Pantry pantry = Pantry.builder().pantryName("pantryName").group(group).build();
        group.setPantry(pantry);
        shoppingListProduct.setPurchased(true);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        Exception exception = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.transferProductsToPantry(id, email));
        assertEquals("You have not permissions to do that", exception.getMessage());
        verify(shoppingListProductRepository, times(0)).deleteAll(shoppingList.getProductsList());
        verify(pantryProductService, times(0)).addProductsToPantryFromList(any(Pantry.class), anyList());

    }

    @Test
    void test_transferProductsToNoProductsToTransfer() {
        final Authority authority1 = Authority.builder().group(group).authorityName(AuthorityEnum.ADD).build();
        user.setAuthorities(Set.of(authority, authority1));
        final Pantry pantry = Pantry.builder().pantryName("pantryName").group(group).build();
        group.setPantry(pantry);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        Exception exception = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.transferProductsToPantry(id, email));
        assertEquals("Cannot transfer unpurchased shopping list products", exception.getMessage());
        verify(shoppingListProductRepository, times(0)).deleteAll(shoppingList.getProductsList());
        verify(pantryProductService, times(0)).addProductsToPantryFromList(any(Pantry.class), anyList());
    }

    @Test
    void test_addRecipeProductsToShoppingListSuccessfulWithAddingQuantityToExistingProduct() {
        final int quantityBeforeAdding = shoppingListProduct.getQuantity();
        final RecipeProduct recipeProduct = RecipeProduct.builder().product(product).unit(Unit.GRAMS).quantity(100).build();
        final List<RecipeProduct> recipeProductList = Collections.singletonList(recipeProduct);
        authority.setAuthorityName(AuthorityEnum.ADD_TO_SHOPPING_LIST);

        this.service.addRecipeProductsToShoppingList(id, user, recipeProductList);

        verify(shoppingListProductRepository).save(shoppingListProduct);
        verify(shoppingListProductRepository, times(0)).saveAll(anyList());
        assertEquals(recipeProduct.getProduct(), shoppingListProduct.getProduct());
        assertEquals(recipeProduct.getUnit(), shoppingListProduct.getUnit());
        assertEquals(recipeProduct.getQuantity() + quantityBeforeAdding, shoppingListProduct.getQuantity());
    }

    @Test
    void test_addRecipeProductsToShoppingListSuccessfulAsNewProduct() {
        final Product product1 = Product.builder().productName("productName2").category(Category.ANIMAL_PRODUCTS).build();
        final RecipeProduct recipeProduct = RecipeProduct.builder().product(product1).unit(Unit.GRAMS).quantity(100).build();
        final List<RecipeProduct> recipeProductList = Collections.singletonList(recipeProduct);
        authority.setAuthorityName(AuthorityEnum.ADD_TO_SHOPPING_LIST);

        this.service.addRecipeProductsToShoppingList(id, user, recipeProductList);

        verify(shoppingListProductRepository, times(0)).save(shoppingListProduct);
        verify(shoppingListProductRepository).saveAll(this.listOfProductsArgCaptor.capture());
        List<ShoppingListProduct> addedProducts = this.listOfProductsArgCaptor.getValue();
        assertEquals(recipeProduct.getProduct(), addedProducts.get(0).getProduct());
        assertEquals(recipeProduct.getUnit(), addedProducts.get(0).getUnit());
        assertEquals(recipeProduct.getQuantity(), addedProducts.get(0).getQuantity());
    }
}

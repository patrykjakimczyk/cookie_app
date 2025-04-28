package com.cookie.app.service.impl;

import com.cookie.app.exception.ResourceNotFoundException;
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
import com.cookie.app.model.request.FilterRequest;
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

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ShoppingListProductServiceImplTest {
    private final String email = "email@email.com";
    private final String pantryName = "pantryName";
    private final String filter = "filter";
    private final String col = "col";
    private final String idCol = "id";
    private final Long id = 1L;

    @Captor
    private ArgumentCaptor<PageRequest> pageRequestArgCaptor;
    @Captor
    private ArgumentCaptor<List<ShoppingListProduct>> listOfProductsArgCaptor;
    @Captor
    private ArgumentCaptor<List<PantryProductDTO>> listOfPantryProductsArgCaptor;
    @Captor
    private ArgumentCaptor<ShoppingListProduct> listProductArgCaptor;

    @Spy
    private AuthorityMapper authorityMapper = new AuthorityMapperImpl();
    @Spy
    private ShoppingListProductMapper shoppingListProductMapper = new ShoppingListProductMapperImpl(new ProductMapperImpl());
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ShoppingListProductRepository shoppingListProductRepository;
    @Mock
    private PantryProductService pantryProductService;
    @InjectMocks
    private ShoppingListProductServiceImpl service;

    private Product product;
    private ShoppingListProduct shoppingListProduct;
    private ShoppingList shoppingList;
    private Authority authority;
    private User user;
    private Group group;

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
        final FilterRequest filterRequest = new FilterRequest(null, col, Sort.Direction.ASC);
        final PageImpl<ShoppingListProduct> pageResponse = new PageImpl<>(List.of(shoppingListProduct));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(pageResponse).when(shoppingListProductRepository).findShoppingListProductByShoppingListId(
                eq(id),
                this.pageRequestArgCaptor.capture()
        );
        PageResult<ShoppingListProductDTO> result = this.service.getShoppingListProducts(id, 1, filterRequest, email);
        PageRequest pageRequest = this.pageRequestArgCaptor.getValue();

        verify(shoppingListProductRepository).findShoppingListProductByShoppingListId(eq(id), any(PageRequest.class));
        assertThat(result.totalElements()).isEqualTo(pageResponse.getTotalElements());
        assertThat(result.content().get(0).product().productName()).isEqualTo(pageResponse.getContent().get(0).getProduct().getProductName());
        assertThat(result.content().get(0).id()).isEqualTo(pageResponse.getContent().get(0).getId());
        assertThat(pageRequest.getSort().getOrderFor(col)).isNotNull();
        assertThat(Objects.requireNonNull(pageRequest.getSort().getOrderFor(col)).getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(pageRequest.getPageNumber()).isZero();
    }

    @Test
    void test_getShoppingListProductsSuccessfulWithFilterAndDescSort() {
        final FilterRequest filterRequest = new FilterRequest(filter, col, Sort.Direction.DESC);
        final PageImpl<ShoppingListProduct> pageResponse = new PageImpl<>(List.of(shoppingListProduct));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(pageResponse).when(shoppingListProductRepository).findProductsInShoppingListWithFilter(
                eq(id),
                eq(filter),
                this.pageRequestArgCaptor.capture()
        );

        PageResult<ShoppingListProductDTO> result = this.service.getShoppingListProducts(id, 1, filterRequest, email);
        PageRequest pageRequest = this.pageRequestArgCaptor.getValue();

        verify(shoppingListProductRepository).findProductsInShoppingListWithFilter(eq(id), eq(filter), any(PageRequest.class));
        assertThat(result.totalElements()).isEqualTo(pageResponse.getTotalElements());
        assertThat(result.content().get(0).product().productName()).isEqualTo(pageResponse.getContent().get(0).getProduct().getProductName());
        assertThat(result.content().get(0).id()).isEqualTo(pageResponse.getContent().get(0).getId());
        assertThat(pageRequest.getSort().getOrderFor(idCol)).isNotNull();
        assertThat(Objects.requireNonNull(pageRequest.getSort().getOrderFor(idCol)).getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(pageRequest.getSort().getOrderFor(col)).isNotNull();
        assertThat(Objects.requireNonNull(pageRequest.getSort().getOrderFor(col)).getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(pageRequest.getPageNumber()).isZero();
    }

    @Test
    void test_getShoppingListProductsSuccessfulWithoutSort() {
        final FilterRequest filterRequest = new FilterRequest(filter, null, null);
        final PageImpl<ShoppingListProduct> pageResponse = new PageImpl<>(List.of(shoppingListProduct));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(pageResponse).when(shoppingListProductRepository).findProductsInShoppingListWithFilter(
                eq(id),
                eq(filter),
                this.pageRequestArgCaptor.capture()
        );

        PageResult<ShoppingListProductDTO> result = this.service.getShoppingListProducts(id, 1, filterRequest, email);
        PageRequest pageRequest = this.pageRequestArgCaptor.getValue();

        verify(shoppingListProductRepository).findProductsInShoppingListWithFilter(eq(id), eq(filter), any(PageRequest.class));
        assertThat(result.totalElements()).isEqualTo(pageResponse.getTotalElements());
        assertThat(result.content().get(0).product().productName()).isEqualTo(pageResponse.getContent().get(0).getProduct().getProductName());
        assertThat(result.content().get(0).id()).isEqualTo(pageResponse.getContent().get(0).getId());
        assertThat(pageRequest.getSort().getOrderFor(idCol)).isNotNull();
        assertThat(Objects.requireNonNull(pageRequest.getSort().getOrderFor(idCol)).getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(pageRequest.getPageNumber()).isZero();
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
        assertThat(addedProducts.get(0).getProduct().getProductName()).isEqualTo(productDTO.productName());
        assertThat(addedProducts.get(0).getProduct().getCategory()).isEqualTo(productDTO.category());
        assertThat(addedProducts.get(0).getQuantity()).isEqualTo(listProductDTO.quantity());
        assertThat(addedProducts.get(0).getUnit()).isEqualTo(listProductDTO.unit());
        assertThat(addedProducts.get(1).getProduct().getProductName()).isEqualTo(productDTO2.productName());
        assertThat(addedProducts.get(1).getProduct().getCategory()).isEqualTo(productDTO2.category());
        assertThat(addedProducts.get(1).getQuantity()).isEqualTo(listProductDTO2.quantity());
        assertThat(addedProducts.get(1).getUnit()).isEqualTo(listProductDTO2.unit());
        assertThat(shoppingList.getProductsList()).hasSize(3);
        assertThat(shoppingList.getProductsList()).contains(addedProducts.get(0), addedProducts.get(1));
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
        assertThat(addedProducts.get(0).getProduct().getProductName()).isEqualTo(productDTO.productName());
        assertThat(addedProducts.get(0).getProduct().getCategory()).isEqualTo(productDTO.category());
        assertThat(addedProducts.get(0).getQuantity()).isEqualTo(listProductDTO.quantity());
        assertThat(addedProducts.get(0).getUnit()).isEqualTo(listProductDTO.unit());
        assertThat(addedProducts.get(1).getProduct().getProductName()).isEqualTo(productDTO2.productName());
        assertThat(addedProducts.get(1).getProduct().getCategory()).isEqualTo(productDTO2.category());
        assertThat(addedProducts.get(1).getQuantity()).isEqualTo(listProductDTO2.quantity());
        assertThat(addedProducts.get(1).getUnit()).isEqualTo(listProductDTO2.unit());
        assertThat(shoppingList.getProductsList()).hasSize(2);
        assertThat(shoppingList.getProductsList()).contains(addedProducts.get(0), addedProducts.get(1));
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
        assertThat(addedProducts.get(0).getProduct().getProductName()).isEqualTo(productDTO.productName());
        assertThat(addedProducts.get(0).getProduct().getCategory()).isEqualTo(productDTO.category());
        assertThat(shoppingList.getProductsList().get(0).getQuantity()).isEqualTo(listProductDTO.quantity() + productQuantityBeforeModifing);
        assertThat(addedProducts.get(0).getUnit()).isEqualTo(listProductDTO.unit());
        assertThat(addedProducts.get(1).getProduct().getProductName()).isEqualTo(productDTO2.productName());
        assertThat(addedProducts.get(1).getProduct().getCategory()).isEqualTo(productDTO2.category());
        assertThat(addedProducts.get(1).getQuantity()).isEqualTo(listProductDTO2.quantity());
        assertThat(addedProducts.get(1).getUnit()).isEqualTo(listProductDTO2.unit());
        assertThat(shoppingList.getProductsList()).hasSize(2);
        assertThat(shoppingList.getProductsList()).contains(addedProducts.get(0), addedProducts.get(1));
    }

    @Test
    void test_addProductsToShoppingListListProductDTOIdBiggerThan0() {
        authority.setAuthorityName(AuthorityEnum.ADD_TO_SHOPPING_LIST);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final ShoppingListProductDTO listProductDTO = new ShoppingListProductDTO(1L, productDTO, 100, Unit.GRAMS, false);
        final List<ShoppingListProductDTO> productsToAdd = Collections.singletonList(listProductDTO);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> this.service.addProductsToShoppingList(id, productsToAdd, email))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Shopping list product id must be not set while inserting it to shopping list");
    }

    @Test
    void test_addProductsToShoppingListPurchasedWhileInserting() {
        authority.setAuthorityName(AuthorityEnum.ADD_TO_SHOPPING_LIST);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final ShoppingListProductDTO listProductDTO = new ShoppingListProductDTO(0L, productDTO, 100, Unit.GRAMS, true);
        final List<ShoppingListProductDTO> productsToAdd = Collections.singletonList(listProductDTO);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> this.service.addProductsToShoppingList(id, productsToAdd, email))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Shopping list product cannot be purchased while inserting it to shopping list");
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

        assertThatThrownBy(() -> this.service.removeProductsFromShoppingList(id, productsToRemoveIds, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class);
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
        assertThat(modifiedProduct.getProduct().getProductName()).isEqualTo(shoppingListProductDTO.product().productName());
        assertThat(modifiedProduct.getProduct().getCategory()).isEqualTo(shoppingListProductDTO.product().category());
        assertThat(modifiedProduct.getQuantity()).isEqualTo(shoppingListProductDTO.quantity());
        assertThat(modifiedProduct.getUnit()).isEqualTo(shoppingListProductDTO.unit());
        assertThat(modifiedProduct.isPurchased()).isEqualTo(shoppingListProductDTO.purchased());
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
        assertThat(modifiedProduct.getProduct().getProductName()).isEqualTo(shoppingListProductDTO.product().productName());
        assertThat(modifiedProduct.getProduct().getCategory()).isEqualTo(shoppingListProductDTO.product().category());
        assertThat(modifiedProduct.getQuantity()).isEqualTo(shoppingListProductDTO.quantity() + productQuantityBeforeModifing);
        assertThat(modifiedProduct.getUnit()).isEqualTo(shoppingListProductDTO.unit());
        assertThat(modifiedProduct.isPurchased()).isEqualTo(shoppingListProductDTO.purchased());
        assertThat(modifiedProduct.getId()).isEqualTo(productIdBeforeModifing);
    }

    @Test
    void test_updateShoppingListProductProductDoesNotExist() {
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final ShoppingListProductDTO shoppingListProductDTO = new ShoppingListProductDTO(2L, productDTO, 200, Unit.GRAMS, false);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.empty()).when(shoppingListProductRepository).findById(shoppingListProductDTO.id());

        assertThatThrownBy(() -> this.service.updateShoppingListProduct(id, shoppingListProductDTO, email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Shopping list product was not found");
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

        assertThatThrownBy(() -> this.service.updateShoppingListProduct(id, shoppingListProductDTO, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("Cannot update products from different shopping list");
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

        assertThatThrownBy(() -> this.service.updateShoppingListProduct(id, shoppingListProductDTO, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("Cannot modify invalid shopping list product");
        verify(shoppingListProductRepository, times(0)).save(shoppingListProduct);
        verify(shoppingListProductRepository, times(0)).deleteById(shoppingListProductDTO.id());
    }

    @Test
    void test_updateShoppingListProductIdEquals0() {
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.ANIMAL_PRODUCTS);
        final ShoppingListProductDTO shoppingListProductDTO = new ShoppingListProductDTO(0L, productDTO, 200, Unit.GRAMS, false);

        assertThatThrownBy(() -> this.service.updateShoppingListProduct(id, shoppingListProductDTO, email))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Cannot modify product because it doesn't exist");
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

        assertThatThrownBy(() -> this.service.changePurchaseStatusForProducts(id, productsToStatusChangeIds, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class);
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
        assertThat(transferedProducts).hasSize(shoppingList.getProductsList().size());
        assertThat(transferedProducts.get(0).product().productId()).isEqualTo(shoppingList.getProductsList().get(0).getProduct().getId());
        assertThat(transferedProducts.get(0).product().category()).isEqualTo(shoppingList.getProductsList().get(0).getProduct().getCategory());
        assertThat(transferedProducts.get(0).quantity()).isEqualTo(shoppingList.getProductsList().get(0).getQuantity());
        assertThat(transferedProducts.get(0).unit()).isEqualTo(shoppingList.getProductsList().get(0).getUnit());
        assertThat(transferedProducts.get(0).purchaseDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void test_transferProductsToPantryGroupDoesNotHaveAssignedPantry() {
        final Authority authority1 = Authority.builder().group(group).authorityName(AuthorityEnum.ADD).build();
        user.setAuthorities(Set.of(authority, authority1));
        shoppingListProduct.setPurchased(true);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> this.service.transferProductsToPantry(id, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("Cannot transfer products because your group does not have assigned pantry");
        verify(shoppingListProductRepository, times(0)).deleteAll(shoppingList.getProductsList());
        verify(pantryProductService, times(0)).addProductsToPantryFromList(any(Pantry.class), anyList());
    }

    @Test
    void test_transferProductsToPantryNoPermissionToAddToPantry() {
        final Pantry pantry = Pantry.builder().pantryName("pantryName").group(group).build();
        group.setPantry(pantry);
        shoppingListProduct.setPurchased(true);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> this.service.transferProductsToPantry(id, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("You have not permissions to do that");
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

        assertThatThrownBy(() -> this.service.transferProductsToPantry(id, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("Cannot transfer unpurchased shopping list products");
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
        assertThat(shoppingListProduct.getProduct()).isEqualTo(recipeProduct.getProduct());
        assertThat(shoppingListProduct.getUnit()).isEqualTo(recipeProduct.getUnit());
        assertThat(shoppingListProduct.getQuantity()).isEqualTo(recipeProduct.getQuantity() + quantityBeforeAdding);
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
        assertThat(addedProducts.get(0).getProduct()).isEqualTo(recipeProduct.getProduct());
        assertThat(addedProducts.get(0).getUnit()).isEqualTo(recipeProduct.getUnit());
        assertThat(addedProducts.get(0).getQuantity()).isEqualTo(recipeProduct.getQuantity());
    }
}

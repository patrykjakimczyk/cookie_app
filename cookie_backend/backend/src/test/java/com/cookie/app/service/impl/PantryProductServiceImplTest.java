package com.cookie.app.service.impl;

import com.cookie.app.exception.ResourceNotFoundException;
import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.ValidationException;
import com.cookie.app.model.dto.PageResult;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.Unit;
import com.cookie.app.model.mapper.*;
import com.cookie.app.model.request.FilterRequest;
import com.cookie.app.repository.PantryProductRepository;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PantryProductServiceImplTest {
    private final String email = "email@email.com";
    private final String pantryName = "pantryName";
    private final String filter = "filter";
    private final String col = "col";
    private final String idCol = "id";
    private final Long id = 1L;

    @Spy
    private AuthorityMapper authorityMapper = new AuthorityMapperImpl();
    @Spy
    private PantryProductMapper pantryProductMapper = new PantryProductMapperImpl(new ProductMapperImpl());
    @Mock
    private UserRepository userRepository;
    @Mock
    private PantryProductRepository pantryProductRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private PantryProductServiceImpl service;

    @Captor
    private ArgumentCaptor<PageRequest> pageRequestArgCaptor;
    @Captor
    private ArgumentCaptor<List<PantryProduct>> listOfProductsArgCaptor;
    @Captor
    private ArgumentCaptor<PantryProduct> pantryProductArgCaptor;

    private Product product;
    private PantryProduct pantryProduct;
    private Pantry pantry;
    private Authority authority;
    private User user;

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
        final FilterRequest filterRequest = new FilterRequest(null, col, Sort.Direction.ASC);
        final PageImpl<PantryProduct> pageResponse = new PageImpl<>(List.of(pantryProduct));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(pageResponse).when(pantryProductRepository).findPantryProductByPantryId(
                eq(id),
                this.pageRequestArgCaptor.capture()
        );
        PageResult<PantryProductDTO> result = this.service.getPantryProducts(id, 1, filterRequest, email);
        PageRequest pageRequest = this.pageRequestArgCaptor.getValue();

        verify(pantryProductRepository).findPantryProductByPantryId(eq(id), any(PageRequest.class));
        assertThat(result.totalElements()).isEqualTo(pageResponse.getTotalElements());
        assertThat(result.content().get(0).product().productName()).isEqualTo(pageResponse.getContent().get(0).getProduct().getProductName());
        assertThat(result.content().get(0).id()).isEqualTo(pageResponse.getContent().get(0).getId());
        assertThat(pageRequest.getSort().getOrderFor(col)).isNotNull();
        assertThat(pageRequest.getSort().getOrderFor(col).getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(pageRequest.getPageNumber()).isZero();
    }

    @Test
    void test_getPantryProductsSuccessfulWithFilterAndDescSort() {
        final FilterRequest filterRequest = new FilterRequest(filter, col, Sort.Direction.DESC);
        final PageImpl<PantryProduct> pageResponse = new PageImpl<>(List.of(pantryProduct));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(pageResponse).when(pantryProductRepository).findProductsInPantryWithFilter(
                eq(id),
                eq(filter),
                this.pageRequestArgCaptor.capture()
        );

        PageResult<PantryProductDTO> result = this.service.getPantryProducts(id, 1, filterRequest, email);
        PageRequest pageRequest = this.pageRequestArgCaptor.getValue();

        verify(pantryProductRepository).findProductsInPantryWithFilter(eq(id), eq(filter), any(PageRequest.class));
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
    void test_getPantryProductsSuccessfulWithoutSort() {
        final FilterRequest filterRequest = new FilterRequest(filter, null, null);
        final PageImpl<PantryProduct> pageResponse = new PageImpl<>(List.of(pantryProduct));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(pageResponse).when(pantryProductRepository).findProductsInPantryWithFilter(
                eq(id),
                eq(filter),
                this.pageRequestArgCaptor.capture()
        );

        PageResult<PantryProductDTO> result = this.service.getPantryProducts(id, 1, filterRequest, email);
        PageRequest pageRequest = this.pageRequestArgCaptor.getValue();

        verify(pantryProductRepository).findProductsInPantryWithFilter(eq(id), eq(filter), any(PageRequest.class));
        assertThat(result.totalElements()).isEqualTo(pageResponse.getTotalElements());
        assertThat(result.content().get(0).product().productName()).isEqualTo(pageResponse.getContent().get(0).getProduct().getProductName());
        assertThat(result.content().get(0).id()).isEqualTo(pageResponse.getContent().get(0).getId());
        assertThat(pageRequest.getSort().getOrderFor(idCol)).isNotNull();
        assertThat(Objects.requireNonNull(pageRequest.getSort().getOrderFor(idCol)).getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(pageRequest.getPageNumber()).isZero();
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
        verify(pantryProductRepository, times(0)).deleteByIdIn(anyList());
        assertThat(addedProducts.get(0).getProduct().getProductName()).isEqualTo(productDTO.productName());
        assertThat(addedProducts.get(0).getProduct().getCategory()).isEqualTo(productDTO.category());
        assertThat(addedProducts.get(0).getQuantity()).isEqualTo(pantryProductDTO.quantity());
        assertThat(addedProducts.get(0).getUnit()).isEqualTo(pantryProductDTO.unit());
        assertThat(addedProducts.get(1).getProduct().getProductName()).isEqualTo(productDTO2.productName());
        assertThat(addedProducts.get(1).getProduct().getCategory()).isEqualTo(productDTO2.category());
        assertThat(addedProducts.get(1).getQuantity()).isEqualTo(pantryProductDTO2.quantity());
        assertThat(addedProducts.get(1).getUnit()).isEqualTo(pantryProductDTO2.unit());
        assertThat(pantry.getPantryProducts()).hasSize(3);
        assertThat(pantry.getPantryProducts()).contains(addedProducts.get(0), addedProducts.get(1));
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
        verify(pantryProductRepository, times(0)).deleteByIdIn(anyList());
        assertThat(addedProducts.get(0).getProduct().getProductName()).isEqualTo(productDTO.productName());
        assertThat(addedProducts.get(0).getProduct().getCategory()).isEqualTo(productDTO.category());
        assertThat(addedProducts.get(0).getQuantity()).isEqualTo(pantryProductDTO.quantity());
        assertThat(addedProducts.get(0).getUnit()).isEqualTo(pantryProductDTO.unit());
        assertThat(addedProducts.get(1).getProduct().getProductName()).isEqualTo(productDTO2.productName());
        assertThat(addedProducts.get(1).getProduct().getCategory()).isEqualTo(productDTO2.category());
        assertThat(addedProducts.get(1).getQuantity()).isEqualTo(pantryProductDTO2.quantity());
        assertThat(addedProducts.get(1).getUnit()).isEqualTo(pantryProductDTO2.unit());
        assertThat(pantry.getPantryProducts()).hasSize(2);
        assertThat(pantry.getPantryProducts()).contains(addedProducts.get(0), addedProducts.get(1));
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
        verify(pantryProductRepository, times(0)).deleteByIdIn(anyList());
        assertThat(addedProducts.get(0).getProduct().getProductName()).isEqualTo(productDTO.productName());
        assertThat(addedProducts.get(0).getProduct().getCategory()).isEqualTo(productDTO.category());
        assertThat(pantry.getPantryProducts().get(0).getQuantity()).isEqualTo(pantryProductDTO.quantity() + productQuantityBeforeModifing);
        assertThat(addedProducts.get(0).getUnit()).isEqualTo(pantryProductDTO.unit());
        assertThat(addedProducts.get(1).getProduct().getProductName()).isEqualTo(productDTO2.productName());
        assertThat(addedProducts.get(1).getProduct().getCategory()).isEqualTo(productDTO2.category());
        assertThat(addedProducts.get(1).getQuantity()).isEqualTo(pantryProductDTO2.quantity());
        assertThat(addedProducts.get(1).getUnit()).isEqualTo(pantryProductDTO2.unit());
        assertThat(pantry.getPantryProducts()).hasSize(2);
        assertThat(pantry.getPantryProducts()).contains(addedProducts.get(0), addedProducts.get(1));
    }

    @Test
    void test_addProductsToPantryPantryProductDTOIdBiggerThan0() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(1L, productDTO, null, null, 100, Unit.GRAMS, 0, null);
        final List<PantryProductDTO> productsToAdd = Collections.singletonList(pantryProductDTO);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> this.service.addProductsToPantry(id, productsToAdd, email))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Pantry product id must be 0 while inserting it to pantry");
    }

    @Test
    void test_addProductsToPantryReservedQuantityOver0() {
        authority.setAuthorityName(AuthorityEnum.ADD);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, productDTO, null, null, 100, Unit.GRAMS, 10, null);
        final List<PantryProductDTO> productsToAdd = Collections.singletonList(pantryProductDTO);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> this.service.addProductsToPantry(id, productsToAdd, email))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Pantry product reserved quantity must be 0 while inserting it to pantry");
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
        verify(pantryProductRepository, times(0)).deleteByIdIn(anyList());
        assertThat(addedProducts.get(0).getProduct().getProductName()).isEqualTo(productDTO.productName());
        assertThat(addedProducts.get(0).getProduct().getCategory()).isEqualTo(productDTO.category());
        assertThat(addedProducts.get(0).getQuantity()).isEqualTo(pantryProductDTO.quantity());
        assertThat(addedProducts.get(0).getUnit()).isEqualTo(pantryProductDTO.unit());
        assertThat(addedProducts.get(1).getProduct().getProductName()).isEqualTo(productDTO2.productName());
        assertThat(addedProducts.get(1).getProduct().getCategory()).isEqualTo(productDTO2.category());
        assertThat(addedProducts.get(1).getQuantity()).isEqualTo(pantryProductDTO2.quantity());
        assertThat(addedProducts.get(1).getUnit()).isEqualTo(pantryProductDTO2.unit());
        assertThat(pantry.getPantryProducts()).hasSize(3);
        assertThat(pantry.getPantryProducts()).contains(addedProducts.get(0), addedProducts.get(1));
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

        assertThatThrownBy(() -> this.service.removeProductsFromPantry(id, productsToRemoveIds, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class);
        verify(pantryProductRepository, times(0)).deleteByIdIn(productsToRemoveIds);
    }

    @Test
    void test_updatePantryProductSuccessful() {
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
        assertThat(modifiedProduct.getProduct().getProductName()).isEqualTo(pantryProductDTO.product().productName());
        assertThat(modifiedProduct.getProduct().getCategory()).isEqualTo(pantryProductDTO.product().category());
        assertThat(modifiedProduct.getQuantity()).isEqualTo(pantryProductDTO.quantity());
        assertThat(modifiedProduct.getUnit()).isEqualTo(pantryProductDTO.unit());
        assertThat(modifiedProduct.getReserved()).isEqualTo(pantryProductDTO.reserved());
        assertThat(modifiedProduct.getPurchaseDate()).isEqualTo(pantryProductDTO.purchaseDate());
        assertThat(modifiedProduct.getExpirationDate()).isEqualTo(pantryProductDTO.expirationDate());
        assertThat(modifiedProduct.getPlacement()).isEqualTo(pantryProductDTO.placement());
    }

    @Test
    void test_updatePantryProductSuccessfulStackingWithDifferentProduct() {
        final long productQuantityBeforeModifing = pantryProduct.getQuantity();
        final long productIdBeforeModifing = pantryProduct.getId();
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(2L, productDTO, null, null, 200, Unit.GRAMS, 100, null);
        final PantryProduct foundPantryProduct = new PantryProduct(3L, pantry, product, null, null, 100, Unit.GRAMS, 100, "placement");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(foundPantryProduct)).when(pantryProductRepository).findById(pantryProductDTO.id());
        doReturn(null).when(pantryProductRepository).save(
                this.pantryProductArgCaptor.capture()
        );
        this.service.updatePantryProduct(id, pantryProductDTO, email);
        PantryProduct modifiedProduct = this.pantryProductArgCaptor.getValue();

        verify(pantryProductRepository).save(pantryProduct);
        verify(pantryProductRepository).deleteById(pantryProductDTO.id());
        assertThat(modifiedProduct.getProduct().getProductName()).isEqualTo(pantryProductDTO.product().productName());
        assertThat(modifiedProduct.getProduct().getCategory()).isEqualTo(pantryProductDTO.product().category());
        assertThat(modifiedProduct.getQuantity()).isEqualTo(pantryProductDTO.quantity() + productQuantityBeforeModifing);
        assertThat(modifiedProduct.getUnit()).isEqualTo(pantryProductDTO.unit());
        assertThat(modifiedProduct.getReserved()).isEqualTo(pantryProductDTO.reserved());
        assertThat(modifiedProduct.getPurchaseDate()).isEqualTo(pantryProductDTO.purchaseDate());
        assertThat(modifiedProduct.getExpirationDate()).isEqualTo(pantryProductDTO.expirationDate());
        assertThat(modifiedProduct.getPlacement()).isEqualTo(pantryProductDTO.placement());
        assertThat(modifiedProduct.getId()).isEqualTo(productIdBeforeModifing);
    }

    @Test
    void test_updatePantryProductProductDoesNotExist() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(2L, productDTO, null, null, 200, Unit.GRAMS, 100, null);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.empty()).when(pantryProductRepository).findById(pantryProductDTO.id());

        assertThatThrownBy(() -> this.service.updatePantryProduct(id, pantryProductDTO, email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pantry product was not found");
        verify(pantryProductRepository, times(0)).save(pantryProduct);
        verify(pantryProductRepository, times(0)).deleteById(pantryProductDTO.id());
    }

    @Test
    void test_updatePantryProductProductFromDifferentPantry() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.CEREAL);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(2L, productDTO, null, null, 200, Unit.GRAMS, 100, null);
        final PantryProduct foundPantryProduct = new PantryProduct(2L, new Pantry(), product, null, null, 100, Unit.PIECES, 0, "placement");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(foundPantryProduct)).when(pantryProductRepository).findById(pantryProductDTO.id());

        assertThatThrownBy(() -> this.service.updatePantryProduct(id, pantryProductDTO, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("Cannot modify products from different pantry");
        verify(pantryProductRepository, times(0)).save(pantryProduct);
        verify(pantryProductRepository, times(0)).deleteById(pantryProductDTO.id());
    }

    @Test
    void test_updatePantryProductInvalidProduct() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.ANIMAL_PRODUCTS);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(2L, productDTO, null, null, 200, Unit.GRAMS, 100, null);
        final PantryProduct foundPantryProduct = new PantryProduct(2L, pantry, product, null, null, 100, Unit.PIECES, 0, "placement");

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(foundPantryProduct)).when(pantryProductRepository).findById(pantryProductDTO.id());

        assertThatThrownBy(() -> this.service.updatePantryProduct(id, pantryProductDTO, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("Cannot modify invalid pantry product");
        verify(pantryProductRepository, times(0)).save(pantryProduct);
        verify(pantryProductRepository, times(0)).deleteById(pantryProductDTO.id());
    }

    @Test
    void test_updatePantryProductIdEquals0() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final ProductDTO productDTO = new ProductDTO(0L, "productName", Category.ANIMAL_PRODUCTS);
        final PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, productDTO, null, null, 200, Unit.GRAMS, 100, null);

        assertThatThrownBy(() -> this.service.updatePantryProduct(id, pantryProductDTO, email))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Cannot modify product because it doesn't exist");
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
        assertThat(productAfterReserve.reserved()).isEqualTo(reservedQuantity);
        assertThat(productAfterReserve.quantity()).isEqualTo(quantityBeforeReserving - reservedQuantity);
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
        assertThat(productAfterUnreserve.reserved()).isEqualTo(reservedQuantityBeforeUnreserving + reserveQuantity);
        assertThat(productAfterUnreserve.quantity()).isEqualTo(quantityBeforeUnreserving - reserveQuantity);
    }

    @Test
    void test_reservePantryProductUnreserveQuantityIsBiggerThanReservedQuantity() {
        final int reservedQuantity = 200;
        authority.setAuthorityName(AuthorityEnum.RESERVE);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(pantryProduct)).when(pantryProductRepository).findById(id);
        PantryProductDTO productAfterReserve = this.service.reservePantryProduct(id, id, reservedQuantity, email);

        verify(pantryProductRepository, times(0)).save(pantryProduct);
        assertThat(productAfterReserve).isNull();
    }

    @Test
    void test_reservePantryProductReserveQuantityIsBiggerThanAvailableQuantity() {
        final int reservedQuantity = -100;
        authority.setAuthorityName(AuthorityEnum.RESERVE);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(pantryProduct)).when(pantryProductRepository).findById(id);
        PantryProductDTO productAfterReserve = this.service.reservePantryProduct(id, id, reservedQuantity, email);

        verify(pantryProductRepository, times(0)).save(pantryProduct);
        assertThat(productAfterReserve).isNull();
    }

    @Test
    void test_reservePantryProductsFromRecipeSuccessful() {
        final int reservedQuantityBeforeReserving = pantryProduct.getReserved();
        final int quantityBeforeReserving = pantryProduct.getQuantity();
        authority.setAuthorityName(AuthorityEnum.RESERVE);
        final RecipeProduct recipeProduct = new RecipeProduct(id, product, 100, Unit.GRAMS);
        final List<RecipeProduct> recipeProducts = List.of(recipeProduct);

        List<RecipeProduct> unreservedProducts = this.service.reservePantryProductsFromRecipe(id, user, recipeProducts);

        assertThat(unreservedProducts).isEmpty();
        assertThat(pantryProduct.getReserved()).isEqualTo(reservedQuantityBeforeReserving + recipeProduct.getQuantity());
        assertThat(pantryProduct.getQuantity()).isEqualTo(quantityBeforeReserving - recipeProduct.getQuantity());
        verify(pantryProductRepository).save(pantryProduct);
    }

    @Test
    void test_reservePantryProductsFromRecipeNoProductsFound() {
        authority.setAuthorityName(AuthorityEnum.RESERVE);
        final RecipeProduct recipeProduct = new RecipeProduct(id, product, 100, Unit.PIECES);
        final List<RecipeProduct> recipeProducts = List.of(recipeProduct);

        List<RecipeProduct> unreservedProducts = this.service.reservePantryProductsFromRecipe(id, user, recipeProducts);

        assertThat(unreservedProducts).hasSize(recipeProducts.size()).contains(recipeProduct);
        verify(pantryProductRepository, times(0)).save(pantryProduct);
    }

    @Test
    void test_getRecipeProductsNotInPantryReturnsMissingProduct() {
        final RecipeProduct recipeProduct = new RecipeProduct(id, product, 100, Unit.PIECES);
        final List<RecipeProduct> recipeProducts = List.of(recipeProduct);

        List<RecipeProduct> missingProducts = this.service.getRecipeProductsNotInPantry(pantry, recipeProducts);

        assertThat(missingProducts).hasSize(recipeProducts.size()).contains(recipeProduct);
    }

    @Test
    void test_getRecipeProductsNotInPantryReturnsEmptyList() {
        final RecipeProduct recipeProduct = new RecipeProduct(id, product, 100, Unit.GRAMS);
        final List<RecipeProduct> recipeProducts = List.of(recipeProduct);

        List<RecipeProduct> missingProducts = this.service.getRecipeProductsNotInPantry(pantry, recipeProducts);

        assertThat(missingProducts).isEmpty();
    }

    @Test
    void test_getRecipeProductsNotInPantryReturnsSameRecipeList() {
        final RecipeProduct recipeProduct = new RecipeProduct(id, product, 100, Unit.GRAMS);
        final List<RecipeProduct> recipeProducts = List.of(recipeProduct);

        List<RecipeProduct> missingProducts = this.service.getRecipeProductsNotInPantry(null, recipeProducts);

        assertThat(missingProducts).hasSize(recipeProducts.size()).contains(recipeProduct);
    }
}

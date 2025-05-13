package com.cookie.app.controller;

import com.cookie.app.model.dto.PageResult;
import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.Unit;
import com.cookie.app.model.request.FilterRequest;
import com.cookie.app.service.ShoppingListProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ShoppingListProductControllerTest extends AbstractControllerTest {
    private final long listProductId = 1L;
    private final long listId = 1L;

    @Mock
    ShoppingListProductService shoppingListProductService;
    @InjectMocks
    ShoppingListProductController controller;

    @Test
    void test_getShoppingListProductsSuccessful() {
        final FilterRequest filterRequest = new FilterRequest("", "", null);
        final int pageNr = 1;
        final List<ShoppingListProductDTO> shoppingListProductDTOS = Collections.singletonList(createShoppingListProduct());
        final PageResult<ShoppingListProductDTO> pageResponse = new PageResult<>(shoppingListProductDTOS, shoppingListProductDTOS.size(), 1, 0);

        doReturn(pageResponse).when(shoppingListProductService).getShoppingListProducts(listId, pageNr, filterRequest, authentication.getName());
        ResponseEntity<PageResult<ShoppingListProductDTO>> response = controller.getShoppingListProducts(listId, pageNr, filterRequest, authentication);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalElements()).isEqualTo(shoppingListProductDTOS.size());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test_addProductsToShoppingListSuccessful() {
        final List<ShoppingListProductDTO> shoppingListProductDTOS = Collections.singletonList(createShoppingListProduct());

        ResponseEntity<Void> response = controller.addProductsToShoppingList(listId, shoppingListProductDTOS, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void test_removeProductsFromShoppingListSuccessful() {
        final List<Long> listProductsIds = Collections.singletonList(listProductId);

        ResponseEntity<Void> response = controller.removeProductsFromShoppingList(listId, listProductsIds, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test_updateShoppingListProductSuccessful() {
        final ShoppingListProductDTO shoppingListProductDTO = createShoppingListProduct();

        ResponseEntity<Void> response = controller.updateShoppingListProduct(listId, shoppingListProductDTO, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test_changePurchaseStatusForProductsSuccessful() {
        final List<Long> listProductsIds = Collections.singletonList(listProductId);

        ResponseEntity<Void> response = controller.changePurchaseStatusForProducts(listId, listProductsIds, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test_transferProductsToPantrySuccessful() {

        ResponseEntity<Void> response = controller.transferProductsToPantry(listId, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ShoppingListProductDTO createShoppingListProduct() {
        final ProductDTO productDTO = new ProductDTO(0L, "name", Category.CEREAL);

        return new ShoppingListProductDTO(
                listProductId,
                productDTO,
                100,
                Unit.GRAMS,
                false
        );
    }
}
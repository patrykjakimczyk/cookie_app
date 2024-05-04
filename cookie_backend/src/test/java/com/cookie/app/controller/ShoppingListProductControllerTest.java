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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ShoppingListProductControllerTest extends AbstractControllerTest {
    final long listProductId = 1L;
    final long listId = 1L;

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

        doReturn(pageResponse).when(shoppingListProductService).getShoppingListProducts(this.listId, pageNr, filterRequest, authentication.getName());
        ResponseEntity<PageResult<ShoppingListProductDTO>> response = this.controller.getShoppingListProducts(this.listId, pageNr, filterRequest, authentication);

        assertEquals(shoppingListProductDTOS.size(), response.getBody().totalElements());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_addProductsToShoppingListSuccessful() {
        final List<ShoppingListProductDTO> shoppingListProductDTOS = Collections.singletonList(createShoppingListProduct());

        ResponseEntity<Void> response = this.controller.addProductsToShoppingList(this.listId, shoppingListProductDTOS, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void test_removeProductsFromShoppingListSuccessful() {
        final List<Long> listProductsIds = Collections.singletonList(listProductId);

        ResponseEntity<Void> response = this.controller.removeProductsFromShoppingList(listId, listProductsIds, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_updateShoppingListProductSuccessful() {
        final ShoppingListProductDTO shoppingListProductDTO = createShoppingListProduct();

        ResponseEntity<Void> response = this.controller.updateShoppingListProduct(listId, shoppingListProductDTO, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_changePurchaseStatusForProductsSuccessful() {
        final List<Long> listProductsIds = Collections.singletonList(listProductId);

        ResponseEntity<Void> response = this.controller.changePurchaseStatusForProducts(listId, listProductsIds, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_transferProductsToPantrySuccessful() {

        ResponseEntity<Void> response = this.controller.transferProductsToPantry(listId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
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

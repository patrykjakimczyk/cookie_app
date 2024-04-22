package com.cookie.app.controller;

import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.enums.Category;
import com.cookie.app.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {
    @Mock
    private ProductService productService;
    @InjectMocks
    private ProductController controller;

    @Test
    void test_getProductsWithFilterSuccessful() {
        final String filterValue = "prod";
        final ProductDTO product = new ProductDTO(1L, "product", Category.CEREAL);
        final List<ProductDTO> listResponse = List.of(product);

        doReturn(listResponse).when(productService).getProductsWithFilter(filterValue);
        ResponseEntity<List<ProductDTO>> response = this.controller.getProductsWithFilter(filterValue);

        assertEquals(listResponse.size(), response.getBody().size());
        assertEquals(product.productId(), response.getBody().get(0).productId());
        assertEquals(product.productName(), response.getBody().get(0).productName());
        assertEquals(product.category(), response.getBody().get(0).category());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_getProductsWithFilterSuccessfulWithEmptyContent() {
        final String filterValue = "prod";
        final List<ProductDTO> listResponse = Collections.emptyList();

        doReturn(listResponse).when(productService).getProductsWithFilter(filterValue);
        ResponseEntity<List<ProductDTO>> response = this.controller.getProductsWithFilter(filterValue);

        assertTrue(response.getBody().isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

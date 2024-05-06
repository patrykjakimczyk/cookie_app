package com.cookie.app.service.impl;

import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.entity.Product;
import com.cookie.app.model.enums.Category;
import com.cookie.app.model.mapper.ProductMapper;
import com.cookie.app.model.mapper.ProductMapperImpl;
import com.cookie.app.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Spy
    ProductMapper productMapper = new ProductMapperImpl();
    @Mock
    ProductRepository productRepository;
    @InjectMocks
    ProductServiceImpl service;

    @Test
    void test_getProductsWithFilterSuccessful() {
        final String filterValue = "prod";
        final Product product = new Product(1L, "product", Category.CEREAL);
        final PageImpl<Product> pageResponse = new PageImpl<>(List.of(product));

        doReturn(pageResponse).when(productRepository).findProductsWithFilter(eq(filterValue), any(PageRequest.class));
        List<ProductDTO> returnValue = this.service.getProductsWithFilter(filterValue);

        assertEquals(pageResponse.getContent().size(), returnValue.size());
        assertEquals(product.getId(), returnValue.get(0).productId());
        assertEquals(product.getProductName(), returnValue.get(0).productName());
        assertEquals(product.getCategory(), returnValue.get(0).category());
    }

    @Test
    void test_getProductsWithFilterSuccessfulReturnsEmptyContent() {
        final String filterValue = "prod";
        final PageImpl<Product> pageResponse = new PageImpl<>(Collections.emptyList());

        doReturn(pageResponse).when(productRepository).findProductsWithFilter(eq(filterValue), any(PageRequest.class));
        List<ProductDTO> returnValue = this.service.getProductsWithFilter(filterValue);

        assertEquals(pageResponse.getContent().size(), returnValue.size());
        assertTrue(returnValue.isEmpty());
    }
}

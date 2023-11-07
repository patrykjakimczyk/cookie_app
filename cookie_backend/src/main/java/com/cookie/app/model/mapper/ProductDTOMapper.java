package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.entity.Product;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ProductDTOMapper implements Function<Product, ProductDTO> {
    @Override
    public ProductDTO apply(Product product) {
        return new ProductDTO(product.getId(), product.getProductName(), product.getCategory());
    }
}

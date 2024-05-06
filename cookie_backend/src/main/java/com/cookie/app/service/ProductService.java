package com.cookie.app.service;

import com.cookie.app.model.dto.ProductDTO;

import java.util.List;

public interface ProductService {
    List<ProductDTO> getProductsWithFilter(String filterValue);
}

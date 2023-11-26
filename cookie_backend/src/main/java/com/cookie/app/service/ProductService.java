package com.cookie.app.service;

import com.cookie.app.model.dto.ProductDTO;
import org.springframework.data.domain.Page;

public interface ProductService {
    Page<ProductDTO> getProductsWithFilter(String filterValue);
}

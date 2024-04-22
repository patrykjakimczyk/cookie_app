package com.cookie.app.service;

import com.cookie.app.model.dto.ProductDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    List<ProductDTO> getProductsWithFilter(String filterValue);
}

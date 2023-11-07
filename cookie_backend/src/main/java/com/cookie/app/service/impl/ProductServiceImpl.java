package com.cookie.app.service.impl;

import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.mapper.ProductDTOMapper;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductDTOMapper productDTOMapper;

    @Override
    public List<ProductDTO> getProducts(String filterValue) {
        return this.productRepository.findProductsWithFilter(filterValue)
                .stream()
                .map(productDTOMapper)
                .collect(Collectors.toList());
    }
}

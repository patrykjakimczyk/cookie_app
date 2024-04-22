package com.cookie.app.service.impl;

import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.entity.Product;
import com.cookie.app.model.mapper.ProductMapperDTO;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 10;

    private final ProductRepository productRepository;
    private final ProductMapperDTO productDTOMapper;

    @Override
    public List<ProductDTO> getProductsWithFilter(String filterValue) {
        PageRequest pageRequest = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        Page<Product> productsPage = this.productRepository.findProductsWithFilter(filterValue, pageRequest);
        return productsPage.get()
                        .map(productDTOMapper::apply)
                        .toList();
    }
}

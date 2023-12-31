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

@RequiredArgsConstructor
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapperDTO productDTOMapper;

    @Override
    public Page<ProductDTO> getProductsWithFilter(String filterValue) {
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Product> productsPage = this.productRepository.findProductsWithFilter(filterValue, pageRequest);
        return new PageImpl<>(
                productsPage.get()
                        .map(productDTOMapper::apply)
                        .toList()
        );
    }
}

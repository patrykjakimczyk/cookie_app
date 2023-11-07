package com.cookie.app.controller;

import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
public class ProductController {
    private static final String PRODUCTS_URL = "/products";
    private final ProductService productService;

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(PRODUCTS_URL)
    public ResponseEntity<List<ProductDTO>> getPantryProducts(
            @PathVariable(value = "id") long id,
            @PathVariable(value = "page") int page,
            @RequestParam String filterValue
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.productService.getProducts(filterValue));
    }
}

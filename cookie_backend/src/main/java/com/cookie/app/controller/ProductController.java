package com.cookie.app.controller;

import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.print.attribute.standard.Media;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/v1/products", produces = { MediaType.APPLICATION_JSON_VALUE })
@RestController
public class ProductController {
    private final ProductService productService;

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProductsWithFilter(
            @RequestParam String filterValue
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.productService.getProductsWithFilter(filterValue));
    }
}

package com.cookie.app.controller;

import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.dto.MealDTO;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/v1/products", produces = { MediaType.APPLICATION_JSON_VALUE })
@RestController
public class ProductController {
    private final ProductService productService;

    @Operation(summary = "Find products by filter")
    @ApiResponse(responseCode = "200", description = "Found products returned",
            content = { @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ProductDTO.class))) })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProductsWithFilter(
            @RequestParam
            @Pattern(
                    regexp = RegexConstants.FILTER_VALUE_REGEX,
                    message = "Filter value can only contains letters, digits, whitespaces, dashes and its length must be greater than 0"
            ) String filterValue
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.productService.getProductsWithFilter(filterValue));
    }
}

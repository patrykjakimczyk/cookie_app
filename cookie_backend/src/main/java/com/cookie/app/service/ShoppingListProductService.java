package com.cookie.app.service;

import com.cookie.app.model.dto.ShoppingListProductDTO;
import org.springframework.data.domain.Page;

public interface ShoppingListProductService {
    Page<ShoppingListProductDTO> getShoppingListProducts(
            long id,
            int page,
            String filterValue,
            String sortColName,
            String sortDirection,
            String userEmail
    );
}

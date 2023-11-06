package com.cookie.app.service;


import com.cookie.app.model.response.PantryProductDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PantryProductService {
    Page<PantryProductDTO> getPantryProducts(
            long pantryId,
            int page,
            String filterValue,
            String sortColName,
            String sortDirection,
            String userEmail
    );

    void addProductsToPantry(long pantryId, List<PantryProductDTO> productDTOs, String userEmail);
    void deleteProductsFromPantry(long pantryId, List<Long> productIds, String userEmail);
}

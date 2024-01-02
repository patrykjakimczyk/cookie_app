package com.cookie.app.service;


import com.cookie.app.model.dto.PantryProductDTO;
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
    void removeProductsFromPantry(long pantryId, List<Long> productIds, String userEmail);
    void modifyPantryProduct(long pantryId, PantryProductDTO pantryProduct, String userEmail);
    PantryProductDTO reservePantryProduct(long pantryId, long pantryProductId, int reserved, String userEmail);
}

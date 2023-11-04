package com.cookie.app.service;

import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.request.UpdatePantryRequest;
import com.cookie.app.model.response.DeletePantryResponse;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.model.response.PantryProductDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PantryService {
    void createUserPantry(CreatePantryRequest request, String userEmail);

    GetPantryResponse getUserPantry(String userEmail);

    DeletePantryResponse deleteUserPantry(String userEmail);

    GetPantryResponse updateUserPantry(UpdatePantryRequest request, String userEmail);


    Page<PantryProductDTO> getPantryProducts(
            long id,
            int page,
            String filterCol,
            String filterValue,
            String sortColName,
            String sortDirection,
            String userEmail
    );

    void addProductsToPantry(long id, List<PantryProductDTO> productDTOs, String userEmail);
}

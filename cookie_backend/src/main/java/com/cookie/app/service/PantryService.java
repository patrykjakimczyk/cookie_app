package com.cookie.app.service;

import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.model.response.PantryProductDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PantryService {
    void createPantry(CreatePantryRequest request, String userEmail);

    GetPantryResponse getUserPantry(String userEmail);

    Page<PantryProductDTO> getPantryProducts(long id, int page, String userEmail);
}

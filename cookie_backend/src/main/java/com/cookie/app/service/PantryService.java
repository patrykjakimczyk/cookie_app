package com.cookie.app.service;

import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.request.UpdatePantryRequest;
import com.cookie.app.model.response.DeletePantryResponse;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.model.response.GetUserPantriesResponse;

public interface PantryService {
    void createPantry(CreatePantryRequest request, String userEmail);

    GetPantryResponse getPantry(String userEmail);

    DeletePantryResponse deletePantry(String userEmail);

    GetPantryResponse updatePantry(UpdatePantryRequest request, String userEmail);
}

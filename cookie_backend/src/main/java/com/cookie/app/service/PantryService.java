package com.cookie.app.service;

import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.response.GetPantryResponse;

public interface PantryService {
    void createPantry(CreatePantryRequest request, String userEmail);

    GetPantryResponse getUserPantry(String userEmail);
}

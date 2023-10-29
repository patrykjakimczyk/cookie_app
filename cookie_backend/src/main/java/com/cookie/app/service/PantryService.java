package com.cookie.app.service;

import com.cookie.app.model.request.CreatePantryRequest;

public interface PantryService {
    void createPantry(CreatePantryRequest request, String userEmail);
}

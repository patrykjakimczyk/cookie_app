package com.cookie.app.service;

import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.request.UpdatePantryRequest;
import com.cookie.app.model.response.DeletePantryResponse;
import com.cookie.app.model.response.GetPantryResponse;

public interface PantryService {
    void createUserPantry(CreatePantryRequest request, String userEmail);

    GetPantryResponse getUserPantry(String userEmail);

    DeletePantryResponse deleteUserPantry(String userEmail);

    GetPantryResponse updateUserPantry(UpdatePantryRequest request, String userEmail);
}

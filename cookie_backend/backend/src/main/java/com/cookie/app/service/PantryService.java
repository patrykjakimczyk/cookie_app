package com.cookie.app.service;

import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.request.UpdatePantryRequest;
import com.cookie.app.model.response.DeletePantryResponse;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.model.response.GetUserPantriesResponse;

public interface PantryService {
    GetPantryResponse createPantry(CreatePantryRequest request, String userEmail);
    GetPantryResponse getPantry(long pantryId, String userEmail);
    GetUserPantriesResponse getAllUserPantries(String userEmail);
    DeletePantryResponse deletePantry(long pantryId, String userEmail);
    GetPantryResponse updatePantry(long pantryId, UpdatePantryRequest request, String userEmail);
}

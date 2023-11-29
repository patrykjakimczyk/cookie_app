package com.cookie.app.model.response;

import java.util.List;

public record GetUserPantriesResponse(List<GetPantryResponse> userPantries) {
}

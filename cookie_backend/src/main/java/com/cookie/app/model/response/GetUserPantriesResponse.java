package com.cookie.app.model.response;

import com.cookie.app.model.dto.PantryDTO;

import java.util.List;

public record GetUserPantriesResponse(List<PantryDTO> pantries) {
}

package com.cookie.app.model.dto;

import com.cookie.app.model.enums.Category;

public record ProductDTO(Long id, String productName, Category category) {
}

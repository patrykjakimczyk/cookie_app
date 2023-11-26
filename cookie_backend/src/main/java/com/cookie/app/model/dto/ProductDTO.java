package com.cookie.app.model.dto;

import com.cookie.app.model.enums.Category;

public record ProductDTO(String productName, Category category) {
}

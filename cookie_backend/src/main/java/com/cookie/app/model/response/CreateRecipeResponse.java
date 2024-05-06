package com.cookie.app.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateRecipeResponse(@Schema(example = "1") long recipeId) {
}

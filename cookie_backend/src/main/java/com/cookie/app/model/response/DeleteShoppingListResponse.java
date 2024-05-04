package com.cookie.app.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record DeleteShoppingListResponse(@Schema(example = "listName") String deletedListName) {}

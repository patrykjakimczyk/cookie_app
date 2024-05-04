package com.cookie.app.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        @Schema(example = "10")
        long totalElements,
        @Schema(example = "2")
        int totalPages,
        @Schema(example = "1")
        int pageNr
) {
    public PageResult(Page<T> page) {
        this(page.getContent(), page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }
}

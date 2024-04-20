package com.cookie.app.model.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int pageNr
) {
    public PageResult(Page<T> page) {
        this(page.getContent(), page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }
}

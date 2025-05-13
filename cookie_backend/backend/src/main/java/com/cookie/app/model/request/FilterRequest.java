package com.cookie.app.model.request;

import com.cookie.app.model.RegexConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public class FilterRequest {
    @Schema(example = "filter")
    @Pattern(
            regexp = RegexConstants.FILTER_VALUE_REGEX,
            message = "Filter value can only contains letters, digits, whitespaces, dashes and its length must be greater than 0"
    ) private final String filterValue;

    @Schema(example = "quantity")
    @Pattern(
            regexp = RegexConstants.SORT_COL_REGEX,
            message = "Filter value can only contains letters, underscores and its length must be greater than 0"
    ) private final String sortColName;

    @Schema(example = "DESC")
    private final Sort.Direction sortDirection;

    public FilterRequest(String filterValue, String sortColName, Sort.Direction sortDirection) {
        this.filterValue = filterValue;
        this.sortColName = sortColName;
        this.sortDirection = sortDirection;
    }
}

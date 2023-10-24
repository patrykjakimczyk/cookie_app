package com.cookie.app.model.response;

import java.util.List;

public record RegistrationResponse(List<String> duplicates) {
}

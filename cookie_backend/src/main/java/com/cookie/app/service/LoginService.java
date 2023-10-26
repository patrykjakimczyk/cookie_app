package com.cookie.app.service;

import com.cookie.app.model.request.RegistrationRequest;

import java.util.List;

public interface LoginService {
    String getUsername(String email);
    List<String> userRegistration(RegistrationRequest request);
}

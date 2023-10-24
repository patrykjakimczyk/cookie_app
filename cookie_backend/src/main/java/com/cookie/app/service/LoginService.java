package com.cookie.app.service;

import com.cookie.app.model.request.RegistrationRequest;

import java.util.List;

public interface LoginService {
    List<String> userRegistration(RegistrationRequest request);
}

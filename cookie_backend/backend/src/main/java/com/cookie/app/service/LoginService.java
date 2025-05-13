package com.cookie.app.service;

import com.cookie.app.model.request.RegistrationRequest;
import com.cookie.app.model.response.LoginResponse;
import com.cookie.app.model.response.RegistrationResponse;

public interface LoginService {
    LoginResponse getLoginInfo(String email);
    RegistrationResponse userRegistration(RegistrationRequest request);
}

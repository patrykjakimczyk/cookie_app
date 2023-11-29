package com.cookie.app.service;

import com.cookie.app.model.request.CreateGroupRequest;

public interface GroupService {
    void createGroup(CreateGroupRequest request, String userEmail);
}

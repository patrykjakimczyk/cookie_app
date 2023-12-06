package com.cookie.app.service;

import com.cookie.app.model.dto.GroupDTO;
import com.cookie.app.model.request.AddUserToGroupRequest;
import com.cookie.app.model.request.AssignAuthoritiesToUserRequest;
import com.cookie.app.model.request.CreateGroupRequest;
import com.cookie.app.model.request.UpdateGroupRequest;
import com.cookie.app.model.response.GetUserGroupsResponse;

public interface GroupService {
    void createGroup(CreateGroupRequest request, String userEmail);

    GroupDTO getGroup(Long groupId, String userEmail);

    GetUserGroupsResponse getUserGroups(String userEmail);

    void updateGroup(Long groupId, UpdateGroupRequest updateGroupRequest, String userEmail);

    void deleteGroup(Long groupId, String userEmail);

    void addUserToGroup(Long groupId, AddUserToGroupRequest request, String userEmail);

    void removeUserFromGroup(Long groupId, Long userToRemoveId, String userEmail);

    void assignAuthoritiesToUser(Long groupId, AssignAuthoritiesToUserRequest request, String userEmail);

    void takeAwayAuthoritiesFromUser(Long groupId, AssignAuthoritiesToUserRequest request, String userEmail);
}

package com.cookie.app.service;

import com.cookie.app.model.dto.GroupDetailsDTO;
import com.cookie.app.model.request.UserWithAuthoritiesRequest;
import com.cookie.app.model.request.CreateGroupRequest;
import com.cookie.app.model.request.UpdateGroupRequest;
import com.cookie.app.model.response.GetUserGroupsResponse;

public interface GroupService {
    void createGroup(CreateGroupRequest request, String userEmail);

    GroupDetailsDTO getGroup(Long groupId, String userEmail);

    GetUserGroupsResponse getUserGroups(String userEmail);

    void updateGroup(Long groupId, UpdateGroupRequest updateGroupRequest, String userEmail);

    void deleteGroup(Long groupId, String userEmail);

    void addUserToGroup(Long groupId, String usernameToAdd, String userEmail);

    void removeUserFromGroup(Long groupId, Long userToRemoveId, String userEmail);

    void assignAuthoritiesToUser(Long groupId, UserWithAuthoritiesRequest request, String userEmail);

    void takeAwayAuthoritiesFromUser(Long groupId, UserWithAuthoritiesRequest request, String userEmail);
}

package com.cookie.app.service;

import com.cookie.app.model.dto.GroupDetailsDTO;
import com.cookie.app.model.request.UserWithAuthoritiesRequest;
import com.cookie.app.model.request.CreateGroupRequest;
import com.cookie.app.model.request.UpdateGroupRequest;
import com.cookie.app.model.response.AssignAuthoritiesToUserResponse;
import com.cookie.app.model.response.GetUserGroupsResponse;
import com.cookie.app.model.response.GroupNameTakenResponse;

public interface GroupService {
    GroupNameTakenResponse createGroup(CreateGroupRequest request, String userEmail);
    GroupDetailsDTO getGroupDetails(Long groupId, String userEmail);
    GetUserGroupsResponse getUserGroups(String userEmail);
    GroupNameTakenResponse updateGroup(Long groupId, UpdateGroupRequest updateGroupRequest, String userEmail);
    void deleteGroup(Long groupId, String userEmail);
    void addUserToGroup(Long groupId, String usernameToAdd, String userEmail);
    void removeUserFromGroup(Long groupId, Long userToRemoveId, String userEmail);
    AssignAuthoritiesToUserResponse assignAuthoritiesToUser(Long groupId, UserWithAuthoritiesRequest request, String userEmail);
    void removeAuthoritiesFromUser(Long groupId, UserWithAuthoritiesRequest request, String userEmail);
}

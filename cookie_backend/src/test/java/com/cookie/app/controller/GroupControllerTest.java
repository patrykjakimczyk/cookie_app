package com.cookie.app.controller;

import com.cookie.app.model.dto.AuthorityDTO;
import com.cookie.app.model.dto.GroupDTO;
import com.cookie.app.model.dto.GroupDetailsDTO;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.request.AddUserToGroupRequest;
import com.cookie.app.model.request.CreateGroupRequest;
import com.cookie.app.model.request.UpdateGroupRequest;
import com.cookie.app.model.request.UserWithAuthoritiesRequest;
import com.cookie.app.model.response.AssignAuthoritiesToUserResponse;
import com.cookie.app.model.response.GetUserGroupsResponse;
import com.cookie.app.model.response.GetGroupResponse;
import com.cookie.app.service.impl.GroupServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest extends AbstractControllerTest {
    final String groupName = "groupName";
    final long groupId = 1L;

    @Mock
    GroupServiceImpl groupService;
    @InjectMocks
    GroupController controller;

    @Test
    void test_createGroupSuccessful() {
        final CreateGroupRequest createGroupRequest = new CreateGroupRequest(groupName);
        final GetGroupResponse serviceResponse = new GetGroupResponse(groupId);

        doReturn(serviceResponse).when(groupService).createGroup(createGroupRequest, authentication.getName());
        ResponseEntity<GetGroupResponse> response = this.controller.createGroup(createGroupRequest, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(serviceResponse.groupId(), response.getBody().groupId());
    }

    @Test
    void test_getGroupDetailsSuccessful() {
        final GroupDetailsDTO groupDetailsDTO = new GroupDetailsDTO(groupId, groupName, null, null, 0L, null, null);

        doReturn(groupDetailsDTO).when(groupService).getGroupDetails(groupId, authentication.getName());
        ResponseEntity<GroupDetailsDTO> response = this.controller.getGroupDetails(groupId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(groupDetailsDTO.id(), response.getBody().id());
        assertEquals(groupDetailsDTO.groupName(), response.getBody().groupName());
    }

    @Test
    void test_getUserGroupsSuccessful() {
        final GroupDTO groupDTO = new GroupDTO(groupId, groupName, null, 1, 0L);
        final GetUserGroupsResponse serviceResponse = new GetUserGroupsResponse(Collections.singletonList(groupDTO));

        doReturn(serviceResponse).when(groupService).getUserGroups(authentication.getName());
        ResponseEntity<GetUserGroupsResponse> response = this.controller.getUserGroups(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse.userGroups().size(), response.getBody().userGroups().size());
        assertEquals(serviceResponse.userGroups().get(0).id(), response.getBody().userGroups().get(0).id());
        assertEquals(serviceResponse.userGroups().get(0).groupName(), response.getBody().userGroups().get(0).groupName());
    }

    @Test
    void test_updateGroupSuccessful() {
        final UpdateGroupRequest updateGroupRequest = new UpdateGroupRequest(groupName);
        final GetGroupResponse serviceResponse = new GetGroupResponse(groupId);

        doReturn(serviceResponse).when(groupService).updateGroup(groupId, updateGroupRequest, authentication.getName());
        ResponseEntity<GetGroupResponse> response = this.controller.updateGroup(groupId, updateGroupRequest, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse.groupId(), response.getBody().groupId());
    }

    @Test
    void test_deleteGroupSuccessful() {

        doNothing().when(groupService).deleteGroup(groupId, authentication.getName());
        ResponseEntity<Void> response = this.controller.deleteGroup(groupId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_addUserToGroupSuccessful() {
        final String userName = "username";
        final AddUserToGroupRequest request = new AddUserToGroupRequest(userName);

        doNothing().when(groupService).addUserToGroup(groupId, userName, authentication.getName());
        ResponseEntity<Void> response = this.controller.addUserToGroup(groupId, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_removeUserFromGroupSuccessful() {
        final long userId = 1L;

        doNothing().when(groupService).removeUserFromGroup(groupId, userId, authentication.getName());
        ResponseEntity<Void> response = this.controller.removeUserFromGroup(groupId, userId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_assignAuthoritiesToUserSuccessful() {
        final Set<AuthorityEnum> authorities = Collections.singleton(AuthorityEnum.MODIFY);
        final UserWithAuthoritiesRequest request = new UserWithAuthoritiesRequest(1L, authorities);
        final AuthorityDTO authorityDTO = new AuthorityDTO(AuthorityEnum.MODIFY, 1L, groupId);
        final AssignAuthoritiesToUserResponse serviceResponse = new AssignAuthoritiesToUserResponse(Collections.singleton(authorityDTO));

        doReturn(serviceResponse).when(groupService).assignAuthoritiesToUser(groupId, request, authentication.getName());
        ResponseEntity<AssignAuthoritiesToUserResponse> response =
                this.controller.assignAuthoritiesToUser(groupId, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse.assignedAuthorities().size(), response.getBody().assignedAuthorities().size());
        assertTrue(response.getBody().assignedAuthorities().contains(authorityDTO));
    }

    @Test
    void test_removeAuthoritiesFromUserSuccessful() {
        final Set<AuthorityEnum> authorities = Collections.singleton(AuthorityEnum.MODIFY);
        final UserWithAuthoritiesRequest request = new UserWithAuthoritiesRequest(1L, authorities);

        doNothing().when(groupService).removeAuthoritiesFromUser(groupId, request, authentication.getName());
        ResponseEntity<Void> response =
                this.controller.removeAuthoritiesFromUser(groupId, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

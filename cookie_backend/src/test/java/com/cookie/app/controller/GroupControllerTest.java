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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest extends AbstractControllerTest {
    private final String groupName = "groupName";
    private final long groupId = 1L;

    @Mock
    private GroupServiceImpl groupService;
    @InjectMocks
    private GroupController controller;

    @Test
    void test_createGroupSuccessful() {
        final CreateGroupRequest createGroupRequest = new CreateGroupRequest(groupName);
        final GetGroupResponse serviceResponse = new GetGroupResponse(groupId);

        doReturn(serviceResponse).when(groupService).createGroup(createGroupRequest, authentication.getName());
        ResponseEntity<GetGroupResponse> response = controller.createGroup(createGroupRequest, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().groupId()).isEqualTo(serviceResponse.groupId());
    }

    @Test
    void test_getGroupDetailsSuccessful() {
        final GroupDetailsDTO groupDetailsDTO = new GroupDetailsDTO(groupId, groupName, null, null, 0L, null, null);

        doReturn(groupDetailsDTO).when(groupService).getGroupDetails(groupId, authentication.getName());
        ResponseEntity<GroupDetailsDTO> response = controller.getGroupDetails(groupId, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(groupDetailsDTO.id());
        assertThat(response.getBody().groupName()).isEqualTo(groupDetailsDTO.groupName());
    }

    @Test
    void test_getUserGroupsSuccessful() {
        final GroupDTO groupDTO = new GroupDTO(groupId, groupName, null, 1, 0L);
        final GetUserGroupsResponse serviceResponse = new GetUserGroupsResponse(Collections.singletonList(groupDTO));

        doReturn(serviceResponse).when(groupService).getUserGroups(authentication.getName());
        ResponseEntity<GetUserGroupsResponse> response = controller.getUserGroups(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().userGroups()).hasSize(serviceResponse.userGroups().size());
        assertThat(response.getBody().userGroups().get(0).id()).isEqualTo(serviceResponse.userGroups().get(0).id());
        assertThat(response.getBody().userGroups().get(0).groupName()).isEqualTo(serviceResponse.userGroups().get(0).groupName());
    }

    @Test
    void test_updateGroupSuccessful() {
        final UpdateGroupRequest updateGroupRequest = new UpdateGroupRequest(groupName);
        final GetGroupResponse serviceResponse = new GetGroupResponse(groupId);

        doReturn(serviceResponse).when(groupService).updateGroup(groupId, updateGroupRequest, authentication.getName());
        ResponseEntity<GetGroupResponse> response = controller.updateGroup(groupId, updateGroupRequest, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().groupId()).isEqualTo(serviceResponse.groupId());
    }

    @Test
    void test_deleteGroupSuccessful() {
        doNothing().when(groupService).deleteGroup(groupId, authentication.getName());
        ResponseEntity<Void> response = controller.deleteGroup(groupId, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test_addUserToGroupSuccessful() {
        final String userName = "username";
        final AddUserToGroupRequest request = new AddUserToGroupRequest(userName);

        doNothing().when(groupService).addUserToGroup(groupId, userName, authentication.getName());
        ResponseEntity<Void> response = controller.addUserToGroup(groupId, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test_removeUserFromGroupSuccessful() {
        final long userId = 1L;

        doNothing().when(groupService).removeUserFromGroup(groupId, userId, authentication.getName());
        ResponseEntity<Void> response = controller.removeUserFromGroup(groupId, userId, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test_assignAuthoritiesToUserSuccessful() {
        final Set<AuthorityEnum> authorities = Collections.singleton(AuthorityEnum.MODIFY);
        final UserWithAuthoritiesRequest request = new UserWithAuthoritiesRequest(1L, authorities);
        final AuthorityDTO authorityDTO = new AuthorityDTO(AuthorityEnum.MODIFY, 1L, groupId);
        final AssignAuthoritiesToUserResponse serviceResponse = new AssignAuthoritiesToUserResponse(Collections.singleton(authorityDTO));

        doReturn(serviceResponse).when(groupService).assignAuthoritiesToUser(groupId, request, authentication.getName());
        ResponseEntity<AssignAuthoritiesToUserResponse> response =
                controller.assignAuthoritiesToUser(groupId, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().assignedAuthorities()).hasSize(serviceResponse.assignedAuthorities().size());
        assertThat(response.getBody().assignedAuthorities()).contains(authorityDTO);
    }

    @Test
    void test_removeAuthoritiesFromUserSuccessful() {
        final Set<AuthorityEnum> authorities = Collections.singleton(AuthorityEnum.MODIFY);
        final UserWithAuthoritiesRequest request = new UserWithAuthoritiesRequest(1L, authorities);

        doNothing().when(groupService).removeAuthoritiesFromUser(groupId, request, authentication.getName());
        ResponseEntity<Void> response =
                controller.removeAuthoritiesFromUser(groupId, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
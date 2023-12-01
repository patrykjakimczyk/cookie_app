package com.cookie.app.controller;

import com.cookie.app.model.dto.GroupDTO;
import com.cookie.app.model.request.AddUserToGroupRequest;
import com.cookie.app.model.request.CreateGroupRequest;
import com.cookie.app.model.request.UpdateGroupRequest;
import com.cookie.app.model.response.GetUserGroupsResponse;
import com.cookie.app.service.GroupService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RestController
public class GroupController {
    private static final String GROUP_URL = "/group";
    private static final String GROUP_ID_URL = "/group/{id}";
    private final GroupService groupService;

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(GROUP_URL)
    public ResponseEntity<Void> createGroup(
            @RequestBody @Valid CreateGroupRequest createGroupRequest,
            Authentication authentication
    ) {
        this.groupService.createGroup(createGroupRequest, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(GROUP_ID_URL)
    public ResponseEntity<GroupDTO> getGroup(@PathVariable("id") long groupId, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.groupService.getGroup(groupId, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(GROUP_URL)
    public ResponseEntity<GetUserGroupsResponse> getUserGroupsIds(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.groupService.getUserGroupsIds(authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(GROUP_ID_URL)
    public ResponseEntity<Void> createGroup(
            @PathVariable("id") long groupId,
            @RequestBody @Valid UpdateGroupRequest updateGroupRequest,
            Authentication authentication
    ) {
        this.groupService.updateGroup(groupId, updateGroupRequest, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(GROUP_ID_URL)
    public ResponseEntity<Void> deleteGroup(@PathVariable("id") long groupId, Authentication authentication) {
        this.groupService.deleteGroup(groupId, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(GROUP_ID_URL)
    public ResponseEntity<Void> addUserToGroup(
            @PathVariable("id") long groupId,
            @RequestBody @Valid AddUserToGroupRequest addUserToGroupRequest,
            Authentication authentication
    ) {
        this.groupService.addUserToGroup(groupId, addUserToGroupRequest, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}

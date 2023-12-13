package com.cookie.app.controller;

import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.dto.GroupDetailsDTO;
import com.cookie.app.model.request.AddUserToGroupRequest;
import com.cookie.app.model.request.UserWithAuthoritiesRequest;
import com.cookie.app.model.request.CreateGroupRequest;
import com.cookie.app.model.request.UpdateGroupRequest;
import com.cookie.app.model.response.GetUserGroupsResponse;
import com.cookie.app.service.GroupService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
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
    private static final String GROUP_ID_USERS_URL = "/group/{id}/users";
    private static final String GROUP_ID_AUTHORITIES_URL = "/group/{id}/authorities";
    private final GroupService groupService;

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(GROUP_URL)
    public ResponseEntity<Void> createGroup(
            @RequestBody @Valid CreateGroupRequest createGroupRequest,
            Authentication authentication
    ) {
        log.info("Performing group creation for email {}", authentication.getName());
        this.groupService.createGroup(createGroupRequest, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(GROUP_ID_URL)
    public ResponseEntity<GroupDetailsDTO> getGroup(
            @PathVariable("id") @Valid @Min(1) long groupId,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.groupService.getGroup(groupId, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(GROUP_URL)
    public ResponseEntity<GetUserGroupsResponse> getUserGroups(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.groupService.getUserGroups(authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(GROUP_ID_URL)
    public ResponseEntity<Void> updateGroup(
            @PathVariable("id") @Valid @Min(1) long groupId,
            @RequestBody @Valid UpdateGroupRequest updateGroupRequest,
            Authentication authentication
    ) {
        log.info("Performing group update for email {}", authentication.getName());
        this.groupService.updateGroup(groupId, updateGroupRequest, authentication.getName());

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(GROUP_ID_URL)
    public ResponseEntity<Void> deleteGroup(
            @PathVariable("id") @Valid @Min(1) long groupId,
            Authentication authentication
    ) {
        log.info("Performing group deletion for email {}", authentication.getName());
        this.groupService.deleteGroup(groupId, authentication.getName());

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(GROUP_ID_USERS_URL)
    public ResponseEntity<Void> addUserToGroup(
            @PathVariable("id") @Valid @Min(1) long groupId,
            @RequestBody @Valid AddUserToGroupRequest addUserToGroupRequest,
            Authentication authentication
    ) {
        this.groupService.addUserToGroup(groupId, addUserToGroupRequest.usernameToAdd(), authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(GROUP_ID_USERS_URL)
    public ResponseEntity<Void> removeUserFromGroup(
            @PathVariable("id") @Valid @Min(1) long groupId,
            @RequestParam @Valid @Min(1) long userToRemoveId,
            Authentication authentication
    ) {
        this.groupService.removeUserFromGroup(groupId, userToRemoveId, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(GROUP_ID_AUTHORITIES_URL)
    public ResponseEntity<Void> assignAuthoritiesToUser(
            @PathVariable("id") @Valid @Min(1) long groupId,
            @RequestBody @Valid UserWithAuthoritiesRequest request,
            Authentication authentication
    ) {
        this.groupService.assignAuthoritiesToUser(groupId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(GROUP_ID_AUTHORITIES_URL)
    public ResponseEntity<Void> takeAwayAuthoritiesFromUser(
            @PathVariable("id") @Valid @Min(1) long groupId,
            @RequestBody @Valid UserWithAuthoritiesRequest request,
            Authentication authentication
    ) {
        this.groupService.takeAwayAuthoritiesFromUser(groupId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}

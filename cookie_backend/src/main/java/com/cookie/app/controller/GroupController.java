package com.cookie.app.controller;

import com.cookie.app.model.dto.GroupDetailsDTO;
import com.cookie.app.model.request.AddUserToGroupRequest;
import com.cookie.app.model.request.UserWithAuthoritiesRequest;
import com.cookie.app.model.request.CreateGroupRequest;
import com.cookie.app.model.request.UpdateGroupRequest;
import com.cookie.app.model.response.AssignAuthoritiesToUserResponse;
import com.cookie.app.model.response.GetUserGroupsResponse;
import com.cookie.app.service.GroupService;
import com.cookie.app.model.response.GroupNameTakenResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/v1/groups", produces = { MediaType.APPLICATION_JSON_VALUE })
//@Validated
@RestController
public class GroupController {
    private static final String GROUP_ID_URL = "/{groupId}";
    private static final String GROUP_ID_USERS_URL = "/{groupId}/users";
    private static final String GROUP_ID_AUTHORITIES_URL = "/{groupId}/authorities";

    private final GroupService groupService;

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<GroupNameTakenResponse> createGroup(
            @RequestBody @Valid CreateGroupRequest createGroupRequest,
            Authentication authentication
    ) {
        log.info("Performing group creation by creator with email={}", authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.groupService.createGroup(createGroupRequest, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(GROUP_ID_URL)
    public ResponseEntity<GroupDetailsDTO> getGroupDetails(
            @PathVariable @Positive(message = "Id must be greater than 0") long groupId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(this.groupService.getGroupDetails(groupId, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<GetUserGroupsResponse> getUserGroups(Authentication authentication) {
        return ResponseEntity.ok(this.groupService.getUserGroups(authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(GROUP_ID_URL)
    public ResponseEntity<GroupNameTakenResponse> updateGroup(
            @PathVariable @Positive(message = "Id must be greater than 0") long groupId,
            @RequestBody @Valid UpdateGroupRequest updateGroupRequest,
            Authentication authentication
    ) {
        log.info("Performing group update by creator with email={}", authentication.getName());
        return ResponseEntity.ok(this.groupService.updateGroup(groupId, updateGroupRequest, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(GROUP_ID_URL)
    public ResponseEntity<Void> deleteGroup(
            @PathVariable @Positive(message = "Id must be greater than 0") long groupId,
            Authentication authentication
    ) {
        log.info("Performing group deletion by creator for email={}", authentication.getName());
        this.groupService.deleteGroup(groupId, authentication.getName());

        return ResponseEntity.ok().build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(GROUP_ID_USERS_URL)
    public ResponseEntity<Void> addUserToGroup(
            @PathVariable @Positive(message = "Id must be greater than 0") long groupId,
            @RequestBody @Valid AddUserToGroupRequest addUserToGroupRequest,
            Authentication authentication
    ) {
        log.info(
                "Performing creator addition with username={} to group with id={} by creator with email={}",
                addUserToGroupRequest.usernameToAdd(),
                groupId,
                authentication.getName()
        );
        this.groupService.addUserToGroup(groupId, addUserToGroupRequest.usernameToAdd(), authentication.getName());

        return ResponseEntity.ok().build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(GROUP_ID_USERS_URL)
    public ResponseEntity<Void> removeUserFromGroup(
            @PathVariable @Positive(message = "Group id must be greater than 0") long groupId,
            @RequestParam @Positive(message = "User Id must be greater than 0") long userToRemoveId,
            Authentication authentication
    ) {
        log.info(
                "Performing creator removal with id={} from group with id={} by creator with email={}",
                userToRemoveId,
                groupId,
                authentication.getName()
        );
        this.groupService.removeUserFromGroup(groupId, userToRemoveId, authentication.getName());

        return ResponseEntity.ok().build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(GROUP_ID_AUTHORITIES_URL)
    public ResponseEntity<AssignAuthoritiesToUserResponse> assignAuthoritiesToUser(
            @PathVariable @Positive(message = "Group id must be greater than 0") long groupId,
            @RequestBody @Valid UserWithAuthoritiesRequest request,
            Authentication authentication
    ) {
        log.info(
                "Performing authorities assignment for creator with id={} from group with id={} by creator with email={}",
                request.userId(),
                groupId,
                authentication.getName()
        );
        return ResponseEntity.ok(this.groupService.assignAuthoritiesToUser(groupId, request, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(GROUP_ID_AUTHORITIES_URL)
    public ResponseEntity<Void> removeAuthoritiesFromUser(
            @PathVariable @Positive(message = "Group id must be greater than 0") long groupId,
            @RequestBody @Valid UserWithAuthoritiesRequest request,
            Authentication authentication
    ) {
        log.info(
                "Performing authorities removal from creator with id={} from group with id={} by creator with email={}",
                request.userId(),
                groupId,
                authentication.getName()
        );
        this.groupService.removeAuthoritiesFromUser(groupId, request, authentication.getName());

        return ResponseEntity.ok().build();
    }
}

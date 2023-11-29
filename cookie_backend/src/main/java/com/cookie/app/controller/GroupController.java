package com.cookie.app.controller;

import com.cookie.app.model.request.CreateGroupRequest;
import com.cookie.app.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
public class GroupController {
    private static final String GROUP_URL = "/user";
    private final GroupService groupService;

    @PostMapping(GROUP_URL)
    private ResponseEntity<Void> createGroup(@Valid CreateGroupRequest createGroupRequest, Authentication authentication) {
        this.groupService.createGroup(createGroupRequest, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }
}

package com.cookie.app.model.response;

import com.cookie.app.model.dto.GroupDTO;

import java.util.List;

public record GetUserGroupsResponse(List<GroupDTO> userGroups) {}

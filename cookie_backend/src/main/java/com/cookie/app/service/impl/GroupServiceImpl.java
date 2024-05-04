package com.cookie.app.service.impl;

import com.cookie.app.exception.ResourceNotFoundException;
import com.cookie.app.exception.UserAlreadyAddedToGroupException;
import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.model.dto.GroupDTO;
import com.cookie.app.model.dto.GroupDetailsDTO;
import com.cookie.app.model.entity.Authority;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.*;
import com.cookie.app.model.request.UserWithAuthoritiesRequest;
import com.cookie.app.model.request.CreateGroupRequest;
import com.cookie.app.model.request.UpdateGroupRequest;
import com.cookie.app.model.response.AssignAuthoritiesToUserResponse;
import com.cookie.app.model.response.GetGroupResponse;
import com.cookie.app.model.response.GetUserGroupsResponse;
import com.cookie.app.repository.AuthorityRepository;
import com.cookie.app.repository.GroupRepository;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public non-sealed class GroupServiceImpl extends AbstractCookieService implements GroupService {
    private final GroupRepository groupRepository;
    private final AuthorityRepository authorityRepository;
    private final GroupMapper groupMapper;
    private final GroupDetailsMapper groupDetailsMapper;

    public GroupServiceImpl(UserRepository userRepository,
                            ProductRepository productRepository,
                            AuthorityRepository authorityRepository,
                            GroupRepository groupRepository,
                            GroupMapper groupMapper,
                            GroupDetailsMapper groupDetailsMapper,
                            AuthorityMapper authorityMapper) {
        super(userRepository, productRepository, authorityMapper);
        this.groupRepository = groupRepository;
        this.authorityRepository = authorityRepository;
        this.groupMapper = groupMapper;
        this.groupDetailsMapper = groupDetailsMapper;
    }

    @Transactional
    @Override
    public GetGroupResponse createGroup(CreateGroupRequest request, String userEmail) {
        if (this.groupRepository.findByGroupName(request.groupName()).isPresent()) {
            return new GetGroupResponse(0);
        }

        User user = super.getUserByEmail(userEmail);

        Group group = Group.builder()
                .groupName(request.groupName())
                .creator(user)
                .creationDate(LocalDateTime.now())
                .users(List.of(user))
                .build();

        Set<Authority> authorities = createAuthoritiesList(user, group, Set.of(AuthorityEnum.values()));

        this.groupRepository.save(group);
        this.authorityRepository.saveAll(authorities);
        return new GetGroupResponse(group.getId());
    }

    @Override
    public GroupDetailsDTO getGroupDetails(Long groupId, String userEmail) {
        GroupUserAndAuthorities groupAndUser = getGroupAndUser(groupId, userEmail);
        Group group = groupAndUser.group();
        User user = groupAndUser.user();

        if (!group.getUsers().contains(user)) {
            log.info("User={} tried to access group he does not belong to", userEmail);
            throw new UserPerformedForbiddenActionException("Group does not exist");
        }

        return this.groupDetailsMapper.mapToDto(group);
    }

    @Override
    public GetUserGroupsResponse getUserGroups(String userEmail) {
        User user = super.getUserByEmail(userEmail);
        List<GroupDTO> userGroups = user.getGroups()
                .stream()
                .map(groupMapper::mapToDto)
                .toList();

        return new GetUserGroupsResponse(userGroups);
    }

    @Transactional
    @Override
    public GetGroupResponse updateGroup(Long groupId, UpdateGroupRequest request, String userEmail) {
        if (this.groupRepository.findByGroupName(request.newGroupName()).isPresent()) {
            return new GetGroupResponse(0);
        }

        GroupUserAndAuthorities groupAndAuthorities = getGroupUserAndHisAuthorities(
                groupId, userEmail, AuthorityEnum.MODIFY_GROUP, "modify group"
        );
        Group group = groupAndAuthorities.group();

        group.setGroupName(request.newGroupName());
        this.groupRepository.save(group);
        return new GetGroupResponse(group.getId());
    }

    @Transactional
    @Override
    public void deleteGroup(Long groupId, String userEmail) {
        GroupUserAndAuthorities groupAndAuthorities = getGroupUserAndHisAuthorities(
                groupId, userEmail, AuthorityEnum.MODIFY_GROUP, "delete group"
        );
        Group group = groupAndAuthorities.group();

        this.authorityRepository.deleteByGroup(group);
        this.groupRepository.delete(group);
    }

    @Transactional
    @Override
    public void addUserToGroup(Long groupId, String usernameToAdd, String userEmail) {
        GroupUserAndAuthorities groupAndAuthorities = getGroupUserAndHisAuthorities(
                groupId, userEmail, AuthorityEnum.ADD_TO_GROUP, "add another user to group"
        );
        Group group = groupAndAuthorities.group();
        User userToAdd = this.userRepository.findByUsername(usernameToAdd).orElseThrow(() ->
                new ResourceNotFoundException("You tried to add non existing user to group"));

        if (group.getUsers().contains(userToAdd)) {
            log.info("User={} tried to add user which is already added to group", userEmail);
            throw new UserAlreadyAddedToGroupException("You tried to add user which is already added to group");
        }

        group.getUsers().add(userToAdd);
        Set<Authority> authoritiesForAddedUser = createAuthoritiesList(
                userToAdd, group, AuthorityEnum.BASIC_AUTHORITIES);

        this.groupRepository.save(group);
        this.authorityRepository.saveAll(authoritiesForAddedUser);
    }

    @Transactional
    @Override
    public void removeUserFromGroup(Long groupId, Long userToRemoveId, String userEmail) {
        GroupUserAndAuthorities groupUserAndAuthorities = getGroupUserAndHisAuthorities(groupId, userEmail);
        Group group = groupUserAndAuthorities.group();
        Set<AuthorityEnum> authorities = groupUserAndAuthorities.authorities();
        User user = groupUserAndAuthorities.user();
        User userToRemove;

        if (user.getId() == userToRemoveId) {
            userToRemove = user;
        } else if (!authorities.contains(AuthorityEnum.MODIFY_GROUP)) {
            log.info("User={} tried to remove another user from group without permissions", userEmail);
            throw new UserPerformedForbiddenActionException("You have no permissions to do that");
        } else {
            userToRemove = this.userRepository.findById(userToRemoveId).orElseThrow(() ->
                    new ResourceNotFoundException("User tried to remove non existing user from group"));
        }

        if (!group.getUsers().contains(userToRemove)) {
            log.info("User={} tried to remove user which is not in the group", userEmail);
            throw new UserPerformedForbiddenActionException("You tried to remove user which is not in the group");
        }

        if (group.getCreator().getId() == userToRemove.getId()) {
            log.info("User={} tried to remove group's creator from the group", userEmail);
            throw new UserPerformedForbiddenActionException("You tried to remove group's creator from the group");
        }

        group.getUsers().remove(userToRemove);

        this.groupRepository.save(group);
        this.authorityRepository.deleteByUserAndGroup(userToRemove, group);
    }

    @Transactional
    @Override
    public AssignAuthoritiesToUserResponse assignAuthoritiesToUser(Long groupId, UserWithAuthoritiesRequest request, String userEmail) {
        GroupUserAndAuthorities groupUserAndAuthorities = getGroupUserAndHisAuthorities(
                groupId, userEmail, AuthorityEnum.MODIFY_GROUP, "assign authorities to user"
        );
        Group group = groupUserAndAuthorities.group();
        User user = groupUserAndAuthorities.user();
        User userToAssignAuthorities;

        if (user.getId() == request.userId()) {
            userToAssignAuthorities = user;
        } else {
            userToAssignAuthorities = this.userRepository.findById(request.userId()).orElseThrow(() ->
                    new ResourceNotFoundException("You tried to assign authorities to non existing user"));
        }

        if (!group.getUsers().contains(userToAssignAuthorities)) {
            log.info("User={} tried to assign authorities to user which is not in the group", userEmail);
            throw new UserPerformedForbiddenActionException("You tried to assign authorities to user which is not in the group");
        }

        Set<Authority> authoritiesToAssign = createAuthoritiesList(userToAssignAuthorities, group, request.authorities());
        authoritiesToAssign = authoritiesToAssign
                .stream()
                .filter(authority -> !userToAssignAuthorities.getAuthorities().contains(authority))
                .collect(Collectors.toSet());

        this.authorityRepository.saveAll(authoritiesToAssign);

        return new AssignAuthoritiesToUserResponse(
                authoritiesToAssign
                        .stream()
                        .map(this.authorityMapper::mapToDto)
                        .collect(Collectors.toSet())
        );
    }

    @Transactional
    @Override
    public void removeAuthoritiesFromUser(Long groupId, UserWithAuthoritiesRequest request, String userEmail) {
        GroupUserAndAuthorities groupUserAndAuthorities = getGroupUserAndHisAuthorities(
                groupId, userEmail, AuthorityEnum.MODIFY_GROUP, "take away authorities from user"
        );
        Group group = groupUserAndAuthorities.group();
        User user = groupUserAndAuthorities.user();
        User userToTakeAwayAuthorities;

        if (user.getId() == request.userId()) {
            userToTakeAwayAuthorities = user;
        } else {
            userToTakeAwayAuthorities = this.userRepository.findById(request.userId()).orElseThrow(() ->
                    new ResourceNotFoundException("You tried to take away authorities from non existing user"));
        }

        if (!group.getUsers().contains(userToTakeAwayAuthorities)) {
            log.info("User={} tried to take away authorities from user which is not in the group", userEmail);
            throw new UserPerformedForbiddenActionException("You tried to take away authorities from user which is not in the group");
        }

        Set<Authority> authoritiesToTakeAway =
                userToTakeAwayAuthorities.getAuthorities()
                        .stream()
                        .filter(authority -> request.authorities().contains(authority.getAuthorityName()) &&
                                authority.getGroup().equals(group))
                        .collect(Collectors.toSet());

        if (authoritiesToTakeAway.isEmpty()) {
            return;
        }

        this.authorityRepository.deleteAll(authoritiesToTakeAway);
    }

    private Set<Authority> createAuthoritiesList(User user, Group group, Set<AuthorityEnum> authoritiesSet) {
        Set<Authority> authorities = new HashSet<>();

        for (AuthorityEnum authorityEnum : authoritiesSet) {
            Authority authority = Authority
                    .builder()
                    .authorityName(authorityEnum)
                    .user(user)
                    .group(group)
                    .build();

            authorities.add(authority);
        }

        return authorities;
    }

    private GroupUserAndAuthorities getGroupAndUser(long groupId, String userEmail) {
        User user = super.getUserByEmail(userEmail);
        Group group = this.groupRepository.findById(groupId).orElseThrow(() ->
                new ResourceNotFoundException("Group does not exist"));

        return new GroupUserAndAuthorities(group, user, null);
    }

    private GroupUserAndAuthorities getGroupUserAndHisAuthorities(long groupId, String userEmail) {
        GroupUserAndAuthorities groupUserAndAuthorities = getGroupAndUser(groupId, userEmail);
        Set<Authority> userAuthoritiesForGroup = this.authorityRepository
                .findAuthoritiesByUserAndGroup(groupUserAndAuthorities.user(), groupUserAndAuthorities.group());
        Set<AuthorityEnum> authorities = userAuthoritiesForGroup
                .stream()
                .map(Authority::getAuthorityName)
                .collect(Collectors.toSet());

        return new GroupUserAndAuthorities(groupUserAndAuthorities.group, groupUserAndAuthorities.user, authorities);
    }

    private GroupUserAndAuthorities getGroupUserAndHisAuthorities(long groupId,
                                                                  String userEmail,
                                                                  AuthorityEnum authority,
                                                                  String action
    ) {
        GroupUserAndAuthorities groupAndAuthorities = getGroupUserAndHisAuthorities(groupId, userEmail);

        if (!groupAndAuthorities.authorities().contains(authority)) {
            log.info("User={} tried to {} without permissions", userEmail, action);
            throw new UserPerformedForbiddenActionException("You have no permissions to do that");
        }

        return groupAndAuthorities;
    }

    private record GroupUserAndAuthorities(Group group, User user, Set<AuthorityEnum> authorities) {
    }
}

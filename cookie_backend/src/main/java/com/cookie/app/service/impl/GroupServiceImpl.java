package com.cookie.app.service.impl;

import com.cookie.app.exception.UserAlreadyAddedToGroupException;
import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.model.dto.GroupDTO;
import com.cookie.app.model.dto.GroupDetailsDTO;
import com.cookie.app.model.entity.Authority;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.GroupDetailsMapperDTO;
import com.cookie.app.model.mapper.GroupMapperDTO;
import com.cookie.app.model.request.UserWithAuthoritiesRequest;
import com.cookie.app.model.request.CreateGroupRequest;
import com.cookie.app.model.request.UpdateGroupRequest;
import com.cookie.app.model.response.AssignAuthoritiesToUserResponse;
import com.cookie.app.model.response.GetUserGroupsResponse;
import com.cookie.app.repository.AuthorityRepository;
import com.cookie.app.repository.GroupRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
public class GroupServiceImpl extends AbstractCookieService implements GroupService {
    private final GroupRepository groupRepository;
    private final AuthorityRepository authorityRepository;
    private final GroupMapperDTO groupMapperDTO;
    private final GroupDetailsMapperDTO groupDetailsMapperDTO;
    private final AuthorityMapperDTO authorityMapperDTO;

    public GroupServiceImpl(
            UserRepository userRepository,
            GroupRepository groupRepository,
            AuthorityRepository authorityRepository,
            GroupMapperDTO groupMapperDTO,
            GroupDetailsMapperDTO groupDetailsMapperDTO,
            AuthorityMapperDTO authorityMapperDTO
    ) {
        super(userRepository);
        this.groupRepository = groupRepository;
        this.authorityRepository = authorityRepository;
        this.groupMapperDTO = groupMapperDTO;
        this.groupDetailsMapperDTO = groupDetailsMapperDTO;
        this.authorityMapperDTO = authorityMapperDTO;
    }

    @Override
    public void createGroup(CreateGroupRequest request, String userEmail) {
        User user = this.getUserByEmail(userEmail);

        Group group = Group.builder()
                .groupName(request.groupName())
                .creator(user)
                .creationDate(Timestamp.from(Instant.now()))
                .users(List.of(user))
                .build();

        List<Authority> authorities = this.createAuthoritiesList(user, group, AuthorityEnum.ALL_AUTHORITIES);

        this.groupRepository.save(group);
        this.authorityRepository.saveAll(authorities);
    }

    @Override
    public GroupDetailsDTO getGroup(Long groupId, String userEmail) {
        User user = this.getUserByEmail(userEmail);

        Optional<Group> groupOptional = this.groupRepository.findById(groupId);

        if (groupOptional.isEmpty()) {
            log.info(String.format("User: %s tried to access group which does not exists", userEmail));
            throw new UserPerformedForbiddenActionException("Group does not exists");
        }

        Group group = groupOptional.get();

        if (!group.getUsers().contains(user)) {
            log.info(String.format("User: %s tried to access group he does not belong to", userEmail));
            throw new UserPerformedForbiddenActionException("Group does not exists");
        }

        return this.groupDetailsMapperDTO.apply(group);
    }

    @Override
    public GetUserGroupsResponse getUserGroups(String userEmail) {
        User user = this.getUserByEmail(userEmail);

        List<GroupDTO> userGroupsIds = user.getGroups()
                .stream()
                .map(groupMapperDTO::apply)
                .toList();

        return new GetUserGroupsResponse(userGroupsIds);
    }

    @Override
    public void updateGroup(Long groupId, UpdateGroupRequest request, String userEmail) {
        User user = this.getUserByEmail(userEmail);

        Optional<Group> groupOptional = this.groupRepository.findById(groupId);

        if (groupOptional.isEmpty()) {
            log.info(String.format("User: %s tried to modify group with does not exists", userEmail));
            throw new UserPerformedForbiddenActionException("Group does not exists");
        }

        Group group = groupOptional.get();

        List<Authority> userAuthoritiesForGroup = this.authorityRepository.findAuthoritiesByUserAndGroup(user, group);

        Set<AuthorityEnum> authorities = userAuthoritiesForGroup
                .stream()
                .map(Authority::getAuthority)
                .collect(Collectors.toSet());

        if (!authorities.contains(AuthorityEnum.MODIFY_GROUP)) {
            log.info(String.format("User: %s tried to modify group without permissions", userEmail));
            throw new UserPerformedForbiddenActionException("You have no permissions to do that");
        }

        group.setGroupName(request.newGroupName());
        this.groupRepository.save(group);
    }

    @Override
    public void deleteGroup(Long groupId, String userEmail) {
        User user = this.getUserByEmail(userEmail);

        Optional<Group> groupOptional = this.groupRepository.findById(groupId);

        if (groupOptional.isEmpty()) {
            log.info(String.format("User: %s tried to delete group with does not exists", userEmail));
            throw new UserPerformedForbiddenActionException("Group does not exists");
        }

        Group group = groupOptional.get();

        List<Authority> userAuthoritiesForGroup = this.authorityRepository.findAuthoritiesByUserAndGroup(user, group);

        Set<AuthorityEnum> authorities = userAuthoritiesForGroup
                .stream()
                .map(Authority::getAuthority)
                .collect(Collectors.toSet());

        if (!authorities.contains(AuthorityEnum.MODIFY_GROUP)) {
            log.info(String.format("User: %s tried to delete group without permissions", userEmail));
            throw new UserPerformedForbiddenActionException("You have no permissions to do that");
        }

        this.authorityRepository.deleteByGroup(group);
        this.groupRepository.delete(group);
    }

    @Override
    public void addUserToGroup(Long groupId, String usernameToAdd, String userEmail) {
        User user = this.getUserByEmail(userEmail);

        Optional<Group> groupOptional = this.groupRepository.findById(groupId);

        if (groupOptional.isEmpty()) {
            log.info(String.format("User: %s tried to add another user to non existing group", userEmail));
            throw new UserPerformedForbiddenActionException("Group does not exists");
        }

        Group group = groupOptional.get();

        List<Authority> userAuthoritiesForGroup = this.authorityRepository.findAuthoritiesByUserAndGroup(user, group);

        Set<AuthorityEnum> authorities = userAuthoritiesForGroup
                .stream()
                .map(Authority::getAuthority)
                .collect(Collectors.toSet());

        if (!authorities.contains(AuthorityEnum.ADD_TO_GROUP)) {
            log.info(String.format("User: %s tried to add another user to group without permissions", userEmail));
            throw new UserPerformedForbiddenActionException("You have no permissions to do that");
        }

        Optional<User> userToAddOptional = this.userRepository.findByUsername(usernameToAdd);

        if (userToAddOptional.isEmpty()) {
            throw new UserPerformedForbiddenActionException("User tried to add non existing user to group");
        }

        User userToAdd = userToAddOptional.get();

        if (group.getUsers().contains(userToAdd)) {
            log.info(String.format("User: %s tried to add user which is already added to group", userEmail));
            throw new UserAlreadyAddedToGroupException("You tried to add user which is already added to group");
        }

        group.getUsers().add(userToAdd);
        List<Authority> authoritiesForAddedUser = this.createAuthoritiesList(userToAdd, group, AuthorityEnum.BASIC_AUTHORITIES);

        this.groupRepository.save(group);
        this.authorityRepository.saveAll(authoritiesForAddedUser);
    }

    @Override
    public void removeUserFromGroup(Long groupId, Long userToRemoveId, String userEmail) {
        User user = this.getUserByEmail(userEmail);

        Optional<Group> groupOptional = this.groupRepository.findById(groupId);

        if (groupOptional.isEmpty()) {
            log.info(String.format("User: %s tried to add another user to non existing group", userEmail));
            throw new UserPerformedForbiddenActionException("Group does not exists");
        }

        Group group = groupOptional.get();

        List<Authority> userAuthoritiesForGroup = this.authorityRepository.findAuthoritiesByUserAndGroup(user, group);

        Set<AuthorityEnum> authorities = userAuthoritiesForGroup
                .stream()
                .map(Authority::getAuthority)
                .collect(Collectors.toSet());

        Optional<User> userToRemoveOptional = this.userRepository.findById(userToRemoveId);

        if (userToRemoveOptional.isEmpty()) {
            throw new UserPerformedForbiddenActionException("User tried to remove non existing user from group");
        }

        User userToRemove = userToRemoveOptional.get();

        if (!authorities.contains(AuthorityEnum.MODIFY_GROUP) && userToRemove.getId() != user.getId()) {
            log.info(String.format("User: %s tried to remove another user from group without permissions", userEmail));
            throw new UserPerformedForbiddenActionException("You have no permissions to do that");
        }

        if (!group.getUsers().contains(userToRemove)) {
            log.info(String.format("User: %s tried to remove user which is not in the group", userEmail));
            throw new UserPerformedForbiddenActionException("You tried to remove user which is not in the group");
        }

        if (group.getCreator().getId() == userToRemove.getId()) {
            log.info(String.format("User: %s tried to remove group's creator from the group", userEmail));
            throw new UserPerformedForbiddenActionException("You tried to remove group's creator from the group");
        }

        group.getUsers().remove(userToRemove);

        this.groupRepository.save(group);
        this.authorityRepository.deleteByUserAndGroup(userToRemove, group);
    }

    @Override
    public AssignAuthoritiesToUserResponse assignAuthoritiesToUser(Long groupId, UserWithAuthoritiesRequest request, String userEmail) {
        User user = this.getUserByEmail(userEmail);

        Optional<Group> groupOptional = this.groupRepository.findById(groupId);

        if (groupOptional.isEmpty()) {
            log.info(String.format("User: %s tried to assign authorities to another user for non existing group", userEmail));
            throw new UserPerformedForbiddenActionException("Group does not exists");
        }

        Group group = groupOptional.get();

        List<Authority> userAuthoritiesForGroup = this.authorityRepository.findAuthoritiesByUserAndGroup(user, group);

        Set<AuthorityEnum> authorities = userAuthoritiesForGroup
                .stream()
                .map(Authority::getAuthority)
                .collect(Collectors.toSet());

        if (!authorities.contains(AuthorityEnum.MODIFY_GROUP)) {
            log.info(String.format("User: %s tried to assign authorities to user without permissions", userEmail));
            throw new UserPerformedForbiddenActionException("You have no permissions to do that");
        }

        Optional<User> userToAssignAuthoritiesOptional = this.userRepository.findById(request.userId());

        if (userToAssignAuthoritiesOptional.isEmpty()) {
            throw new UserPerformedForbiddenActionException("You tried to assign authorities to non existing user");
        }

        User userToAssignAuthorities = userToAssignAuthoritiesOptional.get();

        if (!group.getUsers().contains(userToAssignAuthorities)) {
            log.info(String.format("User: %s tried to assign authorities to user which is not in the group", userEmail));
            throw new UserPerformedForbiddenActionException("You tried to assign authorities to user which is not in the group");
        }

        List<Authority> authoritiesToAssign = this.createAuthoritiesList(userToAssignAuthorities, group, request.authorities());
        authoritiesToAssign = authoritiesToAssign
                .stream()
                .filter(authority -> !userToAssignAuthorities.getAuthorities()
                        .stream()
                        .map(Authority::getAuthority)
                        .collect(Collectors.toSet())
                        .contains(authority.getAuthority()))
                .toList();

        this.authorityRepository.saveAll(authoritiesToAssign);

        return new AssignAuthoritiesToUserResponse(
                authoritiesToAssign
                        .stream()
                        .map(this.authorityMapperDTO::apply)
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public void removeAuthoritiesFromUser(Long groupId, UserWithAuthoritiesRequest request, String userEmail) {
        User user = this.getUserByEmail(userEmail);

        Optional<Group> groupOptional = this.groupRepository.findById(groupId);

        if (groupOptional.isEmpty()) {
            log.info(String.format("User: %s tried to take away authorities from another user for non existing group", userEmail));
            throw new UserPerformedForbiddenActionException("Group does not exists");
        }

        Group group = groupOptional.get();

        List<Authority> userAuthoritiesForGroup = this.authorityRepository.findAuthoritiesByUserAndGroup(user, group);

        Set<AuthorityEnum> authorities = userAuthoritiesForGroup
                .stream()
                .map(Authority::getAuthority)
                .collect(Collectors.toSet());

        if (!authorities.contains(AuthorityEnum.MODIFY_GROUP)) {
            log.info(String.format("User: %s tried to take away authorities from user without permissions", userEmail));
            throw new UserPerformedForbiddenActionException("You does not have authorities to take away authorities from other users");
        }

        Optional<User> userToTakeAwayAuthoritiesOptional = this.userRepository.findById(request.userId());

        if (userToTakeAwayAuthoritiesOptional.isEmpty()) {
            throw new UserPerformedForbiddenActionException("You tried to take away authorities from non existing user");
        }

        User userToTakeAwayAuthorities = userToTakeAwayAuthoritiesOptional.get();

        if (!group.getUsers().contains(userToTakeAwayAuthorities)) {
            log.info(String.format("User: %s tried to take away authorities from user which is not in the group", userEmail));
            throw new UserPerformedForbiddenActionException("You tried to take away authorities from user which is not in the group");
        }

        List<Authority> authoritiesToTakeAway =
                userToTakeAwayAuthorities.getAuthorities()
                .stream()
                .filter(authority -> request.authorities().contains(authority.getAuthority()))
                .toList();

        this.authorityRepository.deleteAll(authoritiesToTakeAway);
    }

    private List<Authority> createAuthoritiesList(User user, Group group, Set<AuthorityEnum> authoritiesSet) {
        List<Authority> authorities = new ArrayList<>();

        for (AuthorityEnum authorityEnum : authoritiesSet) {
            Authority authority = Authority
                    .builder()
                    .authority(authorityEnum)
                    .user(user)
                    .group(group)
                    .build();

            authorities.add(authority);
        }

        return authorities;
    }










}

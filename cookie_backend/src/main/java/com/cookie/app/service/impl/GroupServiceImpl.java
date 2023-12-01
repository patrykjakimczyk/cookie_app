package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.dto.GroupDTO;
import com.cookie.app.model.entity.Authority;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.GroupMapperDTO;
import com.cookie.app.model.request.AddUserToGroupRequest;
import com.cookie.app.model.request.CreateGroupRequest;
import com.cookie.app.model.request.UpdateGroupRequest;
import com.cookie.app.model.response.GetUserGroupsResponse;
import com.cookie.app.repository.AuthorityRepository;
import com.cookie.app.repository.GroupRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;
    private final GroupMapperDTO groupMapperDTO;

    @Override
    public void  createGroup(CreateGroupRequest request, String userEmail) {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (userOptional.isEmpty()) {
            throw new UserWasNotFoundAfterAuthException("User was not found in database after authentication");
        }

        User user = userOptional.get();

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
    public GroupDTO getGroup(Long groupId, String userEmail) {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (userOptional.isEmpty()) {
            throw new UserWasNotFoundAfterAuthException("User was not found in database after authentication");
        }

        User user = userOptional.get();

        Optional<Group> groupOptional = this.groupRepository.findById(groupId);

        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();

            if (group.getUsers().contains(user)) {
                return this.groupMapperDTO.apply(group);
            }

            log.info(String.format("User: %s tried to access group he does not belong to", userEmail));
            throw new UserPerformedForbiddenActionException("Group does not exists");
        }

        return null;
    }

    @Override
    public GetUserGroupsResponse getUserGroupsIds(String userEmail) {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (userOptional.isEmpty()) {
            throw new UserWasNotFoundAfterAuthException("User was not found in database after authentication");
        }

        User user = userOptional.get();

        List<Long> userGroupsIds = user.getGroups()
                .stream()
                .map(Group::getId)
                .toList();

        return new GetUserGroupsResponse(userGroupsIds);
    }

    @Override
    public void updateGroup(Long groupId, UpdateGroupRequest request, String userEmail) {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (userOptional.isEmpty()) {
            throw new UserWasNotFoundAfterAuthException("User was not found in database after authentication");
        }

        User user = userOptional.get();

        Optional<Group> groupOptional = this.groupRepository.findById(groupId);

        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();

            List<Authority> userAuthoritiesForGroup = this.authorityRepository.findAuthoritiesByUserAndGroup(user, group);

            Set<AuthorityEnum> authorities = userAuthoritiesForGroup
                    .stream()
                    .map(Authority::getAuthority)
                    .collect(Collectors.toSet());

            if (authorities.contains(AuthorityEnum.MODIFY_GROUP)) {
                group.setGroupName(request.newGroupName());
                this.groupRepository.save(group);
            } else {
                log.info(String.format("User: %s tried to modify group without permissions", userEmail));
                throw new UserPerformedForbiddenActionException("Group does not exists");
            }
        } else {
            log.info(String.format("User: %s tried to modify group with does not exists", userEmail));
            throw new UserPerformedForbiddenActionException("Group does not exists");
        }
    }

    @Override
    public void deleteGroup(Long groupId, String userEmail) {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (userOptional.isEmpty()) {
            throw new UserWasNotFoundAfterAuthException("User was not found in database after authentication");
        }

        User user = userOptional.get();

        Optional<Group> groupOptional = this.groupRepository.findById(groupId);

        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();

            List<Authority> userAuthoritiesForGroup = this.authorityRepository.findAuthoritiesByUserAndGroup(user, group);

            Set<AuthorityEnum> authorities = userAuthoritiesForGroup
                    .stream()
                    .map(Authority::getAuthority)
                    .collect(Collectors.toSet());

            if (authorities.contains(AuthorityEnum.DELETE_GROUP)) {
                this.groupRepository.delete(group);
            } else {
                log.info(String.format("User: %s tried to delete group without permissions", userEmail));
                throw new UserPerformedForbiddenActionException("Group does not exists");
            }
        } else {
            log.info(String.format("User: %s tried to delete group with does not exists", userEmail));
            throw new UserPerformedForbiddenActionException("Group does not exists");
        }
    }

    @Override
    public void addUserToGroup(Long groupId, AddUserToGroupRequest request, String userEmail) {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (userOptional.isEmpty()) {
            throw new UserWasNotFoundAfterAuthException("User was not found in database after authentication");
        }

        User user = userOptional.get();

        Optional<Group> groupOptional = this.groupRepository.findById(groupId);

        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();

            List<Authority> userAuthoritiesForGroup = this.authorityRepository.findAuthoritiesByUserAndGroup(user, group);

            Set<AuthorityEnum> authorities = userAuthoritiesForGroup
                    .stream()
                    .map(Authority::getAuthority)
                    .collect(Collectors.toSet());

            if (authorities.contains(AuthorityEnum.ADD_TO_GROUP)) {
                Optional<User> userToAddOptional = this.userRepository.findByEmail(userEmail);

                if (userToAddOptional.isEmpty()) {
                    throw new UserPerformedForbiddenActionException("User tried to add non existing user to group");
                }

                User userToAdd = userToAddOptional.get();


                group.getUsers().add(userToAdd);

                List<Authority> authoritiesForAddedUser = this.createAuthoritiesList(user, group, AuthorityEnum.ALL_AUTHORITIES);

                this.groupRepository.save(group);
                this.authorityRepository.saveAll(authoritiesForAddedUser);
            } else {
                log.info(String.format("User: %s tried to add another user to group without permissions", userEmail));
                throw new UserPerformedForbiddenActionException("Group does not exists");
            }
        } else {
            log.info(String.format("User: %s tried to add another user to non existing group", userEmail));
            throw new UserPerformedForbiddenActionException("Group does not exists");
        }
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

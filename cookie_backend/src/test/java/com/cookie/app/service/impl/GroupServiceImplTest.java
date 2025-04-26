package com.cookie.app.service.impl;

import com.cookie.app.exception.ResourceNotFoundException;
import com.cookie.app.exception.UserAlreadyAddedToGroupException;
import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.model.dto.AuthorityDTO;
import com.cookie.app.model.dto.GroupDetailsDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.*;
import com.cookie.app.model.request.CreateGroupRequest;
import com.cookie.app.model.request.UpdateGroupRequest;
import com.cookie.app.model.request.UserWithAuthoritiesRequest;
import com.cookie.app.model.response.AssignAuthoritiesToUserResponse;
import com.cookie.app.model.response.GetUserGroupsResponse;
import com.cookie.app.model.response.GetGroupResponse;
import com.cookie.app.repository.AuthorityRepository;
import com.cookie.app.repository.GroupRepository;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {
    final String groupName = "groupName";
    final String email = "email";
    final long groupId = 1L;
    final long id = 1L;

    @Spy
    AuthorityMapper authorityMapper = new AuthorityMapperImpl();
    UserMapper userMapper = new UserMapperImpl(authorityMapper);
    @Spy
    GroupDetailsShoppingListMapper groupDetailsShoppingListMapper = new GroupDetailsShoppingListMapperImpl();
    @Spy
    GroupDetailsMapper groupDetailsMapper = new GroupDetailsMapperImpl(userMapper, groupDetailsShoppingListMapper);
    @Spy
    GroupMapper groupMapper = new GroupMapperImpl(userMapper);
    @Mock
    UserRepository userRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    AuthorityRepository authorityRepository;
    @Mock
    GroupRepository groupRepository;
    @InjectMocks
    GroupServiceImpl service;

    @Captor
    ArgumentCaptor<Group> groupArgCaptor;
    @Captor
    ArgumentCaptor<Set<Authority>> authoritiesListArgCaptor;

    User user;
    Group group;
    Authority authority;
    ShoppingList shoppingList;

    @BeforeEach
    void init() {
        user = User.builder()
                .id(id)
                .email(email)
                .build();
        shoppingList = ShoppingList.builder().id(0).listName("list").build();
        group = Group.builder()
                .id(id)
                .groupName(groupName)
                .users(new ArrayList<>(Collections.singletonList(user)))
                .shoppingLists(Collections.singletonList(shoppingList))
                .creator(user)
                .build();
        user.setGroups(Collections.singletonList(group));
        authority = Authority.builder()
                .id(id)
                .group(group)
                .user(user)
                .authorityName(AuthorityEnum.MODIFY_GROUP)
                .build();
        user.setAuthorities(Set.of(authority));
    }

    @Test
    void test_createGroupSuccessful() {
        final CreateGroupRequest createGroupRequest = new CreateGroupRequest(groupName);

        doReturn(Optional.empty()).when(groupRepository).findByGroupName(groupName);
        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        this.service.createGroup(createGroupRequest, email);

        verify(groupRepository).save(this.groupArgCaptor.capture());
        verify(authorityRepository).saveAll(this.authoritiesListArgCaptor.capture());

        Group group = this.groupArgCaptor.getValue();
        assertThat(group.getGroupName()).isEqualTo(groupName);
        assertThat(group.getCreator()).isEqualTo(user);
        assertThat(group.getUsers()).contains(user);
        assertThat(group.getPantry()).isNull();
        assertThat(group.getMeals()).isNull();
        assertThat(group.getShoppingLists()).isNull();
        Set<Authority> authorities = this.authoritiesListArgCaptor.getValue();
        assertThat(authorities).hasSize(AuthorityEnum.values().length);
    }

    @Test
    void test_createGroupNameTaken() {
        final CreateGroupRequest createGroupRequest = new CreateGroupRequest(groupName);

        doReturn(Optional.of(group)).when(groupRepository).findByGroupName(groupName);
        GetGroupResponse response = this.service.createGroup(createGroupRequest, email);

        verify(groupRepository, times(0)).save(any(Group.class));
        verify(authorityRepository, times(0)).saveAll(anyList());
        verify(userRepository, times(0)).findByEmail(email);
        assertThat(response.groupId()).isZero();
    }

    @Test
    void test_getGroupDetailsSuccessful() {
        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        GroupDetailsDTO response = this.service.getGroupDetails(groupId, email);

        assertThat(response.id()).isEqualTo(group.getId());
        assertThat(response.groupName()).isEqualTo(group.getGroupName());
        assertThat(response.creator().id()).isEqualTo(group.getCreator().getId());
        assertThat(response.creator().username()).isEqualTo(group.getCreator().getUsername());
        assertThat(response.users()).hasSize(group.getUsers().size());
        assertThat(response.pantryId()).isZero();
        assertThat(response.pantryName()).isEmpty();
        assertThat(response.shoppingLists()).hasSize(group.getShoppingLists().size());
        assertThat(response.shoppingLists().get(0).listId()).isEqualTo(group.getShoppingLists().get(0).getId());
        assertThat(response.shoppingLists().get(0).listName()).isEqualTo(group.getShoppingLists().get(0).getListName());
    }

    @Test
    void test_getGroupDetailsUserIsNotAGroupMember() {
        group.setUsers(Collections.emptyList());
        group.setCreator(new User());

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);

        assertThatThrownBy(() -> this.service.getGroupDetails(groupId, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class);
    }

    @Test
    void test_getGroupDetailsGroupDoesNotExist() {
        group.setUsers(Collections.emptyList());
        group.setCreator(new User());

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.empty()).when(groupRepository).findById(groupId);

        assertThatThrownBy(() -> this.service.getGroupDetails(groupId, email))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void test_getUserGroupsSuccessful() {
        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetUserGroupsResponse response = this.service.getUserGroups(email);

        assertThat(response.userGroups()).hasSize(user.getGroups().size());
        assertThat(response.userGroups().get(0).id()).isEqualTo(user.getGroups().get(0).getId());
        assertThat(response.userGroups().get(0).groupName()).isEqualTo(user.getGroups().get(0).getGroupName());
        assertThat(response.userGroups().get(0).creator().id()).isEqualTo(user.getGroups().get(0).getCreator().getId());
        assertThat(response.userGroups().get(0).creator().username()).isEqualTo(user.getGroups().get(0).getCreator().getUsername());
        assertThat(response.userGroups().get(0).users()).isEqualTo(user.getGroups().get(0).getUsers().size());
        assertThat(response.userGroups().get(0).pantryId()).isZero();
    }

    @Test
    void test_getUserGroupsNoGroups() {
        user.setGroups(Collections.emptyList());

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetUserGroupsResponse response = this.service.getUserGroups(email);

        assertThat(response.userGroups()).isEmpty();
    }

    @Test
    void test_updateGroupSuccessful() {
        final UpdateGroupRequest updateGroupRequest = new UpdateGroupRequest(groupName);

        doReturn(Optional.empty()).when(groupRepository).findByGroupName(groupName);
        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        this.service.updateGroup(groupId, updateGroupRequest, email);

        verify(groupRepository).save(this.groupArgCaptor.capture());

        Group group = this.groupArgCaptor.getValue();
        assertThat(group.getGroupName()).isEqualTo(updateGroupRequest.newGroupName());
        assertThat(group.getCreator()).isEqualTo(user);
        assertThat(group.getUsers()).contains(user);
    }

    @Test
    void test_updateGroupNameTaken() {
        final UpdateGroupRequest updateGroupRequest = new UpdateGroupRequest(groupName);

        doReturn(Optional.of(group)).when(groupRepository).findByGroupName(groupName);
        GetGroupResponse response = this.service.updateGroup(groupId, updateGroupRequest, email);

        verify(groupRepository, times(0)).save(any(Group.class));
        verify(userRepository, times(0)).findByEmail(email);
        verify(groupRepository, times(0)).findById(groupId);
        verify(authorityRepository, times(0)).findAuthoritiesByUserAndGroup(user, group);
        assertThat(response.groupId()).isZero();
    }

    @Test
    void test_updateGroupNoRequiredAuthority() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final UpdateGroupRequest updateGroupRequest = new UpdateGroupRequest(groupName);

        doReturn(Optional.empty()).when(groupRepository).findByGroupName(groupName);
        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);

        assertThatThrownBy(() -> this.service.updateGroup(groupId, updateGroupRequest, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class);
    }

    @Test
    void test_deleteGroupSuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        this.service.deleteGroup(groupId, email);

        verify(authorityRepository).deleteByGroup(this.groupArgCaptor.capture());
        verify(groupRepository).delete(group);
        Group deletedGroup = this.groupArgCaptor.getValue();
        assertThat(deletedGroup).isEqualTo(group);
    }

    @Test
    void test_deleteGroupNoRequiredAuthority() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);

        assertThatThrownBy(() -> this.service.deleteGroup(groupId, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class);
    }

    @Test
    void test_addUserToGroupSuccessful() {
        authority.setAuthorityName(AuthorityEnum.ADD_TO_GROUP);
        final String usernameToAdd = "userToAdd";
        final User userToAdd = User.builder().id(2L).username(usernameToAdd).build();

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToAdd)).when(userRepository).findByUsername(usernameToAdd);
        this.service.addUserToGroup(groupId, usernameToAdd, email);

        verify(groupRepository).save(group);
        verify(authorityRepository).saveAll(this.authoritiesListArgCaptor.capture());
        Set<Authority> authorities = this.authoritiesListArgCaptor.getValue();
        assertThat(authorities).hasSize(AuthorityEnum.BASIC_AUTHORITIES.size());
    }

    @Test
    void test_addUserToGroupUserNotFound() {
        authority.setAuthorityName(AuthorityEnum.ADD_TO_GROUP);
        final String usernameToAdd = "userToAdd";

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.empty()).when(userRepository).findByUsername(usernameToAdd);

        assertThatThrownBy(() -> this.service.addUserToGroup(groupId, usernameToAdd, email))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(groupRepository, times(0)).save(group);
        verify(authorityRepository, times(0)).saveAll(this.authoritiesListArgCaptor.capture());
    }

    @Test
    void test_addUserToGroupUserAlreadyAdded() {
        authority.setAuthorityName(AuthorityEnum.ADD_TO_GROUP);
        final String usernameToAdd = "userToAdd";
        final User userToAdd = User.builder().id(2L).username(usernameToAdd).build();
        group.getUsers().add(userToAdd);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToAdd)).when(userRepository).findByUsername(usernameToAdd);

        assertThatThrownBy(() -> this.service.addUserToGroup(groupId, usernameToAdd, email))
                .isInstanceOf(UserAlreadyAddedToGroupException.class);
        verify(groupRepository, times(0)).save(group);
        verify(authorityRepository, times(0)).saveAll(this.authoritiesListArgCaptor.capture());
    }

    @Test
    void test_removeUserFromGroupSuccessful() {
        final String usernameToRemove = "userToRemove";
        final long userIdToRemove = 2L;
        final User userToRemove = User.builder().id(userIdToRemove).username(usernameToRemove).build();
        group.getUsers().add(userToRemove);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToRemove)).when(userRepository).findById(userIdToRemove);
        this.service.removeUserFromGroup(groupId, userIdToRemove, email);

        verify(groupRepository).save(group);
        verify(authorityRepository).deleteByUserAndGroup(userToRemove, group);
        assertThat(group.getUsers()).doesNotContain(userToRemove);
    }

    @Test
    void test_removeUserFromGroupUserRemovingHimself() {
        final String newCreatorName = "creator";
        final long newCreatorId = 2L;
        final User newCreator = User.builder().id(newCreatorId).username(newCreatorName).build();
        group.getUsers().add(newCreator);
        group.setCreator(newCreator);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        this.service.removeUserFromGroup(groupId, id, email);

        verify(groupRepository).save(group);
        verify(authorityRepository).deleteByUserAndGroup(user, group);
        assertThat(group.getUsers()).doesNotContain(user);
    }

    @Test
    void test_removeUserFromGroupNoRequiredAuthority() {
        authority.setAuthorityName(AuthorityEnum.ADD_TO_GROUP);
        final String usernameToRemove = "userToRemove";
        final long userIdToRemove = 2L;
        final User userToRemove = User.builder().id(userIdToRemove).username(usernameToRemove).build();
        group.getUsers().add(userToRemove);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.removeUserFromGroup(groupId, userIdToRemove, email));
        assertEquals("You have no permissions to do that", ex.getMessage());
        verify(groupRepository, times(0)).save(group);
        verify(authorityRepository, times(0)).deleteByUserAndGroup(userToRemove, group);
        assertTrue(group.getUsers().contains(userToRemove));
    }

    @Test
    void test_removeUserFromGroupUserToRemoveNotFound() {
        final long userIdToRemove = 2L;

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.empty()).when(userRepository).findById(userIdToRemove);

        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                this.service.removeUserFromGroup(groupId, userIdToRemove, email));
        assertEquals("User tried to remove non existing user from group", ex.getMessage());
        verify(groupRepository, times(0)).save(group);
        verify(authorityRepository, times(0)).deleteByUserAndGroup(any(User.class), eq(group));
    }

    @Test
    void test_removeUserFromGroupUserToRemoveIsNotAGroupMember() {
        final String usernameToRemove = "userToRemove";
        final long userIdToRemove = 2L;
        final User userToRemove = User.builder().id(userIdToRemove).username(usernameToRemove).build();

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToRemove)).when(userRepository).findById(userIdToRemove);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.removeUserFromGroup(groupId, userIdToRemove, email));
        assertEquals("You tried to remove user which is not in the group", ex.getMessage());
        verify(groupRepository, times(0)).save(group);
        verify(authorityRepository, times(0)).deleteByUserAndGroup(any(User.class), eq(group));
    }

    @Test
    void test_removeUserFromGroupRemovingCreator() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.removeUserFromGroup(groupId, id, email));
        assertEquals("You tried to remove group's creator from the group", ex.getMessage());
        verify(groupRepository, times(0)).save(group);
        verify(authorityRepository, times(0)).deleteByUserAndGroup(any(User.class), eq(group));
    }

    @Test
    void test_assignAuthoritiesToUserSuccessful() {
        final String usernameToAssignAuthorities = "userName";
        final long userIdToAssignAuthorities = 2L;
        final User userToAssignAuthorities = User.builder()
                .id(userIdToAssignAuthorities).username(usernameToAssignAuthorities).authorities(Collections.emptySet()).build();
        group.getUsers().add(userToAssignAuthorities);
        UserWithAuthoritiesRequest request = new UserWithAuthoritiesRequest(userIdToAssignAuthorities, Set.of(AuthorityEnum.ADD_TO_GROUP));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToAssignAuthorities)).when(userRepository).findById(userIdToAssignAuthorities);
        AssignAuthoritiesToUserResponse response = this.service.assignAuthoritiesToUser(groupId, request, email);

        verify(authorityRepository).saveAll(any());
        assertEquals(request.authorities().size(), response.assignedAuthorities().size());
        List<AuthorityDTO> authorities = new ArrayList<>(response.assignedAuthorities());
        assertEquals(group.getId(), authorities.get(0).groupId());
        assertEquals(AuthorityEnum.ADD_TO_GROUP, authorities.get(0).authority());
    }

    @Test
    void test_assignAuthoritiesToUserSuccessfulForLoggedUser() {
        UserWithAuthoritiesRequest request = new UserWithAuthoritiesRequest(user.getId(), Set.of(AuthorityEnum.ADD_TO_GROUP));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        AssignAuthoritiesToUserResponse response = this.service.assignAuthoritiesToUser(groupId, request, email);

        verify(authorityRepository).saveAll(any());
        assertEquals(request.authorities().size(), response.assignedAuthorities().size());
        List<AuthorityDTO> authorities = new ArrayList<>(response.assignedAuthorities());
        assertEquals(group.getId(), authorities.get(0).groupId());
        assertEquals(AuthorityEnum.ADD_TO_GROUP, authorities.get(0).authority());
    }

    @Test
    void test_assignAuthoritiesToUserNotFoundUser() {
        final long userIdToAssignAuthorities = 2L;
        UserWithAuthoritiesRequest request = new UserWithAuthoritiesRequest(userIdToAssignAuthorities, Set.of(AuthorityEnum.ADD_TO_GROUP));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.empty()).when(userRepository).findById(userIdToAssignAuthorities);

        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                this.service.assignAuthoritiesToUser(groupId, request, email));
        assertEquals("You tried to assign authorities to non existing user", ex.getMessage());
        verify(authorityRepository, times(0)).saveAll(anyList());
    }

    @Test
    void test_assignAuthoritiesToUserWhichIsNotAGroupMember() {
        final String usernameToAssignAuthorities = "userName";
        final long userIdToAssignAuthorities = 2L;
        final User userToAssignAuthorities = User.builder()
                .id(userIdToAssignAuthorities).username(usernameToAssignAuthorities).authorities(Collections.emptySet()).build();
        UserWithAuthoritiesRequest request = new UserWithAuthoritiesRequest(userIdToAssignAuthorities, Set.of(AuthorityEnum.ADD_TO_GROUP));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToAssignAuthorities)).when(userRepository).findById(userIdToAssignAuthorities);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.assignAuthoritiesToUser(groupId, request, email));
        assertEquals("You tried to assign authorities to user which is not in the group", ex.getMessage());
        verify(authorityRepository, times(0)).saveAll(anyList());
    }

    @Test
    void test_removeAuthoritiesFromUserSuccessful() {
        final String usernameToRemoveAuthorities = "userName";
        final long userIdToRemoveAuthorities = 2L;
        final User userToRemoveAuthorities = User.builder()
                .id(userIdToRemoveAuthorities).username(usernameToRemoveAuthorities).authorities(Collections.emptySet()).build();
        final Authority authority1 = Authority.builder()
                .authorityName(AuthorityEnum.ADD_TO_GROUP).group(group).user(userToRemoveAuthorities).build();
        userToRemoveAuthorities.setAuthorities(Set.of(authority1));
        group.getUsers().add(userToRemoveAuthorities);
        final UserWithAuthoritiesRequest request =
                new UserWithAuthoritiesRequest(userIdToRemoveAuthorities, Set.of(AuthorityEnum.ADD_TO_GROUP));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToRemoveAuthorities)).when(userRepository).findById(userIdToRemoveAuthorities);
        this.service.removeAuthoritiesFromUser(groupId, request, email);

        verify(authorityRepository).deleteAll(this.authoritiesListArgCaptor.capture());
        Set<Authority> removedAutorities = this.authoritiesListArgCaptor.getValue();
        assertEquals(request.authorities().size(), removedAutorities.size());
        assertTrue(removedAutorities.contains(authority1));
    }

    @Test
    void test_removeAuthoritiesFromUserSuccessfulNoAuthoritiesRemoved() {
        final String usernameToRemoveAuthorities = "userName";
        final long userIdToRemoveAuthorities = 2L;
        final User userToRemoveAuthorities = User.builder()
                .id(userIdToRemoveAuthorities).username(usernameToRemoveAuthorities).authorities(Collections.emptySet()).build();
        final Authority authority1 = Authority.builder()
                .authorityName(AuthorityEnum.ADD_TO_GROUP).group(new Group()).user(userToRemoveAuthorities).build();
        userToRemoveAuthorities.setAuthorities(Set.of(authority1));
        group.getUsers().add(userToRemoveAuthorities);
        final UserWithAuthoritiesRequest request =
                new UserWithAuthoritiesRequest(userIdToRemoveAuthorities, Set.of(AuthorityEnum.ADD_TO_GROUP));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToRemoveAuthorities)).when(userRepository).findById(userIdToRemoveAuthorities);
        this.service.removeAuthoritiesFromUser(groupId, request, email);

        verify(authorityRepository, times(0)).deleteAll(anyList());
    }

    @Test
    void test_removeAuthoritiesFromUserSuccessfulForLoggedUser() {
        final UserWithAuthoritiesRequest request =
                new UserWithAuthoritiesRequest(user.getId(), Set.of(AuthorityEnum.MODIFY_GROUP));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        this.service.removeAuthoritiesFromUser(groupId, request, email);

        verify(authorityRepository).deleteAll(this.authoritiesListArgCaptor.capture());
        Set<Authority> removedAutorities = this.authoritiesListArgCaptor.getValue();
        assertThat(removedAutorities).hasSize(request.authorities().size());
        assertThat(removedAutorities).contains(authority);
    }

    @Test
    void test_removeAuthoritiesFromUserNotFoundUser() {
        final String usernameToRemoveAuthorities = "userName";
        final long userIdToRemoveAuthorities = 2L;
        final User userToRemoveAuthorities = User.builder()
                .id(userIdToRemoveAuthorities).username(usernameToRemoveAuthorities).authorities(Collections.emptySet()).build();
        final Authority authority1 = Authority.builder()
                .authorityName(AuthorityEnum.ADD_TO_GROUP).group(group).user(userToRemoveAuthorities).build();
        userToRemoveAuthorities.setAuthorities(Set.of(authority1));
        final UserWithAuthoritiesRequest request =
                new UserWithAuthoritiesRequest(userIdToRemoveAuthorities, Set.of(AuthorityEnum.ADD_TO_GROUP));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.empty()).when(userRepository).findById(userIdToRemoveAuthorities);

        assertThatThrownBy(() -> this.service.removeAuthoritiesFromUser(groupId, request, email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("You tried to take away authorities from non existing user");
        verify(authorityRepository, times(0)).deleteAll(anyList());
    }

    @Test
    void test_removeAuthoritiesFromUserWhichIsNotGroupMember() {
        final String usernameToRemoveAuthorities = "userName";
        final long userIdToRemoveAuthorities = 2L;
        final User userToRemoveAuthorities = User.builder()
                .id(userIdToRemoveAuthorities).username(usernameToRemoveAuthorities).authorities(Collections.emptySet()).build();
        final Authority authority1 = Authority.builder()
                .authorityName(AuthorityEnum.ADD_TO_GROUP).group(group).user(userToRemoveAuthorities).build();
        userToRemoveAuthorities.setAuthorities(Set.of(authority1));
        final UserWithAuthoritiesRequest request =
                new UserWithAuthoritiesRequest(userIdToRemoveAuthorities, Set.of(AuthorityEnum.ADD_TO_GROUP));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(Set.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToRemoveAuthorities)).when(userRepository).findById(userIdToRemoveAuthorities);

        assertThatThrownBy(() -> this.service.removeAuthoritiesFromUser(groupId, request, email))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("You tried to take away authorities from user which is not in the group");
        verify(authorityRepository, times(0)).deleteAll(anyList());
    }
}

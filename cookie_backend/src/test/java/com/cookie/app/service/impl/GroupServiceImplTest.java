package com.cookie.app.service.impl;

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
import com.cookie.app.model.response.GroupNameTakenResponse;
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
    ArgumentCaptor<List<Authority>> authoritiesListArgCaptor;

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
        GroupNameTakenResponse response = this.service.createGroup(createGroupRequest, email);

        verify(groupRepository).save(this.groupArgCaptor.capture());
        verify(authorityRepository).saveAll(this.authoritiesListArgCaptor.capture());
        assertFalse(response.groupNameTaken());

        Group group = this.groupArgCaptor.getValue();
        assertEquals(groupName, group.getGroupName());
        assertEquals(user, group.getCreator());
        assertTrue(group.getUsers().contains(user));
        assertNull(group.getPantry());
        assertNull(group.getMeals());
        assertNull(group.getShoppingLists());
        List<Authority> authorities = this.authoritiesListArgCaptor.getValue();
        assertEquals(AuthorityEnum.values().length, authorities.size());
    }

    @Test
    void test_createGroupNameTaken() {
        final CreateGroupRequest createGroupRequest = new CreateGroupRequest(groupName);

        doReturn(Optional.of(group)).when(groupRepository).findByGroupName(groupName);
        GroupNameTakenResponse response = this.service.createGroup(createGroupRequest, email);

        verify(groupRepository, times(0)).save(any(Group.class));
        verify(authorityRepository, times(0)).saveAll(anyList());
        verify(userRepository, times(0)).findByEmail(email);
        assertTrue(response.groupNameTaken());
    }

    @Test
    void test_getGroupDetailsSuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        GroupDetailsDTO response = this.service.getGroupDetails(groupId, email);

        assertEquals(group.getId(), response.id());
        assertEquals(group.getGroupName(), response.groupName());
        assertEquals(group.getCreator().getId(), response.creator().id());
        assertEquals(group.getCreator().getUsername(), response.creator().username());
        assertEquals(group.getUsers().size(), response.users().size());
        assertEquals(0L, response.pantryId());
        assertEquals("", response.pantryName());
        assertEquals(group.getShoppingLists().size(), response.shoppingLists().size());
        assertEquals(group.getShoppingLists().get(0).getId(), response.shoppingLists().get(0).listId());
        assertEquals(group.getShoppingLists().get(0).getListName(), response.shoppingLists().get(0).listName());
    }

    @Test
    void test_getGroupDetailsUserIsNotAGroupMember() {
        group.setUsers(Collections.emptyList());
        group.setCreator(new User());

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.service.getGroupDetails(groupId, email));
    }

    @Test
    void test_getGroupDetailsGroupDoesNotExist() {
        group.setUsers(Collections.emptyList());
        group.setCreator(new User());

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.empty()).when(groupRepository).findById(groupId);

       assertThrows(UserPerformedForbiddenActionException.class, () -> this.service.getGroupDetails(groupId, email));
    }

    @Test
    void test_getUserGroupsSuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetUserGroupsResponse response = this.service.getUserGroups(email);

        assertEquals(user.getGroups().size(), response.userGroups().size());
        assertEquals(user.getGroups().get(0).getId(), response.userGroups().get(0).id());
        assertEquals(user.getGroups().get(0).getGroupName(), response.userGroups().get(0).groupName());
        assertEquals(user.getGroups().get(0).getCreator().getId(), response.userGroups().get(0).creator().id());
        assertEquals(user.getGroups().get(0).getCreator().getUsername(), response.userGroups().get(0).creator().username());
        assertEquals(user.getGroups().get(0).getUsers().size(), response.userGroups().get(0).users());
        assertEquals(0L, response.userGroups().get(0).pantryId());
    }

    @Test
    void test_getUserGroupsNoGroups() {
        user.setGroups(Collections.emptyList());

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        GetUserGroupsResponse response = this.service.getUserGroups(email);

        assertTrue(response.userGroups().isEmpty());
    }

    @Test
    void test_updateGroupSuccessful() {
        final UpdateGroupRequest updateGroupRequest = new UpdateGroupRequest(groupName);

        doReturn(Optional.empty()).when(groupRepository).findByGroupName(groupName);
        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        GroupNameTakenResponse response = this.service.updateGroup(groupId, updateGroupRequest, email);

        verify(groupRepository).save(this.groupArgCaptor.capture());
        assertFalse(response.groupNameTaken());

        Group group = this.groupArgCaptor.getValue();
        assertEquals(updateGroupRequest.newGroupName(), group.getGroupName());
        assertEquals(user, group.getCreator());
        assertTrue(group.getUsers().contains(user));
    }

    @Test
    void test_updateGroupNameTaken() {
        final UpdateGroupRequest updateGroupRequest = new UpdateGroupRequest(groupName);

        doReturn(Optional.of(group)).when(groupRepository).findByGroupName(groupName);
        GroupNameTakenResponse response = this.service.updateGroup(groupId, updateGroupRequest, email);

        verify(groupRepository, times(0)).save(any(Group.class));
        verify(userRepository, times(0)).findByEmail(email);
        verify(groupRepository, times(0)).findById(groupId);
        verify(authorityRepository, times(0)).findAuthoritiesByUserAndGroup(user, group);
        assertTrue(response.groupNameTaken());
    }

    @Test
    void test_updateGroupNoRequiredAuthority() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);
        final UpdateGroupRequest updateGroupRequest = new UpdateGroupRequest(groupName);

        doReturn(Optional.empty()).when(groupRepository).findByGroupName(groupName);
        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.service.updateGroup(groupId, updateGroupRequest, email));
    }

    @Test
    void test_deleteGroupSuccessful() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        this.service.deleteGroup(groupId, email);

        verify(authorityRepository).deleteByGroup(this.groupArgCaptor.capture());
        verify(groupRepository).delete(group);
        Group deletedGroup = this.groupArgCaptor.getValue();
        assertEquals(group, deletedGroup);
    }

    @Test
    void test_deleteGroupNoRequiredAuthority() {
        authority.setAuthorityName(AuthorityEnum.MODIFY);

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.service.deleteGroup(groupId, email));
    }

    @Test
    void test_addUserToGroupSuccessful() {
        authority.setAuthorityName(AuthorityEnum.ADD_TO_GROUP);
        final String usernameToAdd = "userToAdd";
        final User userToAdd = User.builder().id(2L).username(usernameToAdd).build();

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToAdd)).when(userRepository).findByUsername(usernameToAdd);
        this.service.addUserToGroup(groupId, usernameToAdd, email);

        verify(groupRepository).save(group);
        verify(authorityRepository).saveAll(this.authoritiesListArgCaptor.capture());
        List<Authority> authorities = this.authoritiesListArgCaptor.getValue();
        assertEquals(AuthorityEnum.BASIC_AUTHORITIES.size(), authorities.size());
        assertEquals(group, authorities.get(0).getGroup());
        assertEquals(userToAdd.getId(), authorities.get(0).getUser().getId());
        assertEquals(userToAdd.getUsername(), authorities.get(0).getUser().getUsername());
    }

    @Test
    void test_addUserToGroupUserNotFound() {
        authority.setAuthorityName(AuthorityEnum.ADD_TO_GROUP);
        final String usernameToAdd = "userToAdd";

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.empty()).when(userRepository).findByUsername(usernameToAdd);

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.service.addUserToGroup(groupId, usernameToAdd, email));
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
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToAdd)).when(userRepository).findByUsername(usernameToAdd);

        assertThrows(UserAlreadyAddedToGroupException.class, () -> this.service.addUserToGroup(groupId, usernameToAdd, email));
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
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToRemove)).when(userRepository).findById(userIdToRemove);
        this.service.removeUserFromGroup(groupId, userIdToRemove, email);

        verify(groupRepository).save(group);
        verify(authorityRepository).deleteByUserAndGroup(userToRemove, group);
        assertFalse(group.getUsers().contains(userToRemove));
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
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        this.service.removeUserFromGroup(groupId, id, email);

        verify(groupRepository).save(group);
        verify(authorityRepository).deleteByUserAndGroup(user, group);
        assertFalse(group.getUsers().contains(user));
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
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);

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
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.empty()).when(userRepository).findById(userIdToRemove);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.removeUserFromGroup(groupId, userIdToRemove, email));
        assertEquals("User tried to remove non existing creator from group", ex.getMessage());
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
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToRemove)).when(userRepository).findById(userIdToRemove);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.removeUserFromGroup(groupId, userIdToRemove, email));
        assertEquals("You tried to remove creator which is not in the group", ex.getMessage());
        verify(groupRepository, times(0)).save(group);
        verify(authorityRepository, times(0)).deleteByUserAndGroup(any(User.class), eq(group));
    }

    @Test
    void test_removeUserFromGroupRemovingCreator() {

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);

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
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToAssignAuthorities)).when(userRepository).findById(userIdToAssignAuthorities);
        AssignAuthoritiesToUserResponse response = this.service.assignAuthoritiesToUser(groupId, request, email);

        verify(authorityRepository).saveAll(anyList());
        assertEquals(request.authorities().size(), response.assignedAuthorities().size());
        List<AuthorityDTO> authorities = new ArrayList<>(response.assignedAuthorities());
        assertEquals(group.getId(), authorities.get(0).groupId());
        assertEquals(userToAssignAuthorities.getId(), authorities.get(0).userId());
        assertEquals(AuthorityEnum.ADD_TO_GROUP, authorities.get(0).authority());
    }

    @Test
    void test_assignAuthoritiesToUserSuccessfulForLoggedUser() {
        UserWithAuthoritiesRequest request = new UserWithAuthoritiesRequest(user.getId(), Set.of(AuthorityEnum.ADD_TO_GROUP));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        AssignAuthoritiesToUserResponse response = this.service.assignAuthoritiesToUser(groupId, request, email);

        verify(authorityRepository).saveAll(anyList());
        assertEquals(request.authorities().size(), response.assignedAuthorities().size());
        List<AuthorityDTO> authorities = new ArrayList<>(response.assignedAuthorities());
        assertEquals(group.getId(), authorities.get(0).groupId());
        assertEquals(user.getId(), authorities.get(0).userId());
        assertEquals(AuthorityEnum.ADD_TO_GROUP, authorities.get(0).authority());
    }

    @Test
    void test_assignAuthoritiesToUserNotFoundUser() {
        final long userIdToAssignAuthorities = 2L;
        UserWithAuthoritiesRequest request = new UserWithAuthoritiesRequest(userIdToAssignAuthorities, Set.of(AuthorityEnum.ADD_TO_GROUP));

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        doReturn(Optional.of(group)).when(groupRepository).findById(groupId);
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.empty()).when(userRepository).findById(userIdToAssignAuthorities);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.assignAuthoritiesToUser(groupId, request, email));
        assertEquals("You tried to assign authorities to non existing creator", ex.getMessage());
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
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToAssignAuthorities)).when(userRepository).findById(userIdToAssignAuthorities);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.assignAuthoritiesToUser(groupId, request, email));
        assertEquals("You tried to assign authorities to creator which is not in the group", ex.getMessage());
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
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToRemoveAuthorities)).when(userRepository).findById(userIdToRemoveAuthorities);
        this.service.removeAuthoritiesFromUser(groupId, request, email);

        verify(authorityRepository).deleteAll(this.authoritiesListArgCaptor.capture());
        List<Authority> removedAutorities = this.authoritiesListArgCaptor.getValue();
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
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
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
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        this.service.removeAuthoritiesFromUser(groupId, request, email);

        verify(authorityRepository).deleteAll(this.authoritiesListArgCaptor.capture());
        List<Authority> removedAutorities = this.authoritiesListArgCaptor.getValue();
        assertEquals(request.authorities().size(), removedAutorities.size());
        assertTrue(removedAutorities.contains(authority));
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
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.empty()).when(userRepository).findById(userIdToRemoveAuthorities);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.removeAuthoritiesFromUser(groupId, request, email));
        assertEquals("You tried to take away authorities from non existing creator", ex.getMessage());
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
        doReturn(List.of(authority)).when(authorityRepository).findAuthoritiesByUserAndGroup(user, group);
        doReturn(Optional.of(userToRemoveAuthorities)).when(userRepository).findById(userIdToRemoveAuthorities);

        Exception ex = assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.service.removeAuthoritiesFromUser(groupId, request, email));
        assertEquals("You tried to take away authorities from creator which is not in the group", ex.getMessage());
        verify(authorityRepository, times(0)).deleteAll(anyList());
    }
}

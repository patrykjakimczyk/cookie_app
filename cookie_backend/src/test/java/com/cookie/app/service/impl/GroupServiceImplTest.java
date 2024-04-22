package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.model.dto.GroupDTO;
import com.cookie.app.model.dto.GroupDetailsDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.*;
import com.cookie.app.model.request.CreateGroupRequest;
import com.cookie.app.model.request.UpdateGroupRequest;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {
    final String groupName = "groupName";
    final String email = "email";
    final long groupId = 1L;
    final long id = 1L;

    @Spy
    GroupMapperDTO groupMapperDTO = new GroupMapperDTO(new UserMapperDTO(new AuthorityMapperDTO()));
    @Spy
    GroupDetailsMapperDTO groupDetailsMapperDTO = new GroupDetailsMapperDTO(
            new UserMapperDTO(new AuthorityMapperDTO()),
            new GroupDetailsShoppingListMapperDTO()
    );
    @Spy
    AuthorityMapperDTO authorityMapperDTO;
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

    @BeforeEach
    void init() {
        user = User.builder()
                .id(id)
                .email(email)
                .authorities(Collections.emptySet())
                .build();
        group = Group.builder()
                .id(id)
                .groupName(groupName)
                .users(Collections.singletonList(user))
                .shoppingLists(Collections.emptyList())
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
        assertEquals(AuthorityEnum.ALL_AUTHORITIES.size(), authorities.size());
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
        assertTrue(response.shoppingLists().isEmpty());
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

}

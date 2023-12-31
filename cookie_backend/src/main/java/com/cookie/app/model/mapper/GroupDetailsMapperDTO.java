package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.GroupDetailsDTO;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GroupDetailsMapperDTO implements Function<Group, GroupDetailsDTO> {
    private final UserMapperDTO userMapperDTO;
    private final GroupDetailsShoppingListMapperDTO groupDetailsShoppingListMapperDTO;

    @Override
    public GroupDetailsDTO apply(Group group) {
        User creator = group.getCreator();
        this.extractAuthoritiesOnlyForGroup(creator, group);

        return new GroupDetailsDTO(
                group.getId(),
                group.getGroupName(),
                userMapperDTO.apply(creator),
                group.getUsers()
                        .stream()
                        .map(user -> {
                            this.extractAuthoritiesOnlyForGroup(user, group);

                            return userMapperDTO.apply(user);
                        })
                        .toList(),
                group.getPantry() != null ? group.getPantry().getId() : 0L,
                group.getPantry() != null ? group.getPantry().getPantryName() : "",
                group.getShoppingLists()
                        .stream()
                        .map(this.groupDetailsShoppingListMapperDTO::apply)
                        .toList()
        );
    }

    public void extractAuthoritiesOnlyForGroup(User user, Group group) {
        user.setAuthorities(
                user.getAuthorities()
                        .stream()
                        .filter(authority -> authority.getGroup().getId() == group.getId())
                        .collect(Collectors.toSet())
        );
    }
}

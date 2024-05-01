package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.UserDTO;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {AuthorityMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {

    UserDTO mapToDto(User entity);

    @Named("creator")
    default UserDTO mapToCreator(Group group) {
        group.getCreator().setAuthorities(null);

        return mapToDto(group.getCreator());
    }

    @Named("groupUsers")
    default List<UserDTO> mapToGroupUsersDto(Group group) {
        return group.getUsers().stream()
                .map(user -> {
                    extractAuthoritiesOnlyForGroup(user, group);

                    return mapToDto(user);
                })
                .toList();
    }

    default void extractAuthoritiesOnlyForGroup(User user, Group group) {
        user.setAuthorities(
                user.getAuthorities()
                        .stream()
                        .filter(authority -> authority.getGroup().getId() == group.getId())
                        .collect(Collectors.toSet())
        );
    }
}

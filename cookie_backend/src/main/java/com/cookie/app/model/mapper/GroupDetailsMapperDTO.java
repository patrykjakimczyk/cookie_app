package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.GroupDetailsDTO;
import com.cookie.app.model.entity.Group;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GroupDetailsMapperDTO implements Function<Group, GroupDetailsDTO> {
    private final UserMapperDTO userMapperDTO;
    @Override
    public GroupDetailsDTO apply(Group group) {
        return new GroupDetailsDTO(
                group.getId(),
                group.getGroupName(),
                userMapperDTO.apply(group.getCreator()),
                group.getUsers()
                        .stream()
                        .map(user -> {
                            user.setAuthorities(
                                    user.getAuthorities()
                                            .stream()
                                            .filter(authority -> authority.getGroup().getId() == group.getId())
                                            .collect(Collectors.toSet())
                            );

                            return userMapperDTO.apply(user);
                        })
                        .toList()
        );
    }
}

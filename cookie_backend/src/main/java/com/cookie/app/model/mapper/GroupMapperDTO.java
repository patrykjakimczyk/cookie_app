package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.GroupDTO;
import com.cookie.app.model.entity.Group;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class GroupMapperDTO implements Function<Group, GroupDTO> {
    private final UserMapperDTO userMapperDTO;

    @Override
    public GroupDTO apply(Group group) {
        group.getCreator().setAuthorities(null);

        return new GroupDTO(
                group.getId(),
                group.getGroupName(),
                userMapperDTO.apply(group.getCreator()),
                group.getUsers().size()
        );
    }
}

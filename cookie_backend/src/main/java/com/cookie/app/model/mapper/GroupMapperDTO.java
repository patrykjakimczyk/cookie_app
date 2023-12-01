package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.GroupDTO;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.PantryProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class GroupMapperDTO implements Function<Group, GroupDTO> {
    private final UserMapperDTO userMapperDTO;
    @Override
    public GroupDTO apply(Group group) {
        return new GroupDTO(
                group.getId(),
                group.getGroupName(),
                userMapperDTO.apply(group.getCreator()),
                group.getUsers()
                        .stream()
                        .map(userMapperDTO::apply)
                        .toList()
        );
    }
}

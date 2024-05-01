package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.GroupDTO;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.Pantry;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {UserMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface GroupMapper {

    @Mapping(source = "entity", target = "creator", qualifiedByName = "creator")
    @Mapping(source = "entity", target = "users", qualifiedByName = "groupSizeMap")
    @Mapping(source = "entity.pantry", target = "pantryId", qualifiedByName = "pantryIdMap")
    GroupDTO mapToDto(Group entity);

    @Named("groupSizeMap")
    default int groupSizeMap(Group group) {
        return group.getUsers().size();
    }

    @Named("pantryIdMap")
    default long pantryIdMap(Pantry pantry) {
        return pantry != null ? pantry.getId() : 0L;
    }
}

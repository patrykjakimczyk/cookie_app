package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.GroupDetailsDTO;
import com.cookie.app.model.entity.Group;

import com.cookie.app.model.entity.Pantry;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
        componentModel = "spring", uses = {UserMapper.class, GroupDetailsShoppingListMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface GroupDetailsMapper {

    @Mapping(source = "entity", target = "users", qualifiedByName = "groupUsers")
    @Mapping(source = "entity.pantry", target = "pantryId", qualifiedByName = "pantryIdMap")
    @Mapping(source = "entity.pantry", target = "pantryName", qualifiedByName = "pantryNameMap")
    GroupDetailsDTO mapToDto(Group entity);

    @Named("pantryNameMap")
    default String pantryNameMap(Pantry pantry) {
        return pantry != null ? pantry.getPantryName() : "";
    }

    @Named("pantryIdMap")
    default long pantryIdMap(Pantry pantry) {
        return pantry != null ? pantry.getId() : 0L;
    }
}

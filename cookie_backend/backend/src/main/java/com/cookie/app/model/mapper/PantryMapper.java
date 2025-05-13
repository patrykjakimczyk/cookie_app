package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.PantryDTO;
import com.cookie.app.model.entity.Pantry;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PantryMapper {

    @Mapping(source = "entity.id", target = "pantryId")
    @Mapping(source = "entity", target = "nrOfProducts", qualifiedByName = "nrOfProducts")
    @Mapping(source = "entity.group.id", target = "groupId")
    @Mapping(source = "entity.group.groupName", target = "groupName")
    PantryDTO mapToDto(Pantry entity);

    @Named("nrOfProducts")
    default int mapToNrOfProducts(Pantry pantry) {
        return pantry.getPantryProducts().size();
    }
}

package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.AuthorityDTO;
import com.cookie.app.model.entity.Authority;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface AuthorityMapper {

    @Mapping(source = "entity.authorityName", target = "authority")
    @Mapping(source = "entity.user.id", target = "userId")
    @Mapping(source = "entity.group.id", target = "groupId")
    AuthorityDTO mapToDto(Authority entity);

    Set<AuthorityDTO> mapToDtoSet(Set<Authority> entities);
}

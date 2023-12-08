package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.AuthorityDTO;
import com.cookie.app.model.entity.Authority;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class AuthorityMapperDTO implements Function<Authority, AuthorityDTO> {
    @Override
    public AuthorityDTO apply(Authority authority) {
        return new AuthorityDTO(
                authority.getAuthority(),
                authority.getUser().getId(),
                authority.getGroup().getId()
        );
    }
}

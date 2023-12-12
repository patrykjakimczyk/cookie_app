package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.UserDTO;
import com.cookie.app.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserMapperDTO implements Function<User, UserDTO> {
    private final AuthorityMapperDTO authorityMapperDTO;

    @Override
    public UserDTO apply(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getAuthorities() != null ?
                        user.getAuthorities()
                            .stream()
                            .map(authorityMapperDTO::apply)
                            .collect(Collectors.toSet()) : new HashSet<>()
        );
    }
}

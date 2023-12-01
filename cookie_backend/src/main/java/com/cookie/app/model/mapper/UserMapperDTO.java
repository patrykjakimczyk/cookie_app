package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.UserDTO;
import com.cookie.app.model.entity.User;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class UserMapperDTO implements Function<User, UserDTO> {
    @Override
    public UserDTO apply(User user) {
        return new UserDTO(user.getId(), user.getUsername());
    }
}

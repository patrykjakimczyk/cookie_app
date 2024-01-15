package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.Pantry;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public abstract class AbstractPantryService extends AbstractCookieService {

    protected AbstractPantryService(UserRepository userRepository,
                                    ProductRepository productRepository,
                                    AuthorityMapperDTO authorityMapperDTO) {
        super(userRepository, productRepository, authorityMapperDTO);
    }

    protected Pantry getPantryIfUserHasAuthority(long pantryId, String userEmail, AuthorityEnum requiredAuthority) {
        User user = this.getUserByEmail(userEmail);
        Pantry pantry = this.findPantryInUserGroups(pantryId, user).orElseThrow(
                () -> {
                    log.info("User: {} tried to access pantry without being a member of the pantry's group", userEmail);
                    return new UserPerformedForbiddenActionException("You cannot access the pantry because you are not member of its group");
                }
        );

        if (requiredAuthority!= null && !this.userHasAuthority(user, pantry.getGroup().getId(), requiredAuthority)) {
            log.info("User: {} tried to perform action in pantry without required permission", userEmail);
            throw new UserPerformedForbiddenActionException("You have not permissions to do that");
        }

        return pantry;
    }

    protected Optional<Pantry> findPantryInUserGroups(long pantryId, User user) {
        for (Group group : user.getGroups()) {
            if (group.getPantry() != null && group.getPantry().getId() == pantryId) {
                return Optional.of(group.getPantry());
            }
        }

        return Optional.empty();
    }
}

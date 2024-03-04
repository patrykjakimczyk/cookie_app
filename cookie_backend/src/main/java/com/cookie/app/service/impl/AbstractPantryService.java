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
        User user = super.getUserByEmail(userEmail);

        return getPantryIfUserHasAuthority(pantryId, user, requiredAuthority);
    }

    protected Pantry getPantryIfUserHasAuthority(long pantryId, User user, AuthorityEnum requiredAuthority) {
        Pantry pantry = findPantryInUserGroups(pantryId, user).orElseThrow(
                () -> {
                    log.info("User: {} tried to access pantry without being a member of the pantry's group", user.getEmail());
                    return new UserPerformedForbiddenActionException("You cannot access the pantry because you are not member of its group");
                }
        );

        if (requiredAuthority!= null && !super.userHasAuthority(user, pantry.getGroup().getId(), requiredAuthority)) {
            log.info("User: {} tried to perform action in pantry without required permission", user.getEmail());
            throw new UserPerformedForbiddenActionException("You have not permissions to do that");
        }

        return pantry;
    }

    protected Optional<Pantry> findPantryInUserGroups(long pantryId, User user) {
        return user.getGroups()
                .stream()
                .map(Group::getPantry)
                .filter(pantry -> pantry != null && pantry.getId() == pantryId)
                .findFirst();
    }
}

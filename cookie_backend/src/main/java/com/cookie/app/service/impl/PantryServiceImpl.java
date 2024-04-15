package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.Pantry;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.PantryMapperDTO;
import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.request.UpdatePantryRequest;
import com.cookie.app.model.response.DeletePantryResponse;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.model.response.GetUserPantriesResponse;
import com.cookie.app.repository.PantryRepository;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class PantryServiceImpl extends AbstractPantryService implements PantryService {
    private final PantryRepository pantryRepository;
    private final PantryMapperDTO pantryMapperDTO;

    public PantryServiceImpl(UserRepository userRepository,
                             ProductRepository productRepository,
                             AuthorityMapperDTO authorityMapperDTO,
                             PantryRepository pantryRepository,
                             PantryMapperDTO pantryMapperDTO) {
        super(userRepository, productRepository, authorityMapperDTO);
        this.pantryRepository = pantryRepository;
        this.pantryMapperDTO = pantryMapperDTO;
    }

    @Override
    public GetPantryResponse createPantry(CreatePantryRequest request, String userEmail) {
        User user = super.getUserByEmail(userEmail);
        Optional<Group> userGroupOptional = super.findUserGroupById(user, request.groupId());
        Group userGroup = userGroupOptional.orElseThrow(
                () -> new UserPerformedForbiddenActionException("You tried to create pantry for non existing group")
        );

        if (!super.userHasAuthority(user, userGroup.getId(), AuthorityEnum.MODIFY_PANTRY)) {
            log.info("User={} tried to create pantry without permission", userEmail);
            throw new UserPerformedForbiddenActionException("You tried to create pantry without permission");
        }

        Pantry pantry = Pantry
                .builder()
                .pantryName(request.pantryName())
                .group(userGroup)
                .build();

        this.pantryRepository.save(pantry);

        return createGetPantryResponse(pantry, user);
    }

    @Override
    public GetPantryResponse getPantry(long pantryId, String userEmail) {
        User user = super.getUserByEmail(userEmail);
        Optional<Pantry> pantryOptional = super.findPantryInUserGroups(pantryId, user);

        if (pantryOptional.isEmpty()) {
            return new GetPantryResponse(0, null, 0, null, null);
        }

        Pantry pantry = pantryOptional.get();

        return createGetPantryResponse(pantry, user);
    }

    @Override
    public GetUserPantriesResponse getAllUserPantries(String userEmail) {
        User user = super.getUserByEmail(userEmail);

        return new GetUserPantriesResponse(
                user.getGroups()
                        .stream()
                        .filter(group -> group.getPantry() != null)
                        .map(group -> this.pantryMapperDTO.apply(group.getPantry()))
                        .toList()
        );
    }

    @Override
    public DeletePantryResponse deletePantry(long pantryId, String userEmail) {
        Pantry pantry = super.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.MODIFY_PANTRY);

        this.pantryRepository.delete(pantry);

        return new DeletePantryResponse(pantry.getPantryName());
    }

    @Override
    public GetPantryResponse updatePantry(long pantryId, UpdatePantryRequest request, String userEmail) {
        User user = super.getUserByEmail(userEmail);
        Pantry pantry = super.getPantryIfUserHasAuthority(pantryId, user, AuthorityEnum.MODIFY_PANTRY);

        pantry.setPantryName(request.pantryName());
        this.pantryRepository.save(pantry);

        return createGetPantryResponse(pantry, user);
    }

    private GetPantryResponse createGetPantryResponse(Pantry pantry, User user) {
        return new GetPantryResponse(
                pantry.getId(),
                pantry.getPantryName(),
                pantry.getGroup().getId(),
                pantry.getGroup().getGroupName(),
                super.getAuthorityDTOsForSpecificGroup(user, pantry.getGroup())
        );
    }
}

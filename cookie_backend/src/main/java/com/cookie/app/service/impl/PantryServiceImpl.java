package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.model.dto.AuthorityDTO;
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
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
public class PantryServiceImpl extends AbstractCookieService implements PantryService{
    private final PantryRepository pantryRepository;
    private final PantryMapperDTO pantryMapperDTO;
    private final AuthorityMapperDTO authorityMapperDTO;

    public PantryServiceImpl(
            UserRepository userRepository,
            PantryRepository pantryRepository,
            PantryMapperDTO pantryMapperDTO,
            AuthorityMapperDTO authorityMapperDTO
    ) {
        super(userRepository);
        this.pantryRepository = pantryRepository;
        this.pantryMapperDTO = pantryMapperDTO;
        this.authorityMapperDTO = authorityMapperDTO;
    }

    @Override
    public GetPantryResponse createPantry(CreatePantryRequest request, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        Optional<Group> userGroupOptional = this.findUserGroupById(user, request.groupId());
        Group userGroup = userGroupOptional.orElseThrow(
                () -> new UserPerformedForbiddenActionException("You tried to create pantry for non existing group")
        );

        if (!this.userHasAuthority(user, userGroup.getId(), AuthorityEnum.MODIFY_PANTRY)) {
            log.info(String.format("User: %s tried to create pantry without permission", userEmail));
            throw new UserPerformedForbiddenActionException("You tried to create pantry without permission");
        }

        Pantry pantry = Pantry
                .builder()
                .pantryName(request.pantryName())
                .group(userGroup)
                .build();

        this.pantryRepository.save(pantry);

        return new GetPantryResponse(
                pantry.getId(),
                pantry.getPantryName(),
                this.getAuthorityDTOsForSpecificGroup(user, userGroup)
        );
    }

    @Override
    public GetPantryResponse getPantry(long pantryId, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        Optional<Pantry> pantryOptional = this.findPantryInUserGroups(pantryId, user);

        // przerobic na wyrzucanie pantrynotfoundexception
        if (pantryOptional.isEmpty()) {
            return new GetPantryResponse(null, null, null);
        }

        Pantry pantry = pantryOptional.get();

        return new GetPantryResponse(
                pantry.getId(),
                pantry.getPantryName(),
                this.getAuthorityDTOsForSpecificGroup(user, pantry.getGroup())
        );
    }

    @Override
    public GetUserPantriesResponse getAllUserPantries(String userEmail) {
        User user = this.getUserByEmail(userEmail);

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
        Pantry pantry = this.getPantryIfUserHasAuthority(pantryId, userEmail, null);

        this.pantryRepository.delete(pantry);

        return new DeletePantryResponse(pantry.getPantryName());
    }

    @Override
    public GetPantryResponse updatePantry(long pantryId, UpdatePantryRequest request, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        Pantry pantry = this.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.MODIFY_PANTRY);

        pantry.setPantryName(request.pantryName());
        this.pantryRepository.save(pantry);

        return new GetPantryResponse(
                pantry.getId(),
                pantry.getPantryName(),
                this.getAuthorityDTOsForSpecificGroup(user, pantry.getGroup())
        );
    }

    private Set<AuthorityDTO> getAuthorityDTOsForSpecificGroup(User user, Group userGroup) {
        return user.getAuthorities()
                .stream()
                .filter(authority -> authority.getGroup().getId() == userGroup.getId())
                .map(authorityMapperDTO::apply)
                .collect(Collectors.toSet());
    }
}

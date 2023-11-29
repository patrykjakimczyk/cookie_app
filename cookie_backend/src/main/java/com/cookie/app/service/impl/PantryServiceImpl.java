package com.cookie.app.service.impl;

import com.cookie.app.exception.PantryNotFoundException;
import com.cookie.app.exception.UserHasAssignedPantryException;
import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.exception.ValidationException;
import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.Pantry;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.request.UpdatePantryRequest;
import com.cookie.app.model.response.DeletePantryResponse;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.model.response.GetUserPantriesResponse;
import com.cookie.app.repository.PantryRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class PantryServiceImpl implements PantryService {
    private final UserRepository userRepository;
    private final PantryRepository pantryRepository;

    @Override
    public void createPantry(CreatePantryRequest request, String userEmail) {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (userOptional.isEmpty()) {
            throw new UserWasNotFoundAfterAuthException("User was not found in database after authentication");
        }

        User user = userOptional.get();

        if (user.getPantry() != null) {
            throw new UserHasAssignedPantryException("User already has an assigned pantry");
        }

        Pantry pantry = Pantry
                .builder()
                .pantryName(request.pantryName())
                .user(user)
                .build();

        this.pantryRepository.save(pantry);
    }

    @Override
    public GetPantryResponse getPantry(String userEmail) {
        Pantry pantry = this.getPantryForUser(userEmail);

        if (pantry == null) {
            return new GetPantryResponse(null, null);
        }

        return new GetPantryResponse(pantry.getId(), pantry.getPantryName());
    }

    @Override
    public DeletePantryResponse deletePantry(String userEmail) {
        Pantry pantry = this.getPantryForUser(userEmail);

        if (pantry == null) {
            throw new PantryNotFoundException("User cannot remove the pantry because it does not exist");
        }

        this.pantryRepository.delete(pantry);

        return new DeletePantryResponse(pantry.getPantryName());
    }

    @Override
    public GetPantryResponse updatePantry(UpdatePantryRequest request, String userEmail) {
        Pantry pantry = this.getPantryForUser(userEmail);

        if (pantry == null) {
            throw new PantryNotFoundException("User cannot update the pantry because it does not exist");
        }

        pantry.setPantryName(request.pantryName());
        this.pantryRepository.save(pantry);

        return new GetPantryResponse(pantry.getId(), pantry.getPantryName());
    }

    private Pantry getPantryForUser(String userEmail) {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (userOptional.isEmpty()) {
            throw new UserWasNotFoundAfterAuthException("User was not found in database after authentication");
        }

        return userOptional.get().getPantry();
    }
}

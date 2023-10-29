package com.cookie.app.service.impl;

import com.cookie.app.exception.UserHasAssignedPantryException;
import com.cookie.app.model.entity.Pantry;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.repository.PantryRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class PantryServiceImpl implements PantryService {
    private final UserRepository userRepository;
    private final PantryRepository pantryRepository;

    @Override
    public void createPantry(CreatePantryRequest request, String userEmail) {
        User user = this.userRepository.findByEmail(userEmail).get(); //user must be present if authentication passed

        if (user.getPantry() != null) {
            throw new UserHasAssignedPantryException("User already has an assigned pantry");
        }

        Pantry pantry = Pantry
                .builder()
                .pantryName(request.pantryName())
                .user(user)
                .build();

//        user.setPantry(pantry);
        this.pantryRepository.save(pantry);
    }
}

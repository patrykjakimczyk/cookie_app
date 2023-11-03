package com.cookie.app.service.impl;

import com.cookie.app.exception.PantryNotFoundException;
import com.cookie.app.exception.UserHasAssignedPantryException;
import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.entity.Pantry;
import com.cookie.app.model.entity.PantryProduct;
import com.cookie.app.model.entity.Product;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.model.response.PantryProductDTO;
import com.cookie.app.repository.PantryProductRepository;
import com.cookie.app.repository.PantryRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class PantryServiceImpl implements PantryService {
    private static final int PRODUCTS_PAGE_SIZE = 20;
    private final UserRepository userRepository;
    private final PantryRepository pantryRepository;
    private final PantryProductRepository pantryProductRepository;

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

//        user.setPantry(pantry);
        this.pantryRepository.save(pantry);
    }

    @Override
    public GetPantryResponse getUserPantry(String userEmail) {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (userOptional.isEmpty()) {
            throw new UserWasNotFoundAfterAuthException("User was not found in database after authentication");
        }

        Pantry pantry = userOptional.get().getPantry();
        if (pantry == null) {
            return new GetPantryResponse(null, null);
        }

        return new GetPantryResponse(pantry.getId(), pantry.getPantryName());
    }

    @Override
    public Page<PantryProductDTO> getPantryProducts(long id, int page, String userEmail) {
        Optional<Pantry> pantryOptional = this.pantryRepository.findById(id);
        Pantry pantry = pantryOptional.orElseThrow(() -> new PantryNotFoundException("Pantry was not found"));

        if (this.canUserAccessPantry(pantry, userEmail)) {
            PageRequest pageRequest = PageRequest.of(page, PRODUCTS_PAGE_SIZE);
            return pantryProductRepository
                    .findProductsInPantry(pantry.getId(), pageRequest)
                    .map(this::mapToPantryProductDTO);
        } else {
            throw new PantryNotFoundException("Pantry was not found");
        }
    }

    private PantryProductDTO mapToPantryProductDTO(PantryProduct pantryProduct) {
        return new PantryProductDTO(
                pantryProduct.getId(),
                pantryProduct.getProduct().getProductName(),
                pantryProduct.getProduct().getCategory(),
                pantryProduct.getPurchaseDate(),
                pantryProduct.getExpirationDate(),
                pantryProduct.getQuantity(),
                pantryProduct.getPlacement()
        );
    }

    private boolean canUserAccessPantry(Pantry pantry, String userEmail) {
        return pantry.getUser().getEmail().equals(userEmail);
    }
}

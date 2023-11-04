package com.cookie.app.service.impl;

import com.cookie.app.exception.PantryNotFoundException;
import com.cookie.app.exception.UserHasAssignedPantryException;
import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.entity.Pantry;
import com.cookie.app.model.entity.PantryProduct;
import com.cookie.app.model.entity.Product;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.request.UpdatePantryRequest;
import com.cookie.app.model.response.DeletePantryResponse;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.model.response.PantryProductDTO;
import com.cookie.app.repository.PantryProductRepository;
import com.cookie.app.repository.PantryRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryService;
import com.cookie.app.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public void createUserPantry(CreatePantryRequest request, String userEmail) {
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
    public GetPantryResponse getUserPantry(String userEmail) {
        Pantry pantry = this.getPantryForUser(userEmail);

        if (pantry == null) {
            return new GetPantryResponse(null, null);
        }

        return new GetPantryResponse(pantry.getId(), pantry.getPantryName());
    }

    @Override
    public DeletePantryResponse deleteUserPantry(String userEmail) {
        Pantry pantry = this.getPantryForUser(userEmail);

        if (pantry == null) {
            throw new PantryNotFoundException("User cannot remove the pantry because it does not exist");
        }

        this.pantryRepository.delete(pantry);

        return new DeletePantryResponse(pantry.getPantryName());
    }

    @Override
    public GetPantryResponse updateUserPantry(UpdatePantryRequest request, String userEmail) {
        Pantry pantry = this.getPantryForUser(userEmail);

        if (pantry == null) {
            throw new PantryNotFoundException("User cannot update the pantry because it does not exist");
        }

        if (
                request.pantryName().length() > 3 &&
                request.pantryName().length() <= 30 &&
                !request.pantryName().equals(pantry.getPantryName())
        ) {
            pantry.setPantryName(request.pantryName());
            this.pantryRepository.save(pantry);

            return new GetPantryResponse(pantry.getId(), pantry.getPantryName());
        }

        return new GetPantryResponse(null, null);
    }

    @Override
    public Page<PantryProductDTO> getPantryProducts(
            long id,
            int page,
            String filterCol,
            String filterValue,
            String sortColName,
            String sortDirection,
            String userEmail
    ) {
        Optional<Pantry> pantryOptional = this.pantryRepository.findById(id);
        Pantry pantry = pantryOptional.orElseThrow(() -> {
            log.info("User with email={} tried to download products from pantry which does not exist", userEmail);
            return new PantryNotFoundException("Pantry was not found");
        });

        if (this.cannotUserAccessPantry(pantry, userEmail)) {
            log.info("User with email={} tried to download products not from his pantry", userEmail);
            throw new PantryNotFoundException("Pantry was not found");
        }

        PageRequest pageRequest = this.createPageRequest(page, sortColName, sortDirection);

        if (!StringUtil.isBlank(filterCol) && !StringUtil.isBlank(filterValue)) {
            return pantryProductRepository
                    .findProductsInPantryWithFilter(pantry.getId(), filterCol, filterValue, pageRequest)
                    .map(this::mapToPantryProductDTO);
        }
        return pantryProductRepository
                .findProductsInPantry(pantry.getId(), pageRequest)
                .map(this::mapToPantryProductDTO);
    }

    @Override
    public void addProductsToPantry(long id, List<PantryProductDTO> productDTOs, String userEmail) {
        Optional<Pantry> pantryOptional = this.pantryRepository.findById(id);
        Pantry pantry = pantryOptional.orElseThrow(() -> new PantryNotFoundException("Pantry was not found"));

        if (this.cannotUserAccessPantry(pantry, userEmail)) {
            log.info("User with email={} tried to add products not to his pantry", userEmail);
            throw new PantryNotFoundException("Pantry was not found");
        }

        List<PantryProduct> products = productDTOs
                .stream()
                .map(productDTO -> this.mapToPantryProduct(productDTO, pantry))
                .toList();

        this.pantryProductRepository.saveAll(products);
    }

    private Pantry getPantryForUser(String userEmail) {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (userOptional.isEmpty()) {
            throw new UserWasNotFoundAfterAuthException("User was not found in database after authentication");
        }

        return userOptional.get().getPantry();
    }

    private PageRequest createPageRequest(int page, String sortColName, String sortDirection) {
        PageRequest pageRequest = PageRequest.of(page, PRODUCTS_PAGE_SIZE);

        if (StringUtil.isBlank(sortColName) && StringUtil.isBlank(sortDirection)) {
            return pageRequest;
        }

        if (sortDirection.equals("DESC")) {
            return pageRequest.withSort(Sort.by(Sort.Direction.DESC, sortColName));
        }
        return pageRequest.withSort(Sort.by(Sort.Direction.ASC, sortColName));
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

    private PantryProduct mapToPantryProduct(PantryProductDTO pantryProductDTO, Pantry pantry) {
        Product product = new Product();
        product.setProductName(pantryProductDTO.productName());
        product.setCategory(pantryProductDTO.category());

        PantryProduct pantryProduct = PantryProduct
                .builder()
                .pantry(pantry)
                .product(product)
                .purchaseDate(pantryProductDTO.purchaseDate())
                .expirationDate(pantryProductDTO.expirationDate())
                .quantity(pantryProductDTO.quantity())
                .placement(pantryProductDTO.placement())
                .build();

        if (pantryProductDTO.id() != null && pantryProductDTO.id() > 0) {
            pantryProduct.setId(pantryProductDTO.id());
        }

        return pantryProduct;
    }

    private boolean cannotUserAccessPantry(Pantry pantry, String userEmail) {
        return !pantry.getUser().getEmail().equals(userEmail);
    }
}

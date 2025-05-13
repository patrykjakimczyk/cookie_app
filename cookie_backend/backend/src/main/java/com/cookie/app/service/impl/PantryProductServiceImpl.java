package com.cookie.app.service.impl;

import com.cookie.app.exception.*;
import com.cookie.app.model.dto.PageResult;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapper;
import com.cookie.app.model.mapper.PantryProductMapper;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.request.FilterRequest;
import com.cookie.app.repository.PantryProductRepository;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public non-sealed class PantryProductServiceImpl extends AbstractPantryService implements PantryProductService {
    private final PantryProductRepository pantryProductRepository;
    private final PantryProductMapper pantryProductMapper;

    public PantryProductServiceImpl(
            UserRepository userRepository,
            AuthorityMapper authorityMapper,
            PantryProductRepository pantryProductRepository,
            ProductRepository productRepository,
            PantryProductMapper pantryProductMapper
    ) {
        super(userRepository, productRepository, authorityMapper);
        this.pantryProductRepository = pantryProductRepository;
        this.pantryProductMapper = pantryProductMapper;
    }

    @Override
    public PageResult<PantryProductDTO> getPantryProducts(
            long pantryId,
            int page,
            FilterRequest filterRequest,
            String userEmail
    ) {
        Pantry pantry = super.getPantryIfUserHasAuthority(pantryId, userEmail, null);
        PageRequest pageRequest = super.createPageRequest(page - 1, filterRequest.getSortColName(), filterRequest.getSortDirection());

        if (filterRequest.getFilterValue() != null && !StringUtils.isBlank(filterRequest.getFilterValue().trim())) {
            return new PageResult<>(this.pantryProductRepository
                    .findProductsInPantryWithFilter(pantry.getId(), filterRequest.getFilterValue(), pageRequest)
                    .map(pantryProductMapper::mapToDto));
        }
        return new PageResult<>(this.pantryProductRepository
                .findPantryProductByPantryId(pantry.getId(), pageRequest)
                .map(pantryProductMapper::mapToDto));
    }

    @Transactional
    @Override
    public void addProductsToPantry(long pantryId, List<PantryProductDTO> pantryProducts, String userEmail) {
        Pantry pantry = super.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.ADD);

        addProductsToPantry(pantryProducts, pantry);
        log.info("User with email={} added={} products to pantry from with id={}",
                userEmail,
                pantryProducts.size(),
                pantry.getId());
    }

    @Transactional
    @Override
    public void addProductsToPantryFromList(Pantry pantry, List<PantryProductDTO> pantryProducts) {
        addProductsToPantry(pantryProducts, pantry);
    }

    @Transactional
    @Override
    public void removeProductsFromPantry(long pantryId, List<Long> pantryProductsIds, String userEmail) {
        Pantry pantry = super.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.MODIFY);

        if (isAnyProductNotOnList(pantry, pantryProductsIds)) {
            log.info("User with email={} tried to remove products from different pantry", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot remove products from different pantry");
        }

        this.pantryProductRepository.deleteByIdIn(pantryProductsIds);
        log.info("User with email={} removed={} products from pantry with id={}",
                userEmail,
                pantryProductsIds.size(),
                pantry.getId());
    }

    @Transactional
    @Override
    public void updatePantryProduct(long pantryId, PantryProductDTO pantryProductToModify, String userEmail) {
        if (pantryProductToModify.id() == 0) {
            log.info("User with email={} tried to modify product which is not saved in database", userEmail);
            throw new ValidationException("Cannot modify product because it doesn't exist");
        }

        Pantry pantry = super.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.MODIFY);
        //If this method doesn't throw exception, it means that pantryProduct exists in the pantry
        getPantryProductById(pantryId, pantryProductToModify.id(), userEmail, "modify");

        Product productToModify = Product
                .builder()
                .id(pantryProductToModify.product().productId())
                .productName(pantryProductToModify.product().productName())
                .category(pantryProductToModify.product().category())
                .build();

        PantryProduct modifiedProduct = findPantryProductInPantry(pantry, pantryProductToModify, productToModify);

        if (modifiedProduct == null) {
            log.info("User with email={} tried to modify invalid pantry product", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot modify invalid pantry product");
        }

        this.pantryProductRepository.save(modifiedProduct);
    }

    @Transactional
    @Override
    public PantryProductDTO reservePantryProduct(long pantryId, long pantryProductId, int reserved, String userEmail) {
        //if this method doesn't throw any exception, creator can access this pantry
        super.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.RESERVE);
        PantryProduct pantryProduct = getPantryProductById(pantryId, pantryProductId, userEmail, "reserve");

        if (reserved > pantryProduct.getQuantity() || (reserved * -1) > pantryProduct.getReserved()) {
            return null;
        }

        pantryProduct.setReserved(pantryProduct.getReserved() + reserved);
        pantryProduct.setQuantity(pantryProduct.getQuantity() - reserved);
        this.pantryProductRepository.save(pantryProduct);

        return this.pantryProductMapper.mapToDto(pantryProduct);
    }

    @Transactional
    @Override
    public List<RecipeProduct>  reservePantryProductsFromRecipe(long pantryId, User user, List<RecipeProduct> recipeProducts) {
        Pantry pantry = super.getPantryIfUserHasAuthority(pantryId, user, AuthorityEnum.RESERVE);
        List<RecipeProduct> unreservedProducts = new ArrayList<>();

        for (RecipeProduct recipeProduct : recipeProducts) {
            Optional<PantryProduct> optionalPantryProduct = pantry.getPantryProducts()
                    .stream()
                    .filter(pantryProduct -> areRecipeAndPantryProductsEqual(recipeProduct, pantryProduct))
                    .findFirst();

            if (optionalPantryProduct.isEmpty()) {
                unreservedProducts.add(recipeProduct);
                continue;
            }

            PantryProduct pantryProduct = optionalPantryProduct.get();
            pantryProduct.setReserved(pantryProduct.getReserved() + recipeProduct.getQuantity());
            pantryProduct.setQuantity(pantryProduct.getQuantity() - recipeProduct.getQuantity());
            this.pantryProductRepository.save(pantryProduct);
        }

        return unreservedProducts;
    }

    @Override
    public List<RecipeProduct> getRecipeProductsNotInPantry(Pantry pantry, List<RecipeProduct> recipeProducts) {
        if (pantry == null) {
            return recipeProducts;
        }

        List<RecipeProduct> missingProducts = new ArrayList<>();

        for (RecipeProduct recipeProduct : recipeProducts) {
            Optional<PantryProduct> optionalPantryProduct = pantry.getPantryProducts()
                    .stream()
                    .filter(pantryProduct -> areRecipeAndPantryProductsEqual(recipeProduct, pantryProduct))
                    .findFirst();

            if (optionalPantryProduct.isEmpty()) {
                missingProducts.add(recipeProduct);
            }
        }

        return missingProducts;
    }

    private PantryProduct getPantryProductById(long pantryId, long pantryProductId, String userEmail, String action) {
        PantryProduct pantryProduct = this.pantryProductRepository.findById(pantryProductId).orElseThrow(() -> {
            log.info("User with email={} tried to {} product which does not exists", userEmail, action);
            return new ResourceNotFoundException("Pantry product was not found");
        });

        if (pantryProduct.getPantry().getId() != pantryId) {
            log.info("User with email={} tried to {} product from different pantry", userEmail, action);
            throw new UserPerformedForbiddenActionException(String.format("Cannot %s products from different pantry", action));
        }

        return pantryProduct;
    }

    private boolean areRecipeAndPantryProductsEqual(RecipeProduct recipeProduct, PantryProduct pantryProduct) {
        return recipeProduct.getProduct().equals(pantryProduct.getProduct()) &&
                recipeProduct.getUnit() == pantryProduct.getUnit() &&
                recipeProduct.getQuantity() <= pantryProduct.getQuantity();
    }

    private boolean isAnyProductNotOnList(Pantry pantry, List<Long> pantryProductIds) {
        List<Long> pantryProductsIds = pantry
                .getPantryProducts()
                .stream()
                .map(PantryProduct::getId)
                .toList();

        return super.isAnyProductNotOnList(pantryProductsIds, pantryProductIds);
    }

    private PantryProduct mapToPantryProduct(PantryProductDTO pantryProductDTO, Pantry pantry) {
        Product product = super.checkIfProductExists(pantryProductDTO.product());
        PantryProduct foundPantryProduct = findPantryProductInPantry(pantry, pantryProductDTO, product);

        if (foundPantryProduct != null) {
            return foundPantryProduct;
        }

        return PantryProduct
                .builder()
                .pantry(pantry)
                .product(product)
                .purchaseDate(pantryProductDTO.purchaseDate())
                .expirationDate(pantryProductDTO.expirationDate())
                .quantity(pantryProductDTO.quantity())
                .unit(pantryProductDTO.unit())
                .reserved(0)
                .placement(pantryProductDTO.placement())
                .build();
    }

    private PantryProduct findPantryProductInPantry(Pantry pantry, PantryProductDTO pantryProductDTO, Product product) {
            List<PantryProduct> pantryProducts = pantry.getPantryProducts();

            if (pantryProducts.isEmpty()) {
                return null;
            }

            for (PantryProduct pantryProduct : pantryProducts) {
                if (pantryProductDTO.id() > 0 && pantryProductDTO.id() == pantryProduct.getId()) {
                    // if pantry products ids are equal, we are modifying pantry product
                    pantryProduct.setQuantity(pantryProductDTO.quantity());
                    pantryProduct.setUnit(pantryProductDTO.unit());
                    pantryProduct.setReserved(pantryProductDTO.reserved());
                    pantryProduct.setPlacement(pantryProductDTO.placement());
                    pantryProduct.setPurchaseDate(pantryProductDTO.purchaseDate());
                    pantryProduct.setExpirationDate(pantryProductDTO.expirationDate());

                    return pantryProduct;
                } else if (arePantryProductsEqual(pantryProduct, pantryProductDTO, product)) {
                    if (pantryProductDTO.id() > 0) {
                        this.pantryProductRepository.deleteById(pantryProductDTO.id());
                    }
                    // if pantry products ids are not equal, we are adding exact same pantry product, so we need to sum quantities
                    pantryProduct.setQuantity(pantryProduct.getQuantity() + pantryProductDTO.quantity());
                    pantryProduct.setReserved(pantryProduct.getReserved() + pantryProductDTO.reserved());

                    return pantryProduct;
                }
            }

            return null;
    }

    private boolean arePantryProductsEqual(PantryProduct pantryProduct, PantryProductDTO pantryProductDTO, Product product) {
        return pantryProduct.getProduct().equals(product) &&
                pantryProduct.getUnit() == pantryProductDTO.unit() &&
                Objects.equals(pantryProduct.getPurchaseDate(), pantryProductDTO.purchaseDate()) &&
                Objects.equals(pantryProduct.getExpirationDate(), pantryProductDTO.expirationDate()) &&
                Objects.equals(pantryProduct.getPlacement(), pantryProductDTO.placement());
    }

    private void addProductsToPantry(List<PantryProductDTO> pantryProductDTOS, Pantry pantry) {
        List<PantryProduct> productsToAdd = new ArrayList<>();

        for (PantryProductDTO productDTO : pantryProductDTOS) {
            if (productDTO.id() > 0) {
                throw new ValidationException(
                        "Pantry product id must be 0 while inserting it to pantry");
            } else if (productDTO.reserved() > 0) {
                throw new ValidationException(
                        "Pantry product reserved quantity must be 0 while inserting it to pantry");
            }

            PantryProduct pantryProduct = mapToPantryProduct(productDTO, pantry);

            if (pantryProduct.getId() == 0L) {
                pantry.getPantryProducts().add(pantryProduct);
            }
            productsToAdd.add(pantryProduct);
        }

        this.pantryProductRepository.saveAll(productsToAdd);
    }
}

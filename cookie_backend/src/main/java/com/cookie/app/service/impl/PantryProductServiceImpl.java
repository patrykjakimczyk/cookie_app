package com.cookie.app.service.impl;

import com.cookie.app.exception.*;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.PantryProductMapperDTO;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.repository.PantryProductRepository;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
public class PantryProductServiceImpl extends AbstractPantryService implements PantryProductService {
    private final PantryProductRepository pantryProductRepository;
    private final PantryProductMapperDTO pantryProductMapper;

    public PantryProductServiceImpl(
            UserRepository userRepository,
            AuthorityMapperDTO authorityMapperDTO,
            PantryProductRepository pantryProductRepository,
            ProductRepository productRepository,
            PantryProductMapperDTO pantryProductMapper
    ) {
        super(userRepository, productRepository, authorityMapperDTO);
        this.pantryProductRepository = pantryProductRepository;
        this.pantryProductMapper = pantryProductMapper;
    }

    @Override
    public Page<PantryProductDTO> getPantryProducts(
            long pantryId,
            int page,
            String filterValue,
            String sortColName,
            String sortDirection,
            String userEmail
    ) {
        Pantry pantry = super.getPantryIfUserHasAuthority(pantryId, userEmail, null);

        PageRequest pageRequest = super.createPageRequest(page, sortColName, sortDirection);
        if (!StringUtils.isBlank(filterValue)) {
            return this.pantryProductRepository
                    .findProductsInPantryWithFilter(pantry.getId(), filterValue, pageRequest)
                    .map(pantryProductMapper);
        }
        return this.pantryProductRepository
                .findProductsInPantry(pantry.getId(), pageRequest)
                .map(pantryProductMapper);
    }

    @Override
    public void addProductsToPantry(long pantryId, List<PantryProductDTO> pantryProductDTOS, String userEmail) {
        Pantry pantry = super.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.ADD);

        addProductsToPantry(pantryProductDTOS, pantry);
    }

    @Override
    public void addProductsToPantryFromList(Pantry pantry, List<PantryProductDTO> pantryProductDTOS, User user) {
        addProductsToPantry(pantryProductDTOS, pantry);
    }

    @Override
    public void removeProductsFromPantry(long pantryId, List<Long> pantryProductIds, String userEmail) {
        Pantry pantry = super.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.MODIFY);

        if (isAnyProductNotOnList(pantry, pantryProductIds)) {
            log.info("User with email={} tried to remove products from different pantry", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot remove products from different pantry");
        }

        this.pantryProductRepository.deleteByIdIn(pantryProductIds);
    }

    @Override
    public void modifyPantryProduct(long pantryId, PantryProductDTO pantryProduct, String userEmail) {
        if (pantryProduct.id() == 0) {
            log.info("User with email={} tried to modify product which is not saved in database", userEmail);
            throw new ValidationException("Cannot modify product because it doesn't exist");
        }

        Pantry pantry = super.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.MODIFY);

        PantryProduct productToModify = PantryProduct
                .builder()
                .id(pantryProduct.id())
                .product(
                        Product
                                .builder()
                                .id(pantryProduct.product().productId())
                                .productName(pantryProduct.product().productName())
                                .category(pantryProduct.product().category())
                                .build()
                )
                .build();

        if (!isAnyProductNotOnList(pantry.getPantryProducts(), List.of(productToModify))) {
            log.info("User with email={} tried to modify product from different pantry", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot remove products from different pantry");
        }

        this.pantryProductRepository.save(mapToPantryProduct(pantryProduct, pantry, productToModify.getProduct()));
    }

    @Override
    public PantryProductDTO reservePantryProduct(long pantryId, long pantryProductId, int reserved, String userEmail) {
        //if this method doesn't throw any exception, user can access this pantry
        super.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.RESERVE);

        Optional<PantryProduct> pantryProductOptional = this.pantryProductRepository.findById(pantryProductId);
        PantryProduct pantryProduct = pantryProductOptional.orElseThrow(() -> {
            log.info("User with email={} tried to reserve product which does not exists", userEmail);
            return new UserPerformedForbiddenActionException("Pantry product was not found");
        });

        if (pantryProduct.getPantry().getId() != pantryId) {
            log.info("User with email={} tried to reserve product from different pantry", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot reserve products from different pantry");
        }

        if (reserved > pantryProduct.getQuantity() || (reserved * -1) > pantryProduct.getReserved()) {
            return null;
        }

        pantryProduct.setReserved(pantryProduct.getReserved() + reserved);
        pantryProduct.setQuantity(pantryProduct.getQuantity() - reserved);
        this.pantryProductRepository.save(pantryProduct);

        return this.pantryProductMapper.apply(pantryProduct);
    }

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
                continue;
            }

            PantryProduct pantryProduct = optionalPantryProduct.get();
            pantryProduct.setQuantity(pantryProduct.getQuantity() - recipeProduct.getQuantity());
        }

        return missingProducts;
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

    private PantryProduct mapToPantryProduct(PantryProductDTO pantryProductDTO, Pantry pantry, Product existingProduct) {
        Product product = existingProduct != null ? existingProduct : super.checkIfProductExists(pantryProductDTO.product());
        PantryProduct foundPantryProduct = null;
        // if product id > 0 then there is a chance that we have that product in our pantry, because product is in database
        if (product.getId() > 0) {
            foundPantryProduct = findPantryProductInPantry(pantry, pantryProductDTO, product);
        }

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

        pantryProductDTOS.forEach(productDTO -> {
            if (productDTO.id() > 0) {
                throw new ValidationException(
                        "Pantry product id must be 0 while inserting it to pantry");
            } else if (productDTO.reserved() > 0) {
                throw new ValidationException(
                        "Pantry product reserved quantity must be 0 while inserting it to pantry");
            }

            PantryProduct pantryProduct = mapToPantryProduct(productDTO, pantry, null);
            pantry.getPantryProducts().add(pantryProduct);
            productsToAdd.add(pantryProduct);
        });

        this.pantryProductRepository.saveAll(productsToAdd);
    }
}

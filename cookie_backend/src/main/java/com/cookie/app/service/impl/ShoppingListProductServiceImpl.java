package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.ValidationException;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.ShoppingListProductMapperDTO;
import com.cookie.app.repository.PantryProductRepository;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.ShoppingListProductRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryProductService;
import com.cookie.app.service.ShoppingListProductService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
public class ShoppingListProductServiceImpl extends AbstractShoppingListService implements ShoppingListProductService {
    private final ShoppingListProductRepository shoppingListProductRepository;
    private final PantryProductService pantryProductService;
    private final ShoppingListProductMapperDTO shoppingListProductMapper;

    public ShoppingListProductServiceImpl(UserRepository userRepository,
                                          ProductRepository productRepository,
                                          AuthorityMapperDTO authorityMapperDTO,
                                          ShoppingListProductRepository shoppingListProductRepository,
                                          PantryProductService pantryProductService,
                                          ShoppingListProductMapperDTO shoppingListProductMapper) {
        super(userRepository, productRepository, authorityMapperDTO);
        this.shoppingListProductRepository = shoppingListProductRepository;
        this.pantryProductService = pantryProductService;
        this.shoppingListProductMapper = shoppingListProductMapper;
    }

    @Override
    public Page<ShoppingListProductDTO> getShoppingListProducts(
            long listId,
            int page,
            String filterValue,
            String sortColName,
            String sortDirection,
            String userEmail
    ) {
        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(listId, user, null);

        PageRequest pageRequest = this.createPageRequest(page, sortColName, sortDirection);

        if(StringUtils.isBlank(filterValue)) {
            return this.shoppingListProductRepository
                    .findProductsInShoppingList(shoppingList.getId(), pageRequest)
                    .map(shoppingListProductMapper);
        }

        return this.shoppingListProductRepository
                .findProductsInShoppingListWithFilter(shoppingList.getId(), filterValue, pageRequest)
                .map(shoppingListProductMapper);
    }

    @Override
    public void addProductsToShoppingList(long listId, List<ShoppingListProductDTO> listProductDTOList, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(listId, user, AuthorityEnum.ADD_TO_SHOPPING_LIST);

        List<ShoppingListProduct> newShoppingListProducts = new ArrayList<>();

        listProductDTOList.forEach(listProductDTO -> {
            if (listProductDTO.id() > 0) {
                throw new ValidationException(
                        "Shopping list product id must be not set while inserting it to shopping list");
            } else if (listProductDTO.purchased()) {
                throw new ValidationException(
                        "Shopping list product cannot be purchased while inserting it to shopping list");
            }

            ShoppingListProduct shoppingListProduct = mapToShoppingListProduct(listProductDTO, shoppingList, null);
            shoppingList.getProductsList().add(shoppingListProduct);
            newShoppingListProducts.add(shoppingListProduct);
        });

        log.info("User with email {} added {} products to shopping list with id {}",
                userEmail,
                newShoppingListProducts.size(),
                shoppingList.getId());
        this.shoppingListProductRepository.saveAll(newShoppingListProducts);
    }

    @Override
    public void removeProductsFromShoppingList(long listId, List<Long> listProductIds, String userEmail) {
        listProductIds.stream()
                .filter(id -> id == null || id == 0)
                .findAny()
                .ifPresent(id -> {
                    throw new UserPerformedForbiddenActionException("Cannot remove products because list of ids is incorrect");
                });

        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(listId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);

        if (this.isAnyProductNotOnList(shoppingList, listProductIds)) {
            log.info("User with email={} tried to remove products from different shopping list", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot remove products from different shopping list");
        }

        log.info("User with email {} removed {} products from shopping list with id {}",
                userEmail,
                listProductIds.size(),
                shoppingList.getId());
        this.shoppingListProductRepository.deleteByIdIn(listProductIds);
    }

    @Override
    public void modifyShoppingListProduct(long listId, ShoppingListProductDTO listProductDTO, String userEmail) {
        if (listProductDTO.id() == 0) {
            log.info("User with email={} tried to modify product which is not saved in database", userEmail);
            throw new ValidationException("Cannot modify product because it doesn't exist");
        }

        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(listId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);

        ShoppingListProduct productToModify = ShoppingListProduct
                .builder()
                .id(listProductDTO.id())
                .product(Product
                        .builder()
                        .id(listProductDTO.product().productId())
                        .productName(listProductDTO.product().productName())
                        .category(listProductDTO.product().category())
                        .build()
                )
                .build();

        if (this.isAnyProductNotOnList(shoppingList.getProductsList(), List.of(productToModify))) {
            log.info("User with email={} tried to modify product from different shopping list", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot modify product from different shopping list");
        }

        this.shoppingListProductRepository.save(
                mapToShoppingListProduct(listProductDTO, shoppingList, productToModify.getProduct())
        );
    }

    @Override
    public void changePurchaseStatusForProducts(long listId, List<Long> listProductIds, String userEmail) {
        listProductIds.stream()
                .filter(id -> id == null || id == 0)
                .findAny()
                .ifPresent(id -> {
                    throw new UserPerformedForbiddenActionException("Cannot remove products because list of ids is incorrect");
                });

        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(listId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);

        if (this.isAnyProductNotOnList(shoppingList, listProductIds)) {
            log.info("User with email={} tried to modify products from different shopping list", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot modify products from different shopping list");
        }

        List<ShoppingListProduct> shoppingListProducts = shoppingList.getProductsList()
                .stream()
                .filter(shoppingListProduct -> listProductIds.contains(shoppingListProduct.getId()))
                .peek(shoppingListProduct -> shoppingListProduct.setPurchased(!shoppingListProduct.isPurchased()))
                .toList();

        this.shoppingListProductRepository.saveAll(shoppingListProducts);
    }

    @Override
    public void transferProductsToPantry(long listId, String userEmail) {
        User user = super.getUserByEmail(userEmail);
        ShoppingList shoppingList = super.getShoppingListIfUserHasAuthority(listId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);
        Pantry pantry = shoppingList.getGroup().getPantry();

        if (pantry == null) {
            log.info("User with email={} tried to transfer shopping list for group with not assigned pantry", userEmail);
            throw new UserPerformedForbiddenActionException("User cannot transfer products because his group does not have assigned pantry");
        }

        if (!super.userHasAuthority(user, pantry.getGroup().getId(), AuthorityEnum.ADD)) {
            log.info("User: {} tried to perform action in pantry without required permission", user.getEmail());
            throw new UserPerformedForbiddenActionException("You have not permissions to do that");
        }

        List<ShoppingListProduct> purchasedProducts = shoppingList.getProductsList()
                .stream()
                .filter(ShoppingListProduct::isPurchased)
                .toList();

        if (purchasedProducts.isEmpty()) {
            log.info("User with email={} tried to transfer shopping list containing unpurchased products to a pantry", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot transfer unpurchased shopping list products");
        }

        List<PantryProductDTO> newPantryProducts = mapListProductsToPantryProducts(purchasedProducts, pantry);

        log.info("User with email {} transfered {} products to shopping list with id {}",
                userEmail,
                newPantryProducts.size(),
                shoppingList.getId()
        );

        this.shoppingListProductRepository.deleteAll(purchasedProducts);
        this.pantryProductService.addProductsToPantryFromList(pantry, newPantryProducts);
    }

    @Override
    public void addRecipeProductsToShoppingList(long listId, User user, List<RecipeProduct> recipeProducts) {
        ShoppingList shoppingList = super.getShoppingListIfUserHasAuthority(listId, user, AuthorityEnum.ADD_TO_SHOPPING_LIST);
        List<ShoppingListProduct> addedProducts = new ArrayList<>();

        for (RecipeProduct recipeProduct : recipeProducts) {
            Optional<ShoppingListProduct> optionalShoppingListProduct = shoppingList.getProductsList()
                    .stream()
                    .filter(listProduct -> areRecipeAndListProductEquals(recipeProduct, listProduct))
                    .findFirst();

            if (optionalShoppingListProduct.isEmpty()) {
                addedProducts.add(
                        ShoppingListProduct.builder()
                        .shoppingList(shoppingList)
                        .product(recipeProduct.getProduct())
                        .quantity(recipeProduct.getQuantity())
                        .unit(recipeProduct.getUnit())
                        .purchased(false)
                        .build()
                );
                continue;
            }

            ShoppingListProduct shoppingListProduct = optionalShoppingListProduct.get();
            shoppingListProduct.setQuantity(shoppingListProduct.getQuantity() + recipeProduct.getQuantity());
            this.shoppingListProductRepository.save(shoppingListProduct);
        }

        this.shoppingListProductRepository.saveAll(addedProducts);
    }

    private boolean areRecipeAndListProductEquals(RecipeProduct recipeProduct, ShoppingListProduct listProduct) {
        return recipeProduct.getProduct().equals(listProduct.getProduct()) &&
                recipeProduct.getUnit() == listProduct.getUnit();
    }

    private List<PantryProductDTO> mapListProductsToPantryProducts(List<ShoppingListProduct> purchasedProducts, Pantry pantry) {
        List<PantryProductDTO> newPantryProducts = new ArrayList<>();
        Timestamp currentTimestamp = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.DAYS));

        for (ShoppingListProduct purchasedProduct : purchasedProducts) {
            PantryProductDTO pantryProduct = new PantryProductDTO(
                    0L,
                    new ProductDTO(
                            purchasedProduct.getProduct().getId(),
                            purchasedProduct.getProduct().getProductName(),
                            purchasedProduct.getProduct().getCategory()
                    ),
                    currentTimestamp,
                    null,
                    purchasedProduct.getQuantity(),
                    purchasedProduct.getUnit(),
                    0,
                    ""
            );

            newPantryProducts.add(pantryProduct);
        }

        return newPantryProducts;
    }

    private boolean isAnyProductNotOnList(ShoppingList shoppingList, List<Long> productIds) {
        List<Long> shoppingListProductsIds = shoppingList.getProductsList().stream().map(ShoppingListProduct::getId).toList();
        return this.isAnyProductNotOnList(shoppingListProductsIds, productIds);
    }

    private ShoppingListProduct mapToShoppingListProduct(ShoppingListProductDTO listProductDTO, ShoppingList shoppingList, Product existingProduct) {
        Product product = existingProduct != null ? existingProduct : this.checkIfProductExists(listProductDTO.product());
        ShoppingListProduct foundShoppingListProduct = null;
        // if product id > 0 then there is a chance that we have that product in our pantry, because product is in database
        if (product.getId() > 0) {
            foundShoppingListProduct = this.findProductInShoppingList(shoppingList, listProductDTO, product);
        }

        if (foundShoppingListProduct != null) {
            return foundShoppingListProduct;
        }

        return ShoppingListProduct
                .builder()
                .shoppingList(shoppingList)
                .product(product)
                .quantity(listProductDTO.quantity())
                .unit(listProductDTO.unit())
                .purchased(false)
                .build();
    }

    private ShoppingListProduct findProductInShoppingList(ShoppingList shoppingList, ShoppingListProductDTO listProductDTO, Product product) {
        List<ShoppingListProduct> productsList = shoppingList.getProductsList();

        if (productsList.isEmpty()) {
            return null;
        }

        for (ShoppingListProduct shoppingListProduct : productsList) {
            if (listProductDTO.id() > 0 && shoppingListProduct.getId() == listProductDTO.id()) {
                // if products ids are equal, we are modifying shopping list product
                shoppingListProduct.setQuantity(listProductDTO.quantity());
                shoppingListProduct.setUnit(listProductDTO.unit());

                return shoppingListProduct;
            } else if (this.areShoppingListProductsEqual(shoppingListProduct, listProductDTO, product)) {
                if (listProductDTO.id() > 0) {
                    this.shoppingListProductRepository.deleteById(listProductDTO.id());
                }
                // if products ids are not equal, we are adding exact same shopping list product, so we need to sum quantities
                shoppingListProduct.setQuantity(shoppingListProduct.getQuantity() + listProductDTO.quantity());

                return shoppingListProduct;
            }
        }

        return null;
    }

    private boolean areShoppingListProductsEqual(
            ShoppingListProduct shoppingListProduct,
            ShoppingListProductDTO listProductDTO,
            Product product
    ) {
        return shoppingListProduct.getProduct().equals(product) &&
                shoppingListProduct.getUnit() == listProductDTO.unit();
    }
}

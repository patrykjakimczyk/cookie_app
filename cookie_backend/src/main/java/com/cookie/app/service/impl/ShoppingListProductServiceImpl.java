package com.cookie.app.service.impl;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.ValidationException;
import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.ShoppingListProductMapperDTO;
import com.cookie.app.repository.PantryProductRepository;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.ShoppingListProductRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.ShoppingListProductService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Transactional
@Service
public class ShoppingListProductServiceImpl extends AbstractShoppingListService implements ShoppingListProductService {
    private final ShoppingListProductRepository shoppingListProductRepository;
    private final PantryProductRepository pantryProductRepository;
    private final ShoppingListProductMapperDTO shoppingListProductMapper;

    public ShoppingListProductServiceImpl(UserRepository userRepository,
                                          ProductRepository productRepository,
                                          AuthorityMapperDTO authorityMapperDTO,
                                          ShoppingListProductRepository shoppingListProductRepository,
                                          PantryProductRepository pantryProductRepository,
                                          ShoppingListProductMapperDTO shoppingListProductMapper) {
        super(userRepository, productRepository, authorityMapperDTO);
        this.shoppingListProductRepository = shoppingListProductRepository;
        this.pantryProductRepository = pantryProductRepository;
        this.shoppingListProductMapper = shoppingListProductMapper;
    }

    @Override
    public Page<ShoppingListProductDTO> getShoppingListProducts(
            long groupId,
            int page,
            String filterValue,
            String sortColName,
            String sortDirection,
            String userEmail
    ) {
        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(groupId, user, null);

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
    public void addProductsToShoppingList(long groupId, List<ShoppingListProductDTO> productDTOList, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(groupId, user, AuthorityEnum.ADD_TO_SHOPPING_LIST);

        List<ShoppingListProduct> newShoppingListProducts = new ArrayList<>();

        productDTOList.forEach(productDTO -> {
            if (productDTO.getId() > 0) {
                throw new ValidationException(
                        "Shopping list product id must be not set while inserting it to shopping list");
            } else if (productDTO.isPurchased()) {
                throw new ValidationException(
                        "Shopping list product cannot be purchased while inserting it to shopping list");
            }

            ShoppingListProduct shoppingListProduct = mapToShoppingListProduct(productDTO, shoppingList, null);
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
    public void removeProductsFromShoppingList(long groupId, List<Long> productIds, String userEmail) {
        productIds.stream()
                .filter(id -> id == null || id == 0)
                .findAny()
                .ifPresent(id -> {
                    throw new UserPerformedForbiddenActionException("Cannot remove products because list of ids is incorrect");
                });

        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(groupId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);

        if (this.isAnyProductNotOnList(shoppingList, productIds)) {
            log.info("User with email={} tried to remove products from different shopping list", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot remove products from different shopping list");
        }

        log.info("User with email {} removed {} products from shopping list with id {}",
                userEmail,
                productIds.size(),
                shoppingList.getId());
        this.shoppingListProductRepository.deleteByIdIn(productIds);
    }

    @Override
    public void modifyShoppingListProduct(long groupId, ShoppingListProductDTO productDTO, String userEmail) {
        if (productDTO.getId() == 0) {
            log.info("User with email={} tried to modify product which is not saved in database", userEmail);
            throw new ValidationException("Cannot modify product because it doesn't exist");
        }

        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(groupId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);

        ShoppingListProduct productToModify = ShoppingListProduct
                .builder()
                .id(productDTO.getId())
                .product(Product
                        .builder()
                        .productName(productDTO.getProductName())
                        .category(productDTO.getCategory())
                        .build()
                )
                .build();

        if (this.isAnyProductNotOnList(shoppingList.getProductsList(), List.of(productToModify))) {
            log.info("User with email={} tried to modify product from different shopping list", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot modify product from different shopping list");
        }

        this.shoppingListProductRepository.save(
                mapToShoppingListProduct(productDTO, shoppingList, productToModify.getProduct())
        );
    }

    @Override
    public void changePurchaseStatusForProducts(long groupId, List<Long> productIds, String userEmail) {
        productIds.stream()
                .filter(id -> id == null || id == 0)
                .findAny()
                .ifPresent(id -> {
                    throw new UserPerformedForbiddenActionException("Cannot remove products because list of ids is incorrect");
                });

        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(groupId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);

        if (this.isAnyProductNotOnList(shoppingList, productIds)) {
            log.info("User with email={} tried to modify products from different shopping list", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot modify products from different shopping list");
        }

        List<ShoppingListProduct> shoppingListProducts = shoppingList.getProductsList()
                .stream()
                .filter(shoppingListProduct -> productIds.contains(shoppingListProduct.getId()))
                .peek(shoppingListProduct -> shoppingListProduct.setPurchased(!shoppingListProduct.isPurchased()))
                .toList();

        this.shoppingListProductRepository.saveAll(shoppingListProducts);
    }

    @Override
    public void transferProductsToPantry(long shoppingListId, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(shoppingListId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);
        Pantry pantry = shoppingList.getGroup().getPantry();

        if (pantry == null) {
            log.info("User with email={} tried to transfer shopping list for group with not assigned pantry", userEmail);
            throw new UserPerformedForbiddenActionException("User cannot transfer products because his group does not have assigned pantry");
        }

        List<ShoppingListProduct> purchasedProducts = shoppingList.getProductsList()
                .stream()
                .filter(ShoppingListProduct::isPurchased)
                .toList();

        if (purchasedProducts.isEmpty()) {
            log.info("User with email={} tried to transfer shopping list containing unpurchased products to a pantry", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot transfer unpurchased shopping list products");
        }

        List<PantryProduct> newPantryProducts = this.mapListProductsToPantryProducts(purchasedProducts, pantry);

        log.info("User with email {} transfered {} products to shopping list with id {}",
                userEmail,
                newPantryProducts.size(),
                shoppingList.getId());
        this.shoppingListProductRepository.deleteAll(purchasedProducts);
        this.pantryProductRepository.saveAll(newPantryProducts);
    }

    private List<PantryProduct> mapListProductsToPantryProducts(List<ShoppingListProduct> purchasedProducts, Pantry pantry) {
        List<PantryProduct> newPantryProducts = new ArrayList<>();
        Timestamp currentTimestamp = Timestamp.from(Instant.now());

        for (ShoppingListProduct purchasedProduct : purchasedProducts) {
            PantryProduct pantryProduct = PantryProduct.builder()
                    .pantry(pantry)
                    .product(purchasedProduct.getProduct())
                    .purchaseDate(currentTimestamp)
                    .quantity(purchasedProduct.getQuantity())
                    .unit(purchasedProduct.getUnit())
                    .reserved(0)
                    .build();

            newPantryProducts.add(pantryProduct);
        }

        return newPantryProducts;
    }

    private boolean isAnyProductNotOnList(ShoppingList shoppingList, List<Long> productIds) {
        List<Long> shoppingListProductsIds = shoppingList.getProductsList().stream().map(ShoppingListProduct::getId).toList();
        return this.isAnyProductNotOnList(shoppingListProductsIds, productIds);
    }

    private ShoppingListProduct mapToShoppingListProduct(ShoppingListProductDTO productDTO, ShoppingList shoppingList, Product existingProduct) {
        Product product = existingProduct != null ? existingProduct : this.checkIfProductExists(productDTO);
        ShoppingListProduct foundShoppingListProduct = null;
        // if product id > 0 then there is a chance that we have that product in our pantry, because product is in database
        if (product.getId() > 0) {
            foundShoppingListProduct = this.findProductInShoppingList(shoppingList, productDTO, product);
        }

        if (foundShoppingListProduct != null) {
            return foundShoppingListProduct;
        }

        return ShoppingListProduct
                .builder()
                .shoppingList(shoppingList)
                .product(product)
                .quantity(productDTO.getQuantity())
                .unit(productDTO.getUnit())
                .purchased(false)
                .build();
    }

    private ShoppingListProduct findProductInShoppingList(ShoppingList shoppingList, ShoppingListProductDTO productDTO, Product product) {
        List<ShoppingListProduct> productsList = shoppingList.getProductsList();

        if (productsList.isEmpty()) {
            return null;
        }

        if (productDTO.getId() != null) {
            for (ShoppingListProduct shoppingListProduct : productsList) {
                if (shoppingListProduct.getId() == productDTO.getId()) {
                    // if products ids are equal, we are modifying shopping list product
                    shoppingListProduct.setQuantity(productDTO.getQuantity());
                    shoppingListProduct.setUnit(productDTO.getUnit());

                    return shoppingListProduct;
                } else if (this.areShoppingListProductsEqual(shoppingListProduct, productDTO, product)) {
                    this.shoppingListProductRepository.deleteById(productDTO.getId());
                    // if products ids are not equal, we are adding exact same shopping list product, so we need to sum quantities
                    shoppingListProduct.setQuantity(shoppingListProduct.getQuantity() + productDTO.getQuantity());

                    return shoppingListProduct;
                }
            }
        } else {
            for (ShoppingListProduct shoppingListProduct : productsList) {
                if (this.areShoppingListProductsEqual(shoppingListProduct, productDTO, product)) {
                    // if products are equal, we are adding exact same pantry product, so we need to sum quantities
                    shoppingListProduct.setQuantity(shoppingListProduct.getQuantity() + productDTO.getQuantity());

                    return shoppingListProduct;
                }
            }
        }

        return null;
    }

    private boolean areShoppingListProductsEqual(
            ShoppingListProduct shoppingListProduct,
            ShoppingListProductDTO productDTO,
            Product product
    ) {
        return shoppingListProduct.getProduct().equals(product) &&
                shoppingListProduct.getUnit() == productDTO.getUnit();
    }
}

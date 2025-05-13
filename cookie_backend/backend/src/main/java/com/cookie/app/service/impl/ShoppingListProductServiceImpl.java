package com.cookie.app.service.impl;

import com.cookie.app.exception.ResourceNotFoundException;
import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.ValidationException;
import com.cookie.app.model.dto.PageResult;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapper;
import com.cookie.app.model.mapper.ShoppingListProductMapper;
import com.cookie.app.model.request.FilterRequest;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.ShoppingListProductRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryProductService;
import com.cookie.app.service.ShoppingListProductService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public non-sealed class ShoppingListProductServiceImpl extends AbstractShoppingListService implements ShoppingListProductService {
    private final ShoppingListProductRepository shoppingListProductRepository;
    private final PantryProductService pantryProductService;
    private final ShoppingListProductMapper shoppingListProductMapper;

    public ShoppingListProductServiceImpl(UserRepository userRepository,
                                          ProductRepository productRepository,
                                          AuthorityMapper authorityMapper,
                                          ShoppingListProductRepository shoppingListProductRepository,
                                          PantryProductService pantryProductService,
                                          ShoppingListProductMapper shoppingListProductMapper) {
        super(userRepository, productRepository, authorityMapper);
        this.shoppingListProductRepository = shoppingListProductRepository;
        this.pantryProductService = pantryProductService;
        this.shoppingListProductMapper = shoppingListProductMapper;
    }

    @Override
    public PageResult<ShoppingListProductDTO> getShoppingListProducts(
            long listId,
            int page,
            FilterRequest filterRequest,
            String userEmail
    ) {
        ShoppingList shoppingList = super.getShoppingListIfUserHasAuthority(listId, userEmail, null);
        PageRequest pageRequest = super
                .createPageRequest(page - 1, filterRequest.getSortColName(), filterRequest.getSortDirection());

        if(StringUtils.isBlank(filterRequest.getFilterValue())) {
            return new PageResult<>(this.shoppingListProductRepository
                    .findShoppingListProductByShoppingListId(shoppingList.getId(), pageRequest)
                    .map(shoppingListProductMapper::mapToDto));
        }

        return new PageResult<>(this.shoppingListProductRepository
                .findProductsInShoppingListWithFilter(shoppingList.getId(), filterRequest.getFilterValue(), pageRequest)
                .map(shoppingListProductMapper::mapToDto));
    }

    @Transactional
    @Override
    public void addProductsToShoppingList(long listId, List<ShoppingListProductDTO> listProductDTOList, String userEmail) {
        ShoppingList shoppingList = super.getShoppingListIfUserHasAuthority(listId, userEmail, AuthorityEnum.ADD_TO_SHOPPING_LIST);
        List<ShoppingListProduct> newShoppingListProducts = new ArrayList<>();

        for (ShoppingListProductDTO listProductDTO : listProductDTOList) {
            if (listProductDTO.id() > 0) {
                throw new ValidationException(
                        "Shopping list product id must be not set while inserting it to shopping list");
            } else if (listProductDTO.purchased()) {
                throw new ValidationException(
                        "Shopping list product cannot be purchased while inserting it to shopping list");
            }

            ShoppingListProduct shoppingListProduct = mapToShoppingListProduct(listProductDTO, shoppingList);

            if (shoppingListProduct.getId() == 0L) {
                shoppingList.getProductsList().add(shoppingListProduct);
            }
            newShoppingListProducts.add(shoppingListProduct);
        }

        log.info("User with email={} added={} products to shopping list with id={}",
                userEmail,
                newShoppingListProducts.size(),
                shoppingList.getId());
        this.shoppingListProductRepository.saveAll(newShoppingListProducts);
    }

    @Transactional
    @Override
    public void removeProductsFromShoppingList(long listId, List<Long> listProductIds, String userEmail) {
        ShoppingList shoppingList = checkIfAllProductIdsAreOnList(listId, listProductIds, userEmail);

        this.shoppingListProductRepository.deleteByIdIn(listProductIds);
        log.info("User with email {} removed {} products from shopping list with id {}",
                userEmail,
                listProductIds.size(),
                shoppingList.getId());
    }

    @Transactional
    @Override
    public void updateShoppingListProduct(long listId, ShoppingListProductDTO listProductDTO, String userEmail) {
        if (listProductDTO.id() == 0) {
            log.info("User with email={} tried to modify product which is not saved in database", userEmail);
            throw new ValidationException("Cannot modify product because it doesn't exist");
        }

        ShoppingList shoppingList = super.getShoppingListIfUserHasAuthority(listId, userEmail, AuthorityEnum.MODIFY_SHOPPING_LIST);
        ShoppingListProduct shoppingListProduct = this.shoppingListProductRepository
                .findById(listProductDTO.id())
                .orElseThrow(() -> {
                    log.info("User with email={} tried to update product which does not exists", userEmail);
                    return new ResourceNotFoundException("Shopping list product was not found");
                });

        if (shoppingListProduct.getShoppingList().getId() != listId) {
            log.info("User with email={} tried to update product from different shopping list", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot update products from different shopping list");
        }

        Product productToModify = Product.builder()
                .id(listProductDTO.product().productId())
                .productName(listProductDTO.product().productName())
                .category(listProductDTO.product().category())
                .build();

        ShoppingListProduct modifiedProduct = findProductInShoppingList(shoppingList, listProductDTO, productToModify);

        if (modifiedProduct == null) {
            log.info("User with email={} tried to modify invalid shopping list product", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot modify invalid shopping list product");
        }

        this.shoppingListProductRepository.save(modifiedProduct);
    }

    @Transactional
    @Override
    public void changePurchaseStatusForProducts(long listId, List<Long> listProductIds, String userEmail) {
        ShoppingList shoppingList =
                super.getShoppingListIfUserHasAuthority(listId, userEmail, AuthorityEnum.MODIFY_SHOPPING_LIST);
        List<ShoppingListProduct> listProductsToChange = shoppingList.getProductsList()
                .stream()
                .filter(shoppingListProduct -> listProductIds.contains(shoppingListProduct.getId()))
                .toList();

        if (listProductsToChange.size() != listProductIds.size()) {
            log.info("User with email={} tried to change purchase status for products which are not on shopping list", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot modify purchase status for products which are not on shopping list");
        }

        listProductsToChange.forEach(listProduct -> listProduct.setPurchased(!listProduct.isPurchased()));

        this.shoppingListProductRepository.saveAll(listProductsToChange);
    }

    @Transactional
    @Override
    public void transferProductsToPantry(long listId, String userEmail) {
        User user = super.getUserByEmail(userEmail);
        ShoppingList shoppingList = super.getShoppingListIfUserHasAuthority(listId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);
        Pantry pantry = shoppingList.getGroup().getPantry();

        if (pantry == null) {
            log.info("User with email={} tried to transfer shopping list for group with not assigned pantry", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot transfer products because your group does not have assigned pantry");
        }

        if (!super.userHasAuthority(user, pantry.getGroup().getId(), AuthorityEnum.ADD)) {
            log.info("User={} tried to perform action in pantry without required permission", user.getEmail());
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

        List<PantryProductDTO> newPantryProducts = mapListProductsToPantryProducts(purchasedProducts);

        this.shoppingListProductRepository.deleteAll(purchasedProducts);
        this.pantryProductService.addProductsToPantryFromList(pantry, newPantryProducts);

        log.info("User with email={} transfered={} products from shopping list with id={} to pantry with id={}",
                userEmail,
                newPantryProducts.size(),
                shoppingList.getId(),
                pantry.getId()
        );
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

        if (!addedProducts.isEmpty()) {
            this.shoppingListProductRepository.saveAll(addedProducts);
        }
    }

    private boolean areRecipeAndListProductEquals(RecipeProduct recipeProduct, ShoppingListProduct listProduct) {
        return recipeProduct.getProduct().equals(listProduct.getProduct()) &&
                recipeProduct.getUnit() == listProduct.getUnit();
    }

    private List<PantryProductDTO> mapListProductsToPantryProducts(List<ShoppingListProduct> purchasedProducts) {
        List<PantryProductDTO> newPantryProducts = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        for (ShoppingListProduct purchasedProduct : purchasedProducts) {
            PantryProductDTO pantryProduct = new PantryProductDTO(
                    0L,
                    new ProductDTO(
                            purchasedProduct.getProduct().getId(),
                            purchasedProduct.getProduct().getProductName(),
                            purchasedProduct.getProduct().getCategory()
                    ),
                    currentDate,
                    null,
                    purchasedProduct.getQuantity(),
                    purchasedProduct.getUnit(),
                    0,
                    null
            );

            newPantryProducts.add(pantryProduct);
        }

        return newPantryProducts;
    }

    private ShoppingList checkIfAllProductIdsAreOnList(long listId, List<Long> listProductIds, String userEmail) {
        ShoppingList shoppingList = super.getShoppingListIfUserHasAuthority(listId, userEmail, AuthorityEnum.MODIFY_SHOPPING_LIST);

        if (this.isAnyProductNotOnList(shoppingList, listProductIds)) {
            log.info("User with email={} tried to remove products from different shopping list", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot remove products from different shopping list");
        }

        return shoppingList;
    }

    private boolean isAnyProductNotOnList(ShoppingList shoppingList, List<Long> productIds) {
        List<Long> shoppingListProductsIds = shoppingList.getProductsList()
                .stream()
                .map(ShoppingListProduct::getId)
                .toList();

        return super.isAnyProductNotOnList(shoppingListProductsIds, productIds);
    }

    private ShoppingListProduct mapToShoppingListProduct(ShoppingListProductDTO listProductDTO, ShoppingList shoppingList) {
        Product product = super.checkIfProductExists(listProductDTO.product());
        ShoppingListProduct foundShoppingListProduct = findProductInShoppingList(shoppingList, listProductDTO, product);

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
                shoppingListProduct.getUnit() == listProductDTO.unit() &&
                shoppingListProduct.isPurchased() == listProductDTO.purchased();
    }
}

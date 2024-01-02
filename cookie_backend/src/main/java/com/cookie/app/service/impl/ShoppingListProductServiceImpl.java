package com.cookie.app.service.impl;

import com.cookie.app.exception.InvalidProductDataException;
import com.cookie.app.exception.ModifyingProductsFromWrongPantryException;
import com.cookie.app.exception.ModifyingProductsFromWrongShoppingListException;
import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.entity.*;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.mapper.ShoppingListProductMapperDTO;
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

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ShoppingListProductServiceImpl extends AbstractCookieService implements ShoppingListProductService {
    private final ShoppingListProductRepository shoppingListProductRepository;
    private final ProductRepository productRepository;
    private final ShoppingListProductMapperDTO shoppingListProductMapper;

    public ShoppingListProductServiceImpl(
            UserRepository userRepository,
            AuthorityMapperDTO authorityMapperDTO,
            ShoppingListProductRepository shoppingListProductRepository,
            ProductRepository productRepository, ShoppingListProductMapperDTO shoppingListProductMapper
    ) {
        super(userRepository, authorityMapperDTO);
        this.shoppingListProductRepository = shoppingListProductRepository;
        this.productRepository = productRepository;
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

        productDTOList.forEach(productDTO -> {
            if (productDTO.id() != null) {
                throw new InvalidProductDataException(
                        "Shopping list product id must be not set while inserting it to shopping list");
            } else if (productDTO.purchased()) {
                throw new InvalidProductDataException(
                        "Shopping list product cannot be purchased while inserting it to shopping list");
            }

            ShoppingListProduct shoppingListProduct = mapToShoppingListProduct(productDTO, shoppingList);
            shoppingList.getProductsList().add(shoppingListProduct);
            this.shoppingListProductRepository.save(shoppingListProduct);
        });
    }

    @Transactional
    @Override
    public void removeProductsFromShoppingList(long groupId, List<Long> productIds, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(groupId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);

        if (!this.areAllProductsInShoppingList(shoppingList.getProductsList(), productIds)) {
            log.info("User with email={} tried to remove products from different shopping list", userEmail);
            throw new ModifyingProductsFromWrongShoppingListException("Cannot remove products from different shopping list");
        }

        this.shoppingListProductRepository.deleteByIdIn(productIds);
    }

    @Transactional
    @Override
    public void modifyShoppingListProduct(long groupId, ShoppingListProductDTO productDTO, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(groupId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);

        if (!this.areAllProductsInShoppingList(shoppingList.getProductsList(), List.of(productDTO.id()))) {
            log.info("User with email={} tried to remove products from different shopping list", userEmail);
            throw new ModifyingProductsFromWrongShoppingListException("Cannot modify products from different shopping list");
        }

        this.shoppingListProductRepository.save(mapToShoppingListProduct(productDTO, shoppingList));
    }

    @Override
    public void changePurchaseStatusForProducts(long groupId, List<Long> productIds, String userEmail) {
        User user = this.getUserByEmail(userEmail);
        ShoppingList shoppingList = this.getShoppingListIfUserHasAuthority(groupId, user, AuthorityEnum.MODIFY_SHOPPING_LIST);

        if (!this.areAllProductsInShoppingList(shoppingList.getProductsList(), productIds)) {
            log.info("User with email={} tried to remove products from different shopping list", userEmail);
            throw new ModifyingProductsFromWrongShoppingListException("Cannot modify products from different shopping list");
        }

        List<ShoppingListProduct> shoppingListProducts = shoppingList.getProductsList()
                .stream()
                .filter(shoppingListProduct -> productIds.contains(shoppingListProduct.getId()))
                .peek(shoppingListProduct -> shoppingListProduct.setPurchased(!shoppingListProduct.isPurchased()))
                .toList();

        this.shoppingListProductRepository.saveAll(shoppingListProducts);
    }

    private boolean areAllProductsInShoppingList(List<ShoppingListProduct> shoppingListProducts, List<Long> productIds) {
        List<Long> pantryProductsIds = shoppingListProducts
                .stream()
                .map(ShoppingListProduct::getId)
                .toList();

        for (Long productIdToRemove : productIds) {
            if (!pantryProductsIds.contains(productIdToRemove)) {
                return false;
            }
        }

        return true;
    }

    private ShoppingListProduct mapToShoppingListProduct(ShoppingListProductDTO productDTO, ShoppingList shoppingList) {
        Product product;
        Optional<Product> productOptional = this.productRepository.findByProductName(productDTO.productName());

        if (productOptional.isPresent()) {
            Product foundProduct = productOptional.get();
            if (foundProduct.getCategory() == productDTO.category()) {
                product = foundProduct;
            } else {
                product = new Product();
                product.setProductName(productDTO.productName());
                product.setCategory(productDTO.category());
                this.productRepository.save(product);
            }
        } else {
            product = new Product();
            product.setProductName(productDTO.productName());
            product.setCategory(productDTO.category());
            this.productRepository.save(product);
        }

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
                .quantity(productDTO.quantity())
                .unit(productDTO.unit())
                .purchased(false)
                .build();
    }

    private ShoppingListProduct findProductInShoppingList(ShoppingList shoppingList, ShoppingListProductDTO productDTO, Product product) {
        List<ShoppingListProduct> productsList = shoppingList.getProductsList();

        if (productsList.isEmpty()) {
            return null;
        }

        if (productDTO.id() != null) {
            for (ShoppingListProduct shoppingListProduct : productsList) {
                if (shoppingListProduct.getId() == productDTO.id()) {
                    // if products ids are equal, we are modifying shopping list product
                    shoppingListProduct.setQuantity(productDTO.quantity());
                    shoppingListProduct.setUnit(productDTO.unit());

                    return shoppingListProduct;
                } else if (this.areShoppingListProductsEqual(shoppingListProduct, productDTO, product)) {
                    this.shoppingListProductRepository.deleteById(productDTO.id());
                    // if products ids are not equal, we are adding exact same shopping list product, so we need to sum quantities
                    shoppingListProduct.setQuantity(shoppingListProduct.getQuantity() + productDTO.quantity());

                    return shoppingListProduct;
                }
            }
        } else {
            for (ShoppingListProduct shoppingListProduct : productsList) {
                if (this.areShoppingListProductsEqual(shoppingListProduct, productDTO, product)) {
                    // if products are equal, we are adding exact same pantry product, so we need to sum quantities
                    shoppingListProduct.setQuantity(shoppingListProduct.getQuantity() + productDTO.quantity());

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
                shoppingListProduct.getUnit() == productDTO.unit();
    }
}

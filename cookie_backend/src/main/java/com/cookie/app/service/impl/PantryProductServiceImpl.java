package com.cookie.app.service.impl;

import com.cookie.app.exception.*;
import com.cookie.app.model.entity.Pantry;
import com.cookie.app.model.entity.PantryProduct;
import com.cookie.app.model.entity.Product;
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        Pantry pantry = this.getPantryIfUserHasAuthority(pantryId, userEmail, null);

        PageRequest pageRequest = this.createPageRequest(page, sortColName, sortDirection);
        if (!StringUtils.isBlank(filterValue)) {
            return pantryProductRepository
                    .findProductsInPantryWithFilter(pantry.getId(), filterValue, pageRequest)
                    .map(pantryProductMapper);
        }
        return pantryProductRepository
                .findProductsInPantry(pantry.getId(), pageRequest)
                .map(pantryProductMapper);
    }

    @Override
    public void addProductsToPantry(long pantryId, List<PantryProductDTO> productDTOs, String userEmail) {
        Pantry pantry = this.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.ADD);

        productDTOs.forEach(productDTO -> {
            if (productDTO.getId() != null) {
                throw new ValidationException(
                        "Pantry product id must be not set while inserting it to pantry");
            } else if (productDTO.getReserved() > 0) {
                throw new ValidationException(
                        "Pantry product reserved quantity must be 0 while inserting it to pantry");
            }

            PantryProduct pantryProduct = mapToPantryProduct(productDTO, pantry);
            pantry.getPantryProducts().add(pantryProduct);
            this.pantryProductRepository.save(pantryProduct);
        });
    }

    @Override
    public void removeProductsFromPantry(long pantryId, List<Long> productIds, String userEmail) {
        Pantry pantry = this.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.MODIFY);

        if (!this.areAllProductsInPantry(pantry.getPantryProducts(), productIds)) {
            log.info("User with email={} tried to remove products from different pantry", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot remove products from different pantry");
        }

        this.pantryProductRepository.deleteByIdIn(productIds);
    }

    @Override
    public void modifyPantryProduct(long pantryId, PantryProductDTO pantryProduct, String userEmail) {
        Pantry pantry = this.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.MODIFY);

        if (!this.areAllProductsInPantry(pantry.getPantryProducts(), List.of(pantryProduct.getId()))) {
            log.info("User with email={} tried to modify product from different pantry", userEmail);
            throw new UserPerformedForbiddenActionException("Cannot remove products from different pantry");
        }

        this.pantryProductRepository.save(mapToPantryProduct(pantryProduct, pantry));
    }

    @Override
    public PantryProductDTO reservePantryProduct(long pantryId, long pantryProductId, int reserved, String userEmail) {
        //if this method doesn't throw any exception, user can access this pantry
        this.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.RESERVE);

        Optional<PantryProduct> pantryProductOptional = this.pantryProductRepository.findById(pantryProductId);
        PantryProduct pantryProduct = pantryProductOptional.orElseThrow(() -> {
            log.info("User with email={} tried to reserve product which does not exists", userEmail);
            throw new UserPerformedForbiddenActionException("Pantry product was not found");
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

    private boolean areAllProductsInPantry(List<PantryProduct> pantryProducts, List<Long> productIds) {
        List<Long> pantryProductsIds = pantryProducts
                .stream()
                .map(PantryProduct::getId)
                .toList();

        for (Long productIdToRemove : productIds) {
            if (!pantryProductsIds.contains(productIdToRemove)) {
                return false;
            }
        }

        return true;
    }

    private PantryProduct mapToPantryProduct(PantryProductDTO pantryProductDTO, Pantry pantry) {
        Product product = this.checkIfProductExists(pantryProductDTO);
        PantryProduct foundPantryProduct = null;
        // if product id > 0 then there is a chance that we have that product in our pantry, because product is in database
        if (product.getId() > 0) {
            foundPantryProduct = this.findPantryProductInPantry(pantry, pantryProductDTO, product);
        }

        if (foundPantryProduct != null) {
            return foundPantryProduct;
        }

        return PantryProduct
                .builder()
                .pantry(pantry)
                .product(product)
                .purchaseDate(pantryProductDTO.getPurchaseDate())
                .expirationDate(pantryProductDTO.getExpirationDate())
                .quantity(pantryProductDTO.getQuantity())
                .unit(pantryProductDTO.getUnit())
                .reserved(0)
                .placement(pantryProductDTO.getPlacement())
                .build();
    }

    private PantryProduct findPantryProductInPantry(Pantry pantry, PantryProductDTO pantryProductDTO, Product product) {
            List<PantryProduct> pantryProducts = pantry.getPantryProducts();

            if (pantryProducts.isEmpty()) {
                return null;
            }

            if (pantryProductDTO.getId() != null) {
                for (PantryProduct pantryProduct : pantryProducts) {
                    if (pantryProduct.getId() == pantryProductDTO.getId()) {
                        // if pantry products ids are equal, we are modifying pantry product
                        pantryProduct.setQuantity(pantryProductDTO.getQuantity());
                        pantryProduct.setUnit(pantryProductDTO.getUnit());
                        pantryProduct.setReserved(pantryProductDTO.getReserved());
                        pantryProduct.setPlacement(pantryProductDTO.getPlacement());
                        pantryProduct.setPurchaseDate(pantryProductDTO.getPurchaseDate());
                        pantryProduct.setExpirationDate(pantryProductDTO.getExpirationDate());

                        return pantryProduct;
                    } else if (this.arePantryProductsEqual(pantryProduct, pantryProductDTO, product)) {
                        this.pantryProductRepository.deleteById(pantryProductDTO.getId());
                        // if pantry products ids are not equal, we are adding exact same pantry product, so we need to sum quantities
                        pantryProduct.setQuantity(pantryProduct.getQuantity() + pantryProductDTO.getQuantity());
                        pantryProduct.setReserved(pantryProduct.getReserved() + pantryProductDTO.getReserved());

                        return pantryProduct;
                    }
                }
            } else {
                for (PantryProduct pantryProduct : pantryProducts) {
                    if (this.arePantryProductsEqual(pantryProduct, pantryProductDTO, product)) {
                        // if pantry products are equal, we are adding exact same pantry product, so we need to sum quantities
                        pantryProduct.setQuantity(pantryProduct.getQuantity() + pantryProductDTO.getQuantity());

                        return pantryProduct;
                    }
                }
            }

            return null;
    }

    private boolean arePantryProductsEqual(PantryProduct pantryProduct, PantryProductDTO pantryProductDTO, Product product) {
        return pantryProduct.getProduct().equals(product) &&
                pantryProduct.getUnit() == pantryProductDTO.getUnit() &&
                Objects.equals(pantryProduct.getPurchaseDate(), pantryProductDTO.getPurchaseDate()) &&
                Objects.equals(pantryProduct.getExpirationDate(), pantryProductDTO.getExpirationDate()) &&
                pantryProduct.getPlacement().equals(pantryProductDTO.getPlacement());
    }
}

package com.cookie.app.service.impl;

import com.cookie.app.exception.*;
import com.cookie.app.model.entity.Pantry;
import com.cookie.app.model.entity.PantryProduct;
import com.cookie.app.model.entity.Product;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.AuthorityEnum;
import com.cookie.app.model.mapper.PantryProductMapperDTO;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.repository.PantryProductRepository;
import com.cookie.app.repository.PantryRepository;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.PantryProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PantryProductServiceImpl extends AbstractCookieService implements PantryProductService {
    private static final int PRODUCTS_PAGE_SIZE = 20;
    private final PantryRepository pantryRepository;
    private final PantryProductRepository pantryProductRepository;
    private final ProductRepository productRepository;
    private final PantryProductMapperDTO pantryProductMapper;

    public PantryProductServiceImpl(
            UserRepository userRepository,
            PantryRepository pantryRepository,
            PantryProductRepository pantryProductRepository,
            ProductRepository productRepository,
            PantryProductMapperDTO pantryProductMapper
    ) {
        super(userRepository);
        this.pantryRepository = pantryRepository;
        this.pantryProductRepository = pantryProductRepository;
        this.productRepository = productRepository;
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
            if (productDTO.id() != null) {
                throw new InvalidPantryProductDataException(
                        "Pantry product id must be not set while inserting it to pantry");
            } else if (productDTO.reserved() > 0) {
                throw new InvalidPantryProductDataException(
                        "Pantry product reserved quantity must be 0 while inserting it to pantry");
            }

            PantryProduct pantryProduct = mapToPantryProduct(productDTO, pantry);
            pantry.getPantryProducts().add(pantryProduct);
            this.pantryProductRepository.save(pantryProduct);
        });
    }

    @Transactional
    @Override
    public void removeProductsFromPantry(long pantryId, List<Long> productIds, String userEmail) {
        Pantry pantry = this.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.MODIFY);

        if (!this.areAllProductsInPantry(pantry.getPantryProducts(), productIds)) {
            log.info("User with email={} tried to remove products from different pantry", userEmail);
            throw new ModifyingProductsFromWrongPantryException("Cannot remove products from different pantry");
        }

        this.pantryProductRepository.deleteByIdIn(productIds);
    }

    @Transactional
    @Override
    public void modifyPantryProduct(long pantryId, PantryProductDTO pantryProduct, String userEmail) {
        Pantry pantry = this.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.MODIFY);

        if (!this.areAllProductsInPantry(pantry.getPantryProducts(), List.of(pantryProduct.id()))) {
            log.info("User with email={} tried to modify product from different pantry", userEmail);
            throw new ModifyingProductsFromWrongPantryException("Cannot remove products from different pantry");
        }

        this.pantryProductRepository.save(mapToPantryProduct(pantryProduct, pantry));
    }

    @Transactional
    @Override
    public PantryProductDTO reservePantryProduct(long pantryId, long pantryProductId, int reserved, String userEmail) {
        //if this method doesn't throw any exception, user can access this pantry
        this.getPantryIfUserHasAuthority(pantryId, userEmail, AuthorityEnum.RESERVE);

        Optional<PantryProduct> pantryProductOptional = this.pantryProductRepository.findById(pantryProductId);
        PantryProduct pantryProduct = pantryProductOptional.orElseThrow(() -> {
            log.info("User with email={} tried to reserve product which does not exists", userEmail);
            throw new PantryProductNotFoundException("Pantry product was not found");
        });

        if (pantryProduct.getPantry().getId() != pantryId) {
            log.info("User with email={} tried to reserve product from different pantry", userEmail);
            throw new ModifyingProductsFromWrongPantryException("Cannot reserve products from different pantry");
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

    private PageRequest createPageRequest(int page, String sortColName, String sortDirection) {
        PageRequest pageRequest = PageRequest.of(page, PRODUCTS_PAGE_SIZE);
        Sort idSort = Sort.by(Sort.Direction.DESC, "id");
        Sort sort = null;

        if (StringUtils.isBlank(sortColName) && StringUtils.isBlank(sortDirection)) {
            return pageRequest.withSort(idSort);
        }

        if (sortDirection.equals("DESC")) {
            sort = Sort.by(Sort.Direction.DESC, sortColName);
        } else {
            sort = Sort.by(Sort.Direction.ASC, sortColName);
        }

        sort = sort.and(idSort);
        return pageRequest.withSort(sort);
    }

    private PantryProduct mapToPantryProduct(PantryProductDTO pantryProductDTO, Pantry pantry) {
        Product product;
        Optional<Product> productOptional = this.productRepository.findByProductName(pantryProductDTO.productName());

        if (productOptional.isPresent()) {
            Product foundProduct = productOptional.get();
            if (foundProduct.getCategory() == pantryProductDTO.category()) {
                product = foundProduct;
            } else {
                product = new Product();
                product.setProductName(pantryProductDTO.productName());
                product.setCategory(pantryProductDTO.category());
                this.productRepository.save(product);
            }
        } else {
            product = new Product();
            product.setProductName(pantryProductDTO.productName());
            product.setCategory(pantryProductDTO.category());
            this.productRepository.save(product);
        }

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

            if (pantryProductDTO.id() != null) {
                for (PantryProduct pantryProduct : pantryProducts) {
                    if (pantryProduct.getId() == pantryProductDTO.id()) {
                        // if pantry products ids are equal, we are modifying pantry product
                        pantryProduct.setQuantity(pantryProductDTO.quantity());
                        pantryProduct.setUnit(pantryProductDTO.unit());
                        pantryProduct.setReserved(pantryProductDTO.reserved());
                        pantryProduct.setPlacement(pantryProductDTO.placement());
                        pantryProduct.setPurchaseDate(pantryProductDTO.purchaseDate());
                        pantryProduct.setExpirationDate(pantryProductDTO.expirationDate());

                        return pantryProduct;
                    } else if (this.arePantryProductsEqual(pantryProduct, pantryProductDTO, product)) {
                        this.pantryProductRepository.deleteById(pantryProductDTO.id());
                        // if pantry products ids are not equal, we are adding exact same pantry product, so we need to sum quantities
                        pantryProduct.setQuantity(pantryProduct.getQuantity() + pantryProductDTO.quantity());
                        pantryProduct.setReserved(pantryProduct.getReserved() + pantryProductDTO.reserved());

                        return pantryProduct;
                    }
                }
            } else {
                for (PantryProduct pantryProduct : pantryProducts) {
                    if (this.arePantryProductsEqual(pantryProduct, pantryProductDTO, product)) {
                        // if pantry products are equal, we are adding exact same pantry product, so we need to sum quantities
                        pantryProduct.setQuantity(pantryProduct.getQuantity() + pantryProductDTO.quantity());

                        return pantryProduct;
                    }
                }
            }

            return null;
    }

    private boolean arePantryProductsEqual(PantryProduct pantryProduct, PantryProductDTO pantryProductDTO, Product product) {
        return pantryProduct.getProduct().equals(product) &&
                pantryProduct.getUnit() == pantryProductDTO.unit() &&
                pantryProduct.getPurchaseDate().equals(pantryProductDTO.purchaseDate()) &&
                pantryProduct.getExpirationDate().equals(pantryProductDTO.expirationDate()) &&
                pantryProduct.getPlacement().equals(pantryProductDTO.placement());
    }
}

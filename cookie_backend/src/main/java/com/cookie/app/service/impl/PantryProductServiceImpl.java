package com.cookie.app.service.impl;

import com.cookie.app.exception.PantryNotFoundException;
import com.cookie.app.exception.PantryProductIdSetException;
import com.cookie.app.model.entity.Pantry;
import com.cookie.app.model.entity.PantryProduct;
import com.cookie.app.model.entity.Product;
import com.cookie.app.model.mapper.PantryProductMapperDTO;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.repository.PantryProductRepository;
import com.cookie.app.repository.PantryRepository;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.service.PantryProductService;
import com.cookie.app.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class PantryProductServiceImpl implements PantryProductService {
    private static final int PRODUCTS_PAGE_SIZE = 20;
    private final PantryRepository pantryRepository;
    private final PantryProductRepository pantryProductRepository;
    private final ProductRepository productRepository;
    private final PantryProductMapperDTO pantryProductMapper;

    @Override
    public Page<PantryProductDTO> getPantryProducts(
            long pantryId,
            int page,
            String filterValue,
            String sortColName,
            String sortDirection,
            String userEmail
    ) {
        Optional<Pantry> pantryOptional = this.pantryRepository.findById(pantryId);
        Pantry pantry = pantryOptional.orElseThrow(() -> {
            log.info("User with email={} tried to download products from pantry which does not exist", userEmail);
            return new PantryNotFoundException("Pantry was not found");
        });

        if (this.cannotUserAccessPantry(pantry, userEmail)) {
            log.info("User with email={} tried to download products not from his pantry", userEmail);
            throw new PantryNotFoundException("Pantry was not found");
        }

        PageRequest pageRequest = this.createPageRequest(page, sortColName, sortDirection);

        if (!StringUtil.isBlank(filterValue)) {
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
        Optional<Pantry> pantryOptional = this.pantryRepository.findById(pantryId);
        Pantry pantry = pantryOptional.orElseThrow(() -> {
            log.info("User with email={} tried to add products to pantry which does not exist", userEmail);
            return new PantryNotFoundException("Pantry was not found");
        });

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

    @Transactional
    @Override
    public void deleteProductsFromPantry(long pantryId, List<Long> productIds, String userEmail) {
        Optional<Pantry> pantryOptional = this.pantryRepository.findById(pantryId);
        Pantry pantry = pantryOptional.orElseThrow(() -> {
            log.info("User with email={} tried to delete products from pantry which does not exist", userEmail);
            return new PantryNotFoundException("Pantry was not found");
        });

        if (this.cannotUserAccessPantry(pantry, userEmail)) {
            log.info("User with email={} tried to delete products not to his pantry", userEmail);
            throw new PantryNotFoundException("Pantry was not found");
        }

        this.pantryProductRepository.deleteByIdIn(productIds);
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

    private PantryProduct mapToPantryProduct(PantryProductDTO pantryProductDTO, Pantry pantry) {
        if (pantryProductDTO.id() != null) {
            throw new PantryProductIdSetException("Pantry product id must be not set while inserting it to pantry");
        }

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
            }
        } else {
            product = new Product();
            product.setProductName(pantryProductDTO.productName());
            product.setCategory(pantryProductDTO.category());
        }

        return PantryProduct
                .builder()
                .pantry(pantry)
                .product(product)
                .purchaseDate(pantryProductDTO.purchaseDate())
                .expirationDate(pantryProductDTO.expirationDate())
                .quantity(pantryProductDTO.quantity())
                .placement(pantryProductDTO.placement())
                .build();
    }

    private boolean cannotUserAccessPantry(Pantry pantry, String userEmail) {
        return !pantry.getUser().getEmail().equals(userEmail);
    }
}

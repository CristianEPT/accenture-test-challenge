package com.accenture.test.accenturetestchallenge.domain.service;

import com.accenture.test.accenturetestchallenge.domain.entities.ProductEntity;
import com.accenture.test.accenturetestchallenge.domain.model.Product;
import com.accenture.test.accenturetestchallenge.domain.ports.BranchPort;
import com.accenture.test.accenturetestchallenge.domain.ports.FranchisePort;
import com.accenture.test.accenturetestchallenge.domain.ports.ProductPort;
import com.accenture.test.accenturetestchallenge.domain.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements ProductPort {

  private final FranchisePort franchisePort;
  private final BranchPort branchPort;
  private final ProductRepository productRepository;

  @Override
  public Mono<Product> createProduct(String franchiseId, String branchId, Product product) {

    return validateFranchise(franchiseId)
        .then(validateBranch(franchiseId, branchId))
        .then(validateProduct(product))
        .map(validatedProduct -> buildProductEntity(franchiseId, branchId, validatedProduct))
        .flatMap(productRepository::save)
        .map(this::mapEntityToDomain)
        .doOnSuccess(
            savedProduct ->
                log.info(
                    "Branch created successfully. Product ID: {}, Name: {}",
                    savedProduct.getId(),
                    savedProduct.getName()))
        .doOnError(
            error ->
                log.error(
                    "Error occurred during product creation. Product name: {}. Error: {}",
                    product.getName(),
                    error.getMessage(),
                    error));
  }

  private Product mapEntityToDomain(ProductEntity productEntity) {
    return Product.builder()
        .id(productEntity.getId())
        .name(productEntity.getName())
        .branchId(productEntity.getBranchId())
        .franchiseId(productEntity.getFranchiseId())
        .stock(productEntity.getStock())
        .build();
  }

  private ProductEntity buildProductEntity(
      String franchiseId, String branchId, Product validatedProduct) {

    ProductEntity productEntity = new ProductEntity();
    productEntity.setFranchiseId(franchiseId);
    productEntity.setBranchId(branchId);
    productEntity.setName(validatedProduct.getName());
    productEntity.setStock(validatedProduct.getStock());

    return productEntity;
  }

  private Mono<Void> validateFranchise(String franchiseId) {
    if (franchiseId == null || franchiseId.trim().isEmpty()) {
      log.warn("Invalid franchise id received: '{}'", franchiseId);
      return Mono.error(new IllegalArgumentException("Franchise id must not be null or empty"));
    }

    return franchisePort
        .existsFranchise(franchiseId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Franchise does not exist")))
        .then();
  }

  private Mono<Void> validateBranch(String franchiseId, String branchId) {
    if (branchId == null || branchId.trim().isEmpty()) {
      log.warn("Invalid branch id received: '{}'", branchId);
      return Mono.error(new IllegalArgumentException("Branch id must not be null or empty"));
    }

    return branchPort
        .existsBranch(franchiseId, branchId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Branch does not exist")))
        .then();
  }

  private Mono<Product> validateProduct(Product product) {
    if (product.getName() == null
        || product.getName().trim().isEmpty()
        || product.getStock() == null
        || product.getStock() < 0) {
      log.warn("Invalid product received: '{}'", product);
      return Mono.error(
          new IllegalArgumentException("Product name or stock must not be null or empty"));
    }
    return Mono.just(product);
  }

  @Override
  public Mono<Void> deleteProduct(String franchiseId, String branchId, String productId) {
    if (areEmpty(franchiseId, branchId, productId)) {
      log.warn(
          "Invalid input for deletion. FranchiseId: '{}', BranchId: '{}', ProductId: '{}'",
          franchiseId,
          branchId,
          productId);
      return Mono.error(
          new IllegalArgumentException(
              "Franchise ID, Branch ID and Product ID must not be null or empty"));
    }

    return productRepository
        .findByFranchiseIdAndBranchIdAndId(franchiseId, branchId, productId)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Product not found")))
        .flatMap(product -> productRepository.deleteById(productId))
        .doOnSuccess(v -> log.info("Product deleted successfully. ID: {}", productId))
        .doOnError(
            error ->
                log.error(
                    "Error deleting product ID {}: {}", productId, error.getMessage(), error));
  }

  private boolean areEmpty(String franchiseId, String branchId, String productId) {
    return franchiseId == null
        || franchiseId.trim().isEmpty()
        || branchId == null
        || branchId.trim().isEmpty()
        || productId == null
        || productId.trim().isEmpty();
  }

  @Override
  public Mono<Product> updateProductStock(
      String franchiseId, String branchId, String productId, int stock) {

    if (areEmpty(franchiseId, branchId, productId) || stock < 0) {
      log.warn(
          "Invalid input for update stock. FranchiseId: '{}', BranchId: '{}', ProductId: '{}', Stock: '{}'",
          franchiseId,
          branchId,
          productId,
          stock);
      return Mono.error(
          new IllegalArgumentException(
              "Franchise ID, Branch ID and Product ID must not be null or empty"));
    }

    return productRepository
        .findByFranchiseIdAndBranchIdAndId(franchiseId, branchId, productId)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Product not found")))
        .map(
            product -> {
              product.setStock(stock);
              return product;
            })
        .flatMap(productRepository::save)
        .map(this::mapEntityToDomain)
        .doOnSuccess(v -> log.info("Product updated successfully. ID: {}", productId))
        .doOnError(
            error ->
                log.error(
                    "Error updating product ID {}: {}", productId, error.getMessage(), error));
  }

  @Override
  public Flux<Product> getTopProductsByFranchise(String franchiseId) {
    if (franchiseId == null || franchiseId.trim().isEmpty()) {
      log.warn("Invalid franchise ID received for top products: '{}'", franchiseId);
      return Flux.error(new IllegalArgumentException("Franchise ID must not be null or empty"));
    }

    return productRepository
        .findByFranchiseId(franchiseId)
        .map(this::mapEntityToDomain)
        .groupBy(Product::getBranchId)
        .flatMap(
            groupedFlux ->
                groupedFlux
                    .sort(
                        (firstProductStock, secondProductStock) ->
                            Integer.compare(
                                secondProductStock.getStock(), firstProductStock.getStock()))
                    .next())
        .doOnNext(
            product ->
                log.debug(
                    "Top product for branch {} in franchise {}: {} (stock {})",
                    product.getBranchId(),
                    franchiseId,
                    product.getName(),
                    product.getStock()))
        .doOnComplete(
            () ->
                log.info(
                    "Top products by branch fetched successfully for franchise {}", franchiseId))
        .doOnError(
            error ->
                log.error(
                    "Error fetching top products for franchise {}: {}",
                    franchiseId,
                    error.getMessage(),
                    error));
  }

  @Override
  public Mono<Product> updateProductName(
      String franchiseId, String branchId, String productId, String newProductName) {

    if (areEmpty(franchiseId, branchId, productId)
        || newProductName == null
        || newProductName.isEmpty()) {
      log.warn(
          "Invalid input for update stock. FranchiseId: '{}', BranchId: '{}', ProductId: '{}', newProductName: '{}'",
          franchiseId,
          branchId,
          productId,
          newProductName);
      return Mono.error(
          new IllegalArgumentException(
              "Franchise ID, Branch ID, Product ID and new Product name must not be null or empty"));
    }

    return productRepository
        .findByFranchiseIdAndBranchIdAndId(franchiseId, branchId, productId)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Product not found")))
        .map(
            product -> {
              product.setName(newProductName);
              return product;
            })
        .flatMap(productRepository::save)
        .map(this::mapEntityToDomain)
        .doOnSuccess(v -> log.info("Product name updated successfully. ID: {}", productId))
        .doOnError(
            error ->
                log.error(
                    "Error updating product ID {}: {}", productId, error.getMessage(), error));
  }
}

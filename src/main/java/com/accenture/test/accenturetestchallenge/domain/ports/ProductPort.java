package com.accenture.test.accenturetestchallenge.domain.ports;

import com.accenture.test.accenturetestchallenge.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductPort {

  Mono<Product> createProduct(String franchiseId, String branchId, Product product);

  void deleteProduct(String franchiseId, String branchId, String productId);

  Mono<Product> updateProductStock(
      String franchiseId, String branchId, String productId, int stock);

  Flux<Product> getTopProductsByFranchise(String franchiseId);
}

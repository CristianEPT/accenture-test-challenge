package com.accenture.test.accenturetestchallenge.domain.service;

import com.accenture.test.accenturetestchallenge.domain.model.Product;
import com.accenture.test.accenturetestchallenge.domain.ports.ProductPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements ProductPort {

  @Override
  public Mono<Product> createProduct(String franchiseId, String branchId, Product product) {

    return Mono.empty();
  }

  @Override
  public void deleteProduct(String franchiseId, String branchId, String productId) {}

  @Override
  public Mono<Product> updateProductStock(
      String franchiseId, String branchId, String productId, int stock) {
    return Mono.empty();
  }

  @Override
  public Flux<Product> getTopProductsByFranchise(String franchiseId) {
    return Flux.empty();
  }
}

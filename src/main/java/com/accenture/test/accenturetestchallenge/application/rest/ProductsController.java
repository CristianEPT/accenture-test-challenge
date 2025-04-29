package com.accenture.test.accenturetestchallenge.application.rest;

import com.accenture.test.accenturetestchallenge.application.ProductRequest;
import com.accenture.test.accenturetestchallenge.application.ProductResponse;
import com.accenture.test.accenturetestchallenge.application.TopProductResponse;
import com.accenture.test.accenturetestchallenge.application.UpdateStockRequest;
import com.accenture.test.accenturetestchallenge.application.api.ProductApi;
import com.accenture.test.accenturetestchallenge.domain.model.Product;
import com.accenture.test.accenturetestchallenge.domain.ports.ProductPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductsController implements ProductApi {

  private final ProductPort productPort;

  @Override
  public Mono<ResponseEntity<ProductResponse>> addProduct(
      String franchiseId,
      String branchId,
      Mono<ProductRequest> productRequest,
      ServerWebExchange exchange) {
    return productRequest
        .map(this::buildProduct)
        .flatMap(product -> productPort.createProduct(franchiseId, branchId, product))
        .map(this::mapDomainToResponse)
        .map(productResponse -> ResponseEntity.status(HttpStatus.CREATED).body(productResponse));
  }

  @Override
  public Mono<ResponseEntity<Void>> deleteProduct(
      String franchiseId, String branchId, String productId, ServerWebExchange exchange) {
    productPort.deleteProduct(franchiseId, branchId, productId);
    return Mono.just(ResponseEntity.status(HttpStatus.OK).build());
  }

  @Override
  public Mono<ResponseEntity<ProductResponse>> updateProductStock(
      String franchiseId,
      String branchId,
      String productId,
      Mono<UpdateStockRequest> updateStockRequest,
      ServerWebExchange exchange) {
    return updateStockRequest
        .map(UpdateStockRequest::getStock)
        .flatMap(stock -> productPort.updateProductStock(franchiseId, branchId, productId, stock))
        .map(this::mapDomainToResponse)
        .map(productResponse -> ResponseEntity.status(HttpStatus.OK).body(productResponse));
  }

  @Override
  public Mono<ResponseEntity<Flux<TopProductResponse>>> getTopProductsByBranch(
      String franchiseId, ServerWebExchange exchange) {

    return Mono.justOrEmpty(
        ResponseEntity.status(HttpStatus.OK)
            .body(
                productPort
                    .getTopProductsByFranchise(franchiseId)
                    .map(this::mapDomainToResponseTopProduct)));
  }

  private TopProductResponse mapDomainToResponseTopProduct(Product product) {
    TopProductResponse topProductResponse = new TopProductResponse();
    topProductResponse.setStock(product.getStock());
    topProductResponse.setProductName(product.getName());
    topProductResponse.setProductId(product.getId());
    topProductResponse.setBranchId(product.getBranchId());

    return topProductResponse;
  }

  private ProductResponse mapDomainToResponse(Product product) {
    ProductResponse productResponse = new ProductResponse();
    productResponse.setName(product.getName());
    productResponse.setStock(product.getStock());

    return productResponse;
  }

  private Product buildProduct(ProductRequest productRequest) {
    return new Product(productRequest.getName(), productRequest.getStock());
  }
}

package com.accenture.test.accenturetestchallenge.application.rest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.accenture.test.accenturetestchallenge.application.ProductRequest;
import com.accenture.test.accenturetestchallenge.application.ProductResponse;
import com.accenture.test.accenturetestchallenge.application.TopProductResponse;
import com.accenture.test.accenturetestchallenge.application.UpdateStockRequest;
import com.accenture.test.accenturetestchallenge.domain.model.Product;
import com.accenture.test.accenturetestchallenge.domain.ports.ProductPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = ProductsController.class)
class ProductsControllerTest {
  @Autowired private WebTestClient webTestClient;

  @MockitoBean private ProductPort productPort;
  private static final String FRANCHISE_ID = "1";
  private static final String BRANCH_ID = "b1";

  @Test
  void createProduct_whenAllSuccess() {
    ProductRequest request = new ProductRequest();
    request.setName("pizza test");
    request.setStock(2);

    Product product = new Product(FRANCHISE_ID, BRANCH_ID, "123", "pizza test", 2);

    Mockito.when(productPort.createProduct(any(), any(), any())).thenReturn(Mono.just(product));

    webTestClient
        .post()
        .uri("/franchise/" + FRANCHISE_ID + "/branch/" + BRANCH_ID + "/product")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(ProductResponse.class)
        .value(
            response -> {
              assertThat(response.getStock()).isEqualTo(2);
              assertThat(response.getName()).isEqualTo("pizza test");
            });
  }

  @Test
  void createBranch_whenNameOrStockAreNull_returnsError() {
    ProductRequest request = new ProductRequest();

    webTestClient
        .post()
        .uri("/franchise/" + FRANCHISE_ID + "/branch/" + BRANCH_ID + "/product")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody()
        .consumeWith(result -> assertThat(result.getResponseBody()).isNotNull());
  }

  @Test
  void createProduct_whenFailed_returnsError() {

    ProductRequest request = new ProductRequest();
    request.setName("pizza test");
    request.setStock(2);

    Mockito.when(productPort.createProduct(any(), any(), any()))
        .thenThrow(new RuntimeException("error saving product"));

    webTestClient
        .post()
        .uri("/franchise/" + FRANCHISE_ID + "/branch/" + BRANCH_ID + "/product")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody()
        .consumeWith(result -> assertThat(result.getResponseBody()).isNotNull());
  }

  @Test
  void deleteProduct_whenAllSuccess() {

    webTestClient
        .delete()
        .uri("/franchise/" + FRANCHISE_ID + "/branch/" + BRANCH_ID + "/product/123")
        .exchange()
        .expectStatus()
        .isOk();

    verify(productPort, times(1)).deleteProduct(any(), any(), any());
  }

  @Test
  void updateProduct_whenAllSuccess() {
    UpdateStockRequest request = new UpdateStockRequest();
    request.setStock(3);

    Product product = new Product(FRANCHISE_ID, BRANCH_ID, "123", "pizza test", 3);

    Mockito.when(productPort.updateProductStock(any(), any(), any(), anyInt()))
        .thenReturn(Mono.just(product));

    webTestClient
        .put()
        .uri("/franchise/" + FRANCHISE_ID + "/branch/" + BRANCH_ID + "/product/123")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(ProductResponse.class)
        .value(
            response -> {
              assertThat(response.getStock()).isEqualTo(3);
              assertThat(response.getName()).isEqualTo("pizza test");
            });
  }

  @Test
  void getTopProducts_whenAllSuccess() {

    Product product = new Product(FRANCHISE_ID, BRANCH_ID, "123", "pizza test", 3);

    Mockito.when(productPort.getTopProductsByFranchise(any())).thenReturn(Flux.just(product));

    webTestClient
        .get()
        .uri("/franchise/" + FRANCHISE_ID + "/branch/top-products")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(TopProductResponse.class)
        .value(
            topProducts -> {
              assertThat(topProducts).isNotNull();

              TopProductResponse topProduct = topProducts.get(0);
              assertThat(topProduct.getBranchId()).isEqualTo(BRANCH_ID);
              assertThat(topProduct.getProductId()).isEqualTo("123");
              assertThat(topProduct.getProductName()).isEqualTo("pizza test");
              assertThat(topProduct.getStock()).isEqualTo(3);
            });
  }
}

package com.accenture.test.accenturetestchallenge.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.accenture.test.accenturetestchallenge.domain.entities.ProductEntity;
import com.accenture.test.accenturetestchallenge.domain.model.Product;
import com.accenture.test.accenturetestchallenge.domain.ports.BranchPort;
import com.accenture.test.accenturetestchallenge.domain.ports.FranchisePort;
import com.accenture.test.accenturetestchallenge.domain.repositories.ProductRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
  @Mock private FranchisePort franchisePort;
  @Mock private BranchPort branchPort;
  @Mock private ProductRepository productRepository;

  @InjectMocks private ProductService productService;

  @Test
  void shouldCreateProductSuccessfully() {
    String franchiseId = "f1";
    String branchId = "b1";
    Product input = Product.builder().name("product name").stock(10).build();

    ProductEntity savedEntity = new ProductEntity();
    savedEntity.setId("p1");
    savedEntity.setName("product name");
    savedEntity.setStock(10);
    savedEntity.setFranchiseId(franchiseId);
    savedEntity.setBranchId(branchId);

    when(franchisePort.existsFranchise(franchiseId)).thenReturn(Mono.just(true));
    when(branchPort.existsBranch(franchiseId, branchId)).thenReturn(Mono.just(true));
    when(productRepository.save(any())).thenReturn(Mono.just(savedEntity));

    StepVerifier.create(productService.createProduct(franchiseId, branchId, input))
        .assertNext(
            product -> {
              assertEquals("p1", product.getId());
              assertEquals("product name", product.getName());
              assertEquals(10, product.getStock());
              assertEquals(franchiseId, product.getFranchiseId());
              assertEquals(branchId, product.getBranchId());
            })
        .verifyComplete();

    verify(productRepository).save(any());
  }

  @Test
  void shouldReturnErrorWhenBranchIdIsEmpty() {
    String franchiseId = "f1";
    String branchId = "   ";
    Product product = Product.builder().name("product name").stock(8).build();

    when(franchisePort.existsFranchise(franchiseId)).thenReturn(Mono.just(true));

    StepVerifier.create(productService.createProduct(franchiseId, branchId, product))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Branch id must not be null or empty"))
        .verify();

    verifyNoInteractions(productRepository);
  }

  @Test
  void shouldReturnErrorWhenProductNameIsInvalid() {
    String franchiseId = "f1";
    String branchId = "b1";
    Product product = Product.builder().name("   ").stock(5).build();

    when(franchisePort.existsFranchise(franchiseId)).thenReturn(Mono.just(true));
    when(branchPort.existsBranch(franchiseId, branchId)).thenReturn(Mono.just(true));

    StepVerifier.create(productService.createProduct(franchiseId, branchId, product))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Product name or stock must not be null or empty"))
        .verify();

    verifyNoInteractions(productRepository);
  }

  @Test
  void shouldReturnErrorWhenProductStockIsNegative() {
    String franchiseId = "f1";
    String branchId = "b1";
    Product product = Product.builder().name("product name").stock(-1).build();

    when(franchisePort.existsFranchise(franchiseId)).thenReturn(Mono.just(true));
    when(branchPort.existsBranch(franchiseId, branchId)).thenReturn(Mono.just(true));

    StepVerifier.create(productService.createProduct(franchiseId, branchId, product))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Product name or stock must not be null or empty"))
        .verify();

    verifyNoInteractions(productRepository);
  }

  @Test
  void shouldReturnErrorWhenRepositoryFails() {
    String franchiseId = "f1";
    String branchId = "b1";
    Product product = Product.builder().name("product name").stock(12).build();

    when(franchisePort.existsFranchise(franchiseId)).thenReturn(Mono.just(true));
    when(branchPort.existsBranch(franchiseId, branchId)).thenReturn(Mono.just(true));
    when(productRepository.save(any())).thenReturn(Mono.error(new RuntimeException("DB error")));

    StepVerifier.create(productService.createProduct(franchiseId, branchId, product))
        .expectErrorMatches(
            error -> error instanceof RuntimeException && error.getMessage().equals("DB error"))
        .verify();

    verify(productRepository).save(any());
  }

  @Test
  void deleteProduct_shouldDeleteProductSuccessfully() {
    String franchiseId = "f1";
    String branchId = "b1";
    String productId = "p1";

    ProductEntity productEntity = new ProductEntity();
    productEntity.setId(productId);
    productEntity.setFranchiseId(franchiseId);
    productEntity.setBranchId(branchId);

    when(productRepository.findByFranchiseIdAndBranchIdAndId(franchiseId, branchId, productId))
        .thenReturn(Mono.just(productEntity));
    when(productRepository.deleteById(productId)).thenReturn(Mono.empty());

    StepVerifier.create(productService.deleteProduct(franchiseId, branchId, productId))
        .verifyComplete();

    verify(productRepository).findByFranchiseIdAndBranchIdAndId(franchiseId, branchId, productId);
    verify(productRepository).deleteById(productId);
  }

  @Test
  void deleteProduct_shouldReturnErrorWhenFranchiseIdIsNull() {
    String franchiseId = null;
    String branchId = "b1";
    String productId = "p1";

    StepVerifier.create(productService.deleteProduct(franchiseId, branchId, productId))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().contains("must not be null or empty"))
        .verify();

    verifyNoInteractions(productRepository);
  }

  @Test
  void deleteProduct_shouldReturnErrorWhenProductIdIsEmpty() {
    String franchiseId = "f1";
    String branchId = "b1";
    String productId = " ";

    StepVerifier.create(productService.deleteProduct(franchiseId, branchId, productId))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().contains("must not be null or empty"))
        .verify();

    verifyNoInteractions(productRepository);
  }

  @Test
  void deleteProduct_shouldReturnErrorWhenProductNotFound() {
    String franchiseId = "f1";
    String branchId = "b1";
    String productId = "not-found";

    when(productRepository.findByFranchiseIdAndBranchIdAndId(franchiseId, branchId, productId))
        .thenReturn(Mono.empty());

    StepVerifier.create(productService.deleteProduct(franchiseId, branchId, productId))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Product not found"))
        .verify();

    verify(productRepository).findByFranchiseIdAndBranchIdAndId(franchiseId, branchId, productId);
    verify(productRepository, never()).deleteById(anyString());
  }

  @Test
  void deleteProduct_shouldReturnErrorWhenDeleteFails() {
    String franchiseId = "f1";
    String branchId = "b1";
    String productId = "p1";

    ProductEntity productEntity = new ProductEntity();
    productEntity.setId(productId);
    productEntity.setFranchiseId(franchiseId);
    productEntity.setBranchId(branchId);

    when(productRepository.findByFranchiseIdAndBranchIdAndId(franchiseId, branchId, productId))
        .thenReturn(Mono.just(productEntity));
    when(productRepository.deleteById(productId))
        .thenReturn(Mono.error(new RuntimeException("DB failure")));

    StepVerifier.create(productService.deleteProduct(franchiseId, branchId, productId))
        .expectErrorMatches(
            error -> error instanceof RuntimeException && error.getMessage().equals("DB failure"))
        .verify();

    verify(productRepository).findByFranchiseIdAndBranchIdAndId(franchiseId, branchId, productId);
    verify(productRepository).deleteById(productId);
  }

  @Test
  void updateProduct_shouldUpdateStockSuccessfully() {
    String franchiseId = "f1";
    String branchId = "b1";
    String productId = "p1";
    int newStock = 20;

    ProductEntity existing = new ProductEntity();
    existing.setId(productId);
    existing.setFranchiseId(franchiseId);
    existing.setBranchId(branchId);
    existing.setName("product 1");
    existing.setStock(10);

    ProductEntity updated = new ProductEntity();
    updated.setId(productId);
    updated.setFranchiseId(franchiseId);
    updated.setBranchId(branchId);
    updated.setName("product 1");
    updated.setStock(newStock);

    when(productRepository.findByFranchiseIdAndBranchIdAndId(franchiseId, branchId, productId))
        .thenReturn(Mono.just(existing));
    when(productRepository.save(any())).thenReturn(Mono.just(updated));

    StepVerifier.create(
            productService.updateProductStock(franchiseId, branchId, productId, newStock))
        .assertNext(
            product -> {
              assertEquals(productId, product.getId());
              assertEquals("product 1", product.getName());
              assertEquals(franchiseId, product.getFranchiseId());
              assertEquals(branchId, product.getBranchId());
              assertEquals(newStock, product.getStock());
            })
        .verifyComplete();

    verify(productRepository).save(any());
  }

  @Test
  void updateProduct_shouldReturnErrorWhenFranchiseIdIsNull() {
    StepVerifier.create(productService.updateProductStock(null, "b1", "p1", 5))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().contains("must not be null or empty"))
        .verify();

    verifyNoInteractions(productRepository);
  }

  @Test
  void updateProduct_shouldReturnErrorWhenProductIdIsEmpty() {
    StepVerifier.create(productService.updateProductStock("f1", "b1", "  ", 5))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().contains("must not be null or empty"))
        .verify();

    verifyNoInteractions(productRepository);
  }

  @Test
  void updateProduct_shouldReturnErrorWhenStockIsNegative() {
    StepVerifier.create(productService.updateProductStock("f1", "b1", "p1", -1))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().contains("must not be null or empty"))
        .verify();

    verifyNoInteractions(productRepository);
  }

  @Test
  void updateProduct_shouldReturnErrorWhenProductNotFound() {
    when(productRepository.findByFranchiseIdAndBranchIdAndId("f1", "b1", "p1"))
        .thenReturn(Mono.empty());

    StepVerifier.create(productService.updateProductStock("f1", "b1", "p1", 5))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Product not found"))
        .verify();

    verify(productRepository).findByFranchiseIdAndBranchIdAndId("f1", "b1", "p1");
    verify(productRepository, never()).save(any());
  }

  @Test
  void updateProduct_shouldReturnErrorWhenSaveFails() {
    ProductEntity productEntity = new ProductEntity();
    productEntity.setId("p1");
    productEntity.setFranchiseId("f1");
    productEntity.setBranchId("b1");
    productEntity.setName("Sprite");
    productEntity.setStock(10);

    when(productRepository.findByFranchiseIdAndBranchIdAndId("f1", "b1", "p1"))
        .thenReturn(Mono.just(productEntity));
    when(productRepository.save(any())).thenReturn(Mono.error(new RuntimeException("DB error")));

    StepVerifier.create(productService.updateProductStock("f1", "b1", "p1", 5))
        .expectErrorMatches(
            error -> error instanceof RuntimeException && error.getMessage().equals("DB error"))
        .verify();

    verify(productRepository).save(any());
  }

  @Test
  void getTopProducts_shouldReturnErrorWhenFranchiseIdIsNull() {
    StepVerifier.create(productService.getTopProductsByFranchise(null))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Franchise ID must not be null or empty"))
        .verify();

    verifyNoInteractions(productRepository);
  }

  @Test
  void getTopProducts_shouldReturnTopProductPerBranchSuccessfully() {
    String franchiseId = "f1";

    ProductEntity p1 = new ProductEntity();
    p1.setId("p1");
    p1.setFranchiseId(franchiseId);
    p1.setBranchId("A");
    p1.setName("Product A1");
    p1.setStock(5);

    ProductEntity p2 = new ProductEntity();
    p2.setId("p2");
    p2.setFranchiseId(franchiseId);
    p2.setBranchId("A");
    p2.setName("Product A2");
    p2.setStock(10);

    ProductEntity p3 = new ProductEntity();
    p3.setId("p3");
    p3.setFranchiseId(franchiseId);
    p3.setBranchId("B");
    p3.setName("Product B1");
    p3.setStock(20);

    when(productRepository.findByFranchiseId(franchiseId))
        .thenReturn(Flux.fromIterable(List.of(p1, p2, p3)));

    StepVerifier.create(productService.getTopProductsByFranchise(franchiseId))
        .expectNextMatches(product -> product.getBranchId().equals("A") && product.getStock() == 10)
        .expectNextMatches(product -> product.getBranchId().equals("B") && product.getStock() == 20)
        .verifyComplete();

    verify(productRepository).findByFranchiseId(franchiseId);
  }

  @Test
  void getTopProducts_shouldReturnErrorWhenRepositoryFails() {
    String franchiseId = "f1";

    when(productRepository.findByFranchiseId(franchiseId))
        .thenReturn(Flux.error(new RuntimeException("DB error")));

    StepVerifier.create(productService.getTopProductsByFranchise(franchiseId))
        .expectErrorMatches(
            error -> error instanceof RuntimeException && error.getMessage().equals("DB error"))
        .verify();

    verify(productRepository).findByFranchiseId(franchiseId);
  }
}

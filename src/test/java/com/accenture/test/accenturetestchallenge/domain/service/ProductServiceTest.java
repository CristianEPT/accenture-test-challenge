package com.accenture.test.accenturetestchallenge.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.accenture.test.accenturetestchallenge.domain.entities.ProductEntity;
import com.accenture.test.accenturetestchallenge.domain.model.Product;
import com.accenture.test.accenturetestchallenge.domain.ports.BranchPort;
import com.accenture.test.accenturetestchallenge.domain.ports.FranchisePort;
import com.accenture.test.accenturetestchallenge.domain.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
}

package com.accenture.test.accenturetestchallenge.domain.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.accenture.test.accenturetestchallenge.domain.entities.FranchiseEntity;
import com.accenture.test.accenturetestchallenge.domain.repositories.FranchiseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FranchiseServiceTest {

  @Mock
  private FranchiseRepository franchiseRepository;

  @InjectMocks
  private FranchiseService franchiseService;

  private static final String VALID_NAME = "Test Franchise";

  @Test
  void shouldCreateFranchiseSuccessfully() {
    FranchiseEntity savedEntity = new FranchiseEntity();
    savedEntity.setId("123");
    savedEntity.setName(VALID_NAME);

    when(franchiseRepository.save(any())).thenReturn(Mono.just(savedEntity));

    StepVerifier.create(franchiseService.createFranchise(VALID_NAME))
        .expectNextMatches(
            franchise -> franchise.getId().equals("123") && franchise.getName().equals(VALID_NAME))
        .verifyComplete();

    verify(franchiseRepository).save(any(FranchiseEntity.class));
  }

  @Test
  void shouldReturnErrorWhenFranchiseNameIsNull() {
    StepVerifier.create(franchiseService.createFranchise(null))
        .expectErrorMatches(
            throwable ->
                throwable instanceof IllegalArgumentException
                    && throwable.getMessage().equals("Franchise name must not be null or empty"))
        .verify();

    verifyNoInteractions(franchiseRepository);
  }

  @Test
  void shouldReturnErrorWhenFranchiseNameIsEmpty() {
    StepVerifier.create(franchiseService.createFranchise("  "))
        .expectErrorMatches(
            throwable ->
                throwable instanceof IllegalArgumentException
                    && throwable.getMessage().equals("Franchise name must not be null or empty"))
        .verify();

    verifyNoInteractions(franchiseRepository);
  }

  @Test
  void shouldReturnErrorWhenRepositoryFails() {
    when(franchiseRepository.save(any())).thenReturn(Mono.error(new RuntimeException("DB Error")));

    StepVerifier.create(franchiseService.createFranchise(VALID_NAME))
        .expectErrorMatches(
            throwable ->
                throwable instanceof RuntimeException && throwable.getMessage().equals("DB Error"))
        .verify();

    verify(franchiseRepository).save(any(FranchiseEntity.class));
  }
}

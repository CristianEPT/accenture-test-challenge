package com.accenture.test.accenturetestchallenge.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.accenture.test.accenturetestchallenge.domain.entities.BranchEntity;
import com.accenture.test.accenturetestchallenge.domain.ports.FranchisePort;
import com.accenture.test.accenturetestchallenge.domain.repositories.BranchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

  @Mock private FranchisePort franchisePort;

  @Mock private BranchRepository branchRepository;

  @InjectMocks private BranchService branchService;

  private final String VALID_FRANCHISE_ID = "franchise-123";
  private final String VALID_BRANCH_NAME = "Main Branch";

  @Test
  void shouldCreateBranchSuccessfully() {
    BranchEntity savedEntity = new BranchEntity();
    savedEntity.setId("branch-456");
    savedEntity.setFranchiseId(VALID_FRANCHISE_ID);
    savedEntity.setName(VALID_BRANCH_NAME);

    when(franchisePort.existsFranchise(VALID_FRANCHISE_ID)).thenReturn(Mono.just(true));
    when(branchRepository.save(any())).thenReturn(Mono.just(savedEntity));

    StepVerifier.create(branchService.createBranch(VALID_FRANCHISE_ID, VALID_BRANCH_NAME))
        .assertNext(
            branch -> {
              assertEquals("branch-456", branch.getId());
              assertEquals(VALID_BRANCH_NAME, branch.getName());
              assertEquals(VALID_FRANCHISE_ID, branch.getFranchiseId());
            })
        .verifyComplete();

    verify(franchisePort).existsFranchise(VALID_FRANCHISE_ID);
    verify(branchRepository).save(any());
  }

  @Test
  void shouldReturnErrorWhenFranchiseDoesNotExist() {
    when(franchisePort.existsFranchise(VALID_FRANCHISE_ID)).thenReturn(Mono.just(false));

    StepVerifier.create(branchService.createBranch(VALID_FRANCHISE_ID, VALID_BRANCH_NAME))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Franchise does not exist"))
        .verify();

    verify(franchisePort).existsFranchise(VALID_FRANCHISE_ID);
    verifyNoInteractions(branchRepository);
  }

  @Test
  void shouldReturnErrorWhenFranchiseIdIsNull() {
    StepVerifier.create(branchService.createBranch(null, VALID_BRANCH_NAME))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Franchise id must not be null or empty"))
        .verify();

    verifyNoInteractions(franchisePort);
    verifyNoInteractions(branchRepository);
  }

  @Test
  void shouldReturnErrorWhenFranchiseIdIsEmpty() {
    StepVerifier.create(branchService.createBranch("   ", VALID_BRANCH_NAME))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Franchise id must not be null or empty"))
        .verify();

    verifyNoInteractions(franchisePort);
    verifyNoInteractions(branchRepository);
  }

  @Test
  void shouldReturnErrorWhenBranchNameIsNull() {
    when(franchisePort.existsFranchise(VALID_FRANCHISE_ID)).thenReturn(Mono.just(true));

    StepVerifier.create(branchService.createBranch(VALID_FRANCHISE_ID, null))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Branch name must not be null or empty"))
        .verify();

    verify(franchisePort).existsFranchise(VALID_FRANCHISE_ID);
    verifyNoInteractions(branchRepository);
  }

  @Test
  void shouldReturnErrorWhenBranchNameIsEmpty() {
    when(franchisePort.existsFranchise(VALID_FRANCHISE_ID)).thenReturn(Mono.just(true));

    StepVerifier.create(branchService.createBranch(VALID_FRANCHISE_ID, "   "))
        .expectErrorMatches(
            error ->
                error instanceof IllegalArgumentException
                    && error.getMessage().equals("Branch name must not be null or empty"))
        .verify();

    verify(franchisePort).existsFranchise(VALID_FRANCHISE_ID);
    verifyNoInteractions(branchRepository);
  }

  @Test
  void shouldReturnErrorWhenRepositoryFails() {
    when(franchisePort.existsFranchise(VALID_FRANCHISE_ID)).thenReturn(Mono.just(true));
    when(branchRepository.save(any())).thenReturn(Mono.error(new RuntimeException("DB error")));

    StepVerifier.create(branchService.createBranch(VALID_FRANCHISE_ID, VALID_BRANCH_NAME))
        .expectErrorMatches(
            error -> error instanceof RuntimeException && error.getMessage().equals("DB error"))
        .verify();

    verify(franchisePort).existsFranchise(VALID_FRANCHISE_ID);
    verify(branchRepository).save(any());
  }

  @Test
  void shouldUpdateBranchNameSuccessfully() {
    String franchiseId = VALID_FRANCHISE_ID;
    String branchId = "1";
    String newName = "Updated Branch";

    BranchEntity existing = new BranchEntity();
    existing.setId(branchId);
    existing.setFranchiseId(franchiseId);
    existing.setName("Old Name");

    BranchEntity updated = new BranchEntity();
    updated.setId(branchId);
    updated.setFranchiseId(franchiseId);
    updated.setName(newName);

    when(branchRepository.findByFranchiseIdAndId(franchiseId, branchId))
            .thenReturn(Mono.just(existing));
    when(branchRepository.save(any())).thenReturn(Mono.just(updated));

    StepVerifier.create(branchService.updateBranchName(franchiseId, branchId, newName))
            .assertNext(branch -> {
              assertEquals(branchId, branch.getId());
              assertEquals(franchiseId, branch.getFranchiseId());
              assertEquals(newName, branch.getName());
            })
            .verifyComplete();

    verify(branchRepository).findByFranchiseIdAndId(franchiseId, branchId);
    verify(branchRepository).save(any());
  }
}

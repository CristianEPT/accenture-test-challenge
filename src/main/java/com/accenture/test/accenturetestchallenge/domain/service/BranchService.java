package com.accenture.test.accenturetestchallenge.domain.service;

import com.accenture.test.accenturetestchallenge.domain.entities.BranchEntity;
import com.accenture.test.accenturetestchallenge.domain.model.Branch;
import com.accenture.test.accenturetestchallenge.domain.ports.BranchPort;
import com.accenture.test.accenturetestchallenge.domain.ports.FranchisePort;
import com.accenture.test.accenturetestchallenge.domain.repositories.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchService implements BranchPort {

  private final FranchisePort franchisePort;

  private final BranchRepository branchRepository;

  @Override
  public Mono<Branch> createBranch(String franchiseId, String branchName) {
    log.info("Starting branch creation process. Branch name: {}", branchName);

    return validateFranchise(franchiseId)
        .then(validateBranchName(branchName))
        .map(validatedBranchName -> buildBranchEntity(franchiseId, validatedBranchName))
        .flatMap(branchRepository::save)
        .map(this::mapEntityToDomain)
        .doOnSuccess(
            branch ->
                log.info(
                    "Branch created successfully. Branch ID: {}, Name: {}",
                    branch.getId(),
                    branch.getName()))
        .doOnError(
            error ->
                log.error(
                    "Error occurred during branch creation. Branch name: {}. Error: {}",
                    branchName,
                    error.getMessage(),
                    error));
  }

  private Branch mapEntityToDomain(BranchEntity branchEntity) {
    return Branch.builder()
        .franchiseId(branchEntity.getFranchiseId())
        .name(branchEntity.getName())
        .id(branchEntity.getId())
        .build();
  }

  private BranchEntity buildBranchEntity(String franchiseId, String branchName) {
    BranchEntity branchEntity = new BranchEntity();
    branchEntity.setName(branchName);
    branchEntity.setFranchiseId(franchiseId);
    return branchEntity;
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

  private Mono<String> validateBranchName(String branchName) {
    if (branchName == null || branchName.trim().isEmpty()) {
      log.warn("Invalid branch name received: '{}'", branchName);
      return Mono.error(new IllegalArgumentException("Branch name must not be null or empty"));
    }
    return Mono.just(branchName.trim());
  }

  @Override
  public Mono<Boolean> existsBranch(String franchiseId, String branchId) {
    if (franchiseId == null
        || franchiseId.trim().isEmpty()
        || branchId == null
        || branchId.trim().isEmpty()) {
      log.warn("Invalid franchise ID '{}' or Invalid branch ID '{}' ", franchiseId, branchId);
      return Mono.error(
          new IllegalArgumentException("Franchise ID or Branch ID must not be null or empty"));
    }

    return branchRepository
        .existsByFranchiseIdAndId(franchiseId, branchId)
        .doOnSuccess(exists -> log.debug("branch ID {} exists: {}", branchId, exists))
        .doOnError(
            error ->
                log.error("Error while checking existence of branch with ID: {}", branchId, error));
  }

  @Override
  public Mono<Branch> updateBranchName(String franchiseId, String branchId, String newBranchName) {

    if (franchiseId == null
        || franchiseId.trim().isEmpty()
        || branchId == null
        || branchId.trim().isEmpty()) {

      log.warn("Invalid franchise ID '{}' or Invalid branch ID '{}' ", franchiseId, branchId);
      return Mono.error(
          new IllegalArgumentException("Franchise ID or Branch ID must not be null or empty"));
    }

    return branchRepository
        .findByFranchiseIdAndId(franchiseId, branchId)
        .map(
            branchEntity -> {
              branchEntity.setName(newBranchName);
              return branchEntity;
            })
        .flatMap(branchRepository::save)
        .map(this::mapEntityToDomain)
        .doOnSuccess(branch -> log.info("Branch updating successfully. ID: {}", branch.getId()))
        .doOnError(
            error ->
                log.error("Error updating Branch ID {}: {}", branchId, error.getMessage(), error));
  }
}

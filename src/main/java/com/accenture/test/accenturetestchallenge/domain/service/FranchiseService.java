package com.accenture.test.accenturetestchallenge.domain.service;

import com.accenture.test.accenturetestchallenge.domain.entities.FranchiseEntity;
import com.accenture.test.accenturetestchallenge.domain.model.Franchise;
import com.accenture.test.accenturetestchallenge.domain.ports.FranchisePort;
import com.accenture.test.accenturetestchallenge.domain.repositories.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class FranchiseService implements FranchisePort {

  private final FranchiseRepository franchiseRepository;

  @Override
  public Mono<Franchise> createFranchise(String franchiseName) {
    log.info("Starting franchise creation process. Franchise name: {}", franchiseName);

    return validateFranchiseName(franchiseName)
        .map(this::buildFranchiseEntity)
        .flatMap(franchiseRepository::save)
        .map(this::mapEntityToDomain)
        .doOnSuccess(
            franchise ->
                log.info(
                    "Franchise created successfully. Franchise ID: {}, Name: {}",
                    franchise.getId(),
                    franchise.getName()))
        .doOnError(
            error ->
                log.error(
                    "Error occurred during franchise creation. Franchise name: {}. Error: {}",
                    franchiseName,
                    error.getMessage(),
                    error));
  }

  private Mono<String> validateFranchiseName(String franchiseName) {
    if (franchiseName == null || franchiseName.trim().isEmpty()) {
      log.warn("Invalid franchise name received: '{}'", franchiseName);
      return Mono.error(new IllegalArgumentException("Franchise name must not be null or empty"));
    }
    return Mono.just(franchiseName.trim());
  }

  private FranchiseEntity buildFranchiseEntity(String franchiseName) {
    FranchiseEntity entity = new FranchiseEntity();
    entity.setName(franchiseName);
    log.debug("FranchiseEntity built successfully for name: {}", franchiseName);
    return entity;
  }

  private Franchise mapEntityToDomain(FranchiseEntity franchiseEntity) {
    Franchise domain =
        Franchise.builder().id(franchiseEntity.getId()).name(franchiseEntity.getName()).build();
    log.debug(
        "FranchiseEntity mapped to domain object. Franchise ID: {}, Name: {}",
        domain.getId(),
        domain.getName());
    return domain;
  }

  @Override
  public Mono<Boolean> existsFranchise(String franchiseId) {
    if (franchiseId == null || franchiseId.trim().isEmpty()) {
      log.warn("Invalid franchise ID provided for existence check: '{}'", franchiseId);
      return Mono.error(new IllegalArgumentException("Franchise ID must not be null or empty"));
    }

    log.debug("Checking existence of franchise with ID: {}", franchiseId);

    return franchiseRepository
        .existsById(franchiseId)
        .doOnSuccess(exists -> log.debug("Franchise ID {} exists: {}", franchiseId, exists))
        .doOnError(
            error ->
                log.error(
                    "Error while checking existence of franchise with ID: {}", franchiseId, error));
  }

  @Override
  public Mono<Franchise> updateFranchiseName(String franchiseId, String newFranchiseName) {

    if (franchiseId == null || franchiseId.trim().isEmpty()) {
      log.warn("Invalid franchise ID provided for update check: '{}'", franchiseId);
      return Mono.error(new IllegalArgumentException("Franchise ID must not be null or empty"));
    }
    return franchiseRepository
        .findById(franchiseId)
        .map(
            franchiseEntity -> {
              franchiseEntity.setName(newFranchiseName);
              return franchiseEntity;
            })
        .flatMap(franchiseRepository::save)
        .map(this::mapEntityToDomain)
        .doOnSuccess(
            franchise -> log.info("Franchise updating successfully. ID: {}", franchise.getId()))
        .doOnError(
            error ->
                log.error(
                    "Error updating franchise ID {}: {}", franchiseId, error.getMessage(), error));
  }
}

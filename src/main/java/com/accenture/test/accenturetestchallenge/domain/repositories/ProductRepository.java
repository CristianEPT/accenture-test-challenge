package com.accenture.test.accenturetestchallenge.domain.repositories;

import com.accenture.test.accenturetestchallenge.domain.entities.ProductEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveMongoRepository<ProductEntity, String> {

  Mono<ProductEntity> findByFranchiseIdAndBranchIdAndId(
      String franchiseId, String brandId, String id);

  Flux<ProductEntity> findByFranchiseId(String franchiseId);
}

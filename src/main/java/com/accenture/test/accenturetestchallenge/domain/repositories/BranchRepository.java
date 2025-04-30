package com.accenture.test.accenturetestchallenge.domain.repositories;

import com.accenture.test.accenturetestchallenge.domain.entities.BranchEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BranchRepository extends ReactiveMongoRepository<BranchEntity, String> {

  Mono<Boolean> existsByFranchiseIdAndId(String franchiseId, String branchId);
}
